package com.mojobsystem.ui;

import com.mojobsystem.MoContext;
import com.mojobsystem.repository.ApplicationRepository;
import com.mojobsystem.repository.JobRepository;
import com.mojobsystem.service.MoDashboardService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * MO Dashboard — grayscale layout + metrics from {@code data/}.
 */
public class MoDashboardFrame extends JFrame {
    private final JobRepository jobRepository = new JobRepository();
    private final ApplicationRepository applicationRepository = new ApplicationRepository();

    private JLabel metricCourses;
    private JLabel metricOpen;
    private JLabel metricPending;

    public MoDashboardFrame() {
        setTitle("MO System - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MoFrameGeometry.apply(this);
        setLayout(new BorderLayout());
        getContentPane().setBackground(MoUiTheme.PAGE_BG);

        add(NavigationPanel.create(NavigationPanel.Tab.HOME, navActions()), BorderLayout.NORTH);
        add(wrapCentered(buildBody()), BorderLayout.CENTER);
    }

    private NavigationPanel.Actions navActions() {
        return new NavigationPanel.Actions(
                () -> { },
                () -> MoFrameGeometry.navigateReplace(this, () -> new MyJobsFrame().setVisible(true)),
                () -> MoFrameGeometry.navigateReplace(this, () -> new ApplicationReviewPlaceholderFrame(null).setVisible(true)),
                () -> System.exit(0)
        );
    }

    private JPanel wrapCentered(JPanel content) {
        JPanel shell = new JPanel(new GridBagLayout());
        shell.setOpaque(false);
        shell.setBackground(MoUiTheme.PAGE_BG);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, MoUiTheme.GUTTER, 0, MoUiTheme.GUTTER);

        content.setMaximumSize(new Dimension(MoUiTheme.CONTENT_MAX_W, Integer.MAX_VALUE));
        content.setAlignmentX(Component.CENTER_ALIGNMENT);
        shell.add(content, c);
        return shell;
    }

