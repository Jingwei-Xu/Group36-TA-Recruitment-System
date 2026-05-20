package Authentication_Module.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {

    private String username;
    private String password;

    private String passwordHash;
    private String role; // TA / MO / Admin

    // ACTIVE / PENDING / REJECTED
    private String status;

    private String systemUserId;

    // ========================
    // Constructors
    // ========================

    // Backward compatibility constructor
    // Old code can still use:
    // new User(username, password, role)
    public User(String username, String password, String role) {
        this(username, password, null, role, "ACTIVE", null);
    }

    public User(String username, String password, String role, String status) {
        this(username, password, null, role, status, null);
    }

    public User(String username,
                String password,
                String passwordHash,
                String role,
                String status) {

        this(username, password, passwordHash, role, status, null);
    }

    public User(String username,
                String password,
                String passwordHash,
                String role,
                String status,
                String systemUserId) {

        this.username = username;
        this.password = password != null ? password : "";
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.systemUserId = systemUserId;
    }

    // ========================
    // Getters
    // ========================

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public String getSystemUserId() {
        return systemUserId;
    }

    // ========================
    // Setters
    // ========================

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSystemUserId(String systemUserId) {
        this.systemUserId = systemUserId;
    }

    // ========================
    // Password Verification
    // ========================

    public boolean passwordMatches(String plain) {

        if (plain == null) {
            return false;
        }

        // Plain text password match
        if (password != null
                && !password.isBlank()
                && plain.equals(password)) {

            return true;
        }

        // Hash password match
        if (passwordHash != null
                && !passwordHash.isBlank()) {

            String h = passwordHash.trim();

            // SHA-256 hash
            if (h.matches("^[a-fA-F0-9]{64}$")) {
                return sha256Hex(plain).equalsIgnoreCase(h);
            }

            // Fallback plain compare
            return plain.equals(h);
        }

        return plain.equals(password);
    }

    // ========================
    // SHA-256 Utility
    // ========================

    private static String sha256Hex(String input) {

        try {

            MessageDigest md =
                    MessageDigest.getInstance("SHA-256");

            byte[] digest =
                    md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb =
                    new StringBuilder(digest.length * 2);

            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {

            return "";
        }
    }
}