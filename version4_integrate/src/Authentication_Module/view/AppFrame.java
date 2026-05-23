package Authentication_Module.view;

import Authentication_Module.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AppFrame extends JFrame {

    private CardLayout layout;
    private JPanel container;
    // Store page references.
    private Map<String, JPanel> pages = new HashMap<>();
    // Current logged-in user (used by the MO first-login completion page).
    private User currentUser;

    public AppFrame() {

        setTitle("TA Recruitment System");
        setMinimumSize(new Dimension(980, 680));
        setSize(1080, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize.
        layout = new CardLayout();
        container = new JPanel(layout);

        // =========================
        // Register pages.
        // =========================
        HomePagePanel homePanel = new HomePagePanel(this);
        LoginPagePanel loginPanel = new LoginPagePanel(this);
        RegisterPagePanel registerPanel = new RegisterPagePanel(this);
        TAHomePagePanel taPanel = new TAHomePagePanel(this);
        MOHomePagePanel moPanel = new MOHomePagePanel(this);
        AdminHomePagePanel adminPanel = new AdminHomePagePanel(this);
        MOFirstLoginPagePanel moFirstPanel = new MOFirstLoginPagePanel(this);

        container.add(homePanel, "HOME");
        container.add(loginPanel, "LOGIN");
        container.add(registerPanel, "REGISTER");
        container.add(taPanel, "TA");
        container.add(moPanel, "MO");
        container.add(adminPanel, "ADMIN");
        container.add(moFirstPanel, "MO_FIRST");

        // Store page references.
        pages.put("HOME", homePanel);
        pages.put("LOGIN", loginPanel);
        pages.put("REGISTER", registerPanel);
        pages.put("TA", taPanel);
        pages.put("MO", moPanel);
        pages.put("ADMIN", adminPanel);
        pages.put("MO_FIRST", moFirstPanel);

        // Attach to main window.
        setContentPane(container);

        // Show home page by default.
        layout.show(container, "HOME");

        // Ensure all page layouts are initialized correctly.
        SwingUtilities.invokeLater(() -> {
            for (JPanel page : pages.values()) {
                page.revalidate();
                page.repaint();
            }
            revalidate();
            repaint();
        });

        setVisible(true);
    }

    // =========================
    // Core page switching method.
    // =========================
    public void showPage(String name) {
        if ("ADMIN".equals(name)) {
            AdminHomePagePanel adminPanel = (AdminHomePagePanel) pages.get("ADMIN");
            if (adminPanel != null) {
                adminPanel.launchAdminWindow();
            }
        } else {
            layout.show(container, name);
            if ("LOGIN".equals(name)) {
                JPanel loginPanel = pages.get("LOGIN");
                if (loginPanel instanceof LoginPagePanel) {
                    ((LoginPagePanel) loginPanel).clearCredentials();
                }
            }
            if ("HOME".equals(name)) {
                HomePagePanel homePanel = (HomePagePanel) pages.get("HOME");
                if (homePanel != null) {
                    homePanel.refreshButtonStyles();
                }
            }
            if ("REGISTER".equals(name)) {
                JPanel registerPanel = pages.get("REGISTER");
                if (registerPanel != null) {
                    if (registerPanel instanceof RegisterPagePanel) {
                        ((RegisterPagePanel) registerPanel).clearForm();
                    }
                    SwingUtilities.invokeLater(() -> {
                        registerPanel.revalidate();
                        registerPanel.repaint();
                        // Force-refresh all components.
                        for (Component c : registerPanel.getComponents()) {
                            if (c instanceof JPanel) {
                                ((JPanel) c).revalidate();
                                ((JPanel) c).repaint();
                            }
                        }
                    });
                }
            }
            container.revalidate();
            container.repaint();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}