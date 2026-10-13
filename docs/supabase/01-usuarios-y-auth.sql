-- =====================================================================================
-- 01 · Usuarios ligados a Supabase Auth                      (fase 2 · revisar y ejecutar)
-- =====================================================================================
-- Qué hace:
--   1. Quita la columna usuarios.contrasena: la contraseña vive SOLO en Supabase Auth.
--   2. Agrega usuarios.id_auth: une cada fila con su usuario de Auth (auth.users.id).
--      id_usuario sigue igual, así que las demás tablas no cambian.
--   3. Agrega usuarios.frecuencia_ingreso (mensual, quincenal, semanal o diario).
--   4. Crea un trigger: cada vez que alguien se registra en Auth se crean automáticamente
--      su fila en usuarios, su cuenta "Principal" y sus 4 categorías de gasto del diseño.
--   5. Mantiene usuarios.correo igual al correo de Auth si este cambia.
--
-- Antes de ejecutar:
--   - Si en usuarios hay filas de prueba creadas a mano, se conservan, pero quedan sin
--     id_auth (no tienen cuenta en Auth). Si alguna tiene el mismo correo que una persona
--     que se registre después, ese registro fallará por el "unique" de correo: bórrala antes.
--   - Ejecuta este archivo ANTES de 02-permisos-y-rls.sql.
--
-- Cómo ejecutarlo: Supabase → SQL Editor → New query → pegar todo → Run.
-- Todo va dentro de una transacción: si algo falla, no se aplica nada.
-- =====================================================================================

begin;

-- 1. La contraseña no se guarda en esta tabla.
alter table public.usuarios drop column contrasena;

-- 2. Unión con Supabase Auth. Si se borra el usuario de Auth, se borra su fila (y en cascada
--    sus cuentas, categorías, movimientos, metas y conversaciones).
alter table public.usuarios
  add column id_auth uuid unique references auth.users (id) on delete cascade;

-- 3. Frecuencia con la que la persona recibe sus ingresos (onboarding, Pantalla 4).
alter table public.usuarios
  add column frecuencia_ingreso varchar
  check (frecuencia_ingreso in ('mensual', 'quincenal', 'semanal', 'diario'));

-- 4. Al registrarse en Auth: fila en usuarios + cuenta "Principal" + categorías de gasto.
--    "security definer": se ejecuta con permisos del dueño de la base, porque el usuario
--    que se registra todavía no tiene permisos sobre estas tablas.
--    "set search_path = ''": evita que alguien suplante tablas o funciones (recomendación de Supabase).
create or replace function public.crear_perfil_usuario()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  nuevo_id bigint;
begin
  insert into public.usuarios (nombre, correo, id_auth)
  values (
    -- La app envía el nombre completo en los datos del registro ("nombre").
    coalesce(nullif(trim(new.raw_user_meta_data ->> 'nombre'), ''), split_part(new.email, '@', 1)),
    new.email,
    new.id
  )
  returning id_usuario into nuevo_id;

  insert into public.cuentas (id_usuario, nombre, tipo)
  values (nuevo_id, 'Principal', 'principal');

  insert into public.categorias (id_usuario, nombre, tipo) values
    (nuevo_id, 'Gastos Fijos', 'gasto'),
    (nuevo_id, 'Comida',       'gasto'),
    (nuevo_id, 'Diversión',    'gasto'),
    (nuevo_id, 'Transporte',   'gasto');

  return new;
end;
$$;

create trigger al_registrarse_crear_perfil
  after insert on auth.users
  for each row execute function public.crear_perfil_usuario();

-- 5. Si la persona cambia su correo en Auth, se actualiza también en usuarios.
create or replace function public.sincronizar_correo_usuario()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  update public.usuarios set correo = new.email where id_auth = new.id;
  return new;
end;
$$;

create trigger al_cambiar_correo_sincronizar
  after update of email on auth.users
  for each row
  when (old.email is distinct from new.email)
  execute function public.sincronizar_correo_usuario();

-- Estas funciones solo las usa el trigger: nadie puede llamarlas desde la app.
revoke all on function public.crear_perfil_usuario() from public, anon, authenticated;
revoke all on function public.sincronizar_correo_usuario() from public, anon, authenticated;

commit;
