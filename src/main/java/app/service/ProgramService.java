package app.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import app.dao.ConnectionFactory;
import app.dao.PlanDao;
import app.dao.PlanWeekDao;
import app.dao.PlanWeekTrainingDao;
import app.dao.SessionExerciseDao;
import app.dao.TrainingDao;
import app.dao.UserDao;
import app.dao.WorkoutLogDao;
import app.dao.WorkoutSessionDao;
import app.dao.WorkoutLogDao.SessionLogSummary;
import app.model.Plan;
import app.model.PlanWeek;
import app.model.PlanWeekTraining;
import app.model.Training;
import app.model.User;
import app.model.UserRole;
import app.model.WorkoutSession;

public class ProgramService {
    private final PlanDao planDao;
    private final PlanWeekDao planWeekDao;
    private final PlanWeekTrainingDao planWeekTrainingDao;
    private final TrainingDao trainingDao;
    private final UserDao userDao;
    private final WorkoutSessionDao workoutSessionDao;
    private final WorkoutLogDao workoutLogDao;
    private final SessionExerciseDao sessionExerciseDao = new SessionExerciseDao();

    public ProgramService(PlanDao planDao, PlanWeekDao planWeekDao, PlanWeekTrainingDao planWeekTrainingDao,
            TrainingDao trainingDao) {
        this(planDao, planWeekDao, planWeekTrainingDao, trainingDao, new UserDao(), new WorkoutSessionDao(),
                new WorkoutLogDao());
    }

    public ProgramService(PlanDao planDao, PlanWeekDao planWeekDao, PlanWeekTrainingDao planWeekTrainingDao,
            TrainingDao trainingDao,
            UserDao userDao) {
        this(planDao, planWeekDao, planWeekTrainingDao, trainingDao, userDao, new WorkoutSessionDao(),
                new WorkoutLogDao());
    }

    public ProgramService(PlanDao planDao, PlanWeekDao planWeekDao, PlanWeekTrainingDao planWeekTrainingDao,
            TrainingDao trainingDao,
            UserDao userDao, WorkoutSessionDao workoutSessionDao, WorkoutLogDao workoutLogDao) {
        this.planDao = planDao;
        this.planWeekDao = planWeekDao;
        this.planWeekTrainingDao = planWeekTrainingDao;
        this.trainingDao = trainingDao;
        this.userDao = userDao;
        this.workoutSessionDao = workoutSessionDao;
        this.workoutLogDao = workoutLogDao;
    }

