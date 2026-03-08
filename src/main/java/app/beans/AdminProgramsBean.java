package app.beans;

import java.util.List;

import app.model.Plan;

public class AdminProgramsBean {
    private String info;
    private String error;
    private String statusFilter = "ALL";
    private String statusQuerySuffix = "";
    private List<Plan> plans = new java.util.ArrayList<>();

    public AdminProgramsBean() {
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

    public String getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            this.statusFilter = "ALL";
        } else {
            this.statusFilter = statusFilter;
        }
        if ("ALL".equals(this.statusFilter)) {
            statusQuerySuffix = "";
        } else {
            statusQuerySuffix = "&status=" + this.statusFilter;
        }
    }

    public String getStatusQuerySuffix() {
        return statusQuerySuffix;
    }

    public List<Plan> getPlans() {
        return plans;
    }

    public void setPlans(List<Plan> plans) {
        this.plans = plans == null ? new java.util.ArrayList<>() : plans;
    }

    public boolean hasPlans() {
        return plans != null && !plans.isEmpty();
    }
}
