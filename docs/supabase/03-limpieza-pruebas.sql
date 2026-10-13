-- =====================================================================================
-- 03 · Limpieza de los tests de integración          (fase 2 · opcional · revisar y ejecutar)
-- =====================================================================================
-- Para qué sirve:
--   Los tests de integración crean usuarios de prueba reales en Supabase. Con la clave
--   pública de la app no se pueden borrar usuarios de Auth, así que esta función permite que
--   un usuario DE PRUEBA se borre a sí mismo al terminar el test (y en cascada su fila de
--   usuarios, su cuenta, categorías y movimientos).
--
-- Por qué es seguro:
--   - Solo borra al usuario que la llama (auth.uid()), nunca a otro.
--   - Solo funciona si su correo empieza por "prueba-integracion-". Para cualquier usuario
--     real no hace nada y devuelve false.
--   - La app nunca la usa; solo los tests de app/src/test/.../integracion.
--
-- Si no la ejecutas, los tests igual corren, pero los usuarios de prueba quedan en
-- Authentication → Users y tendrás que borrarlos a mano.
--
-- Requisito: haber ejecutado antes 01 y 02.
-- =====================================================================================

begin;

create or replace function public.eliminar_usuario_de_prueba()
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
declare
  borrados integer;
begin
  delete from auth.users
  where id = auth.uid()
    and email like 'prueba-integracion-%';
  get diagnostics borrados = row_count;
  return borrados = 1;
end;
$$;

revoke all on function public.eliminar_usuario_de_prueba() from public, anon;
grant execute on function public.eliminar_usuario_de_prueba() to authenticated;

commit;
