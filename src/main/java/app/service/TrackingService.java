package app.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.postgresql.util.PSQLException;

import app.dao.ConnectionFactory;
import app.dao.ExerciseDao;
import app.dao.SessionExerciseDao;
import app.dao.TrainingDao;
import app.dao.WorkoutLogDao;
import app.dao.WorkoutSessionDao;
import app.model.SessionExercise;
import app.model.Training;
import app.model.User;
import app.model.Exercise;
import app.model.WorkoutLog;
import app.model.WorkoutSession;

public class TrackingService {
    private final TrainingDao trainingDao;
    private final WorkoutSessionDao workoutSessionDao;
    private final SessionExerciseDao sessionExerciseDao;
    private final WorkoutLogDao workoutLogDao;
    private final ExerciseDao exerciseDao;

    public TrackingService(TrainingDao trainingDao, WorkoutSessionDao workoutSessionDao, SessionExerciseDao sessionExerciseDao) {
        this(trainingDao, workoutSessionDao, sessionExerciseDao, new WorkoutLogDao(), new ExerciseDao());
    }

    public TrackingService(TrainingDao trainingDao, WorkoutSessionDao workoutSessionDao, SessionExerciseDao sessionExerciseDao,
            WorkoutLogDao workoutLogDao) {
        this(trainingDao, workoutSessionDao, sessionExerciseDao, workoutLogDao, new ExerciseDao());
    }

    public TrackingService(TrainingDao trainingDao, WorkoutSessionDao workoutSessionDao, SessionExerciseDao sessionExerciseDao,
            WorkoutLogDao workoutLogDao, ExerciseDao exerciseDao) {
        this.trainingDao = trainingDao;
        this.workoutSessionDao = workoutSessionDao;
        this.sessionExerciseDao = sessionExerciseDao;
        this.workoutLogDao = workoutLogDao;
        this.exerciseDao = exerciseDao;
    }

    public StartSessionResult startOrResumeSession(User actor, String trainingIdRaw) throws SQLException {
        requireAuthenticated(actor);
        long trainingId = parsePositiveLong(trainingIdRaw, "Training-ID ist ungültig.");
        return startOrResumeSessionInternal(actor, trainingId, null, null);
    }

    public StartSessionResult startOrResumeSessionInProgram(User actor, long planId, long planWeekId, long trainingId) throws SQLException {
        requireAuthenticated(actor);
        if (planId <= 0 || planWeekId <= 0 || trainingId <= 0) {
            throw new IllegalArgumentException("Plan/Woche/Training ist ungültig.");
        }
        return startOrResumeSessionInternal(actor, trainingId, planId, planWeekId);
    }

