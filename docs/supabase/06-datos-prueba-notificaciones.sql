-- =====================================================================================
-- 06 · Datos de prueba para Notificaciones (usuario juan1)            (solo para pruebas)
-- =====================================================================================
-- Qué hace:
--   Crea 6 notificaciones de prueba para el usuario cuyo nombre es "juan1": dos de cada
--   filtro (Alertas de presupuesto, Recordatorios, Seguridad e IA). Hay leídas y sin leer,
--   una con barra de progreso y una de ayer (para ver cómo quedan las anteriores a "HOY").
--
--   Todas tienen una clave que empieza por "prueba:". Se puede ejecutar varias veces: cada
--   vez borra las de prueba anteriores y las vuelve a crear con la fecha de ese momento.
--   Las notificaciones reales (las que crea la app) no se tocan.
--
-- Requisito: haber ejecutado 05-notificaciones.sql.
-- Cómo ejecutarlo: Supabase → SQL Editor → New query → pegar todo → Run.
-- Para borrarlas al terminar, ver el final del archivo.
-- =====================================================================================

do $$
declare
  v_usuario bigint;
  v_cantidad int;
begin
  -- La tabla de la propuesta inicial no tenía la columna "clave": hay que ejecutar antes el 05.
  if not exists (
    select 1 from information_schema.columns
    where table_schema = 'public' and table_name = 'notificaciones' and column_name = 'clave'
  ) then
    raise exception 'Primero ejecuta docs/supabase/05-notificaciones.sql: la tabla notificaciones que tienes es la de la propuesta (05-notificaciones-PROPUESTA.sql) y no tiene la columna "clave".';
  end if;

  select count(*), min(id_usuario) into v_cantidad, v_usuario
  from public.usuarios
  where lower(trim(nombre)) = 'juan1';

  if v_cantidad = 0 then
    raise exception 'No hay ningún usuario con nombre "juan1". Revisa el nombre en la tabla usuarios.';
  elsif v_cantidad > 1 then
    raise exception 'Hay % usuarios con nombre "juan1". Cambia el filtro por el correo (columna correo).', v_cantidad;
  end if;

  delete from public.notificaciones
  where id_usuario = v_usuario
    and clave like 'prueba:%';

  insert into public.notificaciones (id_usuario, tipo, clave, titulo, mensaje, progreso, leida, fecha) values
    -- Alertas de presupuesto
    (v_usuario, 'gasto_mayor', 'prueba:gasto_mayor',
     'Más gasto en Comida',
     'Llevas 35% más en Comida que el mes pasado a esta fecha: $270,000.00 frente a $200,000.00.',
     null, false, now() - interval '20 minutes'),
    (v_usuario, 'saldo_bajo', 'prueba:saldo_bajo',
     'Saldo en negativo',
     'Tu cuenta Principal tiene un saldo de -$42,000.00. Revisa tus últimos gastos.',
     null, false, now() - interval '2 hours'),
    -- Recordatorios
    (v_usuario, 'avance_meta', 'prueba:avance_meta',
     'Vas por el 50% de tu meta',
     'Llevas $500,000.00 de $1,000,000.00 en tu meta Viaje. ¡Sigue así!',
     0.5, false, now() - interval '3 hours'),
    (v_usuario, 'registrar_movimientos', 'prueba:registrar_movimientos',
     'Registra tus movimientos',
     'Hace 5 días no registras ingresos ni gastos. Mantén tu información al día para que tus resúmenes sean exactos.',
     null, true, now() - interval '1 day'),
    -- Seguridad e IA
    (v_usuario, 'consejo_ia', 'prueba:consejo_ia',
     'Consejo de Finanzia IA',
     'Tu categoría con más gasto este mes es Transporte: $94,000.00, el 94% de tus gastos. Revisa si hay algo que puedas reducir.',
     null, false, now() - interval '5 minutes'),
    (v_usuario, 'inicio_sesion', 'prueba:inicio_sesion',
     'Nuevo inicio de sesión',
     'Se inició sesión en tu cuenta. Si no fuiste tú, cambia tu contraseña.',
     null, true, now() - interval '4 hours');

  raise notice 'Listo: 6 notificaciones de prueba creadas para juan1 (id_usuario %).', v_usuario;
end;
$$;

-- Para ver lo que quedó:
select tipo, titulo, leida, fecha
from public.notificaciones
where id_usuario = (select id_usuario from public.usuarios where lower(trim(nombre)) = 'juan1' limit 1)
order by fecha desc;

-- Para BORRAR las de prueba cuando terminen, ejecuta solo esta línea (quitando los dos guiones):
-- delete from public.notificaciones where clave like 'prueba:%';
