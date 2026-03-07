package app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WorkoutLog {
    private long id;
    private long sessionId;
    private long sessionExerciseId;
    private int setNo;
    private int reps;
    private BigDecimal weight;
    private String note;
    private LocalDateTime loggedAt;

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

    public long getSessionExerciseId() {
        return sessionExerciseId;
    }

    public void setSessionExerciseId(long sessionExerciseId) {
        this.sessionExerciseId = sessionExerciseId;
    }

    public int getSetNo() {
        return setNo;
    }

    public void setSetNo(int setNo) {
        this.setNo = setNo;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(LocalDateTime loggedAt) {
        this.loggedAt = loggedAt;
    }
}
