package com.example.tasystem.ui;

import com.example.tasystem.data.JsonStore;
import com.example.tasystem.data.ProfileData;
import com.example.tasystem.ui.screens.DashboardScreen;
import com.example.tasystem.ui.screens.OnboardingScreen;
import com.example.tasystem.ui.screens.ProfileScreen;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;

public final class AppFrame extends JFrame {
    public static final String ROUTE_ONBOARDING = "onboarding";
    public static final String ROUTE_DASHBOARD = "dashboard";
    public static final String ROUTE_PROFILE = "profile";

    private final JsonStore store;
    private ProfileData profile;

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final OnboardingScreen onboarding;
    private final DashboardScreen dashboard;
    private final ProfileScreen profileScreen;

    public AppFrame(JsonStore store, ProfileData initialProfile) {
        super("TA System");
        this.store = store;
        this.profile = initialProfile;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setSize(1080, 760);
        setLocationRelativeTo(null);

        onboarding = new OnboardingScreen(this);
        dashboard = new DashboardScreen(this);
        profileScreen = new ProfileScreen(this);

        root.add(onboarding, ROUTE_ONBOARDING);
        root.add(dashboard, ROUTE_DASHBOARD);
        root.add(profileScreen, ROUTE_PROFILE);
        setContentPane(root);

        showRoute(ROUTE_ONBOARDING);
    }

    public JsonStore store() {
        return store;
    }

    public ProfileData profile() {
        return profile;
    }

    public void updateProfile(ProfileData next) {
        profile = next;
        store.save(profile);
        dashboard.refresh();
        profileScreen.refresh();
    }

    public void showRoute(String route) {
        if (ROUTE_DASHBOARD.equals(route)) dashboard.refresh();
        if (ROUTE_PROFILE.equals(route)) profileScreen.refresh();
        cards.show(root, route);
    }
}

