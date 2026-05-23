package Authentication_Module.service;

import Authentication_Module.model.User;
import Authentication_Module.util.JsonUtil;

import java.util.List;

public class AuthService {

    private List<User> users;

    // Login message
    private String loginMessage;

    public AuthService() {

        users = JsonUtil.loadAllUsers();
    }

    // ========================
    // Register
    // ========================
    public boolean register(String username, String password, String role) {

        // Password rule:
        // at least 8 chars, contains letters and numbers
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {

            throw new IllegalArgumentException(
                    "Password must be at least 8 characters and contain both letters and numbers."
            );
        }

        // Reload latest users
        users = JsonUtil.loadAllUsers();

        // Check duplicate username
        for (User user : users) {

            if (user.getUsername().equalsIgnoreCase(username)) {

                return false;
            }
        }

        // ========================
        // Account Status Logic
        // ========================

        String status;

        // Admin requires approval
        if (role.equalsIgnoreCase("Admin")) {

            status = "PENDING";

        } else {

            // TA / MO active immediately
            status = "ACTIVE";
        }

        // Create new user
        User newUser =
                new User(username, password, role, status);

        users.add(newUser);

        JsonUtil.saveUser(newUser);

        return true;
    }

    // ========================
    // Login
    // ========================
    public User login(String username, String password) {

        users = JsonUtil.loadAllUsers();

        for (User user : users) {

            // Username matched
            if (user.getUsername().equalsIgnoreCase(username)) {

                // Password incorrect
                if (!user.passwordMatches(password)) {

                    loginMessage =
                            "Invalid username or password";

                    return null;
                }

                // Account not activated
                if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {

                    loginMessage =
                            "Account not activated";

                    return null;
                }

                // Login success
                loginMessage =
                        "Login successful";

                return user;
            }
        }

        // Username not found
        loginMessage =
                "Invalid username or password";

        return null;
    }

    // ========================
    // Get Login Message
    // ========================
    public String getLoginMessage() {

        return loginMessage;
    }

    // ========================
    // Get Role
    // ========================
    public String getRoleByUsername(String username) {

        users = JsonUtil.loadAllUsers();

        for (User user : users) {

            if (user.getUsername().equalsIgnoreCase(username)) {

                return user.getRole();
            }
        }

        return null;
    }
}