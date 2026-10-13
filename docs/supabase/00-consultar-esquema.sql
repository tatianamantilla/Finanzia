-- 00 · Consultar el esquema (SOLO LECTURA: no cambia nada)
--
-- Cómo usarlo:
--   1. Supabase → SQL Editor → New query.
--   2. Pega todo este archivo y pulsa "Run".
--   3. Copia el único resultado (una celda de texto llamada "esquema") y pégalo en el chat.
--
-- Devuelve: columnas de cada tabla del esquema public, claves primarias y foráneas,
-- si RLS está activo, políticas existentes, permisos de los roles de la app,
-- triggers y el contenido de la tabla de catálogo "colores" (no tiene datos de usuarios).

select jsonb_pretty(jsonb_build_object(
  'columnas', (
    select jsonb_agg(jsonb_build_object(
      'tabla', c.table_name,
      'columna', c.column_name,
      'tipo', c.data_type,
      'obligatoria', c.is_nullable = 'NO',
      'por_defecto', c.column_default
    ) order by c.table_name, c.ordinal_position)
    from information_schema.columns c
    where c.table_schema = 'public'
  ),
  'restricciones', (
    select jsonb_agg(jsonb_build_object(
      'tabla', rel.relname,
      'nombre', con.conname,
      'tipo', con.contype,
      'definicion', pg_get_constraintdef(con.oid)
    ) order by rel.relname, con.conname)
    from pg_constraint con
    join pg_class rel on rel.oid = con.conrelid
    join pg_namespace n on n.oid = rel.relnamespace
    where n.nspname = 'public'
  ),
  'rls_activo', (
    select jsonb_agg(jsonb_build_object('tabla', relname, 'rls', relrowsecurity) order by relname)
    from pg_class c join pg_namespace n on n.oid = c.relnamespace
    where n.nspname = 'public' and c.relkind = 'r'
  ),
  'politicas', (
    select jsonb_agg(jsonb_build_object(
      'tabla', tablename, 'politica', policyname, 'accion', cmd,
      'roles', roles, 'usando', qual, 'comprobando', with_check
    ) order by tablename, policyname)
    from pg_policies where schemaname = 'public'
  ),
  'permisos', (
    select jsonb_agg(jsonb_build_object('tabla', table_name, 'rol', grantee, 'permiso', privilege_type)
                     order by table_name, grantee, privilege_type)
    from information_schema.role_table_grants
    where table_schema = 'public' and grantee in ('anon', 'authenticated')
  ),
  'triggers', (
    select jsonb_agg(jsonb_build_object(
      'tabla', event_object_schema || '.' || event_object_table,
      'trigger', trigger_name, 'momento', action_timing, 'evento', event_manipulation,
      'accion', action_statement
    ))
    from information_schema.triggers
    where event_object_schema in ('public', 'auth')
  ),
  'colores', (select jsonb_agg(to_jsonb(c)) from public.colores c)
)) as esquema;
