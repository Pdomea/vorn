package app.beans;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import app.model.Exercise;
import app.model.SessionExercise;
import app.model.WorkoutLog;
import app.model.WorkoutSession;

public class SessionTrackBean {
    private String info;
    private String error;

    private WorkoutSession sessionData;
    private List<SessionExercise> items = new java.util.ArrayList<>();
    private Map<Long, List<WorkoutLog>> logsByExercise = new java.util.HashMap<>();
    private Map<Long, BigDecimal> lastScoreByExerciseId = new java.util.HashMap<>();
    private Map<Long, List<Exercise>> alternativenMap = new java.util.HashMap<>();

    private boolean activeSession;
    private Long startedAtEpochMillis;

    public SessionTrackBean() {
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public WorkoutSession getSessionData() {
        return sessionData;
    }

    public void setSessionData(WorkoutSession sessionData) {
        this.sessionData = sessionData;
        refreshSessionMeta();
    }

    public List<SessionExercise> getItems() {
        return items;
    }

    public void setItems(List<SessionExercise> items) {
        this.items = items == null ? new java.util.ArrayList<>() : items;
    }

    public Map<Long, List<WorkoutLog>> getLogsByExercise() {
        return logsByExercise;
    }

    public void setLogsByExercise(Map<Long, List<WorkoutLog>> logsByExercise) {
        this.logsByExercise = logsByExercise == null ? new java.util.HashMap<>() : logsByExercise;
    }

    public Map<Long, BigDecimal> getLastScoreByExerciseId() {
        return lastScoreByExerciseId;
    }

    public void setLastScoreByExerciseId(Map<Long, BigDecimal> lastScoreByExerciseId) {
        this.lastScoreByExerciseId = lastScoreByExerciseId == null ? new java.util.HashMap<>() : lastScoreByExerciseId;
    }

    public Map<Long, List<Exercise>> getAlternativenMap() {
        return alternativenMap;
    }

    public void setAlternativenMap(Map<Long, List<Exercise>> alternativenMap) {
        this.alternativenMap = alternativenMap == null ? new java.util.HashMap<>() : alternativenMap;
    }

    public boolean isActiveSession() {
        return activeSession;
    }

    public Long getStartedAtEpochMillis() {
        return startedAtEpochMillis;
    }

    private void refreshSessionMeta() {
        activeSession = sessionData != null && "ACTIVE".equals(sessionData.getStatus());
        if (activeSession && sessionData.getStartedAt() != null) {
            startedAtEpochMillis = sessionData.getStartedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return;
        }
        startedAtEpochMillis = null;
    }
}
