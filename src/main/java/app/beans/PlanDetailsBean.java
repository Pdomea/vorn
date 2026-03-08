package app.beans;

import java.util.List;
import java.util.Map;

import app.model.Plan;
import app.model.PlanWeek;
import app.model.PlanWeekTraining;
import app.service.ProgramService.NextTrainingData;
import app.service.ProgramService.WeekProgressData;

public class PlanDetailsBean {
    private String info;
    private String error;

    private Plan selectedPlan;
    private List<PlanWeek> selectedPlanWeeks = new java.util.ArrayList<>();
    private PlanWeek selectedDetailWeek;
    private List<PlanWeekTraining> selectedDetailWeekTrainings = new java.util.ArrayList<>();
    private Map<Long, String> selectedDetailWeekStatuses = new java.util.HashMap<>();
    private WeekProgressData selectedDetailWeekProgress;
    private NextTrainingData selectedPlanNextTraining;

    private int completedSlots;
    private int totalSlots;
    private int progressPercent;

    private String nextTrainingText = "Kein offenes Training";
    private String fallbackPlanImagePath;
    private String selectedPlanImagePath;

    private boolean previewMode;

    public PlanDetailsBean() {
    }

    public boolean isPreviewMode() {
        return previewMode;
    }

    public void setPreviewMode(boolean previewMode) {
        this.previewMode = previewMode;
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

    public Plan getSelectedPlan() {
        return selectedPlan;
    }

    public void setSelectedPlan(Plan selectedPlan) {
        this.selectedPlan = selectedPlan;
    }

    public List<PlanWeek> getSelectedPlanWeeks() {
        return selectedPlanWeeks;
    }

    public void setSelectedPlanWeeks(List<PlanWeek> selectedPlanWeeks) {
        this.selectedPlanWeeks = selectedPlanWeeks == null ? new java.util.ArrayList<>() : selectedPlanWeeks;
    }

    public PlanWeek getSelectedDetailWeek() {
        return selectedDetailWeek;
    }

    public void setSelectedDetailWeek(PlanWeek selectedDetailWeek) {
        this.selectedDetailWeek = selectedDetailWeek;
    }

    public List<PlanWeekTraining> getSelectedDetailWeekTrainings() {
        return selectedDetailWeekTrainings;
    }

    public void setSelectedDetailWeekTrainings(List<PlanWeekTraining> selectedDetailWeekTrainings) {
        this.selectedDetailWeekTrainings = selectedDetailWeekTrainings == null ? new java.util.ArrayList<>()
                : selectedDetailWeekTrainings;
    }

    public Map<Long, String> getSelectedDetailWeekStatuses() {
        return selectedDetailWeekStatuses;
    }

    public void setSelectedDetailWeekStatuses(Map<Long, String> selectedDetailWeekStatuses) {
        this.selectedDetailWeekStatuses = selectedDetailWeekStatuses == null ? new java.util.HashMap<>()
                : selectedDetailWeekStatuses;
    }

    public WeekProgressData getSelectedDetailWeekProgress() {
        return selectedDetailWeekProgress;
    }

    public void setSelectedDetailWeekProgress(WeekProgressData selectedDetailWeekProgress) {
        this.selectedDetailWeekProgress = selectedDetailWeekProgress;
    }

    public NextTrainingData getSelectedPlanNextTraining() {
        return selectedPlanNextTraining;
    }

    public void setSelectedPlanNextTraining(NextTrainingData selectedPlanNextTraining) {
        this.selectedPlanNextTraining = selectedPlanNextTraining;
    }

    public int getCompletedSlots() {
        return completedSlots;
    }

    public void setCompletedSlots(int completedSlots) {
        this.completedSlots = Math.max(0, completedSlots);
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = Math.max(0, totalSlots);
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = Math.max(0, Math.min(100, progressPercent));
    }

    public String getNextTrainingText() {
        return nextTrainingText;
    }

    public void setNextTrainingText(String nextTrainingText) {
        this.nextTrainingText = nextTrainingText;
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
