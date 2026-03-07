package app.model;

public class SessionExercise {
    private long id;
    private long sessionId;
    private long exerciseId;
    private String exerciseNameSnapshot;
    private int plannedSetsSnapshot;
    private int plannedRepsSnapshot;
    private int sortOrder;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getExerciseNameSnapshot() {
        return exerciseNameSnapshot;
    }

    public void setExerciseNameSnapshot(String exerciseNameSnapshot) {
        this.exerciseNameSnapshot = exerciseNameSnapshot;
    }

    public int getPlannedSetsSnapshot() {
        return plannedSetsSnapshot;
    }

    public void setPlannedSetsSnapshot(int plannedSetsSnapshot) {
        this.plannedSetsSnapshot = plannedSetsSnapshot;
    }

    public int getPlannedRepsSnapshot() {
        return plannedRepsSnapshot;
    }

    public void setPlannedRepsSnapshot(int plannedRepsSnapshot) {
        this.plannedRepsSnapshot = plannedRepsSnapshot;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
