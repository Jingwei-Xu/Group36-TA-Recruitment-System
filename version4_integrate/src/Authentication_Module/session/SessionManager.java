package Authentication_Module.session;
import Authentication_Module.model.User;
import TA_Job_Application_Module.service.DataService;

public class SessionManager {

    private static User currentUser;

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
        // Clear cached AI analysis results.
        DataService.getInstance().clearCachedAIResults();
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
