package app.model;

import java.time.LocalDateTime;

public class User {
    private long id;
    private String email;
    private String passwordHash;
    private String displayName;
    private UserRole role;
    private Long activePlanId;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(long id, String email, String passwordHash, String displayName, UserRole role, Long activePlanId, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
        this.activePlanId = activePlanId;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Long getActivePlanId() {
        return activePlanId;
    }

    public void setActivePlanId(Long activePlanId) {
        this.activePlanId = activePlanId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User withoutSensitiveData() {
        return new User(id, email, null, displayName, role, activePlanId, createdAt);
    }
}
