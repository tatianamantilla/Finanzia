-- =====================================================================================
-- 04 · Recuperar contraseña con nombre + correo                (fase 2 · revisar y ejecutar)
-- =====================================================================================
-- Qué hace:
--   Crea dos funciones que la app llama SIN sesión iniciada (pantallas "Olvidé mi contraseña"):
--   1. verificar_identidad(nombre, correo): dice si el nombre coincide con el de esa cuenta.
--   2. restablecer_contrasena(nombre, correo, nueva, cerrar_sesiones): vuelve a comprobar
--      nombre y correo y guarda la contraseña nueva en Supabase Auth (cifrada con bcrypt).
--      Si cerrar_sesiones es true, cierra todas las sesiones abiertas de esa cuenta.
--
-- DECISIÓN DEL EQUIPO (spec 02, pendiente 1): se mantiene el diseño con nombre + correo.
--   ADVERTENCIA: no es seguro para usuarios reales. Cualquiera que sepa el nombre y el correo
--   de alguien puede cambiarle la contraseña. Sirve para la demo académica; antes de publicar
--   la app hay que cambiarlo por un código enviado al correo.
--
-- Requisito: haber ejecutado 01 y 02.
-- Cómo ejecutarlo: Supabase → SQL Editor → New query → pegar todo → Run.
-- =====================================================================================

begin;

-- Devuelve: 'ok', 'correo_no_registrado' o 'nombre_no_coincide'.
create or replace function public.verificar_identidad(p_nombre text, p_correo text)
returns text
language plpgsql
stable
security definer
set search_path = ''
as $$
declare
  v_nombre text;
begin
  select u.nombre into v_nombre
  from public.usuarios u
  where lower(u.correo) = lower(trim(p_correo))
    and u.id_auth is not null;

  if not found then
    return 'correo_no_registrado';
  end if;
  if lower(trim(v_nombre)) <> lower(trim(p_nombre)) then
    return 'nombre_no_coincide';
  end if;
  return 'ok';
end;
$$;

-- Devuelve lo mismo que verificar_identidad; si es 'ok', la contraseña ya quedó cambiada.
create or replace function public.restablecer_contrasena(
  p_nombre text,
  p_correo text,
  p_nueva text,
  p_cerrar_sesiones boolean default true
)
returns text
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_resultado text;
  v_id_auth uuid;
begin
  v_resultado := public.verificar_identidad(p_nombre, p_correo);
  if v_resultado <> 'ok' then
    return v_resultado;
  end if;

  -- Mismos requisitos que la app: 8+ caracteres, un número y una mayúscula.
  if length(p_nueva) < 8 or p_nueva !~ '[0-9]' or p_nueva !~ '[A-Z]' then
    return 'contrasena_invalida';
  end if;

  select u.id_auth into v_id_auth
  from public.usuarios u
  where lower(u.correo) = lower(trim(p_correo));

  update auth.users
  set encrypted_password = extensions.crypt(p_nueva, extensions.gen_salt('bf')),
      updated_at = now()
  where id = v_id_auth;

  if p_cerrar_sesiones then
    delete from auth.sessions where user_id = v_id_auth;
  end if;

  return 'ok';
end;
$$;

-- Solo estas dos funciones quedan disponibles sin sesión; las tablas siguen protegidas.
revoke all on function public.verificar_identidad(text, text) from public;
revoke all on function public.restablecer_contrasena(text, text, text, boolean) from public;
grant execute on function public.verificar_identidad(text, text) to anon, authenticated;
grant execute on function public.restablecer_contrasena(text, text, text, boolean) to anon, authenticated;

commit;
