package app.service;

import java.sql.SQLException;
import java.util.List;

import org.postgresql.util.PSQLException;

import app.dao.ExerciseDao;
import app.dao.TrainingDao;
import app.dao.TrainingExerciseDao;
import app.model.Exercise;
import app.model.MuscleGroup;
import app.model.Training;
import app.model.TrainingExercise;
import app.model.User;
import app.model.UserRole;

public class AdminMappingService {
    private final TrainingDao trainingDao;
    private final TrainingExerciseDao trainingExerciseDao;
    private final ExerciseDao exerciseDao;

    public AdminMappingService(TrainingDao trainingDao, TrainingExerciseDao trainingExerciseDao, ExerciseDao exerciseDao) {
        this.trainingDao = trainingDao;
        this.trainingExerciseDao = trainingExerciseDao;
        this.exerciseDao = exerciseDao;
    }

    public MappingPageData loadPage(User actor, String trainingIdRaw, String searchRaw, String muscleGroupIdRaw, String sortDirRaw)
            throws SQLException {
        requireAdmin(actor);
        long trainingId = parsePositiveLong(trainingIdRaw, "Training-ID ist ungültig.");
        String searchTerm = normalizeSearch(searchRaw);
        Long muscleGroupId = parseOptionalPositiveLong(muscleGroupIdRaw, "Muskelgruppe ist ungültig.");
        String sortDir = normalizeSortDirection(sortDirRaw);

        Training training = trainingDao.findTrainingById(trainingId);
        if (training == null) {
            throw new IllegalArgumentException("Training nicht gefunden.");
        }

        List<TrainingExercise> mappings = trainingExerciseDao.findByTrainingId(trainingId);
        List<Exercise> activeExercises = exerciseDao.findActiveExercisesForMapping(muscleGroupId, searchTerm, sortDir);
        List<MuscleGroup> muscleGroups = exerciseDao.findAllMuscleGroups();
        return new MappingPageData(training, mappings, activeExercises, muscleGroups, searchTerm, muscleGroupId, sortDir);
    }

    public void addMapping(User actor, String trainingIdRaw, String exerciseIdRaw, String setsRaw, String repsRaw, String sortRaw)
            throws SQLException {
        requireAdmin(actor);
        long trainingId = parsePositiveLong(trainingIdRaw, "Training-ID ist ungültig.");
        long exerciseId = parsePositiveLong(exerciseIdRaw, "Übung ist ungültig.");
        int plannedSets = parsePositiveInt(setsRaw, "Plan-Sets müssen > 0 sein.");
        int plannedReps = parsePositiveInt(repsRaw, "Plan-Reps müssen > 0 sein.");
        int sortOrder = parsePositiveInt(sortRaw, "Sortierung muss > 0 sein.");

        assertTrainingExists(trainingId);
        Exercise exercise = exerciseDao.findExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("Übung nicht gefunden.");
        }
        if (!"ACTIVE".equals(exercise.getStatus())) {
            throw new IllegalArgumentException("Nur aktive Übungen dürfen zugeordnet werden.");
        }

        try {
            TrainingExercise created = trainingExerciseDao.insertMapping(trainingId, exerciseId, plannedSets, plannedReps, sortOrder);
            if (created == null) {
                throw new IllegalArgumentException("Zuordnung konnte nicht angelegt werden.");
            }
        } catch (PSQLException ex) {
            if ("23505".equals(ex.getSQLState())) {
                throw new IllegalArgumentException("Konflikt: Sortierung oder Übung ist bereits in diesem Training vorhanden.");
            }
            throw ex;
        }
    }

    public void updateMapping(User actor, String trainingIdRaw, String mappingIdRaw, String setsRaw, String repsRaw, String sortRaw)
            throws SQLException {
        requireAdmin(actor);
        long trainingId = parsePositiveLong(trainingIdRaw, "Training-ID ist ungültig.");
        long mappingId = parsePositiveLong(mappingIdRaw, "Zuordnungs-ID ist ungültig.");
        int plannedSets = parsePositiveInt(setsRaw, "Plan-Sets müssen > 0 sein.");
        int plannedReps = parsePositiveInt(repsRaw, "Plan-Reps müssen > 0 sein.");
        int sortOrder = parsePositiveInt(sortRaw, "Sortierung muss > 0 sein.");

        assertTrainingExists(trainingId);

        try {
            boolean updated = trainingExerciseDao.updateMapping(mappingId, trainingId, plannedSets, plannedReps, sortOrder);
            if (!updated) {
                throw new IllegalArgumentException("Zuordnung nicht gefunden.");
            }
        } catch (PSQLException ex) {
            if ("23505".equals(ex.getSQLState())) {
                throw new IllegalArgumentException("Konflikt: Diese Sortierung ist bereits vergeben.");
            }
            throw ex;
        }
    }

    private void assertTrainingExists(long trainingId) throws SQLException {
        Training training = trainingDao.findTrainingById(trainingId);
        if (training == null) {
            throw new IllegalArgumentException("Training nicht gefunden.");
        }
    }

    private void requireAdmin(User actor) {
        if (actor == null || actor.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Nur ADMIN darf diese Aktion ausführen.");
        }
    }

    private long parsePositiveLong(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException(errorMessage);
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private int parsePositiveInt(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException(errorMessage);
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private Long parseOptionalPositiveLong(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parsePositiveLong(value, errorMessage);
    }

    private String normalizeSearch(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private String normalizeSortDirection(String value) {
        if ("desc".equalsIgnoreCase(value)) {
            return "desc";
        }
        return "asc";
    }

    public static final class MappingPageData {
        private final Training training;
        private final List<TrainingExercise> mappings;
        private final List<Exercise> activeExercises;
        private final List<MuscleGroup> muscleGroups;
        private final String searchTerm;
        private final Long selectedMuscleGroupId;
        private final String sortDir;

        public MappingPageData(Training training, List<TrainingExercise> mappings, List<Exercise> activeExercises,
                List<MuscleGroup> muscleGroups, String searchTerm, Long selectedMuscleGroupId, String sortDir) {
            this.training = training;
            this.mappings = mappings;
            this.activeExercises = activeExercises;
            this.muscleGroups = muscleGroups;
            this.searchTerm = searchTerm;
            this.selectedMuscleGroupId = selectedMuscleGroupId;
            this.sortDir = sortDir;
        }

        public Training getTraining() {
            return training;
        }

        public List<TrainingExercise> getMappings() {
            return mappings;
        }

        public List<Exercise> getActiveExercises() {
            return activeExercises;
        }

        public List<MuscleGroup> getMuscleGroups() {
            return muscleGroups;
        }

        public String getSearchTerm() {
            return searchTerm;
        }

        public Long getSelectedMuscleGroupId() {
            return selectedMuscleGroupId;
        }

        public String getSortDir() {
            return sortDir;
        }
    }
}
