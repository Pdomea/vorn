package app.model;

public class Plan {
    private long id;
    private String name;
    private String description;
    private String heroImagePath;
    private String status;

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

    public String getHeroImagePath() {
        return heroImagePath;
    }

    public void setHeroImagePath(String heroImagePath) {
        this.heroImagePath = heroImagePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