    private StartSessionResult startOrResumeSessionInternal(User actor, long trainingId, Long planId, Long planWeekId) throws SQLException {
        Training training = trainingDao.findPublishedTrainingById(trainingId);
        if (training == null) {
            throw new IllegalArgumentException("Nur veröffentlichte Trainings können gestartet werden.");
        }

        WorkoutSession activeSession = workoutSessionDao.findActiveByUserId(actor.getId());
        if (activeSession != null) {
            return StartSessionResult.resumed(activeSession.getId(), activeSession.getTrainingTitle());
        }

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (workoutSessionDao.existsActiveByUserId(connection, actor.getId())) {
                    WorkoutSession latestActive = workoutSessionDao.findActiveByUserId(actor.getId());
                    if (latestActive != null) {
                        connection.rollback();
                        return StartSessionResult.resumed(latestActive.getId(), latestActive.getTrainingTitle());
                    }
                    throw new IllegalArgumentException("Du hast bereits eine aktive Session.");
                }

                WorkoutSession session = workoutSessionDao.insertActiveSession(connection, actor.getId(), trainingId, planId, planWeekId);
                if (session == null) {
                    throw new IllegalArgumentException("Session konnte nicht gestartet werden.");
                }

                int copiedRows = sessionExerciseDao.insertSnapshotFromTraining(connection, session.getId(), trainingId);
                if (copiedRows <= 0) {
                    throw new IllegalArgumentException("Training hat keine zugeordneten Übungen.");
                }

                connection.commit();
                return StartSessionResult.started(session.getId(), training.getTitle());
            } catch (IllegalArgumentException ex) {
                connection.rollback();
                throw ex;
            } catch (PSQLException ex) {
                connection.rollback();
                if ("23505".equals(ex.getSQLState())) {
                    WorkoutSession latestActive = workoutSessionDao.findActiveByUserId(actor.getId());
                    if (latestActive != null) {
                        return StartSessionResult.resumed(latestActive.getId(), latestActive.getTrainingTitle());
                    }
                    throw new IllegalArgumentException("Du hast bereits eine aktive Session.");
                }
                throw ex;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public TrackPageData loadTrackPage(User actor, String sessionIdRaw) throws SQLException {
        requireAuthenticated(actor);
        long sessionId = parsePositiveLong(sessionIdRaw, "Session-ID ist ungültig.");

        WorkoutSession session = workoutSessionDao.findByIdForUser(sessionId, actor.getId());
        if (session == null) {
            throw new IllegalArgumentException("Session nicht gefunden.");
        }

        List<SessionExercise> snapshotItems = sessionExerciseDao.findBySessionId(sessionId);
        List<WorkoutLog> logs = workoutLogDao.findBySessionId(sessionId);
        Map<Long, List<WorkoutLog>> logsByExercise = groupLogsBySessionExercise(snapshotItems, logs);
        Map<Long, BigDecimal> lastScoreByExerciseId = workoutLogDao.findLatestFinishedExerciseScoresByUserAndTraining(
                actor.getId(),
                session.getTrainingId(),
                session.getId());
        Map<Long, List<Exercise>> swapCandidatesBySessionExerciseId = Map.of();
        if ("ACTIVE".equals(session.getStatus())) {
            swapCandidatesBySessionExerciseId = buildSwapCandidates(snapshotItems);
        }
        return new TrackPageData(session, snapshotItems, logsByExercise, lastScoreByExerciseId, swapCandidatesBySessionExerciseId);
    }

    public void saveSetLog(User actor, String sessionIdRaw, String sessionExerciseIdRaw, String setNoRaw, String repsRaw, String weightRaw,
            String noteRaw) throws SQLException {
        requireAuthenticated(actor);
        long sessionId = parsePositiveLong(sessionIdRaw, "Session-ID ist ungültig.");
        long sessionExerciseId = parsePositiveLong(sessionExerciseIdRaw, "Session-Übung ist ungültig.");
        int setNo = parsePositiveInt(setNoRaw, "Set-Nummer muss > 0 sein.");
        int reps = parsePositiveInt(repsRaw, "Reps müssen > 0 sein.");
        BigDecimal weight = parseWeight(weightRaw);
        String note = normalizeNote(noteRaw);

        WorkoutSession session = workoutSessionDao.findByIdForUser(sessionId, actor.getId());
        if (session == null) {
            throw new IllegalArgumentException("Session nicht gefunden.");
        }
        if (!"ACTIVE".equals(session.getStatus())) {
            throw new IllegalArgumentException("Session ist bereits beendet.");
        }

        boolean itemExists = sessionExerciseDao.existsInSession(sessionExerciseId, sessionId);
        if (!itemExists) {
            throw new IllegalArgumentException("Session-Übung nicht gefunden.");
        }

        WorkoutLog saved = workoutLogDao.upsertLog(sessionId, sessionExerciseId, setNo, reps, weight, note);
        if (saved == null) {
            throw new IllegalArgumentException("Set konnte nicht gespeichert werden.");
        }
    }

    public void finishSession(User actor, String sessionIdRaw) throws SQLException {
        requireAuthenticated(actor);
        long sessionId = parsePositiveLong(sessionIdRaw, "Session-ID ist ungültig.");

        WorkoutSession session = workoutSessionDao.findByIdForUser(sessionId, actor.getId());
        if (session == null) {
            throw new IllegalArgumentException("Session nicht gefunden.");
        }
        if (!"ACTIVE".equals(session.getStatus())) {
            throw new IllegalArgumentException("Session ist bereits beendet.");
        }

        boolean finished = workoutSessionDao.finishSessionForUser(sessionId, actor.getId());
        if (!finished) {
            throw new IllegalArgumentException("Session konnte nicht beendet werden.");
        }
    }

    public void finishAndSaveSession(User actor, String sessionIdRaw, List<PendingLogInput> pendingInputs) throws SQLException {
        requireAuthenticated(actor);
        long sessionId = parsePositiveLong(sessionIdRaw, "Session-ID ist ungültig.");

        WorkoutSession session = workoutSessionDao.findByIdForUser(sessionId, actor.getId());
        if (session == null) {
            throw new IllegalArgumentException("Session nicht gefunden.");
        }
        if (!"ACTIVE".equals(session.getStatus())) {
            throw new IllegalArgumentException("Session ist bereits beendet.");
        }

        List<SessionExercise> snapshotItems = sessionExerciseDao.findBySessionId(sessionId);
        if (snapshotItems.isEmpty()) {
            throw new IllegalArgumentException("Session hat keine Übungen.");
        }

        Map<Long, Integer> plannedSetsBySessionExerciseId = new LinkedHashMap<>();
        for (SessionExercise item : snapshotItems) {
            plannedSetsBySessionExerciseId.put(item.getId(), item.getPlannedSetsSnapshot());
        }

        List<ValidatedLogInput> validatedInputs = validatePendingInputs(pendingInputs, plannedSetsBySessionExerciseId);

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                workoutLogDao.deleteBySessionId(connection, sessionId);
                for (ValidatedLogInput input : validatedInputs) {
                    WorkoutLog inserted = workoutLogDao.insertLog(
                            connection,
                            sessionId,
                            input.sessionExerciseId(),
                            input.setNo(),
                            input.reps(),
                            input.weight(),
                            input.note());
                    if (inserted == null) {
                        throw new IllegalArgumentException("Set konnte nicht gespeichert werden.");
                    }
                }

                boolean finished = workoutSessionDao.finishSessionForUser(connection, sessionId, actor.getId());
                if (!finished) {
                    throw new IllegalArgumentException("Session konnte nicht beendet werden.");
                }
                connection.commit();
            } catch (IllegalArgumentException ex) {
                connection.rollback();
                throw ex;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void discardSession(User actor, String sessionIdRaw) throws SQLException {
        requireAuthenticated(actor);
        long sessionId = parsePositiveLong(sessionIdRaw, "Session-ID ist ungültig.");

        WorkoutSession session = workoutSessionDao.findByIdForUser(sessionId, actor.getId());
        if (session == null) {
            throw new IllegalArgumentException("Session nicht gefunden.");
        }
        if (!"ACTIVE".equals(session.getStatus())) {
            throw new IllegalArgumentException("Session ist bereits beendet.");
        }

        boolean deleted = workoutSessionDao.deleteActiveSessionForUser(sessionId, actor.getId());
        if (!deleted) {
            throw new IllegalArgumentException("Session konnte nicht verworfen werden.");
        }
    }

    public SwapResult swapSessionExercise(User actor, String sessionIdRaw, String sessionExerciseIdRaw, String replacementExerciseIdRaw)
            throws SQLException {
        requireAuthenticated(actor);
        long sessionId = parsePositiveLong(sessionIdRaw, "Session-ID ist ungültig.");
        long sessionExerciseId = parsePositiveLong(sessionExerciseIdRaw, "Session-Übung ist ungültig.");
        long replacementExerciseId = parsePositiveLong(replacementExerciseIdRaw, "Ersatz-Übung ist ungültig.");

        WorkoutSession session = workoutSessionDao.findByIdForUser(sessionId, actor.getId());
        if (session == null) {
            throw new IllegalArgumentException("Session nicht gefunden.");
        }
        if (!"ACTIVE".equals(session.getStatus())) {
            throw new IllegalArgumentException("Übungstausch ist nur in aktiver Session möglich.");
        }

        SessionExercise sourceItem = sessionExerciseDao.findByIdInSession(sessionExerciseId, sessionId);
        if (sourceItem == null) {
            throw new IllegalArgumentException("Session-Übung nicht gefunden.");
        }
        String sourceExerciseName = sourceItem.getExerciseNameSnapshot();
        if (sourceItem.getExerciseId() == replacementExerciseId) {
            throw new IllegalArgumentException("Bitte eine andere Übung auswählen.");
        }
        if (workoutLogDao.existsForSessionExercise(sessionId, sessionExerciseId)) {
            throw new IllegalArgumentException("Übung kann nicht getauscht werden, weil bereits Sets gespeichert wurden.");
        }
        Exercise replacementExercise = exerciseDao.findExerciseById(replacementExerciseId);
        if (replacementExercise == null || !"ACTIVE".equals(replacementExercise.getStatus())) {
            throw new IllegalArgumentException("Ersatz-Übung ist nicht verfügbar.");
        }
        if (!exerciseDao.hasSharedMuscleGroup(sourceItem.getExerciseId(), replacementExerciseId)) {
            throw new IllegalArgumentException("Tausch nur innerhalb derselben Muskelgruppe möglich.");
        }

        boolean swapped = sessionExerciseDao.swapExerciseSnapshot(sessionId, sessionExerciseId, replacementExerciseId);
        if (!swapped) {
            throw new IllegalArgumentException("Übung konnte nicht getauscht werden.");
        }
        return new SwapResult(sourceExerciseName, replacementExercise.getName(), sessionExerciseId);
    }

    public HistoryPageData loadHistory(User actor) throws SQLException {
        requireAuthenticated(actor);
        List<WorkoutSession> finishedSessions = workoutSessionDao.findFinishedByUserId(actor.getId());
        List<HistoryEntry> entries = new ArrayList<>();

        for (WorkoutSession session : finishedSessions) {
            List<SessionExercise> snapshotItems = sessionExerciseDao.findBySessionId(session.getId());
            List<WorkoutLog> logs = workoutLogDao.findBySessionId(session.getId());
            Map<Long, List<WorkoutLog>> logsByExercise = groupLogsBySessionExercise(snapshotItems, logs);
            entries.add(new HistoryEntry(session, snapshotItems, logsByExercise));
        }
        return new HistoryPageData(entries);
    }

    public SessionResultData loadFinishedSessionResult(User actor, String sessionIdRaw) throws SQLException {
        requireAuthenticated(actor);
        long sessionId = parsePositiveLong(sessionIdRaw, "Session-ID ist ungültig.");

        WorkoutSession session = workoutSessionDao.findByIdForUser(sessionId, actor.getId());
        if (session == null) {
            throw new IllegalArgumentException("Session nicht gefunden.");
        }
        if (!"FINISHED".equals(session.getStatus())) {
            throw new IllegalArgumentException("Nur abgeschlossene Sessions können angesehen werden.");
        }

        List<SessionExercise> snapshotItems = sessionExerciseDao.findBySessionId(sessionId);
        List<WorkoutLog> logs = workoutLogDao.findBySessionId(sessionId);
        Map<Long, List<WorkoutLog>> logsByExercise = groupLogsBySessionExercise(snapshotItems, logs);

        int loggedSets = 0;
        int totalReps = 0;
        BigDecimal totalVolume = BigDecimal.ZERO;
        for (WorkoutLog log : logs) {
            loggedSets++;
            totalReps += log.getReps();
            if (log.getWeight() != null) {
                totalVolume = totalVolume.add(log.getWeight().multiply(BigDecimal.valueOf(log.getReps())));
            }
        }

        long durationSeconds = 0;
        if (session.getStartedAt() != null && session.getEndedAt() != null) {
            durationSeconds = Math.max(0L, Duration.between(session.getStartedAt(), session.getEndedAt()).getSeconds());
        }

        return new SessionResultData(session, snapshotItems, logsByExercise, loggedSets, totalReps, totalVolume, durationSeconds);
    }

    public WorkoutSession findActiveSession(User actor) throws SQLException {
        requireAuthenticated(actor);
        return workoutSessionDao.findActiveByUserId(actor.getId());
    }

    private void requireAuthenticated(User actor) {
        if (actor == null) {
            throw new SecurityException("Bitte zuerst einloggen.");
        }
    }

    private long parsePositiveLong(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException(message);
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(message);
        }
    }

    private int parsePositiveInt(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException(message);
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(message);
        }
    }

