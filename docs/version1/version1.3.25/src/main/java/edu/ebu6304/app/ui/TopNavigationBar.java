package edu.ebu6304.app.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;

public class TopNavigationBar extends JPanel {
    private final JButton homeBtn = new JButton("Home");
    private final JButton jobBtn = new JButton("Job Management");
    private final JButton reviewBtn = new JButton("Application Review");

    public TopNavigationBar(MainFrame frame) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Style.BORDER));

        initNavButton(homeBtn);
        initNavButton(jobBtn);
        initNavButton(reviewBtn);

        homeBtn.addActionListener(e -> frame.showDashboard());
        jobBtn.addActionListener(e -> javax.swing.JOptionPane.showMessageDialog(this, "Job Management Module is not implemented in this phase."));
        reviewBtn.addActionListener(e -> frame.showApplicationsList());

        add(homeBtn);
        add(jobBtn);
        add(reviewBtn);
    }

    public void setActive(String active) {
        styleInactive(homeBtn);
        styleInactive(jobBtn);
        styleInactive(reviewBtn);

        if ("home".equals(active)) {
            styleActive(homeBtn);
        } else if ("job".equals(active)) {
            styleActive(jobBtn);
        } else if ("review".equals(active)) {
            styleActive(reviewBtn);
        }
    }

    private void initNavButton(JButton btn) {
        btn.setFont(Style.FONT_BODY_BOLD);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Style.BORDER),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        styleInactive(btn);
    }

    private void styleActive(JButton btn) {
        btn.setBackground(new Color(31, 41, 55));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(31, 41, 55)),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
    }

    private void styleInactive(JButton btn) {
        btn.setBackground(new Color(243, 244, 246));
        btn.setForeground(Style.TEXT);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Style.BORDER),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
    }
}
