package app.beans;

import java.util.List;

import app.model.Training;

public class AdminTrainingsBean {
    private String info;
    private String error;

    private List<Training> trainings = List.of();
    private Training editTraining;

    private String sortBy = "id";
    private String sortDir = "asc";
    private String statusFilter = "ALL";

    private String sortQuerySuffix = "&sortBy=id&sortDir=asc";
    private String statusQuerySuffix = "";

    public AdminTrainingsBean() {
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

    public List<Training> getTrainings() {
        return trainings;
    }

    public void setTrainings(List<Training> trainings) {
        this.trainings = trainings == null ? List.of() : trainings;
    }

    public boolean hasTrainings() {
        return trainings != null && !trainings.isEmpty();
    }

    public Training getEditTraining() {
        return editTraining;
    }

    public void setEditTraining(Training editTraining) {
        this.editTraining = editTraining;
    }

    public boolean hasEditTraining() {
        return editTraining != null;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            this.sortBy = "id";
        } else {
            this.sortBy = sortBy;
        }
        refreshSuffixes();
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        if (sortDir == null || sortDir.isBlank()) {
            this.sortDir = "asc";
        } else {
            this.sortDir = sortDir;
        }
        refreshSuffixes();
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
        refreshSuffixes();
    }

    public String getSortQuerySuffix() {
        return sortQuerySuffix;
    }

    public String getStatusQuerySuffix() {
        return statusQuerySuffix;
    }

    public String getFormId() {
        if (editTraining == null) {
            return "";
        }
        return String.valueOf(editTraining.getId());
    }

    public String getFormTitle() {
        if (editTraining == null || editTraining.getTitle() == null) {
            return "";
        }
        return editTraining.getTitle();
    }

    public String getFormDescription() {
        if (editTraining == null || editTraining.getDescription() == null) {
            return "";
        }
        return editTraining.getDescription();
    }

    public String getFormHeadline() {
        if (editTraining == null) {
            return "Neues Training anlegen";
        }
        return "Training bearbeiten (ID " + editTraining.getId() + ")";
    }

    public String getSubmitLabel() {
        if (editTraining == null) {
            return "Training speichern";
        }
        return "Änderungen speichern";
    }

    private void refreshSuffixes() {
        sortQuerySuffix = "&sortBy=" + sortBy + "&sortDir=" + sortDir;
        if ("ALL".equals(statusFilter)) {
            statusQuerySuffix = "";
        } else {
            statusQuerySuffix = "&status=" + statusFilter;
        }
    }
}
