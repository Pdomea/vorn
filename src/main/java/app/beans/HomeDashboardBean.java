package app.beans;

import app.model.Plan;
import app.model.WorkoutSession;
import app.service.ProgramService.NextTrainingData;

public class HomeDashboardBean {
    private String userDisplayName;
    private boolean admin;

    private String info;
    private String error;

    private WorkoutSession activeSession;
    private String activeSessionError;

    private String dashboardError;
    private Plan dashboardSelectedPlan;
    private NextTrainingData dashboardNextTraining;

    private String workoutsCompletedText = "0";
    private String hoursSpentText = "0:00";
    private int progressPercent;

    private String fallbackPlanImagePath;
    private String selectedPlanImagePath;

    public HomeDashboardBean() {
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
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

    public WorkoutSession getActiveSession() {
        return activeSession;
    }

    public void setActiveSession(WorkoutSession activeSession) {
        this.activeSession = activeSession;
    }

    public String getActiveSessionError() {
        return activeSessionError;
    }

    public void setActiveSessionError(String activeSessionError) {
        this.activeSessionError = activeSessionError;
    }

    public String getDashboardError() {
        return dashboardError;
    }

    public void setDashboardError(String dashboardError) {
        this.dashboardError = dashboardError;
    }

    public Plan getDashboardSelectedPlan() {
        return dashboardSelectedPlan;
    }

    public void setDashboardSelectedPlan(Plan dashboardSelectedPlan) {
        this.dashboardSelectedPlan = dashboardSelectedPlan;
    }

    public NextTrainingData getDashboardNextTraining() {
        return dashboardNextTraining;
    }

    public void setDashboardNextTraining(NextTrainingData dashboardNextTraining) {
        this.dashboardNextTraining = dashboardNextTraining;
    }

    public String getWorkoutsCompletedText() {
        return workoutsCompletedText;
    }

    public void setWorkoutsCompletedText(String workoutsCompletedText) {
        this.workoutsCompletedText = workoutsCompletedText;
    }

    public String getHoursSpentText() {
        return hoursSpentText;
    }

    public void setHoursSpentText(String hoursSpentText) {
        this.hoursSpentText = hoursSpentText;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = Math.max(0, Math.min(100, progressPercent));
    }

    public String getFallbackPlanImagePath() {
        return fallbackPlanImagePath;
    }

    public void setFallbackPlanImagePath(String fallbackPlanImagePath) {
        this.fallbackPlanImagePath = fallbackPlanImagePath;
    }

    public String getSelectedPlanImagePath() {
        return selectedPlanImagePath;
    }

    public void setSelectedPlanImagePath(String selectedPlanImagePath) {
        this.selectedPlanImagePath = selectedPlanImagePath;
    }
}