    private BigDecimal parseWeight(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            BigDecimal parsed = new BigDecimal(value.trim().replace(',', '.'));
            if (parsed.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Gewicht darf nicht negativ sein.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Gewicht ist ungültig.");
        }
    }

    private String normalizeNote(String note) {
        if (note == null) {
            return "";
        }
        String normalized = note.trim();
        if (normalized.length() > 500) {
            throw new IllegalArgumentException("Notiz darf maximal 500 Zeichen haben.");
        }
        return normalized;
    }

    private List<ValidatedLogInput> validatePendingInputs(List<PendingLogInput> pendingInputs, Map<Long, Integer> plannedSetsById) {
        if (pendingInputs == null || pendingInputs.isEmpty()) {
            return List.of();
        }

        List<ValidatedLogInput> validated = new ArrayList<>();
        Set<String> uniqueKeys = new HashSet<>();

        for (PendingLogInput input : pendingInputs) {
            if (input == null) {
                continue;
            }

            long sessionExerciseId = input.sessionExerciseId();
            int setNo = input.setNo();
            if (setNo <= 0) {
                throw new IllegalArgumentException("Set-Nummer muss > 0 sein.");
            }
            Integer plannedSets = plannedSetsById.get(sessionExerciseId);
            if (plannedSets == null) {
                throw new IllegalArgumentException("Session-Übung nicht gefunden.");
            }
            if (setNo > plannedSets) {
                throw new IllegalArgumentException("Set-Nummer ist für diese Übung ungültig.");
            }

            String uniqueKey = sessionExerciseId + ":" + setNo;
            if (!uniqueKeys.add(uniqueKey)) {
                throw new IllegalArgumentException("Doppelte Set-Eingabe erkannt.");
            }

            int reps = parsePositiveInt(input.repsRaw(), "Reps müssen > 0 sein.");
            BigDecimal weight = parseWeight(input.weightRaw());
            String note = normalizeNote(input.noteRaw());
            validated.add(new ValidatedLogInput(sessionExerciseId, setNo, reps, weight, note));
        }
        return validated;
    }

