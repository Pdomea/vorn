package app.service;

import java.sql.SQLException;
import java.util.List;

import app.dao.TrainingDao;
import app.dao.TrainingExerciseDao;
import app.model.Training;
import app.model.TrainingExercise;
import app.model.User;
import app.model.UserRole;

public class TrainingService {
    private final TrainingDao trainingDao;
    private final TrainingExerciseDao trainingExerciseDao;

    public TrainingService(TrainingDao trainingDao, TrainingExerciseDao trainingExerciseDao) {
        this.trainingDao = trainingDao;
        this.trainingExerciseDao = trainingExerciseDao;
    }

    public List<Training> getPublishedTrainings() throws SQLException {
        return trainingDao.findPublishedTrainings();
    }

    public TrainingDetailResult getPublishedTrainingDetail(String trainingIdRaw) throws SQLException {
        long trainingId = parseTrainingId(trainingIdRaw);
        Training training = trainingDao.findPublishedTrainingById(trainingId);
        if (training == null) {
            return TrainingDetailResult.notFound();
        }
        List<TrainingExercise> exercises = trainingExerciseDao.findByTrainingId(trainingId);
        return TrainingDetailResult.found(training, exercises);
    }

    public List<Training> getAllTrainingsForAdmin(User actor) throws SQLException {
        return getAllTrainingsForAdmin(actor, "id", "asc", null);
    }

    public List<Training> getAllTrainingsForAdmin(User actor, String statusFilterRaw) throws SQLException {
        return getAllTrainingsForAdmin(actor, "id", "asc", statusFilterRaw);
    }

    public List<Training> getAllTrainingsForAdmin(User actor, String sortByRaw, String sortDirectionRaw,
            String statusFilterRaw)
            throws SQLException {
        requireAdmin(actor);
        String sortBy = normalizeSortBy(sortByRaw);
        String sortDirection = normalizeSortDirection(sortDirectionRaw);
        String statusFilter = normalizeStatusFilter(statusFilterRaw);
        return trainingDao.findAllTrainings(sortBy, sortDirection, "ALL".equals(statusFilter) ? null : statusFilter);
    }

    public Training getTrainingForAdmin(User actor, String trainingIdRaw) throws SQLException {
        requireAdmin(actor);
        long trainingId = parseTrainingId(trainingIdRaw);
        return trainingDao.findTrainingById(trainingId);
    }

    public Training saveTrainingAsAdmin(User actor, String trainingIdRaw, String title, String description)
            throws SQLException {
        requireAdmin(actor);
        String cleanTitle = normalizeTitle(title);
        String cleanDescription = normalizeDescription(description);

        if (trainingIdRaw == null || trainingIdRaw.isBlank()) {
            return trainingDao.insertTraining(cleanTitle, cleanDescription);
        }

        long trainingId = parseTrainingId(trainingIdRaw);
        Training updated = trainingDao.updateTraining(trainingId, cleanTitle, cleanDescription);
        if (updated == null) {
            throw new IllegalArgumentException("Training nicht gefunden.");
        }
        return updated;
    }

    public void publishTrainingAsAdmin(User actor, String trainingIdRaw) throws SQLException {
        requireAdmin(actor);
        long trainingId = parseTrainingId(trainingIdRaw);
        boolean updated = trainingDao.updateStatus(trainingId, "PUBLISHED");
        if (!updated) {
            throw new IllegalArgumentException("Training nicht gefunden.");
        }
    }

    public void hideTrainingAsAdmin(User actor, String trainingIdRaw) throws SQLException {
        requireAdmin(actor);
        long trainingId = parseTrainingId(trainingIdRaw);
        boolean updated = trainingDao.updateStatus(trainingId, "HIDDEN");
        if (!updated) {
            throw new IllegalArgumentException("Training nicht gefunden.");
        }
    }

    public void deleteTrainingAsAdmin(User actor, String trainingIdRaw) throws SQLException {
        requireAdmin(actor);
        long trainingId = parseTrainingId(trainingIdRaw);

        Training training = trainingDao.findTrainingById(trainingId);
        if (training == null) {
            throw new IllegalArgumentException("Training nicht gefunden.");
        }

        int usageReferences = trainingDao.countUsageReferences(trainingId);
        if (usageReferences > 0) {
            throw new IllegalArgumentException("Training wird bereits verwendet. Bitte auf HIDDEN setzen.");
        }

        boolean deleted = trainingDao.deleteTrainingById(trainingId);
        if (!deleted) {
            throw new IllegalArgumentException("Training konnte nicht gelöscht werden.");
        }
    }

    private long parseTrainingId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Training-ID fehlt.");
        }
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException("Training-ID ist ungültig.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Training-ID ist ungültig.");
        }
    }

    private void requireAdmin(User actor) {
        if (actor == null || actor.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Nur ADMIN darf diese Aktion ausführen.");
        }
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Titel ist erforderlich.");
        }
        String trimmed = title.trim();
        if (trimmed.length() < 3) {
            throw new IllegalArgumentException("Titel muss mindestens 3 Zeichen haben.");
        }
        return trimmed;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return "";
        }
        return description.trim();
    }

    public String normalizeStatusFilter(String statusFilterRaw) {
        if ("DRAFT".equalsIgnoreCase(statusFilterRaw)) {
            return "DRAFT";
        }
        if ("PUBLISHED".equalsIgnoreCase(statusFilterRaw)) {
            return "PUBLISHED";
        }
        if ("HIDDEN".equalsIgnoreCase(statusFilterRaw)) {
            return "HIDDEN";
        }
        return "ALL";
    }

    public String normalizeSortBy(String sortByRaw) {
        if ("title".equalsIgnoreCase(sortByRaw)) {
            return "title";
        }
        return "id";
    }

    public String normalizeSortDirection(String sortDirectionRaw) {
        if ("desc".equalsIgnoreCase(sortDirectionRaw)) {
            return "desc";
        }
        return "asc";
    }

    public static final class TrainingDetailResult {
        private final Training training;
        private final List<TrainingExercise> exercises;

        private TrainingDetailResult(Training training, List<TrainingExercise> exercises) {
            this.training = training;
            this.exercises = exercises;
        }

        public static TrainingDetailResult found(Training training, List<TrainingExercise> exercises) {
            return new TrainingDetailResult(training, exercises);
        }

        public static TrainingDetailResult notFound() {
            return new TrainingDetailResult(null, new java.util.ArrayList<>());
        }

        public boolean isFound() {
            return training != null;
        }

        public Training getTraining() {
            return training;
        }

        public List<TrainingExercise> getExercises() {
            return exercises;
        }
    }
}
