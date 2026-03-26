package view;

import java.util.Scanner;
import controller.AuthController;

public class RegisterPage {

    public static void show(AuthController controller) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("\n=== Register ===");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.println("Select Role:");
        System.out.println("1. TA");
        System.out.println("2. MO");
        System.out.println("3. Admin");

        int roleChoice = scanner.nextInt();
        scanner.nextLine();

        String role = "";

        switch (roleChoice) {
            case 1:
                role = "TA";
                break;
            case 2:
                role = "MO";
                break;
            case 3:
                role = "Admin";
                break;
            default:
                System.out.println("Invalid role.");
                return;
        }

        controller.handleRegister(username, password, role);
    }
}