    private Map<Long, List<WorkoutLog>> groupLogsBySessionExercise(List<SessionExercise> snapshotItems, List<WorkoutLog> logs) {
        Map<Long, List<WorkoutLog>> logsByExercise = new LinkedHashMap<>();
        for (SessionExercise item : snapshotItems) {
            logsByExercise.put(item.getId(), new ArrayList<>());
        }
        for (WorkoutLog log : logs) {
            logsByExercise.computeIfAbsent(log.getSessionExerciseId(), ignored -> new ArrayList<>()).add(log);
        }
        return logsByExercise;
    }

    private Map<Long, List<Exercise>> buildSwapCandidates(List<SessionExercise> snapshotItems) throws SQLException {
        Map<Long, List<Exercise>> candidatesByItem = new LinkedHashMap<>();
        for (SessionExercise item : snapshotItems) {
            List<Exercise> candidates = exerciseDao.findActiveSwapCandidatesByExerciseId(item.getExerciseId());
            candidatesByItem.put(item.getId(), candidates);
        }
        return candidatesByItem;
    }

    public static final class StartSessionResult {
        private final long sessionId;
        private final String trainingTitle;
        private final boolean resumed;

        private StartSessionResult(long sessionId, String trainingTitle, boolean resumed) {
            this.sessionId = sessionId;
            this.trainingTitle = trainingTitle;
            this.resumed = resumed;
        }

