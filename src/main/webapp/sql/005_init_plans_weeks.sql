-- BWI520 / vorn - Inkrement 6: Plans + Weeks Basis
-- Vor Ausführung im Query Tool:
-- SET search_path TO bwi520_633959, public;

CREATE TABLE IF NOT EXISTS plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS plan_weeks (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    week_no INT NOT NULL CHECK (week_no > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (plan_id, week_no)
);

CREATE TABLE IF NOT EXISTS plan_week_trainings (
    id BIGSERIAL PRIMARY KEY,
    plan_week_id BIGINT NOT NULL REFERENCES plan_weeks(id) ON DELETE CASCADE,
    training_id BIGINT NOT NULL REFERENCES trainings(id),
    sort_order INT NOT NULL CHECK (sort_order > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (plan_week_id, sort_order)
);

CREATE INDEX IF NOT EXISTS ix_plans_status ON plans(status);
CREATE INDEX IF NOT EXISTS ix_plan_weeks_plan_id ON plan_weeks(plan_id);
CREATE INDEX IF NOT EXISTS ix_plan_week_trainings_week_id ON plan_week_trainings(plan_week_id);
CREATE INDEX IF NOT EXISTS ix_plan_week_trainings_training_id ON plan_week_trainings(training_id);
