-- BWI520 / vorn - Inkrement: Muskelgruppen für Übungen (Admin)
-- Vor Ausführung im Query Tool:
-- SET search_path TO bwi520_633959, public;

CREATE TABLE IF NOT EXISTS muscle_groups (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    label VARCHAR(80) NOT NULL UNIQUE,
    sort_order INT NOT NULL DEFAULT 100 CHECK (sort_order > 0)
);

CREATE TABLE IF NOT EXISTS exercise_muscle_groups (
    exercise_id BIGINT NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    muscle_group_id BIGINT NOT NULL REFERENCES muscle_groups(id),
    PRIMARY KEY (exercise_id, muscle_group_id)
);

CREATE INDEX IF NOT EXISTS ix_exercise_muscle_groups_muscle_group_id
    ON exercise_muscle_groups(muscle_group_id);

CREATE INDEX IF NOT EXISTS ix_exercise_muscle_groups_exercise_id
    ON exercise_muscle_groups(exercise_id);

INSERT INTO muscle_groups (code, label, sort_order)
SELECT 'CHEST', 'Brust', 10
WHERE NOT EXISTS (SELECT 1 FROM muscle_groups WHERE code = 'CHEST');

INSERT INTO muscle_groups (code, label, sort_order)
SELECT 'BACK', 'Rücken', 20
WHERE NOT EXISTS (SELECT 1 FROM muscle_groups WHERE code = 'BACK');

INSERT INTO muscle_groups (code, label, sort_order)
SELECT 'SHOULDERS', 'Schultern', 30
WHERE NOT EXISTS (SELECT 1 FROM muscle_groups WHERE code = 'SHOULDERS');

INSERT INTO muscle_groups (code, label, sort_order)
SELECT 'ARMS', 'Arme', 40
WHERE NOT EXISTS (SELECT 1 FROM muscle_groups WHERE code = 'ARMS');

INSERT INTO muscle_groups (code, label, sort_order)
SELECT 'CORE', 'Core', 50
WHERE NOT EXISTS (SELECT 1 FROM muscle_groups WHERE code = 'CORE');

INSERT INTO muscle_groups (code, label, sort_order)
SELECT 'LEGS', 'Beine', 60
WHERE NOT EXISTS (SELECT 1 FROM muscle_groups WHERE code = 'LEGS');

INSERT INTO muscle_groups (code, label, sort_order)
SELECT 'GLUTES', 'Gesäß', 70
WHERE NOT EXISTS (SELECT 1 FROM muscle_groups WHERE code = 'GLUTES');

INSERT INTO muscle_groups (code, label, sort_order)
SELECT 'CARDIO', 'Cardio', 80
WHERE NOT EXISTS (SELECT 1 FROM muscle_groups WHERE code = 'CARDIO');
