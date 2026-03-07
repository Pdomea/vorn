package app.model;

public class TrainingExercise {
    private long id;
    private long trainingId;
    private long exerciseId;
    private String exerciseName;
    private String exerciseDescription;
    private int plannedSets;
    private int plannedReps;
    private int sortOrder;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(long trainingId) {
        this.trainingId = trainingId;
    }

    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getExerciseDescription() {
        return exerciseDescription;
    }

    public void setExerciseDescription(String exerciseDescription) {
        this.exerciseDescription = exerciseDescription;
    }

    public int getPlannedSets() {
        return plannedSets;
    }

    public void setPlannedSets(int plannedSets) {
        this.plannedSets = plannedSets;
    }

    public int getPlannedReps() {
        return plannedReps;
    }

    public void setPlannedReps(int plannedReps) {
        this.plannedReps = plannedReps;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
