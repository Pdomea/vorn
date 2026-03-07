package app.model;

import java.util.ArrayList;
import java.util.List;

public class Exercise {
    private long id;
    private String name;
    private String description;
    private String status;
    private List<Long> muscleGroupIds = new ArrayList<>();
    private List<String> muscleGroupLabels = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Long> getMuscleGroupIds() {
        return muscleGroupIds;
    }

    public void setMuscleGroupIds(List<Long> muscleGroupIds) {
        if (muscleGroupIds == null) {
            this.muscleGroupIds = new ArrayList<>();
            return;
        }
        this.muscleGroupIds = muscleGroupIds;
    }

    public List<String> getMuscleGroupLabels() {
        return muscleGroupLabels;
    }

    public void setMuscleGroupLabels(List<String> muscleGroupLabels) {
        if (muscleGroupLabels == null) {
            this.muscleGroupLabels = new ArrayList<>();
            return;
        }
        this.muscleGroupLabels = muscleGroupLabels;
    }
}
