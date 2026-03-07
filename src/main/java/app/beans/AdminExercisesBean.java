package app.beans;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import app.model.Exercise;
import app.model.MuscleGroup;

public class AdminExercisesBean {
    private String info;
    private String error;

    private List<Exercise> exercises = List.of();
    private List<MuscleGroup> muscleGroups = List.of();

    private Exercise editExercise;
    private Set<Long> selectedMuscleGroupIds = new LinkedHashSet<>();

    private String sortBy = "name";
    private String sortDir = "asc";
    private String statusFilter = "ALL";
    private String statusQuerySuffix = "";

    public AdminExercisesBean() {
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

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises == null ? List.of() : exercises;
    }

    public boolean hasExercises() {
        return exercises != null && !exercises.isEmpty();
    }

    public List<MuscleGroup> getMuscleGroups() {
        return muscleGroups;
    }

    public void setMuscleGroups(List<MuscleGroup> muscleGroups) {
        this.muscleGroups = muscleGroups == null ? List.of() : muscleGroups;
    }

    public boolean hasMuscleGroups() {
        return muscleGroups != null && !muscleGroups.isEmpty();
    }

    public Exercise getEditExercise() {
        return editExercise;
    }

    public void setEditExercise(Exercise editExercise) {
        this.editExercise = editExercise;
        selectedMuscleGroupIds = new LinkedHashSet<>();
        if (editExercise != null && editExercise.getMuscleGroupIds() != null) {
            selectedMuscleGroupIds.addAll(editExercise.getMuscleGroupIds());
        }
    }

    public boolean isEditMode() {
        return editExercise != null;
    }

    public String getFormId() {
        if (editExercise == null) {
            return "";
        }
        return String.valueOf(editExercise.getId());
    }

    public String getFormName() {
        if (editExercise == null || editExercise.getName() == null) {
            return "";
        }
        return editExercise.getName();
    }

    public String getFormDescription() {
        if (editExercise == null || editExercise.getDescription() == null) {
            return "";
        }
        return editExercise.getDescription();
    }

    public String getFormHeadline() {
        if (editExercise == null) {
            return "Neue Übung anlegen";
        }
        return "Übung bearbeiten";
    }

    public String getSubmitLabel() {
        if (editExercise == null) {
            return "Übung speichern";
        }
        return "Änderungen speichern";
    }

    public boolean isSelectedMuscleGroup(Long muscleGroupId) {
        if (muscleGroupId == null) {
            return false;
        }
        return selectedMuscleGroupIds.contains(muscleGroupId);
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            this.sortBy = "name";
            return;
        }
        this.sortBy = sortBy;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        if (sortDir == null || sortDir.isBlank()) {
            this.sortDir = "asc";
            return;
        }
        this.sortDir = sortDir;
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
}
