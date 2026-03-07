package app.model;

public class PlanWeekTraining {
    private long id;
    private long planWeekId;
    private long trainingId;
    private int sortOrder;
    private String trainingTitle;
    private String trainingStatus;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPlanWeekId() {
        return planWeekId;
    }

    public void setPlanWeekId(long planWeekId) {
        this.planWeekId = planWeekId;
    }

    public long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(long trainingId) {
        this.trainingId = trainingId;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getTrainingTitle() {
        return trainingTitle;
    }

    public void setTrainingTitle(String trainingTitle) {
        this.trainingTitle = trainingTitle;
    }

    public String getTrainingStatus() {
        return trainingStatus;
    }

    public void setTrainingStatus(String trainingStatus) {
        this.trainingStatus = trainingStatus;
    }
}
