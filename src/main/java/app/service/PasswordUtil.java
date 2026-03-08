package app.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {
    private static final int SALT_LENGTH = 16;

    private PasswordUtil() {
    }

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be empty.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        byte[] hash = sha256(salt, plainPassword);

        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return "sha256$" + encoder.encodeToString(salt) + "$" + encoder.encodeToString(hash);
    }

    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }

        String[] parts = storedHash.split("\\$");
        if (parts.length != 3 || !"sha256".equals(parts[0])) {
            return false;
        }

        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            byte[] salt = decoder.decode(parts[1]);
            byte[] expectedHash = decoder.decode(parts[2]);

            byte[] actualHash = sha256(salt, plainPassword);

            String expectedString = Base64.getUrlEncoder().withoutPadding().encodeToString(expectedHash);
            String actualString = Base64.getUrlEncoder().withoutPadding().encodeToString(actualHash);

            return expectedString.equals(actualString);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static byte[] sha256(byte[] salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available.", ex);
        }
    }
}
