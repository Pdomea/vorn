package app.beans;

import java.util.List;
import java.util.Map;

import app.model.Plan;
import app.model.PlanWeek;
import app.model.PlanWeekTraining;
import app.model.Training;
import app.service.ProgramService.PlanDetailData;

public class AdminProgramDetailBean {
    private String info;
    private String error;
    private List<Plan> plans = List.of();
    private List<Training> trainings = List.of();
    private PlanDetailData selectedPlanDetail;

    public AdminProgramDetailBean() {
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

    public List<Plan> getPlans() {
        return plans;
    }

    public void setPlans(List<Plan> plans) {
        this.plans = plans == null ? List.of() : plans;
    }

    public List<Training> getTrainings() {
        return trainings;
    }

    public void setTrainings(List<Training> trainings) {
        this.trainings = trainings == null ? List.of() : trainings;
    }

    public PlanDetailData getSelectedPlanDetail() {
        return selectedPlanDetail;
    }

    public void setSelectedPlanDetail(PlanDetailData selectedPlanDetail) {
        this.selectedPlanDetail = selectedPlanDetail;
    }

    public Plan getSelectedPlan() {
        return selectedPlanDetail == null ? null : selectedPlanDetail.getPlan();
    }

    public List<PlanWeek> getSelectedWeeks() {
        if (selectedPlanDetail == null || selectedPlanDetail.getWeeks() == null) {
            return List.of();
        }
        return selectedPlanDetail.getWeeks();
    }

    public Map<Long, List<PlanWeekTraining>> getWeekTrainings() {
        if (selectedPlanDetail == null || selectedPlanDetail.getWeekTrainings() == null) {
            return Map.of();
        }
        return selectedPlanDetail.getWeekTrainings();
    }

    public boolean isSelectedPlanActive() {
        Plan selectedPlan = getSelectedPlan();
        return selectedPlan != null && "ACTIVE".equals(selectedPlan.getStatus());
    }
}
