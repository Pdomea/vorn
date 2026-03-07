-- BWI520 / vorn - Inkrement 7: Persistenter aktiver Plan pro User
-- Vereinfachte Fresh-Setup-Variante:
-- Dieses Skript ist fuer eine leere bzw. frisch aufgebaute DB gedacht.
-- Bei erneutem Ausfuehren kann das ADD CONSTRAINT-Statement fehlschlagen.
-- Vor Ausführung im Query Tool:
-- SET search_path TO bwi520_633959, public;

ALTER TABLE IF EXISTS users
    ADD COLUMN IF NOT EXISTS active_plan_id BIGINT;

ALTER TABLE users
    ADD CONSTRAINT fk_users_active_plan_id
        FOREIGN KEY (active_plan_id) REFERENCES plans(id);

CREATE INDEX IF NOT EXISTS ix_users_active_plan_id ON users(active_plan_id);