    private JPanel buildBody() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(32, 0, 40, 0));

        JLabel title = new JLabel("MO Dashboard");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        title.setForeground(MoUiTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(title);
        root.add(Box.createVerticalStrut(10));

        JLabel sub = new JLabel("Welcome back! Please select a module to continue.");
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        sub.setForeground(MoUiTheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(sub);
        root.add(Box.createVerticalStrut(24));

        JPanel cards = new JPanel(new GridLayout(1, 2, 24, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.add(moduleCard(
                "Job Management Module",
                MoGrayIcons.book(40),
                "Manage course info, requirements, and job postings",
                new String[]{
                        "Create and update course information",
                        "Post and manage TA job openings",
                        "Maintain module requirements and descriptions"
                },
                "Go to Job Management",
                () -> MoFrameGeometry.navigateReplace(this, () -> new MyJobsFrame().setVisible(true))
        ));
        cards.add(moduleCard(
                "Application Review Module",
                MoGrayIcons.clipboard(40),
                "View TA applications, review, and check allocation results",
                new String[]{
                        "Browse applicants linked to your module jobs",
                        "Review submissions and supporting documents",
                        "Track allocation and hiring outcomes"
                },
                "Go to Application Review",
                () -> MoFrameGeometry.navigateReplace(this, () -> new ApplicationReviewPlaceholderFrame(null).setVisible(true))
        ));
        root.add(cards);
        root.add(Box.createVerticalStrut(22));

        root.add(quickOverviewPanel());
        return root;
    }

    private JPanel moduleCard(String title, javax.swing.ImageIcon icon, String intro, String[] bulletItems, String cta, Runnable onCta) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        MoUiTheme.styleRoundedCard(card, 14);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MoUiTheme.BORDER),
                new EmptyBorder(22, 24, 20, 24)
        ));

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel iconWrap = new JPanel(new BorderLayout());
        iconWrap.setBackground(MoUiTheme.ICON_BOX_BG);
        iconWrap.setBorder(BorderFactory.createLineBorder(MoUiTheme.BORDER));
        iconWrap.setPreferredSize(new Dimension(56, 56));
        JLabel ic = new JLabel(icon);
        ic.setHorizontalAlignment(SwingConstants.CENTER);
        iconWrap.add(ic, BorderLayout.CENTER);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        t.setForeground(MoUiTheme.TEXT_PRIMARY);
        JLabel introL = new JLabel("<html><div style='width:400px;line-height:1.45'>" + intro + "</div></html>");
        introL.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        introL.setForeground(MoUiTheme.TEXT_SECONDARY);
        titles.add(t);
        titles.add(Box.createVerticalStrut(8));
        titles.add(introL);

        header.add(iconWrap, BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);

        JPanel bulletPanel = new JPanel();
        bulletPanel.setLayout(new BoxLayout(bulletPanel, BoxLayout.Y_AXIS));
        bulletPanel.setOpaque(false);
        bulletPanel.setBorder(new EmptyBorder(18, 0, 0, 0));
        for (String b : bulletItems) {
            JLabel line = new JLabel("\u2022  " + b);
            line.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            line.setForeground(MoUiTheme.TEXT_PRIMARY);
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            line.setBorder(new EmptyBorder(0, 0, 6, 0));
            bulletPanel.add(line);
        }
        bulletPanel.add(Box.createVerticalGlue());

        JPanel upper = new JPanel(new BorderLayout(0, 0));
        upper.setOpaque(false);
        upper.add(header, BorderLayout.NORTH);
        upper.add(bulletPanel, BorderLayout.CENTER);

        JButton btn = new JButton(cta);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        btn.setFocusPainted(false);
        MoUiTheme.stylePrimaryButton(btn, 8);
        btn.addActionListener(e -> onCta.run());
        int h = Math.max(46, btn.getPreferredSize().height);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, h));
        btn.setMinimumSize(new Dimension(0, h));

        JPanel btnRow = new JPanel(new BorderLayout());
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(16, 0, 0, 0));
        btnRow.add(btn, BorderLayout.CENTER);

        card.add(upper, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    private JPanel quickOverviewPanel() {
        MoDashboardService.DashboardMetrics m = MoDashboardService.compute(
                jobRepository,
                applicationRepository,
                MoContext.CURRENT_MO_ID
        );

        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setOpaque(false);
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        MoUiTheme.styleRoundedCard(outer, 14);
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MoUiTheme.BORDER),
                new EmptyBorder(20, 24, 24, 24)
        ));
        JLabel h = new JLabel("Quick Overview");
        h.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        h.setForeground(MoUiTheme.TEXT_PRIMARY);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.add(h);
        outer.add(Box.createVerticalStrut(16));

        JPanel grid = new JPanel(new GridLayout(1, 3, 20, 0));
        grid.setOpaque(false);

        metricCourses = new JLabel(String.valueOf(m.activeCourses()), SwingConstants.CENTER);
        metricOpen = new JLabel(String.valueOf(m.openJobPostings()), SwingConstants.CENTER);
        metricPending = new JLabel(String.valueOf(m.pendingReviews()), SwingConstants.CENTER);

        grid.add(statBox(metricCourses, "Active Courses"));
        grid.add(statBox(metricOpen, "Open Job Postings"));
        grid.add(statBox(metricPending, "Pending Reviews"));

        outer.add(grid);
        return outer;
    }

    private JPanel statBox(JLabel valueLabel, String caption) {
        JPanel box = new JPanel(new BorderLayout(0, 8));
        box.setBackground(MoUiTheme.SURFACE);
        box.setOpaque(true);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MoUiTheme.BORDER),
                new EmptyBorder(16, 12, 16, 12)
        ));
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        valueLabel.setForeground(MoUiTheme.TEXT_PRIMARY);
        JLabel cap = new JLabel(caption, SwingConstants.CENTER);
        cap.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        cap.setForeground(MoUiTheme.TEXT_MUTED);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(cap, BorderLayout.CENTER);
        box.add(valueLabel, BorderLayout.CENTER);
        box.add(south, BorderLayout.SOUTH);
        return box;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            refreshMetrics();
        }
    }

    private void refreshMetrics() {
        MoDashboardService.DashboardMetrics m = MoDashboardService.compute(
                jobRepository,
                applicationRepository,
                MoContext.CURRENT_MO_ID
        );
        if (metricCourses != null) {
            metricCourses.setText(String.valueOf(m.activeCourses()));
            metricOpen.setText(String.valueOf(m.openJobPostings()));
            metricPending.setText(String.valueOf(m.pendingReviews()));
        }
    }
}
