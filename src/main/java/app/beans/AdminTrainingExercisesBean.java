package app.beans;

import java.util.List;

import app.model.Exercise;
import app.model.MuscleGroup;
import app.model.Training;
import app.model.TrainingExercise;

public class AdminTrainingExercisesBean {
    private String info;
    private String error;

    private Training training;
    private List<TrainingExercise> mappings = new java.util.ArrayList<>();
    private List<Exercise> activeExercises = new java.util.ArrayList<>();
    private List<MuscleGroup> muscleGroups = new java.util.ArrayList<>();

    private String search = "";
    private Long selectedMuscleGroupId;
    private String sortDir = "asc";

    public AdminTrainingExercisesBean() {
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

    public Training getTraining() {
        return training;
    }

    public void setTraining(Training training) {
        this.training = training;
    }

    public List<TrainingExercise> getMappings() {
        return mappings;
    }

    public void setMappings(List<TrainingExercise> mappings) {
        this.mappings = mappings == null ? new java.util.ArrayList<>() : mappings;
    }

    public boolean hasMappings() {
        return mappings != null && !mappings.isEmpty();
    }

    public List<Exercise> getActiveExercises() {
        return activeExercises;
    }

    public void setActiveExercises(List<Exercise> activeExercises) {
        this.activeExercises = activeExercises == null ? new java.util.ArrayList<>() : activeExercises;
    }

    public boolean hasActiveExercises() {
        return activeExercises != null && !activeExercises.isEmpty();
    }

    public List<MuscleGroup> getMuscleGroups() {
        return muscleGroups;
    }

    public void setMuscleGroups(List<MuscleGroup> muscleGroups) {
        this.muscleGroups = muscleGroups == null ? new java.util.ArrayList<>() : muscleGroups;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        if (search == null) {
            this.search = "";
            return;
        }
        this.search = search;
    }

    public Long getSelectedMuscleGroupId() {
        return selectedMuscleGroupId;
    }

    public void setSelectedMuscleGroupId(Long selectedMuscleGroupId) {
        this.selectedMuscleGroupId = selectedMuscleGroupId;
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

    public String getTrainingIdText() {
        if (training == null) {
            return "";
        }
        return String.valueOf(training.getId());
    }

    public boolean hasTraining() {
        return training != null;
    }

    public int getNextSortOrder() {
        return (mappings == null ? 0 : mappings.size()) + 1;
    }

    public String getSelectedMuscleGroupIdText() {
        if (selectedMuscleGroupId == null) {
            return "";
        }
        return String.valueOf(selectedMuscleGroupId);
    }
}
