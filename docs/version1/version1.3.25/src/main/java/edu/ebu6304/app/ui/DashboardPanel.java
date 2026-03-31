package edu.ebu6304.app.ui;

import edu.ebu6304.app.model.ApplicationRecord;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final MainFrame frame;

    private final JLabel totalApplications = new JLabel("0");
    private final JLabel pendingReviews = new JLabel("0");
    private final JLabel reviewedApplications = new JLabel("0");

    public DashboardPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(12, 12));
        setBackground(Style.BG);

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("MO Dashboard");
        title.setFont(Style.FONT_H1);
        title.setForeground(Style.TEXT);

        JLabel subtitle = new JLabel("Welcome back! Please select a module to continue.");
        subtitle.setFont(Style.FONT_BODY);
        subtitle.setForeground(Style.MUTED);

        JPanel titleWrap = new JPanel(new GridLayout(2, 1, 0, 2));
        titleWrap.setOpaque(false);
        titleWrap.add(title);
        titleWrap.add(subtitle);

        JButton refreshBtn = new JButton("Reload Data");
        Style.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> frame.reloadData());

        top.add(titleWrap, BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);

        JPanel modules = new JPanel(new GridLayout(1, 2, 14, 14));
        modules.setOpaque(false);
        modules.add(buildJobModuleCard());
        modules.add(buildApplicationModuleCard());

        JPanel quickOverview = new JPanel(new GridLayout(1, 3, 14, 14));
        quickOverview.setOpaque(false);
        quickOverview.add(buildStatCard("Total Applications", totalApplications));
        quickOverview.add(buildStatCard("Pending Reviews", pendingReviews));
        quickOverview.add(buildStatCard("Reviewed Applications", reviewedApplications));

        JPanel dataPathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Style.stylePanelCard(dataPathPanel);
        JLabel dataPath = new JLabel("Data Source: " + frame.getDataRootDisplay());
        dataPath.setFont(Style.FONT_SMALL);
        dataPath.setForeground(Style.MUTED);
        dataPathPanel.add(dataPath);

        page.add(top);
        page.add(Box.createVerticalStrut(14));
        page.add(modules);
        page.add(Box.createVerticalStrut(14));
        page.add(quickOverview);
        page.add(Box.createVerticalStrut(14));
        page.add(dataPathPanel);

        add(new JScrollPane(page), BorderLayout.CENTER);
    }

    public void refresh(List<ApplicationRecord> records) {
        long total = records.size();
        long pending = records.stream().filter(r -> !r.isReviewed()).count();
        long reviewed = records.stream().filter(ApplicationRecord::isReviewed).count();

        totalApplications.setText(String.valueOf(total));
        pendingReviews.setText(String.valueOf(pending));
        reviewedApplications.setText(String.valueOf(reviewed));
    }

    private JPanel buildStatCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        Style.stylePanelCard(card);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Style.MUTED);
        titleLabel.setFont(Style.FONT_BODY);

        valueLabel.setFont(Style.FONT_H1);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);
        valueLabel.setForeground(Style.TEXT);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildJobModuleCard() {
        JPanel card = new JPanel(new BorderLayout(8, 10));
        Style.stylePanelCard(card);

        JLabel title = new JLabel("Job Management Module");
        title.setFont(Style.FONT_H2);
        title.setForeground(Style.TEXT);

        JLabel desc = new JLabel("Create and maintain job postings (coming soon)");
        desc.setFont(Style.FONT_BODY);
        desc.setForeground(Style.MUTED);

        JButton btn = new JButton("Go to Job Management");
        Style.stylePrimaryButton(btn);
        btn.addActionListener(e -> javax.swing.JOptionPane.showMessageDialog(this, "Job Management Module is not implemented in this phase."));

        card.add(title, BorderLayout.NORTH);
        card.add(desc, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildApplicationModuleCard() {
        JPanel card = new JPanel(new BorderLayout(8, 10));
        Style.stylePanelCard(card);

        JLabel title = new JLabel("Application Review Module");
        title.setFont(Style.FONT_H2);
        title.setForeground(Style.TEXT);

        JLabel desc = new JLabel("Browse, evaluate and review TA applications");
        desc.setFont(Style.FONT_BODY);
        desc.setForeground(Style.MUTED);

        JButton btn = new JButton("Go to Application Review");
        Style.stylePrimaryButton(btn);
        btn.addActionListener(e -> frame.showApplicationsList());

        card.add(title, BorderLayout.NORTH);
        card.add(desc, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        return card;
    }
}
