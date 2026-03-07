-- BWI520 / vorn - Inkrement 7: Session-Plan-Kontext
-- Vereinfachte Fresh-Setup-Variante:
-- Dieses Skript ist fuer eine leere bzw. frisch aufgebaute DB gedacht.
-- Bei erneutem Ausfuehren koennen ADD CONSTRAINT-Statements fehlschlagen.
-- Vor Ausführung im Query Tool:
-- SET search_path TO bwi520_633959, public;

ALTER TABLE IF EXISTS workout_sessions
    ADD COLUMN IF NOT EXISTS plan_id BIGINT,
    ADD COLUMN IF NOT EXISTS plan_week_id BIGINT;

ALTER TABLE workout_sessions
    ADD CONSTRAINT fk_workout_sessions_plan_id
        FOREIGN KEY (plan_id) REFERENCES plans(id);

ALTER TABLE workout_sessions
    ADD CONSTRAINT fk_workout_sessions_plan_week_id
        FOREIGN KEY (plan_week_id) REFERENCES plan_weeks(id);

CREATE INDEX IF NOT EXISTS ix_workout_sessions_plan_id ON workout_sessions(plan_id);
CREATE INDEX IF NOT EXISTS ix_workout_sessions_plan_week_id ON workout_sessions(plan_week_id);
