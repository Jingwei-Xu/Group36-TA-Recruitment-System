package view;

import java.util.Scanner;
import controller.AuthController;

public class LoginPage {

    public static void show(AuthController controller) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("\n=== Login ===");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        controller.handleLogin(username, password);
    }
}
