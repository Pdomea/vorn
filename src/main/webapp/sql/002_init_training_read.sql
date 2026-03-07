-- BWI520 / vorn - Inkrement 2: Trainings Read-Flow
-- Vor Ausführung im Query Tool:
-- SET search_path TO bwi520_633959, public;

CREATE TABLE IF NOT EXISTS trainings (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED', 'HIDDEN')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS exercises (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

CREATE TABLE IF NOT EXISTS training_exercises (
    id BIGSERIAL PRIMARY KEY,
    training_id BIGINT NOT NULL REFERENCES trainings(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercises(id),
    planned_sets INT NOT NULL CHECK (planned_sets > 0),
    planned_reps INT NOT NULL CHECK (planned_reps > 0),
    sort_order INT NOT NULL CHECK (sort_order > 0),
    UNIQUE (training_id, sort_order),
    UNIQUE (training_id, exercise_id)
);

CREATE INDEX IF NOT EXISTS ix_training_exercises_training_id ON training_exercises(training_id);
CREATE INDEX IF NOT EXISTS ix_training_exercises_exercise_id ON training_exercises(exercise_id);
CREATE INDEX IF NOT EXISTS ix_trainings_status ON trainings(status);

-- Seed trainings
INSERT INTO trainings (title, description, status)
SELECT 'Push Basics', 'Grundlegendes Push-Training für Brust, Schulter und Trizeps.', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM trainings WHERE title = 'Push Basics');

INSERT INTO trainings (title, description, status)
SELECT 'Lower Body Core', 'Fokus auf Beine und Koerperspannung.', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM trainings WHERE title = 'Lower Body Core');

INSERT INTO trainings (title, description, status)
SELECT 'Upper Draft Plan', 'Interner Entwurf, nicht sichtbar für USER.', 'DRAFT'
WHERE NOT EXISTS (SELECT 1 FROM trainings WHERE title = 'Upper Draft Plan');

-- Seed exercises
INSERT INTO exercises (name, description, status)
SELECT 'Bench Press', 'Langhantel-Bankdruecken', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM exercises WHERE name = 'Bench Press');

INSERT INTO exercises (name, description, status)
SELECT 'Overhead Press', 'Stehendes Schulterdruecken', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM exercises WHERE name = 'Overhead Press');

INSERT INTO exercises (name, description, status)
SELECT 'Triceps Dips', 'Dips für Trizeps und Brust', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM exercises WHERE name = 'Triceps Dips');

INSERT INTO exercises (name, description, status)
SELECT 'Squat', 'Kniebeuge mit Langhantel', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM exercises WHERE name = 'Squat');

INSERT INTO exercises (name, description, status)
SELECT 'Romanian Deadlift', 'RDL für hintere Kette', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM exercises WHERE name = 'Romanian Deadlift');

INSERT INTO exercises (name, description, status)
SELECT 'Plank', 'Core-Halteübung', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM exercises WHERE name = 'Plank');

-- Seed mappings: Push Basics
INSERT INTO training_exercises (training_id, exercise_id, planned_sets, planned_reps, sort_order)
SELECT t.id, e.id, 4, 8, 1
FROM trainings t, exercises e
WHERE t.title = 'Push Basics'
  AND e.name = 'Bench Press'
ON CONFLICT (training_id, sort_order) DO NOTHING;

INSERT INTO training_exercises (training_id, exercise_id, planned_sets, planned_reps, sort_order)
SELECT t.id, e.id, 4, 6, 2
FROM trainings t, exercises e
WHERE t.title = 'Push Basics'
  AND e.name = 'Overhead Press'
ON CONFLICT (training_id, sort_order) DO NOTHING;

INSERT INTO training_exercises (training_id, exercise_id, planned_sets, planned_reps, sort_order)
SELECT t.id, e.id, 3, 10, 3
FROM trainings t, exercises e
WHERE t.title = 'Push Basics'
  AND e.name = 'Triceps Dips'
ON CONFLICT (training_id, sort_order) DO NOTHING;

-- Seed mappings: Lower Body Core
INSERT INTO training_exercises (training_id, exercise_id, planned_sets, planned_reps, sort_order)
SELECT t.id, e.id, 5, 5, 1
FROM trainings t, exercises e
WHERE t.title = 'Lower Body Core'
  AND e.name = 'Squat'
ON CONFLICT (training_id, sort_order) DO NOTHING;

INSERT INTO training_exercises (training_id, exercise_id, planned_sets, planned_reps, sort_order)
SELECT t.id, e.id, 4, 8, 2
FROM trainings t, exercises e
WHERE t.title = 'Lower Body Core'
  AND e.name = 'Romanian Deadlift'
ON CONFLICT (training_id, sort_order) DO NOTHING;

INSERT INTO training_exercises (training_id, exercise_id, planned_sets, planned_reps, sort_order)
SELECT t.id, e.id, 3, 45, 3
FROM trainings t, exercises e
WHERE t.title = 'Lower Body Core'
  AND e.name = 'Plank'
ON CONFLICT (training_id, sort_order) DO NOTHING;