    public Plan createPlanWithDefaultWeek(User actor, String nameRaw, String descriptionRaw, String heroImagePathRaw)
            throws SQLException {
        requireAdmin(actor);
        String name = normalizeName(nameRaw);
        String description = normalizeDescription(descriptionRaw);
        String heroImagePath = normalizeHeroImagePath(heroImagePathRaw);

        // DB Transaktion manuell steuern, falls später fehler passieren
        // dann können wir einfach rollback machen und es wird kein unsinn in der db
        // gespeichert
        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Plan createdPlan = planDao.insertPlan(connection, name, description, heroImagePath);
                if (createdPlan == null) {
                    throw new IllegalArgumentException("Plan konnte nicht angelegt werden.");
                }

                PlanWeek createdWeek = planWeekDao.insertWeek(connection, createdPlan.getId(), 1);
                if (createdWeek == null) {
                    throw new IllegalArgumentException("Woche 1 konnte nicht angelegt werden.");
                }

                connection.commit();
                return createdPlan;
            } catch (RuntimeException ex) {
                connection.rollback();
                throw ex;
            } catch (SQLException ex) {
                connection.rollback();
                throw new IllegalArgumentException("Plan konnte nicht angelegt werden.");
            }
        }
    }

    public PlanWeek addNextWeek(User actor, String planIdRaw) throws SQLException {
        requireAdmin(actor);
        long planId = parsePositiveLong(planIdRaw, "Plan-ID ist ungültig.");

        Plan plan = planDao.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan nicht gefunden.");
        }

        int maxWeekNo = planWeekDao.findMaxWeekNo(planId);
        int nextWeekNo = maxWeekNo + 1;
        if (nextWeekNo <= 0) {
            throw new IllegalArgumentException("Nächste Woche konnte nicht berechnet werden.");
        }

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                PlanWeek createdWeek = planWeekDao.insertWeek(connection, planId, nextWeekNo);
                if (createdWeek == null) {
                    throw new IllegalArgumentException("Woche konnte nicht angelegt werden.");
                }
                connection.commit();
                return createdWeek;
            } catch (RuntimeException ex) {
                connection.rollback();
                throw ex;
            } catch (SQLException ex) {
                connection.rollback();
                throw new IllegalArgumentException("Woche konnte nicht angelegt werden.");
            }
        }
    }

    public PlanWeekTraining addTrainingToWeek(User actor, String planWeekIdRaw, String trainingIdRaw,
            String sortOrderRaw) throws SQLException {
        requireAdmin(actor);
        long planWeekId = parsePositiveLong(planWeekIdRaw, "Plan-Woche-ID ist ungültig.");
        long trainingId = parsePositiveLong(trainingIdRaw, "Training-ID ist ungültig.");
        int sortOrder = parsePositiveInt(sortOrderRaw, "Sortierung muss > 0 sein.");

        PlanWeek week = planWeekDao.findById(planWeekId);
        if (week == null) {
            throw new IllegalArgumentException("Plan-Woche nicht gefunden.");
        }

        Plan plan = planDao.findById(week.getPlanId());
        if (plan == null) {
            throw new IllegalArgumentException("Plan nicht gefunden.");
        }

        Training training = trainingDao.findTrainingById(trainingId);
        if (training == null) {
            throw new IllegalArgumentException("Training nicht gefunden.");
        }

        try {
            PlanWeekTraining created = planWeekTrainingDao.insertMapping(planWeekId, trainingId, sortOrder);
            if (created == null) {
                throw new IllegalArgumentException("Training konnte der Woche nicht zugeordnet werden.");
            }
            return created;
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Training konnte der Woche nicht zugeordnet werden.");
        }
    }

    public List<Plan> getAllPlans(User actor) throws SQLException {
        return getAllPlans(actor, null);
    }

    public List<Plan> getAllPlans(User actor, String statusFilterRaw) throws SQLException {
        requireAdmin(actor);
        String statusFilter = normalizePlanStatusFilter(statusFilterRaw);
        return planDao.findAllPlans("ALL".equals(statusFilter) ? null : statusFilter);
    }

    public List<Training> getAllTrainingsForAdmin(User actor) throws SQLException {
        requireAdmin(actor);
        return trainingDao.findAllTrainings();
    }

    public PlanDetailData getPlanDetail(User actor, String planIdRaw) throws SQLException {
        requireAdmin(actor);
        long planId = parsePositiveLong(planIdRaw, "Plan-ID ist ungültig.");

        Plan plan = planDao.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan nicht gefunden.");
        }

        List<PlanWeek> weeks = planWeekDao.findByPlanId(planId);
        Map<Long, List<PlanWeekTraining>> weekTrainings = new LinkedHashMap<>();
        for (PlanWeek week : weeks) {
            weekTrainings.put(week.getId(), planWeekTrainingDao.findByWeekId(week.getId()));
        }

        return new PlanDetailData(plan, weeks, weekTrainings);
    }

    public void archivePlan(User actor, String planIdRaw) throws SQLException {
        requireAdmin(actor);
        long planId = parsePositiveLong(planIdRaw, "Plan-ID ist ungültig.");
        Plan plan = planDao.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan nicht gefunden.");
        }
        if ("ARCHIVED".equals(plan.getStatus())) {
            throw new IllegalArgumentException("Plan ist bereits archiviert.");
        }
        boolean updated = planDao.updateStatus(planId, "ARCHIVED");
        if (!updated) {
            throw new IllegalArgumentException("Plan konnte nicht archiviert werden.");
        }
    }

    public boolean activatePlan(User actor, String planIdRaw) throws SQLException {
        requireAdmin(actor);
        long planId = parsePositiveLong(planIdRaw, "Plan-ID ist ungültig.");
        Plan plan = planDao.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan nicht gefunden.");
        }
        if ("ACTIVE".equals(plan.getStatus())) {
            return false;
        }
        boolean updated = planDao.updateStatus(planId, "ACTIVE");
        if (!updated) {
            throw new IllegalArgumentException("Plan konnte nicht aktiviert werden.");
        }
        return true;
    }

    public void deletePlanAsAdmin(User actor, String planIdRaw) throws SQLException {
        requireAdmin(actor);
        long planId = parsePositiveLong(planIdRaw, "Plan-ID ist ungültig.");

        Plan plan = planDao.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan nicht gefunden.");
        }

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                planDao.clearActivePlanForUsers(connection, planId);
                boolean deleted = planDao.deletePlanById(connection, planId);
                if (!deleted) {
                    throw new IllegalArgumentException("Plan konnte nicht gelöscht werden.");
                }
                connection.commit();
            } catch (RuntimeException ex) {
                connection.rollback();
                throw ex;
            } catch (SQLException ex) {
                connection.rollback();
                throw new IllegalArgumentException("Plan konnte nicht gelöscht werden.");
            }
        }
    }

    public void updatePlanHeroImage(User actor, String planIdRaw, String heroImagePathRaw) throws SQLException {
        requireAdmin(actor);
        long planId = parsePositiveLong(planIdRaw, "Plan-ID ist ungültig.");
        Plan plan = planDao.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan nicht gefunden.");
        }

        String heroImagePath = normalizeHeroImagePath(heroImagePathRaw);
        boolean updated = planDao.updateHeroImagePath(planId, heroImagePath);
        if (!updated) {
            throw new IllegalArgumentException("Bildpfad konnte nicht gespeichert werden.");
        }
    }

    public void updatePlanAsAdmin(User actor, String planIdRaw, String nameRaw, String descriptionRaw)
            throws SQLException {
        requireAdmin(actor);
        long planId = parsePositiveLong(planIdRaw, "Plan-ID ist ungültig.");
        Plan plan = planDao.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan nicht gefunden.");
        }

        String name = normalizeName(nameRaw);
        String description = normalizeDescription(descriptionRaw);

        try {
            boolean updated = planDao.updatePlanMeta(planId, name, description);
            if (!updated) {
                throw new IllegalArgumentException("Plan konnte nicht gespeichert werden.");
            }
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Plan konnte nicht gespeichert werden.");
        }
    }

    public void removeTrainingFromWeek(User actor, String planIdRaw, String mappingIdRaw) throws SQLException {
        requireAdmin(actor);
        long planId = parsePositiveLong(planIdRaw, "Plan-ID ist ungültig.");
        long mappingId = parsePositiveLong(mappingIdRaw, "Zuordnung-ID ist ungültig.");

        Plan plan = planDao.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan nicht gefunden.");
        }

        PlanWeekTraining mapping = planWeekTrainingDao.findById(mappingId);
        if (mapping == null) {
            throw new IllegalArgumentException("Zuordnung nicht gefunden.");
        }

        PlanWeek week = planWeekDao.findById(mapping.getPlanWeekId());
        if (week == null || week.getPlanId() != planId) {
            throw new IllegalArgumentException("Zuordnung gehört nicht zu diesem Plan.");
        }

        boolean deleted = planWeekTrainingDao.deleteMapping(mappingId);
        if (!deleted) {
            throw new IllegalArgumentException("Zuordnung konnte nicht gelöscht werden.");
        }
    }

    public void removeWeek(User actor, String planIdRaw, String planWeekIdRaw) throws SQLException {
        requireAdmin(actor);
        long planId = parsePositiveLong(planIdRaw, "Plan-ID ist ungültig.");
        long planWeekId = parsePositiveLong(planWeekIdRaw, "Plan-Woche-ID ist ungültig.");

        Plan plan = planDao.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan nicht gefunden.");
        }

        PlanWeek week = planWeekDao.findById(planWeekId);
        if (week == null) {
            throw new IllegalArgumentException("Plan-Woche nicht gefunden.");
        }
        if (week.getPlanId() != planId) {
            throw new IllegalArgumentException("Plan-Woche gehört nicht zu diesem Plan.");
        }

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                boolean deleted = planWeekDao.deleteWeek(connection, planWeekId);
                if (!deleted) {
                    throw new IllegalArgumentException("Plan-Woche konnte nicht gelöscht werden.");
                }

                planWeekDao.shiftWeeksDown(connection, planId, week.getWeekNo());
                connection.commit();
            } catch (RuntimeException ex) {
                connection.rollback();
                throw ex;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    public PlanWeek duplicateWeek(User actor, String planIdRaw, String sourcePlanWeekIdRaw) throws SQLException {
        requireAdmin(actor);
        long planId = parsePositiveLong(planIdRaw, "Plan-ID ist ungültig.");
        long sourcePlanWeekId = parsePositiveLong(sourcePlanWeekIdRaw, "Plan-Woche-ID ist ungültig.");

        Plan plan = planDao.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan nicht gefunden.");
        }

        PlanWeek sourceWeek = planWeekDao.findById(sourcePlanWeekId);
        if (sourceWeek == null) {
            throw new IllegalArgumentException("Quell-Woche nicht gefunden.");
        }
        if (sourceWeek.getPlanId() != planId) {
            throw new IllegalArgumentException("Quell-Woche gehört nicht zu diesem Plan.");
        }

        List<PlanWeekTraining> sourceMappings = planWeekTrainingDao.findByWeekId(sourcePlanWeekId);

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int maxWeekNo = planWeekDao.findMaxWeekNo(connection, planId);
                int nextWeekNo = maxWeekNo + 1;
                PlanWeek duplicatedWeek = planWeekDao.insertWeek(connection, planId, nextWeekNo);
                if (duplicatedWeek == null) {
                    throw new IllegalArgumentException("Woche konnte nicht dupliziert werden.");
                }

                for (PlanWeekTraining sourceMapping : sourceMappings) {
                    PlanWeekTraining inserted = planWeekTrainingDao.insertMapping(
                            connection,
                            duplicatedWeek.getId(),
                            sourceMapping.getTrainingId(),
                            sourceMapping.getSortOrder());
                    if (inserted == null) {
                        throw new IllegalArgumentException(
                                "Trainings der Woche konnten nicht vollständig dupliziert werden.");
                    }
                }

                connection.commit();
                return duplicatedWeek;
            } catch (RuntimeException ex) {
                connection.rollback();
                throw ex;
            } catch (SQLException ex) {
                connection.rollback();
                throw new IllegalArgumentException("Woche konnte nicht dupliziert werden.");
            }
        }
    }

    public UserProgramPageData loadUserProgramPage(User actor, String selectedPlanIdRaw) throws SQLException {
        requireAuthenticated(actor);
        List<Plan> activePlans = planDao.findActivePlans();
        if (activePlans.isEmpty()) {
            return new UserProgramPageData(activePlans, null, new java.util.ArrayList<>(), new java.util.HashMap<>(),
                    new java.util.HashMap<>(), new java.util.HashMap<>(), new java.util.HashMap<>(),
                    new java.util.HashMap<>(), null);
        }

        Plan selectedPlan = resolveSelectedActivePlan(actor, selectedPlanIdRaw, activePlans);

        List<PlanWeek> weeks = planWeekDao.findByPlanId(selectedPlan.getId());
        Map<Long, List<PlanWeekTraining>> weekTrainings = new LinkedHashMap<>();
        for (PlanWeek week : weeks) {
            weekTrainings.put(week.getId(),
                    planWeekTrainingDao.findByWeekIdAndTrainingStatus(week.getId(), "PUBLISHED"));
        }

        Map<Long, Map<Long, ProgramTrainingHistorySummary>> weekTrainingHistory = loadLatestHistoryByWeekAndTraining(
                actor.getId(), selectedPlan.getId());
        WorkoutSession activeSession = workoutSessionDao.findActiveByUserId(actor.getId());

        Map<Long, Map<Long, String>> weekTrainingStatus = buildWeekTrainingStatus(selectedPlan, weeks, weekTrainings,
                weekTrainingHistory, activeSession);
        Map<Long, WeekProgressData> weekProgressByWeekId = buildWeekProgressByWeekId(weeks, weekTrainings,
                weekTrainingStatus);
        Map<Long, Long> nextTrainingIdByWeekId = buildNextTrainingIdByWeekId(weeks, weekTrainings, weekTrainingStatus);
        NextTrainingData nextTraining = buildNextTraining(weeks, weekTrainings, weekTrainingStatus);

        return new UserProgramPageData(
                activePlans,
                selectedPlan,
                weeks,
                weekTrainings,
                weekTrainingHistory,
                weekTrainingStatus,
                weekProgressByWeekId,
                nextTrainingIdByWeekId,
                nextTraining);
    }

    public void setActivePlanForUser(User actor, String planIdRaw) throws SQLException {
        requireAuthenticated(actor);
        long planId = parsePositiveLong(planIdRaw, "Plan-ID ist ungültig.");
        Plan plan = planDao.findById(planId);
        if (plan == null || !"ACTIVE".equals(plan.getStatus())) {
            throw new IllegalArgumentException("Plan ist nicht aktiv oder existiert nicht.");
        }

        Long currentPlanId = actor.getActivePlanId();
        if (currentPlanId != null && currentPlanId == planId) {
            throw new IllegalArgumentException("Plan ist bereits aktiv.");
        }

        if (workoutSessionDao.existsActiveByUserId(actor.getId())) {
            throw new IllegalArgumentException(
                    "Planwechsel nicht möglich: Bitte aktive Session zuerst beenden oder verwerfen.");
        }

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                workoutLogDao.deleteByUserId(connection, actor.getId());
                sessionExerciseDao.deleteByUserId(connection, actor.getId());
                workoutSessionDao.deleteByUserId(connection, actor.getId());
                userDao.updateActivePlanId(connection, actor.getId(), planId);
                connection.commit();
            } catch (RuntimeException ex) {
                connection.rollback();
                throw ex;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        }
        actor.setActivePlanId(planId);
    }

    public List<Plan> getActivePlansForUser(User actor) throws SQLException {
        requireAuthenticated(actor);
        return planDao.findActivePlans();
    }

    public ProgramTrainingSelection validateUserProgramTrainingSelection(
            User actor,
            String planIdRaw,
            String planWeekIdRaw,
            String trainingIdRaw) throws SQLException {
        requireAuthenticated(actor);
        long planId = parsePositiveLong(planIdRaw, "Plan-ID ist ungültig.");
        long planWeekId = parsePositiveLong(planWeekIdRaw, "Plan-Woche-ID ist ungültig.");
        long trainingId = parsePositiveLong(trainingIdRaw, "Training-ID ist ungültig.");

        Plan plan = planDao.findById(planId);
        if (plan == null || !"ACTIVE".equals(plan.getStatus())) {
            throw new IllegalArgumentException("Plan nicht gefunden.");
        }

        PlanWeek week = planWeekDao.findById(planWeekId);
        if (week == null || week.getPlanId() != planId) {
            throw new IllegalArgumentException("Plan-Woche gehört nicht zu diesem Plan.");
        }

        List<PlanWeekTraining> publishedMappings = planWeekTrainingDao.findByWeekIdAndTrainingStatus(planWeekId,
                "PUBLISHED");
        boolean trainingAssigned = false;
        for (PlanWeekTraining mapping : publishedMappings) {
            if (mapping.getTrainingId() == trainingId) {
                trainingAssigned = true;
                break;
            }
        }
        if (!trainingAssigned) {
            throw new IllegalArgumentException(
                    "Training ist in dieser Woche nicht veröffentlicht oder nicht zugeordnet.");
        }

        return new ProgramTrainingSelection(planId, planWeekId, trainingId);
    }

    public DashboardMetrics loadDashboardMetrics(User actor, Plan selectedPlan) throws SQLException {
        requireAuthenticated(actor);

        BigDecimal liftedVolumeKg = workoutLogDao.sumFinishedVolumeByUserId(actor.getId());
        int workoutsCompleted = workoutSessionDao.countFinishedByUserId(actor.getId());
        long totalSecondsSpent = workoutSessionDao.sumFinishedDurationSecondsByUserId(actor.getId());

        int totalPlanSlots = 0;
        int completedPlanSlots = 0;
        int progressPercent = 0;
        boolean planCompleted = false;

        if (selectedPlan != null) {
            totalPlanSlots = planWeekTrainingDao.countPublishedSlotsByPlanId(selectedPlan.getId());
            completedPlanSlots = workoutSessionDao.countDistinctFinishedPlanSlotsByUserAndPlan(actor.getId(),
                    selectedPlan.getId());
            if (totalPlanSlots > 0) {
                double progress = (completedPlanSlots * 100.0) / totalPlanSlots;
                progressPercent = (int) Math.round(progress);
                if (progressPercent > 100) {
                    progressPercent = 100;
                }
                planCompleted = completedPlanSlots >= totalPlanSlots;
            }
        }

        return new DashboardMetrics(
                liftedVolumeKg,
                workoutsCompleted,
                totalSecondsSpent,
                formatDurationHoursMinutes(totalSecondsSpent),
                completedPlanSlots,
                totalPlanSlots,
                progressPercent,
                planCompleted);
    }

    private void requireAdmin(User actor) {
        if (actor == null || actor.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Nur ADMIN darf diese Aktion ausführen.");
        }
    }

    private void requireAuthenticated(User actor) {
        if (actor == null) {
            throw new SecurityException("Bitte zuerst einloggen.");
        }
    }

    private Plan resolveSelectedActivePlan(User actor, String selectedPlanIdRaw, List<Plan> activePlans)
            throws SQLException {
        if (selectedPlanIdRaw != null && !selectedPlanIdRaw.isBlank()) {
            long selectedPlanId = parsePositiveLong(selectedPlanIdRaw, "Plan-ID ist ungültig.");
            Plan selectedByParam = findPlanInList(activePlans, selectedPlanId);
            if (selectedByParam == null) {
                throw new IllegalArgumentException("Plan nicht gefunden.");
            }
            return selectedByParam;
        }

        Long storedActivePlanId = actor.getActivePlanId();
        if (storedActivePlanId != null) {
            Plan selectedByStored = findPlanInList(activePlans, storedActivePlanId);
            if (selectedByStored != null) {
                return selectedByStored;
            }
        }

        Plan fallback = activePlans.get(0);
        userDao.updateActivePlanId(actor.getId(), fallback.getId());
        actor.setActivePlanId(fallback.getId());
        return fallback;
    }

    private Plan findPlanInList(List<Plan> plans, long planId) {
        for (Plan plan : plans) {
            if (plan.getId() == planId) {
                return plan;
            }
        }
        return null;
    }

    private Map<Long, Map<Long, ProgramTrainingHistorySummary>> loadLatestHistoryByWeekAndTraining(long userId,
            long planId)
            throws SQLException {
        List<WorkoutSession> latestSessions = workoutSessionDao.findLatestFinishedByTrainingForPlan(userId, planId);
        if (latestSessions.isEmpty()) {
            return new java.util.HashMap<>();
        }

        List<Long> sessionIds = new ArrayList<>();
        for (WorkoutSession session : latestSessions) {
            sessionIds.add(session.getId());
        }
        Map<Long, SessionLogSummary> summariesBySessionId = workoutLogDao.findSessionSummariesBySessionIds(sessionIds);

        // Map in einer Map, damit wir die Trainings pro Woche speichern können
        // key1 = wochen id, key2 = training id
        Map<Long, Map<Long, ProgramTrainingHistorySummary>> historyByWeekAndTraining = new LinkedHashMap<>();
        for (WorkoutSession session : latestSessions) {
            if (session.getPlanWeekId() == null) {
                continue;
            }
            SessionLogSummary summary = summariesBySessionId.get(session.getId());
            int loggedSets = summary == null ? 0 : summary.getLoggedSets();
            int totalReps = summary == null ? 0 : summary.getTotalReps();
            BigDecimal totalVolume = summary == null || summary.getTotalVolume() == null
                    ? BigDecimal.ZERO
                    : summary.getTotalVolume();

            // computeIfAbsent guckt ob es den key schon gibt, wenn nicht wird eine neue
            // leere map angelegt
            historyByWeekAndTraining
                    .computeIfAbsent(session.getPlanWeekId(), ignored -> new LinkedHashMap<>())
                    .put(session.getTrainingId(), new ProgramTrainingHistorySummary(
                            session.getId(),
                            session.getEndedAt(),
                            loggedSets,
                            totalReps,
                            totalVolume));
        }
        return historyByWeekAndTraining;
    }

    private Map<Long, Map<Long, String>> buildWeekTrainingStatus(
            Plan selectedPlan,
            List<PlanWeek> weeks,
            Map<Long, List<PlanWeekTraining>> weekTrainings,
            Map<Long, Map<Long, ProgramTrainingHistorySummary>> weekTrainingHistory,
            WorkoutSession activeSession) {
        Map<Long, Map<Long, String>> statusByWeek = new LinkedHashMap<>();
        for (PlanWeek week : weeks) {
            List<PlanWeekTraining> mappings = weekTrainings.getOrDefault(week.getId(), new java.util.ArrayList<>());
            Map<Long, ProgramTrainingHistorySummary> historyByTraining = weekTrainingHistory.getOrDefault(week.getId(),
                    new java.util.HashMap<>());

            Map<Long, String> statusByTraining = new LinkedHashMap<>();
            for (PlanWeekTraining mapping : mappings) {
                String status = "OFFEN";
                if (isActiveSlot(selectedPlan, week, mapping, activeSession)) {
                    status = "IN_BEARBEITUNG";
                } else if (historyByTraining.containsKey(mapping.getTrainingId())) {
                    status = "ABGESCHLOSSEN";
                }
                statusByTraining.put(mapping.getTrainingId(), status);
            }
            statusByWeek.put(week.getId(), statusByTraining);
        }
        return statusByWeek;
    }

    private boolean isActiveSlot(Plan selectedPlan, PlanWeek week, PlanWeekTraining mapping,
            WorkoutSession activeSession) {
        if (selectedPlan == null || activeSession == null) {
            return false;
        }
        if (!"ACTIVE".equals(activeSession.getStatus())) {
            return false;
        }
        if (activeSession.getPlanId() == null || activeSession.getPlanWeekId() == null) {
            return false;
        }
        return activeSession.getPlanId() == selectedPlan.getId()
                && activeSession.getPlanWeekId() == week.getId()
                && activeSession.getTrainingId() == mapping.getTrainingId();
    }

    private Map<Long, WeekProgressData> buildWeekProgressByWeekId(
            List<PlanWeek> weeks,
            Map<Long, List<PlanWeekTraining>> weekTrainings,
            Map<Long, Map<Long, String>> weekTrainingStatus) {
        Map<Long, WeekProgressData> progressByWeekId = new LinkedHashMap<>();
        for (PlanWeek week : weeks) {
            List<PlanWeekTraining> mappings = weekTrainings.getOrDefault(week.getId(), new java.util.ArrayList<>());
            Map<Long, String> statuses = weekTrainingStatus.getOrDefault(week.getId(), new java.util.HashMap<>());
            int totalSlots = mappings.size();
            int completedSlots = 0;
            for (PlanWeekTraining mapping : mappings) {
                String status = statuses.get(mapping.getTrainingId());
                if ("ABGESCHLOSSEN".equals(status)) {
                    completedSlots++;
                }
            }
            int progressPercent = 0;
            if (totalSlots > 0) {
                progressPercent = (int) Math.round((completedSlots * 100.0) / totalSlots);
            }
            progressByWeekId.put(week.getId(), new WeekProgressData(completedSlots, totalSlots, progressPercent));
        }
        return progressByWeekId;
    }

    private Map<Long, Long> buildNextTrainingIdByWeekId(
            List<PlanWeek> weeks,
            Map<Long, List<PlanWeekTraining>> weekTrainings,
            Map<Long, Map<Long, String>> weekTrainingStatus) {
        Map<Long, Long> nextByWeekId = new LinkedHashMap<>();
        for (PlanWeek week : weeks) {
            List<PlanWeekTraining> mappings = weekTrainings.getOrDefault(week.getId(), new java.util.ArrayList<>());
            Map<Long, String> statuses = weekTrainingStatus.getOrDefault(week.getId(), new java.util.HashMap<>());
            Long nextTrainingId = findNextTrainingIdForMappings(mappings, statuses);
            if (nextTrainingId != null) {
                nextByWeekId.put(week.getId(), nextTrainingId);
            }
        }
        return nextByWeekId;
    }

    private NextTrainingData buildNextTraining(
            List<PlanWeek> weeks,
            Map<Long, List<PlanWeekTraining>> weekTrainings,
            Map<Long, Map<Long, String>> weekTrainingStatus) {
        for (PlanWeek week : weeks) {
            List<PlanWeekTraining> mappings = weekTrainings.getOrDefault(week.getId(), new java.util.ArrayList<>());
            Map<Long, String> statuses = weekTrainingStatus.getOrDefault(week.getId(), new java.util.HashMap<>());

            PlanWeekTraining nextInProgress = findFirstMappingByStatus(mappings, statuses, "IN_BEARBEITUNG");
            if (nextInProgress != null) {
                return new NextTrainingData(
                        week.getId(),
                        week.getWeekNo(),
                        nextInProgress.getTrainingId(),
                        nextInProgress.getTrainingTitle(),
                        "IN_BEARBEITUNG");
            }

            PlanWeekTraining nextOpen = findFirstMappingByStatus(mappings, statuses, "OFFEN");
            if (nextOpen != null) {
                return new NextTrainingData(
                        week.getId(),
                        week.getWeekNo(),
                        nextOpen.getTrainingId(),
                        nextOpen.getTrainingTitle(),
                        "OFFEN");
            }
        }
        return null;
    }

    private Long findNextTrainingIdForMappings(List<PlanWeekTraining> mappings, Map<Long, String> statuses) {
        PlanWeekTraining inProgress = findFirstMappingByStatus(mappings, statuses, "IN_BEARBEITUNG");
        if (inProgress != null) {
            return inProgress.getTrainingId();
        }
        PlanWeekTraining open = findFirstMappingByStatus(mappings, statuses, "OFFEN");
        if (open != null) {
            return open.getTrainingId();
        }
        return null;
    }

    private PlanWeekTraining findFirstMappingByStatus(List<PlanWeekTraining> mappings, Map<Long, String> statuses,
            String wantedStatus) {
        for (PlanWeekTraining mapping : mappings) {
            String status = statuses.get(mapping.getTrainingId());
            if (wantedStatus.equals(status)) {
                return mapping;
            }
        }
        return null;
    }

    private String normalizeName(String nameRaw) {
        if (nameRaw == null || nameRaw.isBlank()) {
            throw new IllegalArgumentException("Planname ist erforderlich.");
        }
        String name = nameRaw.trim();
        if (name.length() < 3) {
            throw new IllegalArgumentException("Planname muss mindestens 3 Zeichen haben.");
        }
        return name;
    }

    private String normalizeDescription(String descriptionRaw) {
        if (descriptionRaw == null) {
            return "";
        }
        return descriptionRaw.trim();
    }

    private String normalizeHeroImagePath(String heroImagePathRaw) {
        if (heroImagePathRaw == null || heroImagePathRaw.isBlank()) {
            return null;
        }
        String heroImagePath = heroImagePathRaw.trim();
        if (heroImagePath.length() > 255) {
            throw new IllegalArgumentException("Bildpfad darf maximal 255 Zeichen lang sein.");
        }
        if (!heroImagePath.startsWith("/")) {
            heroImagePath = "/" + heroImagePath;
        }
        return heroImagePath;
    }

    public String normalizePlanStatusFilter(String statusFilterRaw) {
        if ("ACTIVE".equalsIgnoreCase(statusFilterRaw)) {
            return "ACTIVE";
        }
        if ("ARCHIVED".equalsIgnoreCase(statusFilterRaw)) {
            return "ARCHIVED";
        }
        return "ALL";
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

    private String formatDurationHoursMinutes(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0:00";
        }
        long totalMinutes = totalSeconds / 60;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%d:%02d", hours, minutes);
    }

    public static final class PlanDetailData {
        private final Plan plan;
        private final List<PlanWeek> weeks;
        private final Map<Long, List<PlanWeekTraining>> weekTrainings;

        public PlanDetailData(Plan plan, List<PlanWeek> weeks, Map<Long, List<PlanWeekTraining>> weekTrainings) {
            this.plan = plan;
            this.weeks = weeks;
            this.weekTrainings = weekTrainings;
        }

        public Plan getPlan() {
            return plan;
        }

        public List<PlanWeek> getWeeks() {
            return weeks;
        }

        public Map<Long, List<PlanWeekTraining>> getWeekTrainings() {
            return weekTrainings;
        }
    }

    public static final class UserProgramPageData {
        private final List<Plan> activePlans;
        private final Plan selectedPlan;
        private final List<PlanWeek> selectedPlanWeeks;
        private final Map<Long, List<PlanWeekTraining>> wochenTrainings;
        private final Map<Long, Map<Long, ProgramTrainingHistorySummary>> historyMap;
        private final Map<Long, Map<Long, String>> weekStatusMap;
        private final Map<Long, WeekProgressData> selectedPlanWeekProgress;
        private final Map<Long, Long> nextTrainingMap;
        private final NextTrainingData selectedPlanNextTraining;

        public UserProgramPageData(List<Plan> activePlans, Plan selectedPlan, List<PlanWeek> selectedPlanWeeks,
                Map<Long, List<PlanWeekTraining>> wochenTrainings,
                Map<Long, Map<Long, ProgramTrainingHistorySummary>> historyMap,
                Map<Long, Map<Long, String>> weekStatusMap,
                Map<Long, WeekProgressData> selectedPlanWeekProgress,
                Map<Long, Long> nextTrainingMap,
                NextTrainingData selectedPlanNextTraining) {
            this.activePlans = activePlans;
            this.selectedPlan = selectedPlan;
            this.selectedPlanWeeks = selectedPlanWeeks;
            this.wochenTrainings = wochenTrainings;
            this.historyMap = historyMap;
            this.weekStatusMap = weekStatusMap;
            this.selectedPlanWeekProgress = selectedPlanWeekProgress;
            this.nextTrainingMap = nextTrainingMap;
            this.selectedPlanNextTraining = selectedPlanNextTraining;
        }

        public List<Plan> getActivePlans() {
            return activePlans;
        }

        public Plan getSelectedPlan() {
            return selectedPlan;
        }

        public List<PlanWeek> getSelectedPlanWeeks() {
            return selectedPlanWeeks;
        }

        public Map<Long, List<PlanWeekTraining>> getWochenTrainings() {
            return wochenTrainings;
        }

        public Map<Long, Map<Long, ProgramTrainingHistorySummary>> getHistoryMap() {
            return historyMap;
        }

        public Map<Long, Map<Long, String>> getWeekStatusMap() {
            return weekStatusMap;
        }

        public Map<Long, WeekProgressData> getSelectedPlanWeekProgress() {
            return selectedPlanWeekProgress;
        }

        public Map<Long, Long> getNextTrainingMap() {
            return nextTrainingMap;
        }

        public NextTrainingData getSelectedPlanNextTraining() {
            return selectedPlanNextTraining;
        }
    }

    public static final class ProgramTrainingSelection {
        private final long planId;
        private final long planWeekId;
        private final long trainingId;

        public ProgramTrainingSelection(long planId, long planWeekId, long trainingId) {
            this.planId = planId;
            this.planWeekId = planWeekId;
            this.trainingId = trainingId;
        }

        public long getPlanId() {
            return planId;
        }

        public long getPlanWeekId() {
            return planWeekId;
        }

        public long getTrainingId() {
            return trainingId;
        }
    }

    public static final class ProgramTrainingHistorySummary {
        private final long sessionId;
        private final LocalDateTime endedAt;
        private final int loggedSets;
        private final int totalReps;
        private final BigDecimal totalVolume;

        public ProgramTrainingHistorySummary(long sessionId, LocalDateTime endedAt, int loggedSets, int totalReps,
                BigDecimal totalVolume) {
            this.sessionId = sessionId;
            this.endedAt = endedAt;
            this.loggedSets = loggedSets;
            this.totalReps = totalReps;
            this.totalVolume = totalVolume;
        }

        public long getSessionId() {
            return sessionId;
        }

        public LocalDateTime getEndedAt() {
            return endedAt;
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
    }

    public static final class DashboardMetrics {
        private final BigDecimal liftedVolumeKg;
        private final int workoutsCompleted;
        private final long totalSecondsSpent;
        private final String hoursSpentText;
        private final int completedPlanSlots;
        private final int totalPlanSlots;
        private final int progressPercent;
        private final boolean planCompleted;

        public DashboardMetrics(BigDecimal liftedVolumeKg, int workoutsCompleted, long totalSecondsSpent,
                String hoursSpentText,
                int completedPlanSlots, int totalPlanSlots, int progressPercent, boolean planCompleted) {
            this.liftedVolumeKg = liftedVolumeKg == null ? BigDecimal.ZERO : liftedVolumeKg;
            this.workoutsCompleted = workoutsCompleted;
            this.totalSecondsSpent = totalSecondsSpent;
            this.hoursSpentText = hoursSpentText;
            this.completedPlanSlots = completedPlanSlots;
            this.totalPlanSlots = totalPlanSlots;
            this.progressPercent = progressPercent;
            this.planCompleted = planCompleted;
        }

        public BigDecimal getLiftedVolumeKg() {
            return liftedVolumeKg;
        }

        public int getWorkoutsCompleted() {
            return workoutsCompleted;
        }

        public long getTotalSecondsSpent() {
            return totalSecondsSpent;
        }

        public String getHoursSpentText() {
            return hoursSpentText;
        }

        public int getCompletedPlanSlots() {
            return completedPlanSlots;
        }

        public int getTotalPlanSlots() {
            return totalPlanSlots;
        }

        public int getProgressPercent() {
            return progressPercent;
        }

        public boolean isPlanCompleted() {
            return planCompleted;
        }
    }

    public static final class WeekProgressData {
        private final int completedSlots;
        private final int totalSlots;
        private final int progressPercent;

        public WeekProgressData(int completedSlots, int totalSlots, int progressPercent) {
            this.completedSlots = completedSlots;
            this.totalSlots = totalSlots;
            this.progressPercent = progressPercent;
        }

        public int getCompletedSlots() {
            return completedSlots;
        }

        public int getTotalSlots() {
            return totalSlots;
        }

        public int getProgressPercent() {
            return progressPercent;
        }
    }

    public static final class NextTrainingData {
        private final long planWeekId;
        private final int weekNo;
        private final long trainingId;
        private final String trainingTitle;
        private final String status;

        public NextTrainingData(long planWeekId, int weekNo, long trainingId, String trainingTitle, String status) {
            this.planWeekId = planWeekId;
            this.weekNo = weekNo;
            this.trainingId = trainingId;
            this.trainingTitle = trainingTitle;
            this.status = status;
        }

        public long getPlanWeekId() {
            return planWeekId;
        }

        public int getWeekNo() {
            return weekNo;
        }

        public long getTrainingId() {
            return trainingId;
        }

        public String getTrainingTitle() {
            return trainingTitle;
        }

        public String getStatus() {
            return status;
        }
    }
}
