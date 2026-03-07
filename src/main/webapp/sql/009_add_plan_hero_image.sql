-- BWI520 / vorn - Inkrement 12: Planbild je Plan
-- Vor Ausführung im Query Tool:
-- SET search_path TO bwi520_633959, public;

ALTER TABLE plans
    ADD COLUMN IF NOT EXISTS hero_image_path VARCHAR(255);

