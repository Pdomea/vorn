package app.service;

import java.sql.SQLException;
import java.util.Locale;

import app.dao.UserDao;
import app.model.User;
import app.model.UserRole;

public class AuthService {
    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void initializeSchema() throws SQLException {
        userDao.createTableIfNotExists();
    }

    public User register(String email, String displayName, String plainPassword) throws SQLException {
        String normalizedEmail = normalizeEmail(email);
        validateDisplayName(displayName);
        validatePassword(plainPassword);

        User existingUser = userDao.findByEmail(normalizedEmail);
        if (existingUser != null) {
            throw new IllegalArgumentException("E-Mail ist bereits registriert.");
        }

        User newUser = new User();
        newUser.setEmail(normalizedEmail);
        newUser.setDisplayName(displayName.trim());
        newUser.setPasswordHash(PasswordUtil.hashPassword(plainPassword));
        newUser.setRole(UserRole.USER);

        return userDao.insert(newUser).withoutSensitiveData();
    }

    public User login(String email, String plainPassword) throws SQLException {
        String normalizedEmail = normalizeEmail(email);
        User user = userDao.findByEmail(normalizedEmail);
        if (user == null) {
            return null;
        }

        boolean validPassword = PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash());
        if (!validPassword) {
            return null;
        }
        return user.withoutSensitiveData();
    }

    public boolean seedAdminIfMissing(String email, String displayName, String plainPassword) throws SQLException {
        String normalizedEmail = normalizeEmail(email);
        validateDisplayName(displayName);
        validatePassword(plainPassword);

        if (userDao.existsAdmin()) {
            return false;
        }

        User existingUser = userDao.findByEmail(normalizedEmail);
        if (existingUser != null) {
            userDao.updateRole(existingUser.getId(), UserRole.ADMIN);
            return true;
        }

        User admin = new User();
        admin.setEmail(normalizedEmail);
        admin.setDisplayName(displayName.trim());
        admin.setPasswordHash(PasswordUtil.hashPassword(plainPassword));
        admin.setRole(UserRole.ADMIN);

        userDao.insert(admin);
        return true;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-Mail ist erforderlich.");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("E-Mail ist ungültig.");
        }
        return normalized;
    }

    private void validateDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Anzeigename ist erforderlich.");
        }
        if (displayName.trim().length() < 2) {
            throw new IllegalArgumentException("Anzeigename muss mindestens 2 Zeichen enthalten.");
        }
    }

    private void validatePassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Passwort ist erforderlich.");
        }
        if (plainPassword.length() < 8) {
            throw new IllegalArgumentException("Passwort muss mindestens 8 Zeichen enthalten.");
        }
    }
}
