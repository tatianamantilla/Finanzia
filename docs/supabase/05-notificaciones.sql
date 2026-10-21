-- =====================================================================================
-- 05 · Notificaciones                                          (fase 2 · revisar y ejecutar)
-- =====================================================================================
-- Qué hace:
--   1. Crea la tabla notificaciones: cada fila es un aviso para UN usuario.
--   2. Crea la función generar_notificaciones(): la app la llama al abrir la pantalla de
--      Notificaciones. Revisa los datos del usuario y crea los avisos que correspondan.
--      Nunca repite un aviso: cada uno tiene una "clave" única por usuario.
--   3. Crea dos triggers en Supabase Auth: avisan cuando alguien inicia sesión y cuando se
--      cambia la contraseña.
--   4. Activa RLS: cada persona solo ve y marca SUS notificaciones.
--
-- Tipos de notificación (columna tipo) y el filtro de la pantalla donde aparecen:
--   Alertas de presupuesto:
--     gasto_mayor              Llevas 20 % o más en una categoría que el mes pasado a esta fecha.
--     gasto_inusual            Un gasto de los últimos 7 días es 3 veces (o más) tu promedio en
--                              esa categoría (últimos 90 días, con al menos 3 gastos para comparar).
--     gastos_superan_ingresos  Este mes los gastos ya superan a los ingresos.
--     saldo_bajo               Una cuenta activa quedó en negativo (máximo un aviso por semana).
--   Recordatorios de pago:
--     registrar_movimientos    5 días o más sin registrar ingresos ni gastos.
--     aporte_meta              Desde el día 15 del mes, una meta sin aportes este mes.
--     dia_ingreso              Según la frecuencia del ingreso (mensual, quincenal, semanal o
--                              diario), ya debería haber llegado el siguiente ingreso.
--     resumen_mes              Al empezar el mes: cuánto se ahorró (o se gastó de más) el mes pasado.
--     avance_meta              La meta llegó al 25 %, 50 % o 75 %.
--     meta_cumplida            La meta llegó al 100 %.
--   Seguridad e IA:
--     inicio_sesion            Alguien inició sesión en la cuenta (trigger).
--     contrasena_cambiada      Se cambió la contraseña (trigger).
--     consejo_ia               Una vez por semana: la categoría con más gasto del mes
--                              (la misma fórmula del consejo del Dashboard).
--
-- Los límites (20 %, 3 veces, 5 días, día 15…) están escritos en la función: si el equipo
-- quiere otros, se cambian ahí y se vuelve a ejecutar este archivo.
--
-- Requisito: haber ejecutado 01 y 02 (usa usuarios.id_auth y public.id_usuario_actual()).
-- Si ya ejecutaron 05-notificaciones-PROPUESTA.sql, este archivo la reemplaza: borra la
-- tabla anterior (estaba vacía) y la vuelve a crear.
-- Cómo ejecutarlo: Supabase → SQL Editor → New query → pegar todo → Run.
-- Todo va dentro de una transacción: si algo falla, no se aplica nada.
-- =====================================================================================

begin;

-- ---------------------------------------------------------------------------------
-- 1. Tabla
-- ---------------------------------------------------------------------------------
drop table if exists public.notificaciones;

create table public.notificaciones (
  id_notificacion bigint generated always as identity primary key,
  id_usuario      bigint not null references public.usuarios (id_usuario) on delete cascade,
  tipo            varchar not null check (tipo in (
                    'gasto_mayor', 'gasto_inusual', 'gastos_superan_ingresos', 'saldo_bajo',
                    'registrar_movimientos', 'aporte_meta', 'dia_ingreso', 'resumen_mes',
                    'avance_meta', 'meta_cumplida',
                    'inicio_sesion', 'contrasena_cambiada', 'consejo_ia')),
  -- Identifica el aviso para no repetirlo (por ejemplo "gasto_mayor:3:2026-10").
  clave           varchar not null,
  titulo          varchar not null,
  mensaje         text not null,
  -- Solo en avance_meta y meta_cumplida: de 0 a 1, para la barra de progreso.
  progreso        numeric,
  leida           boolean not null default false,
  fecha           timestamptz not null default now(),
  activo          boolean not null default true,
  unique (id_usuario, clave)
);

