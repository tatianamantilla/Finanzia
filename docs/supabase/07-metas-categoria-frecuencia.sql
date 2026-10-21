-- =====================================================================================
-- 07 · Categoría y frecuencia de aporte en Metas                (fase 2 · revisar y ejecutar)
-- =====================================================================================
-- Responde a las decisiones pendientes 1 y 2 de docs/specs/08-nueva-meta.md
-- (aprobado por Juan, 2026-10-21).
--
-- Qué hace:
--   Agrega dos columnas a la tabla metas. Las metas que ya existen quedan con las dos
--   columnas vacías (null): la pantalla Metas las muestra como "Sin categoría" y
--   "Próximo aporte: aún no hay datos".
--
--   1. categoria: una de las seis categorías fijas de la pantalla Nueva Meta.
--        viajes_ocio  → Viajes & Ocio      emergencia → Emergencia
--        tecnologia   → Tecnología         vehiculo   → Vehículo
--        vivienda     → Vivienda           educacion  → Educación
--   2. frecuencia_aporte: cada cuánto se va a aportar a la meta.
--        quincenal → Quincenal (15 y 30)   mensual → Mensual (Día 30)
--
-- Permisos: no cambian. La política "metas: solo las propias" de 02-permisos-y-rls.sql ya
-- deja que cada persona cree y lea solo sus metas, incluidas estas columnas.
--
-- Cómo ejecutarlo: Supabase → SQL Editor → New query → pegar todo → Run.
-- Se puede ejecutar más de una vez sin problema.
-- =====================================================================================

begin;

alter table public.metas
  add column if not exists categoria varchar
  check (categoria in ('viajes_ocio', 'emergencia', 'tecnologia', 'vehiculo', 'vivienda', 'educacion'));

alter table public.metas
  add column if not exists frecuencia_aporte varchar
  check (frecuencia_aporte in ('quincenal', 'mensual'));

commit;
