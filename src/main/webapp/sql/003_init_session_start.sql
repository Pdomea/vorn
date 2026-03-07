-- BWI520 / vorn - Inkrement 4: Session Start + Snapshot
-- Vor Ausführung im Query Tool:
-- SET search_path TO bwi520_633959, public;

CREATE TABLE IF NOT EXISTS workout_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    training_id BIGINT NOT NULL REFERENCES trainings(id),
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'FINISHED'))
);

CREATE INDEX IF NOT EXISTS ix_workout_sessions_user_id ON workout_sessions(user_id);
CREATE INDEX IF NOT EXISTS ix_workout_sessions_training_id ON workout_sessions(training_id);
CREATE INDEX IF NOT EXISTS ix_workout_sessions_status ON workout_sessions(status);

-- Wichtige Fachregel: max. eine aktive Session pro User.
CREATE UNIQUE INDEX IF NOT EXISTS uq_workout_sessions_user_active
    ON workout_sessions(user_id)
    WHERE status = 'ACTIVE';

CREATE TABLE IF NOT EXISTS session_exercises (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES workout_sessions(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercises(id),
    exercise_name_snapshot VARCHAR(150) NOT NULL,
    planned_sets_snapshot INT NOT NULL CHECK (planned_sets_snapshot > 0),
    planned_reps_snapshot INT NOT NULL CHECK (planned_reps_snapshot > 0),
    sort_order INT NOT NULL CHECK (sort_order > 0),
    UNIQUE (session_id, sort_order),
    UNIQUE (session_id, exercise_id)
);

CREATE INDEX IF NOT EXISTS ix_session_exercises_session_id ON session_exercises(session_id);
CREATE INDEX IF NOT EXISTS ix_session_exercises_exercise_id ON session_exercises(exercise_id);
