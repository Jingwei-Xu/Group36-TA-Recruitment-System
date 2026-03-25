package com.mojobsystem;

import com.formdev.flatlaf.FlatLightLaf;
import com.mojobsystem.ui.MoDashboardFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {
            // Falls back to default LAF if FlatLaf init fails.
        }

        SwingUtilities.invokeLater(() -> {
            MoDashboardFrame frame = new MoDashboardFrame();
            frame.setVisible(true);
        });
    }
}
