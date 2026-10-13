-- =====================================================================================
-- 02 · Permisos y políticas RLS                               (fase 2 · revisar y ejecutar)
-- =====================================================================================
-- Qué hace:
--   - Quita TODO permiso al rol anónimo (anon): sin iniciar sesión no se ve ni se cambia nada.
--   - Da permisos al rol de usuarios con sesión (authenticated).
--   - Activa RLS en las 9 tablas: cada persona solo ve y cambia SUS datos.
--
-- Requisito: haber ejecutado antes 01-usuarios-y-auth.sql (usa la columna usuarios.id_auth).
-- Cómo ejecutarlo: Supabase → SQL Editor → New query → pegar todo → Run.
-- Todo va dentro de una transacción: si algo falla, no se aplica nada.
-- =====================================================================================

begin;

-- ---------------------------------------------------------------------------------
-- A. Permisos por rol (sin esto, la app recibe "permission denied")
-- ---------------------------------------------------------------------------------
revoke all on all tables in schema public from anon;
revoke all on all sequences in schema public from anon;

-- usuarios: puede leer su fila y cambiar solo nombre y frecuencia. No puede crear filas
-- (lo hace el trigger del archivo 01), ni borrarlas, ni cambiar correo o id_auth.
revoke all on public.usuarios from authenticated;
grant select on public.usuarios to authenticated;
grant update (nombre, frecuencia_ingreso) on public.usuarios to authenticated;

-- colores: catálogo de solo lectura.
revoke all on public.colores from authenticated;
grant select on public.colores to authenticated;

-- Tablas de datos de cada persona.
grant select, insert, update, delete on
  public.categorias, public.cuentas, public.movimientos, public.metas,
  public.aportes, public.conversaciones, public.mensajes
  to authenticated;

grant usage, select on all sequences in schema public to authenticated;

-- ---------------------------------------------------------------------------------
-- B. "¿Quién soy?": traduce el usuario de Auth (uuid) a su id_usuario (número).
-- ---------------------------------------------------------------------------------
create or replace function public.id_usuario_actual()
returns bigint
language sql
stable
security definer
set search_path = ''
as $$
  select id_usuario from public.usuarios where id_auth = auth.uid();
$$;

revoke all on function public.id_usuario_actual() from public, anon;
grant execute on function public.id_usuario_actual() to authenticated;

-- ---------------------------------------------------------------------------------
-- C. Activar RLS en todas las tablas
-- ---------------------------------------------------------------------------------
alter table public.usuarios       enable row level security;
alter table public.colores        enable row level security;
alter table public.categorias     enable row level security;
alter table public.cuentas        enable row level security;
alter table public.metas          enable row level security;
alter table public.movimientos    enable row level security;
alter table public.aportes        enable row level security;
alter table public.conversaciones enable row level security;
alter table public.mensajes       enable row level security;

-- ---------------------------------------------------------------------------------
-- D. Políticas
-- ("(select …)" hace que Supabase calcule el usuario una sola vez por consulta.)
-- ---------------------------------------------------------------------------------

-- usuarios: solo la propia fila.
create policy "usuarios: ver la propia fila"
  on public.usuarios for select to authenticated
  using (id_auth = (select auth.uid()));

create policy "usuarios: editar la propia fila"
  on public.usuarios for update to authenticated
  using (id_auth = (select auth.uid()))
  with check (id_auth = (select auth.uid()));

-- colores: cualquiera con sesión puede leerlos.
create policy "colores: lectura para usuarios con sesión"
  on public.colores for select to authenticated
  using (true);

-- Tablas con id_usuario: cada persona maneja solo lo suyo.
create policy "categorias: solo las propias"
  on public.categorias for all to authenticated
  using (id_usuario = (select public.id_usuario_actual()))
  with check (id_usuario = (select public.id_usuario_actual()));

create policy "cuentas: solo las propias"
  on public.cuentas for all to authenticated
  using (id_usuario = (select public.id_usuario_actual()))
  with check (id_usuario = (select public.id_usuario_actual()));

create policy "metas: solo las propias"
  on public.metas for all to authenticated
  using (id_usuario = (select public.id_usuario_actual()))
  with check (id_usuario = (select public.id_usuario_actual()));

create policy "conversaciones: solo las propias"
  on public.conversaciones for all to authenticated
  using (id_usuario = (select public.id_usuario_actual()))
  with check (id_usuario = (select public.id_usuario_actual()));

-- movimientos: propios, y además la cuenta y la categoría deben ser de la misma persona
-- (así nadie puede registrar un movimiento en la cuenta de otro).
create policy "movimientos: solo los propios"
  on public.movimientos for all to authenticated
  using (id_usuario = (select public.id_usuario_actual()))
  with check (
    id_usuario = (select public.id_usuario_actual())
    and exists (
      select 1 from public.cuentas c
      where c.id_cuenta = movimientos.id_cuenta
        and c.id_usuario = (select public.id_usuario_actual())
    )
    and (
      id_categoria is null
      or exists (
        select 1 from public.categorias k
        where k.id_categoria = movimientos.id_categoria
          and k.id_usuario = (select public.id_usuario_actual())
      )
    )
  );

-- aportes: la meta (y la cuenta) deben ser de la persona.
create policy "aportes: solo de metas propias"
  on public.aportes for all to authenticated
  using (
    exists (
      select 1 from public.metas m
      where m.id_meta = aportes.id_meta
        and m.id_usuario = (select public.id_usuario_actual())
    )
  )
  with check (
    exists (
      select 1 from public.metas m
      where m.id_meta = aportes.id_meta
        and m.id_usuario = (select public.id_usuario_actual())
    )
    and exists (
      select 1 from public.cuentas c
      where c.id_cuenta = aportes.id_cuenta
        and c.id_usuario = (select public.id_usuario_actual())
    )
  );

-- mensajes: la conversación debe ser de la persona.
create policy "mensajes: solo de conversaciones propias"
  on public.mensajes for all to authenticated
  using (
    exists (
      select 1 from public.conversaciones v
      where v.id_conversacion = mensajes.id_conversacion
        and v.id_usuario = (select public.id_usuario_actual())
    )
  )
  with check (
    exists (
      select 1 from public.conversaciones v
      where v.id_conversacion = mensajes.id_conversacion
        and v.id_usuario = (select public.id_usuario_actual())
    )
  );

commit;
