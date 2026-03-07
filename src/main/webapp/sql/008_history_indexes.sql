-- BWI520 / vorn - Optional: Performance-Indizes für Verlauf in Wochenansicht
-- Hinweis: Fokus auf einen zusaetzlichen Verlauf-Index fuer Sessions.
-- Vor Ausführung im Query Tool:
-- SET search_path TO bwi520_633959, public;

CREATE INDEX IF NOT EXISTS ix_workout_sessions_user_plan_week_training_finished
    ON workout_sessions(user_id, plan_id, plan_week_id, training_id, ended_at DESC, id DESC)
    WHERE status = 'FINISHED';