-- La pantalla pide "las notificaciones activas del usuario, de la más nueva a la más vieja".
create index notificaciones_usuario_fecha
  on public.notificaciones (id_usuario, fecha desc)
  where activo;

-- ---------------------------------------------------------------------------------
-- 2. Funciones de apoyo (solo las usan las funciones de este archivo)
-- ---------------------------------------------------------------------------------

-- 180000 → "$180,000.00"; -42000 → "-$42,000.00" (mismo formato que el Dashboard).
create or replace function public.formato_monto(p_valor numeric)
returns text
language sql
immutable
set search_path = ''
as $$
  select case when p_valor < 0 then '-' else '' end
         || '$' || to_char(abs(p_valor), 'FM999,999,999,990.00');
$$;

-- Crea el aviso solo si no existe ya uno con la misma clave para ese usuario.
create or replace function public.agregar_notificacion(
  p_usuario  bigint,
  p_tipo     text,
  p_clave    text,
  p_titulo   text,
  p_mensaje  text,
  p_progreso numeric default null
)
returns void
language sql
security definer
set search_path = ''
as $$
  insert into public.notificaciones (id_usuario, tipo, clave, titulo, mensaje, progreso)
  values (p_usuario, p_tipo, p_clave, p_titulo, p_mensaje, p_progreso)
  on conflict (id_usuario, clave) do nothing;
$$;