        public static StartSessionResult started(long sessionId, String trainingTitle) {
            return new StartSessionResult(sessionId, trainingTitle, false);
        }

        public static StartSessionResult resumed(long sessionId, String trainingTitle) {
            return new StartSessionResult(sessionId, trainingTitle, true);
        }

        public long getSessionId() {
            return sessionId;
        }

        public String getTrainingTitle() {
            return trainingTitle;
        }

        public boolean isResumed() {
            return resumed;
        }
    }

    public static final class PendingLogInput {
        private final long sessionExerciseId;
        private final int setNo;
        private final String repsRaw;
        private final String weightRaw;
        private final String noteRaw;

        public PendingLogInput(long sessionExerciseId, int setNo, String repsRaw, String weightRaw, String noteRaw) {
            this.sessionExerciseId = sessionExerciseId;
            this.setNo = setNo;
            this.repsRaw = repsRaw;
            this.weightRaw = weightRaw;
            this.noteRaw = noteRaw;
        }

        public long sessionExerciseId() {
            return sessionExerciseId;
        }

        public int setNo() {
            return setNo;
        }

        public String repsRaw() {
            return repsRaw;
        }

        public String weightRaw() {
            return weightRaw;
        }

        public String noteRaw() {
            return noteRaw;
        }
    }

    private record ValidatedLogInput(long sessionExerciseId, int setNo, int reps, BigDecimal weight, String note) {
    }

