-- BWI520 / vorn - Inkrement 5: Tracking Logs + History
-- Vor Ausführung im Query Tool:
-- SET search_path TO bwi520_633959, public;

CREATE TABLE IF NOT EXISTS workout_logs (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES workout_sessions(id) ON DELETE CASCADE,
    session_exercise_id BIGINT NOT NULL REFERENCES session_exercises(id) ON DELETE CASCADE,
    set_no INT NOT NULL CHECK (set_no > 0),
    reps INT NOT NULL CHECK (reps > 0),
    weight NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (weight >= 0),
    note VARCHAR(500),
    logged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (session_id, session_exercise_id, set_no)
);

CREATE INDEX IF NOT EXISTS ix_workout_logs_session_id ON workout_logs(session_id);
CREATE INDEX IF NOT EXISTS ix_workout_logs_session_exercise_id ON workout_logs(session_exercise_id);
CREATE INDEX IF NOT EXISTS ix_workout_logs_logged_at ON workout_logs(logged_at);