-- ---------------------------------------------------------------------------------
-- 3. Generar los avisos del usuario con sesión (la app la llama al abrir la pantalla)
-- ---------------------------------------------------------------------------------
-- p_zona: zona horaria del teléfono, para que "hoy" y "este mes" sean los del usuario.
create or replace function public.generar_notificaciones(p_zona text default 'America/Bogota')
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_usuario        bigint := public.id_usuario_actual();
  v_frecuencia     text;
  v_hoy            date;
  v_inicio_mes     date;
  v_inicio_ant     date;
  v_fin_ant        date;
  v_mismo_dia_ant  date;
  v_mes            text;
  v_semana         text;
  v_meses          text[] := array['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio',
                                   'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];
  v_ingresos_mes   numeric;
  v_gastos_mes     numeric;
  v_fecha          date;
  v_proxima        date;
  v_proporcion     numeric;
  v_umbral         int;
  r                record;
begin
  if v_usuario is null then
    return;
  end if;

  v_hoy           := (now() at time zone p_zona)::date;
  v_inicio_mes    := date_trunc('month', v_hoy)::date;
  v_inicio_ant    := (v_inicio_mes - interval '1 month')::date;
  v_fin_ant       := v_inicio_mes - 1;
  v_mismo_dia_ant := least(v_inicio_ant + (v_hoy - v_inicio_mes), v_fin_ant);
  v_mes           := to_char(v_inicio_mes, 'YYYY-MM');
  v_semana        := to_char(v_hoy, 'IYYY-IW');

  select u.frecuencia_ingreso into v_frecuencia
  from public.usuarios u
  where u.id_usuario = v_usuario;

  -- Totales del mes (solo movimientos activos de cuentas activas, como el Dashboard).
  select coalesce(sum(m.monto::numeric) filter (where lower(m.tipo) = 'ingreso'), 0),
         coalesce(sum(m.monto::numeric) filter (where lower(m.tipo) = 'gasto'), 0)
    into v_ingresos_mes, v_gastos_mes
  from public.movimientos m
  join public.cuentas c on c.id_cuenta = m.id_cuenta and c.activo
  where m.id_usuario = v_usuario
    and m.activo
    and (m.fecha at time zone p_zona)::date between v_inicio_mes and v_hoy;

  -- ===== Alertas de presupuesto =====

  -- gasto_mayor: categoría con 20 % o más de gasto que el mes pasado a esta misma fecha.
  for r in
    select k.id_categoria,
           k.nombre,
           coalesce(sum(m.monto::numeric) filter (
             where (m.fecha at time zone p_zona)::date >= v_inicio_mes), 0) as este,
           coalesce(sum(m.monto::numeric) filter (
             where (m.fecha at time zone p_zona)::date <= v_mismo_dia_ant), 0) as anterior
    from public.movimientos m
    join public.cuentas c on c.id_cuenta = m.id_cuenta and c.activo
    join public.categorias k on k.id_categoria = m.id_categoria
    where m.id_usuario = v_usuario
      and m.activo
      and lower(m.tipo) = 'gasto'
      and (m.fecha at time zone p_zona)::date between v_inicio_ant and v_hoy
    group by k.id_categoria, k.nombre
  loop
    if r.anterior > 0 and r.este >= r.anterior * 1.2 then
      perform public.agregar_notificacion(
        v_usuario, 'gasto_mayor', 'gasto_mayor:' || r.id_categoria || ':' || v_mes,
        'Más gasto en ' || r.nombre,
        'Llevas ' || round((r.este / r.anterior - 1) * 100) || '% más en ' || r.nombre
          || ' que el mes pasado a esta fecha: ' || public.formato_monto(r.este)
          || ' frente a ' || public.formato_monto(r.anterior) || '.');
    end if;
  end loop;

  -- gasto_inusual: gasto reciente de 3 veces o más el promedio de su categoría.
  for r in
    select m.id_movimiento,
           m.monto::numeric as monto,
           k.nombre as categoria,
           coalesce(nullif(trim(m.descripcion), ''), k.nombre) as descripcion,
           h.promedio,
           h.cantidad
    from public.movimientos m
    join public.cuentas c on c.id_cuenta = m.id_cuenta and c.activo
    join public.categorias k on k.id_categoria = m.id_categoria
    cross join lateral (
      select avg(o.monto::numeric) as promedio, count(*) as cantidad
      from public.movimientos o
      where o.id_usuario = v_usuario
        and o.activo
        and lower(o.tipo) = 'gasto'
        and o.id_categoria = m.id_categoria
        and o.id_movimiento <> m.id_movimiento
        and o.fecha < m.fecha
        and o.fecha >= m.fecha - interval '90 days'
    ) h
    where m.id_usuario = v_usuario
      and m.activo
      and lower(m.tipo) = 'gasto'
      and (m.fecha at time zone p_zona)::date between v_hoy - 7 and v_hoy
  loop
    if r.cantidad >= 3 and r.promedio > 0 and r.monto >= r.promedio * 3 then
      perform public.agregar_notificacion(
        v_usuario, 'gasto_inusual', 'gasto_inusual:' || r.id_movimiento,
        'Gasto inusual en ' || r.categoria,
        '"' || r.descripcion || '" por ' || public.formato_monto(r.monto) || ' es '
          || round(r.monto / r.promedio, 1) || ' veces tu gasto promedio en ' || r.categoria || '.');
    end if;
  end loop;

  -- gastos_superan_ingresos: una vez por mes.
  if v_gastos_mes > v_ingresos_mes then
    perform public.agregar_notificacion(
      v_usuario, 'gastos_superan_ingresos', 'gastos_superan_ingresos:' || v_mes,
      'Tus gastos superan tus ingresos',
      'Este mes llevas ' || public.formato_monto(v_gastos_mes) || ' en gastos y '
        || public.formato_monto(v_ingresos_mes) || ' en ingresos: '
        || public.formato_monto(v_gastos_mes - v_ingresos_mes) || ' más de lo que entró.');
  end if;

  -- saldo_bajo: cuenta activa en negativo (como mucho un aviso por cuenta y semana).
  for r in
    select c.id_cuenta,
           c.nombre,
           coalesce(c.saldo_inicial::numeric, 0) + coalesce((
             select sum(case lower(m.tipo)
                          when 'ingreso' then m.monto::numeric
                          when 'gasto' then -m.monto::numeric
                          else 0 end)
             from public.movimientos m
             where m.id_cuenta = c.id_cuenta
               and m.id_usuario = v_usuario
               and m.activo
           ), 0) as saldo
    from public.cuentas c
    where c.id_usuario = v_usuario
      and c.activo
  loop
    if r.saldo < 0 then
      perform public.agregar_notificacion(
        v_usuario, 'saldo_bajo', 'saldo_bajo:' || r.id_cuenta || ':' || v_semana,
        'Saldo en negativo',
        'Tu cuenta ' || r.nombre || ' tiene un saldo de ' || public.formato_monto(r.saldo)
          || '. Revisa tus últimos gastos.');
    end if;
  end loop;

  -- ===== Recordatorios =====

  -- registrar_movimientos: 5 días o más sin registrar nada (un aviso por cada "pausa").
  select max((m.fecha at time zone p_zona)::date) into v_fecha
  from public.movimientos m
  where m.id_usuario = v_usuario
    and m.activo;
  if v_fecha is null then
    select min(c.fecha_creacion::date) into v_fecha
    from public.cuentas c
    where c.id_usuario = v_usuario;
  end if;
  if v_fecha is not null and v_hoy - v_fecha >= 5 then
    perform public.agregar_notificacion(
      v_usuario, 'registrar_movimientos', 'registrar_movimientos:' || v_fecha,
      'Registra tus movimientos',
      'Hace ' || (v_hoy - v_fecha) || ' días no registras ingresos ni gastos. '
        || 'Mantén tu información al día para que tus resúmenes sean exactos.');
  end if;

  -- aporte_meta: desde el día 15, metas sin terminar que no recibieron aportes este mes.
  if extract(day from v_hoy) >= 15 then
    for r in
      select mt.id_meta, mt.nombre
      from public.metas mt
      where mt.id_usuario = v_usuario
        and mt.activo
        and coalesce((select sum(a.monto::numeric) from public.aportes a where a.id_meta = mt.id_meta), 0)
            < mt.monto_objetivo::numeric
        and not exists (
          select 1 from public.aportes a
          where a.id_meta = mt.id_meta
            and a.fecha::date >= v_inicio_mes
        )
    loop
      perform public.agregar_notificacion(
        v_usuario, 'aporte_meta', 'aporte_meta:' || r.id_meta || ':' || v_mes,
        'Aporta a tu meta',
        'Este mes no has aportado a tu meta ' || r.nombre || '. Un aporte pequeño también suma.');
    end loop;
  end if;

  -- dia_ingreso: según la frecuencia, ya debería haber llegado el siguiente ingreso.
  if v_frecuencia is not null then
    select max((m.fecha at time zone p_zona)::date) into v_fecha
    from public.movimientos m
    where m.id_usuario = v_usuario
      and m.activo
      and lower(m.tipo) = 'ingreso';
    if v_fecha is not null then
      v_proxima := case v_frecuencia
                     when 'mensual'   then (v_fecha + interval '1 month')::date
                     when 'quincenal' then v_fecha + 15
                     when 'semanal'   then v_fecha + 7
                     else v_fecha + 1
                   end;
      if v_hoy >= v_proxima then
        perform public.agregar_notificacion(
          v_usuario, 'dia_ingreso', 'dia_ingreso:' || v_proxima,
          'Registra tu ingreso',
          'Según tu frecuencia (' || v_frecuencia || '), tu siguiente ingreso era el '
            || to_char(v_proxima, 'DD/MM') || '. Regístralo cuando lo recibas.');
      end if;
    end if;
  end if;

  -- resumen_mes: resumen del mes pasado, si tuvo movimientos.
  select count(*) as cantidad,
         coalesce(sum(m.monto::numeric) filter (where lower(m.tipo) = 'ingreso'), 0) as ingresos,
         coalesce(sum(m.monto::numeric) filter (where lower(m.tipo) = 'gasto'), 0) as gastos
    into r
  from public.movimientos m
  join public.cuentas c on c.id_cuenta = m.id_cuenta and c.activo
  where m.id_usuario = v_usuario
    and m.activo
    and (m.fecha at time zone p_zona)::date between v_inicio_ant and v_fin_ant;
  if r.cantidad > 0 then
    perform public.agregar_notificacion(
      v_usuario, 'resumen_mes', 'resumen_mes:' || to_char(v_inicio_ant, 'YYYY-MM'),
      'Resumen de ' || v_meses[extract(month from v_inicio_ant)::int],
      case
        when r.ingresos >= r.gastos then
          'Cerraste ' || v_meses[extract(month from v_inicio_ant)::int] || ' con '
            || public.formato_monto(r.ingresos - r.gastos) || ' ahorrados: ingresaste '
            || public.formato_monto(r.ingresos) || ' y gastaste ' || public.formato_monto(r.gastos) || '.'
        else
          'En ' || v_meses[extract(month from v_inicio_ant)::int] || ' gastaste '
            || public.formato_monto(r.gastos - r.ingresos) || ' más de lo que ingresaste: ingresaste '
            || public.formato_monto(r.ingresos) || ' y gastaste ' || public.formato_monto(r.gastos) || '.'
      end);
  end if;

  -- avance_meta / meta_cumplida.
  for r in
    select mt.id_meta,
           mt.nombre,
           mt.monto_objetivo::numeric as objetivo,
           coalesce((select sum(a.monto::numeric) from public.aportes a where a.id_meta = mt.id_meta), 0) as aportado
    from public.metas mt
    where mt.id_usuario = v_usuario
      and mt.activo
      and mt.monto_objetivo > 0
  loop
    v_proporcion := r.aportado / r.objetivo;
    if v_proporcion >= 1 then
      perform public.agregar_notificacion(
        v_usuario, 'meta_cumplida', 'meta_cumplida:' || r.id_meta,
        '¡Meta cumplida!',
        'Completaste tu meta ' || r.nombre || ': reuniste ' || public.formato_monto(r.aportado) || '. 🎉',
        1);
    elsif v_proporcion >= 0.25 then
      v_umbral := case when v_proporcion >= 0.75 then 75 when v_proporcion >= 0.5 then 50 else 25 end;
      perform public.agregar_notificacion(
        v_usuario, 'avance_meta', 'avance_meta:' || r.id_meta || ':' || v_umbral,
        'Vas por el ' || v_umbral || '% de tu meta',
        'Llevas ' || public.formato_monto(r.aportado) || ' de ' || public.formato_monto(r.objetivo)
          || ' en tu meta ' || r.nombre || '. ¡Sigue así!',
        round(v_proporcion, 4));
    end if;
  end loop;

  -- ===== Seguridad e IA =====

  -- consejo_ia: una vez por semana, la categoría con más gasto del mes.
  if v_gastos_mes > 0 then
    select k.nombre, sum(m.monto::numeric) as total
      into r
    from public.movimientos m
    join public.cuentas c on c.id_cuenta = m.id_cuenta and c.activo
    join public.categorias k on k.id_categoria = m.id_categoria
    where m.id_usuario = v_usuario
      and m.activo
      and lower(m.tipo) = 'gasto'
      and (m.fecha at time zone p_zona)::date between v_inicio_mes and v_hoy
    group by k.nombre
    order by total desc
    limit 1;
    if found then
      perform public.agregar_notificacion(
        v_usuario, 'consejo_ia', 'consejo_ia:' || v_semana,
        'Consejo de Finanzia IA',
        'Tu categoría con más gasto este mes es ' || r.nombre || ': ' || public.formato_monto(r.total)
          || ', el ' || round(r.total / v_gastos_mes * 100) || '% de tus gastos. '
          || 'Revisa si hay algo que puedas reducir.');
    end if;
  end if;
end;
$$;

-- ---------------------------------------------------------------------------------
-- 4. Avisos de seguridad (triggers en Supabase Auth)
-- ---------------------------------------------------------------------------------
-- Si algo falla aquí, el inicio de sesión sigue funcionando: el aviso simplemente no se crea.
create or replace function public.notificar_seguridad()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_usuario bigint;
  v_cuando  text;
begin
  select u.id_usuario into v_usuario
  from public.usuarios u
  where u.id_auth = new.id;
  if v_usuario is null then
    return new;
  end if;

  if tg_argv[0] = 'inicio_sesion' then
    if new.last_sign_in_at is null
       or new.last_sign_in_at is not distinct from old.last_sign_in_at
       -- El inicio de sesión automático al crear la cuenta no se avisa.
       or new.last_sign_in_at < new.created_at + interval '1 minute' then
      return new;
    end if;
    v_cuando := to_char(new.last_sign_in_at at time zone 'America/Bogota', 'DD/MM/YYYY "a las" HH12:MI AM');
    perform public.agregar_notificacion(
      v_usuario, 'inicio_sesion', 'inicio_sesion:' || floor(extract(epoch from new.last_sign_in_at))::bigint,
      'Nuevo inicio de sesión',
      'Se inició sesión en tu cuenta el ' || v_cuando || '. Si no fuiste tú, cambia tu contraseña.');
  else
    if new.encrypted_password is not distinct from old.encrypted_password then
      return new;
    end if;
    v_cuando := to_char(now() at time zone 'America/Bogota', 'DD/MM/YYYY "a las" HH12:MI AM');
    perform public.agregar_notificacion(
      v_usuario, 'contrasena_cambiada', 'contrasena_cambiada:' || floor(extract(epoch from now()))::bigint,
      'Contraseña actualizada',
      'Tu contraseña se cambió el ' || v_cuando || '. Si no fuiste tú, cámbiala de inmediato.');
  end if;
  return new;
exception when others then
  return new;
end;
$$;

drop trigger if exists al_iniciar_sesion_notificar on auth.users;
create trigger al_iniciar_sesion_notificar
  after update of last_sign_in_at on auth.users
  for each row execute function public.notificar_seguridad('inicio_sesion');

drop trigger if exists al_cambiar_contrasena_notificar on auth.users;
create trigger al_cambiar_contrasena_notificar
  after update of encrypted_password on auth.users
  for each row execute function public.notificar_seguridad('contrasena_cambiada');

-- ---------------------------------------------------------------------------------
-- 5. Permisos y RLS (mismo esquema que 02-permisos-y-rls.sql)
-- ---------------------------------------------------------------------------------
revoke all on public.notificaciones from anon, authenticated;
-- El usuario solo puede leerlas y marcarlas como leídas o descartarlas (activo = false).
-- Las crea el sistema (la función y los triggers), no la app.
grant select, update (leida, activo) on public.notificaciones to authenticated;

alter table public.notificaciones enable row level security;

create policy "notificaciones: ver las propias"
  on public.notificaciones for select to authenticated
  using (id_usuario = (select public.id_usuario_actual()));

create policy "notificaciones: marcar las propias"
  on public.notificaciones for update to authenticated
  using (id_usuario = (select public.id_usuario_actual()))
  with check (id_usuario = (select public.id_usuario_actual()));

-- Funciones: la app solo puede llamar a generar_notificaciones.
revoke all on function public.formato_monto(numeric) from public, anon, authenticated;
revoke all on function public.agregar_notificacion(bigint, text, text, text, text, numeric) from public, anon, authenticated;
revoke all on function public.notificar_seguridad() from public, anon, authenticated;
revoke all on function public.generar_notificaciones(text) from public, anon;
grant execute on function public.generar_notificaciones(text) to authenticated;

commit;
