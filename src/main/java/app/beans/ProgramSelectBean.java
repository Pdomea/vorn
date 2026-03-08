package app.beans;

import java.sql.SQLException;
import java.util.List;

import app.model.Plan;
import app.model.User;
import app.service.ProgramService;

public class ProgramSelectBean {
    private String info;
    private String error;
    private List<Plan> activePlans = new java.util.ArrayList<>();
    private Long currentActivePlanId;

    public ProgramSelectBean() {
    }

    public void loadForUser(ProgramService programService, User user) throws SQLException {
        if (programService == null) {
            throw new IllegalArgumentException("ProgramService ist erforderlich.");
        }
        activePlans = programService.getActivePlansForUser(user);
        currentActivePlanId = user == null ? null : user.getActivePlanId();
    }

    public List<Plan> getActivePlans() {
        return activePlans;
    }

    public void setActivePlans(List<Plan> activePlans) {
        this.activePlans = activePlans == null ? new java.util.ArrayList<>() : activePlans;
    }

    public Long getCurrentActivePlanId() {
        return currentActivePlanId;
    }

    public void setCurrentActivePlanId(Long currentActivePlanId) {
        this.currentActivePlanId = currentActivePlanId;
    }

    public boolean hasActivePlans() {
        return activePlans != null && !activePlans.isEmpty();
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
}