    public static final class TrackPageData {
        private final WorkoutSession session;
        private final List<SessionExercise> snapshotItems;
        private final Map<Long, List<WorkoutLog>> logsByExercise;
        private final Map<Long, BigDecimal> lastScoreByExerciseId;
        private final Map<Long, List<Exercise>> swapCandidatesBySessionExerciseId;

        public TrackPageData(WorkoutSession session, List<SessionExercise> snapshotItems, Map<Long, List<WorkoutLog>> logsByExercise,
                Map<Long, BigDecimal> lastScoreByExerciseId, Map<Long, List<Exercise>> swapCandidatesBySessionExerciseId) {
            this.session = session;
            this.snapshotItems = snapshotItems;
            this.logsByExercise = logsByExercise;
            this.lastScoreByExerciseId = lastScoreByExerciseId;
            this.swapCandidatesBySessionExerciseId = swapCandidatesBySessionExerciseId;
        }

        public WorkoutSession getSession() {
            return session;
        }

        public List<SessionExercise> getSnapshotItems() {
            return snapshotItems;
        }

        public Map<Long, List<WorkoutLog>> getLogsByExercise() {
            return logsByExercise;
        }

        public Map<Long, BigDecimal> getLastScoreByExerciseId() {
            return lastScoreByExerciseId;
        }

        public Map<Long, List<Exercise>> getSwapCandidatesBySessionExerciseId() {
            return swapCandidatesBySessionExerciseId;
        }
    }

    public static final class HistoryEntry {
        private final WorkoutSession session;
        private final List<SessionExercise> snapshotItems;
        private final Map<Long, List<WorkoutLog>> logsByExercise;

        public HistoryEntry(WorkoutSession session, List<SessionExercise> snapshotItems, Map<Long, List<WorkoutLog>> logsByExercise) {
            this.session = session;
            this.snapshotItems = snapshotItems;
            this.logsByExercise = logsByExercise;
        }

        public WorkoutSession getSession() {
            return session;
        }

        public List<SessionExercise> getSnapshotItems() {
            return snapshotItems;
        }

        public Map<Long, List<WorkoutLog>> getLogsByExercise() {
            return logsByExercise;
        }
    }

    public static final class SwapResult {
        private final String sourceExerciseName;
        private final String replacementExerciseName;
        private final long sessionExerciseId;

        public SwapResult(String sourceExerciseName, String replacementExerciseName, long sessionExerciseId) {
            this.sourceExerciseName = sourceExerciseName;
            this.replacementExerciseName = replacementExerciseName;
            this.sessionExerciseId = sessionExerciseId;
        }

        public String getSourceExerciseName() {
            return sourceExerciseName;
        }

        public String getReplacementExerciseName() {
            return replacementExerciseName;
        }

        public long getSessionExerciseId() {
            return sessionExerciseId;
        }
    }

    public static final class HistoryPageData {
        private final List<HistoryEntry> entries;

        public HistoryPageData(List<HistoryEntry> entries) {
            this.entries = entries;
        }

        public List<HistoryEntry> getEntries() {
            return entries;
        }
    }

    public static final class SessionResultData {
        private final WorkoutSession session;
        private final List<SessionExercise> snapshotItems;
        private final Map<Long, List<WorkoutLog>> logsByExercise;
        private final int loggedSets;
        private final int totalReps;
        private final BigDecimal totalVolume;
        private final long durationSeconds;

        public SessionResultData(WorkoutSession session, List<SessionExercise> snapshotItems, Map<Long, List<WorkoutLog>> logsByExercise,
                int loggedSets, int totalReps, BigDecimal totalVolume, long durationSeconds) {
            this.session = session;
            this.snapshotItems = snapshotItems;
            this.logsByExercise = logsByExercise;
            this.loggedSets = loggedSets;
            this.totalReps = totalReps;
            this.totalVolume = totalVolume;
            this.durationSeconds = durationSeconds;
        }

        public WorkoutSession getSession() {
            return session;
        }

        public List<SessionExercise> getSnapshotItems() {
            return snapshotItems;
        }

        public Map<Long, List<WorkoutLog>> getLogsByExercise() {
            return logsByExercise;
        }

        public int getLoggedSets() {
            return loggedSets;
        }

        public int getTotalReps() {
            return totalReps;
        }

        public BigDecimal getTotalVolume() {
            return totalVolume;
        }

        public long getDurationSeconds() {
            return durationSeconds;
        }
    }
}
