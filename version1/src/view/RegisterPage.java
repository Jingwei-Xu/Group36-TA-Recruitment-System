import java.util.Scanner;
import controller.AuthController;

public class RegisterPage {

    public static void show(AuthController controller) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("\n===== REGISTER PAGE =====");

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter role (TA / MO / Admin): ");
        String role = scanner.nextLine();

        controller.handleRegister(username, password, role);
    }
}
