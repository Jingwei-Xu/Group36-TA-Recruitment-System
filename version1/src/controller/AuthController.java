package controller;

import model.User;
import service.AuthService;

import view.TAHomePage;
import view.MOHomePage;
import view.AdminHomePage;

public class AuthController {

    private AuthService authService;

    public AuthController() {
        this.authService = new AuthService();
    }

    // ========================
    // Handle Register
    // ========================
    public void handleRegister(String username, String password, String role) {

        boolean success = authService.register(username, password, role);

        if (success) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Username already exists.");
        }
    }

    // ========================
    // Handle Login
    // ========================
    public void handleLogin(String username, String password) {

        User user = authService.login(username, password);

        if (user == null) {
            System.out.println("Invalid username or password.");
            return;
        }

        System.out.println("Login successful!");

        // ⭐ 关键：根据角色跳转
        switch (user.getRole()) {
            case "TA":
                TAHomePage.show();
                break;
            case "MO":
                MOHomePage.show();
                break;
            case "Admin":
                AdminHomePage.show();
                break;
            default:
                System.out.println("Unknown role.");
        }
    }
}
