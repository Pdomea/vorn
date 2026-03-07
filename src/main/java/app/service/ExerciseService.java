package app.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.postgresql.util.PSQLException;

import app.dao.ExerciseDao;
import app.model.Exercise;
import app.model.MuscleGroup;
import app.model.User;
import app.model.UserRole;

public class ExerciseService {
    private final ExerciseDao exerciseDao;

    public ExerciseService(ExerciseDao exerciseDao) {
        this.exerciseDao = exerciseDao;
    }

    public List<Exercise> getAllExercisesForAdmin(User actor, String sortByRaw, String sortDirectionRaw) throws SQLException {
        return getAllExercisesForAdmin(actor, sortByRaw, sortDirectionRaw, null);
    }

    public List<Exercise> getAllExercisesForAdmin(User actor, String sortByRaw, String sortDirectionRaw, String statusFilterRaw)
            throws SQLException {
        requireAdmin(actor);
        String sortBy = normalizeSortBy(sortByRaw);
        String sortDirection = normalizeSortDirection(sortDirectionRaw);
        String statusFilter = normalizeStatusFilter(statusFilterRaw);
        return exerciseDao.findAllExercises(sortBy, sortDirection, "ALL".equals(statusFilter) ? null : statusFilter);
    }

    public Exercise getExerciseForAdmin(User actor, String exerciseIdRaw) throws SQLException {
        requireAdmin(actor);
        long exerciseId = parseExerciseId(exerciseIdRaw);
        return exerciseDao.findExerciseById(exerciseId);
    }

    public Exercise saveExerciseAsAdmin(User actor, String exerciseIdRaw, String name, String description, String[] muscleGroupIdsRaw)
            throws SQLException {
        requireAdmin(actor);
        String cleanName = normalizeName(name);
        String cleanDescription = normalizeDescription(description);
        List<Long> muscleGroupIds = parseMuscleGroupIds(muscleGroupIdsRaw);
        ensureValidMuscleGroups(muscleGroupIds);

        try {
            if (exerciseIdRaw == null || exerciseIdRaw.isBlank()) {
                return exerciseDao.insertExercise(cleanName, cleanDescription, muscleGroupIds);
            }

            long exerciseId = parseExerciseId(exerciseIdRaw);
            Exercise updated = exerciseDao.updateExercise(exerciseId, cleanName, cleanDescription, muscleGroupIds);
            if (updated == null) {
                throw new IllegalArgumentException("Übung nicht gefunden.");
            }
            return updated;
        } catch (PSQLException ex) {
            if ("23505".equals(ex.getSQLState())) {
                throw new IllegalArgumentException("Eine Übung mit diesem Namen existiert bereits.");
            }
            throw ex;
        }
    }

    public void archiveExerciseAsAdmin(User actor, String exerciseIdRaw) throws SQLException {
        requireAdmin(actor);
        long exerciseId = parseExerciseId(exerciseIdRaw);
        boolean updated = exerciseDao.updateStatus(exerciseId, "ARCHIVED");
        if (!updated) {
            throw new IllegalArgumentException("Übung nicht gefunden.");
        }
    }

    public void deleteExerciseAsAdmin(User actor, String exerciseIdRaw) throws SQLException {
        requireAdmin(actor);
        long exerciseId = parseExerciseId(exerciseIdRaw);

        Exercise exercise = exerciseDao.findExerciseById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("Übung nicht gefunden.");
        }

        int usageReferences = exerciseDao.countUsageReferences(exerciseId);
        if (usageReferences > 0) {
            throw new IllegalArgumentException("Übung wird bereits verwendet. Bitte auf ARCHIVED setzen.");
        }

        boolean deleted = exerciseDao.deleteExerciseById(exerciseId);
        if (!deleted) {
            throw new IllegalArgumentException("Übung konnte nicht gelöscht werden.");
        }
    }

    public List<MuscleGroup> getAllMuscleGroupsForAdmin(User actor) throws SQLException {
        requireAdmin(actor);
        return exerciseDao.findAllMuscleGroups();
    }

    private void requireAdmin(User actor) {
        if (actor == null || actor.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Nur ADMIN darf diese Aktion ausführen.");
        }
    }

    private long parseExerciseId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Übungs-ID fehlt.");
        }
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException("Übungs-ID ist ungültig.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Übungs-ID ist ungültig.");
        }
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name ist erforderlich.");
        }
        String trimmed = name.trim();
        if (trimmed.length() < 2) {
            throw new IllegalArgumentException("Name muss mindestens 2 Zeichen haben.");
        }
        return trimmed;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return "";
        }
        return description.trim();
    }

    public String normalizeSortBy(String sortByRaw) {
        if ("muscle".equalsIgnoreCase(sortByRaw)) {
            return "muscle";
        }
        return "name";
    }

    public String normalizeSortDirection(String sortDirectionRaw) {
        if ("desc".equalsIgnoreCase(sortDirectionRaw)) {
            return "desc";
        }
        return "asc";
    }

    public String normalizeStatusFilter(String statusFilterRaw) {
        if ("ACTIVE".equalsIgnoreCase(statusFilterRaw)) {
            return "ACTIVE";
        }
        if ("ARCHIVED".equalsIgnoreCase(statusFilterRaw)) {
            return "ARCHIVED";
        }
        return "ALL";
    }

    private List<Long> parseMuscleGroupIds(String[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Mindestens eine Muskelgruppe auswählen.");
        }

        Set<Long> uniqueIds = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            try {
                long parsed = Long.parseLong(value.trim());
                if (parsed <= 0) {
                    throw new IllegalArgumentException("Muskelgruppe ist ungültig.");
                }
                uniqueIds.add(parsed);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Muskelgruppe ist ungültig.");
            }
        }

        if (uniqueIds.isEmpty()) {
            throw new IllegalArgumentException("Mindestens eine Muskelgruppe auswählen.");
        }
        return new ArrayList<>(uniqueIds);
    }

    private void ensureValidMuscleGroups(List<Long> muscleGroupIds) throws SQLException {
        int existingCount = exerciseDao.countMuscleGroupsByIds(muscleGroupIds);
        if (existingCount != muscleGroupIds.size()) {
            throw new IllegalArgumentException("Mindestens eine ausgewählte Muskelgruppe existiert nicht.");
        }
    }
}
