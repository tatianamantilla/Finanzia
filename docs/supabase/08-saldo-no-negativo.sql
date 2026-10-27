-- =====================================================================================
-- 08 · El saldo de una cuenta nunca queda negativo           (spec 10 · revisar y ejecutar)
-- =====================================================================================
-- Qué hace:
--   La app ya valida la regla antes de guardar. Este archivo agrega la misma regla en la base
--   de datos, para que se cumpla aunque alguien guarde desde otro lugar (otra app, el SQL
--   Editor por error o dos teléfonos a la vez).
--
--   Saldo disponible de una cuenta =
--     saldo_inicial + ingresos − gastos (movimientos activos) − aportes a metas desde esa cuenta.
--
--   Se rechaza (con error) cualquier INSERT, UPDATE o DELETE en movimientos o en aportes que
--   BAJE el saldo de una cuenta y la deje por debajo de 0. Lo que no baja el saldo siempre se
--   permite: así una cuenta que ya estaba en negativo por pruebas anteriores se puede corregir
--   (por ejemplo, bajando un gasto), pero no empeorar.
--
-- Qué NO hace:
--   - No modifica ningún dato existente. Las cuentas que hoy estén en negativo se quedan igual.
--   - No cambia tablas, columnas ni políticas RLS.
--
-- Requisito: haber ejecutado antes 01 y 02 (usa las tablas cuentas, movimientos y aportes).
-- Cómo ejecutarlo: Supabase → SQL Editor → New query → pegar todo → Run.
-- Se puede ejecutar más de una vez sin problema. Todo va en una transacción.
-- =====================================================================================

begin;

-- Saldo disponible actual de una cuenta (con la fila que se acaba de guardar ya incluida).
create or replace function public.saldo_disponible_cuenta(p_id_cuenta bigint)
returns numeric
language sql
stable
security definer
set search_path = ''
as $$
  select c.saldo_inicial
    + coalesce((
        select sum(case when m.tipo = 'ingreso' then m.monto else -m.monto end)
        from public.movimientos m
        where m.id_cuenta = c.id_cuenta and m.activo
      ), 0)
    - coalesce((
        select sum(a.monto)
        from public.aportes a
        where a.id_cuenta = c.id_cuenta
      ), 0)
  from public.cuentas c
  where c.id_cuenta = p_id_cuenta;
$$;

-- Si el cambio bajó el saldo de la cuenta y la dejó en negativo, se cancela todo.
create or replace function public.revisar_saldo_cuenta(p_id_cuenta bigint, p_cambio numeric)
returns void
language plpgsql
security definer
set search_path = ''
as $$
begin
  if p_id_cuenta is null or p_cambio >= 0 then
    return;
  end if;
  -- Bloquea la cuenta para que dos guardados a la vez no se salten la regla.
  perform 1 from public.cuentas where id_cuenta = p_id_cuenta for update;
  if public.saldo_disponible_cuenta(p_id_cuenta) < 0 then
    raise exception 'No tienes saldo suficiente: la cuenta quedaría con saldo negativo.'
      using errcode = 'P0001';
  end if;
end;
$$;

-- Movimientos: un ingreso suma, un gasto resta; un movimiento inactivo no cuenta.
create or replace function public.validar_saldo_movimientos()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  cuenta_nueva bigint;
  cambio_nuevo numeric := 0;
  cuenta_vieja bigint;
  cambio_viejo numeric := 0;
begin
  if tg_op in ('INSERT', 'UPDATE') then
    cuenta_nueva := new.id_cuenta;
    cambio_nuevo := case when not new.activo then 0 when new.tipo = 'ingreso' then new.monto else -new.monto end;
  end if;
  if tg_op in ('UPDATE', 'DELETE') then
    cuenta_vieja := old.id_cuenta;
    cambio_viejo := -(case when not old.activo then 0 when old.tipo = 'ingreso' then old.monto else -old.monto end);
  end if;
  -- Misma cuenta antes y después (lo normal al editar): un solo cambio neto.
  if cuenta_nueva is not null and cuenta_nueva = cuenta_vieja then
    cambio_nuevo := cambio_nuevo + cambio_viejo;
    cuenta_vieja := null;
  end if;
  perform public.revisar_saldo_cuenta(cuenta_nueva, cambio_nuevo);
  perform public.revisar_saldo_cuenta(cuenta_vieja, cambio_viejo);
  return null;
end;
$$;

-- Aportes a metas: siempre restan del saldo de su cuenta (son un ahorro).
create or replace function public.validar_saldo_aportes()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  cuenta_nueva bigint;
  cambio_nuevo numeric := 0;
  cuenta_vieja bigint;
  cambio_viejo numeric := 0;
begin
  if tg_op in ('INSERT', 'UPDATE') then
    cuenta_nueva := new.id_cuenta;
    cambio_nuevo := -new.monto;
  end if;
  if tg_op in ('UPDATE', 'DELETE') then
    cuenta_vieja := old.id_cuenta;
    cambio_viejo := old.monto;
  end if;
  if cuenta_nueva is not null and cuenta_nueva = cuenta_vieja then
    cambio_nuevo := cambio_nuevo + cambio_viejo;
    cuenta_vieja := null;
  end if;
  perform public.revisar_saldo_cuenta(cuenta_nueva, cambio_nuevo);
  perform public.revisar_saldo_cuenta(cuenta_vieja, cambio_viejo);
  return null;
end;
$$;

drop trigger if exists saldo_no_negativo_movimientos on public.movimientos;
create trigger saldo_no_negativo_movimientos
  after insert or update or delete on public.movimientos
  for each row execute function public.validar_saldo_movimientos();

drop trigger if exists saldo_no_negativo_aportes on public.aportes;
create trigger saldo_no_negativo_aportes
  after insert or update or delete on public.aportes
  for each row execute function public.validar_saldo_aportes();

-- Estas funciones solo las usan los triggers: nadie puede llamarlas desde la app.
revoke all on function public.saldo_disponible_cuenta(bigint) from public, anon, authenticated;
revoke all on function public.revisar_saldo_cuenta(bigint, numeric) from public, anon, authenticated;
revoke all on function public.validar_saldo_movimientos() from public, anon, authenticated;
revoke all on function public.validar_saldo_aportes() from public, anon, authenticated;

commit;
