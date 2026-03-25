package com.mojobsystem.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.mojobsystem.model.Job;
import com.mojobsystem.repository.ApplicationRepository;
import com.mojobsystem.repository.JobRepository;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Locale;

/**
 * Job Detail — read-only content; actions delegate persistence to repository.
 */
public class JobDetailFrame extends JFrame {
    private static final Color CARD_BORDER = new Color(0xE5E7EB);
    private static final Color STATUS_OPEN_BG = new Color(0xDCFCE7);
    private static final Color STATUS_OPEN_FG = new Color(0x166534);
    private static final Color STATUS_CLOSED_BG = new Color(0xE0E7FF);
    private static final Color STATUS_CLOSED_FG = new Color(0x3730A3);
    private static final Color STATUS_DRAFT_BG = new Color(0xFFFBEB);
    private static final Color STATUS_DRAFT_FG = new Color(0x92400E);
    private static final Color CHIP_BG = new Color(0xF3F4F6);
    private static final Color CHIP_FG = new Color(0x374151);

    private final MyJobsFrame moJobsFrame;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final Runnable onDataChanged;
    private Job job;

    public JobDetailFrame(MyJobsFrame moJobsFrame,
                          JobRepository jobRepository,
                          Job job,
                          Runnable onDataChanged) {
        this.moJobsFrame = moJobsFrame;
        this.jobRepository = jobRepository;
        this.applicationRepository = new ApplicationRepository();
        this.job = job;
        this.onDataChanged = onDataChanged;

        setTitle("MO System - Job Detail");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        MoFrameGeometry.applyMatching(moJobsFrame, this);
        getContentPane().setBackground(MoUiTheme.PAGE_BG);
        setLayout(new BorderLayout());

        add(NavigationPanel.create(NavigationPanel.Tab.JOB_MANAGEMENT, navActions()), BorderLayout.NORTH);
        add(buildScrollBody(), BorderLayout.CENTER);
    }

    private NavigationPanel.Actions navActions() {
        return new NavigationPanel.Actions(
                () -> MoFrameGeometry.navigateReplace(this, () -> new MoDashboardFrame().setVisible(true)),
                () -> MoFrameGeometry.navigateReplace(this, () -> new MyJobsFrame().setVisible(true)),
                () -> MoFrameGeometry.navigateReplace(this, () -> new ApplicationReviewPlaceholderFrame(moJobsFrame, job.getId()).setVisible(true)),
                () -> System.exit(0)
        );
    }

    private JScrollPane buildScrollBody() {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(16, 0, 40, 0));
        inner.setMaximumSize(new Dimension(MoUiTheme.CONTENT_MAX_W, Integer.MAX_VALUE));

        inner.add(buildHeroCard());
        inner.add(Box.createVerticalStrut(16));
        inner.add(metricRow());
        inner.add(Box.createVerticalStrut(16));

        inner.add(sectionCard("Job description",
                "What candidates will do in this role",
                wrapReadOnlyBody(emptyToPlaceholder(job.getDescription(), "No description provided."))));
        inner.add(Box.createVerticalStrut(12));

        inner.add(sectionCard("Required skills",
                "Skills used to match applicants",
                skillsBody()));
        inner.add(Box.createVerticalStrut(12));

        inner.add(sectionCard("Additional requirements",
                "Extra criteria or notes",
                wrapReadOnlyBody(emptyToPlaceholder(job.getAdditionalRequirements(),
                        "No additional requirements specified."))));
        inner.add(Box.createVerticalStrut(14));

        inner.add(applicantSection());

        JPanel shell = new JPanel(new GridBagLayout());
        shell.setOpaque(false);
        shell.setBackground(MoUiTheme.PAGE_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, MoUiTheme.GUTTER, 0, MoUiTheme.GUTTER);
        shell.add(inner, gbc);

        JScrollPane sp = new JScrollPane(shell);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(MoUiTheme.PAGE_BG);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    /** Same Back control as {@link CreateJobFrame#buildPageHeaderStrip()}. */
    private JPanel buildBackRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton back = new JButton("Back");
        back.setFocusPainted(false);
        back.setContentAreaFilled(false);
        back.setBorder(new EmptyBorder(4, 0, 4, 0));
        back.setForeground(MoUiTheme.TEXT_SECONDARY);
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> {
            dispose();
            moJobsFrame.reloadJobsFromRepository();
            moJobsFrame.setVisible(true);
        });
        row.add(back, BorderLayout.WEST);
        return row;
    }

    private JPanel buildHeroCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE8EAEF)),
                new EmptyBorder(18, 22, 20, 22)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        card.add(buildBackRow(), BorderLayout.NORTH);
        card.add(buildHero(), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildHero() {
        JPanel hero = new JPanel(new BorderLayout(16, 0));
        hero.setOpaque(false);
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel(job.getTitle());
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setForeground(MoUiTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        String moduleLine = job.getModuleCode() + " · " + job.getModuleName();
        JLabel sub = new JLabel(moduleLine);
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        sub.setForeground(MoUiTheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(title);
        left.add(Box.createVerticalStrut(6));
        left.add(sub);

        JPanel titleBand = new JPanel(new BorderLayout(20, 0));
        titleBand.setOpaque(false);
        titleBand.add(left, BorderLayout.CENTER);
        String st = normalizeStatus(job.getStatus());
        titleBand.add(buildHeroActionsRow(st), BorderLayout.EAST);

        hero.add(titleBand, BorderLayout.CENTER);
        return hero;
    }

    /** Status + Edit / Close / Delete: one row, even spacing, all outline buttons. */
    private JPanel buildHeroActionsRow(String normalizedStatus) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        JButton edit = new JButton("Edit");
        MoUiTheme.styleAccentOutlineButton(edit, 10);
        edit.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        edit.setFocusPainted(false);
        edit.addActionListener(e -> {
            dispose();
            new CreateJobFrame(moJobsFrame, jobRepository, job).setVisible(true);
        });

        JButton close = new JButton("Close job");
        MoUiTheme.styleOutlineButton(close, 10);
        close.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        close.setForeground(new Color(0x4B5563));
        close.setFocusPainted(false);
        close.addActionListener(e -> {
            job.setStatus("Closed");
            persistSingleJob();
            onDataChanged.run();
            dispose();
            moJobsFrame.setVisible(true);
        });

        JButton del = new JButton("Delete");
        MoUiTheme.styleDangerOutlineButton(del, 10);
        del.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        del.setFocusPainted(false);
        del.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this,
                    "Permanently delete this job?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                deleteJob();
            }
        });

        row.add(statusPillHero(normalizedStatus));
        row.add(edit);
        row.add(close);
        row.add(del);
        return row;
    }

    private JPanel applicantSection() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(20, 22, 22, 22)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        JLabel h = new JLabel("Applicant management");
        h.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        h.setForeground(MoUiTheme.TEXT_PRIMARY);
        JLabel hint = new JLabel("Review submissions for this posting");
        hint.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        hint.setForeground(MoUiTheme.TEXT_SECONDARY);
        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.add(h);
        titles.add(hint);
        top.add(titles, BorderLayout.WEST);

        int fromApps = applicationRepository.countApplicationsForJob(job.getId());
        int shown = Math.max(fromApps, job.getApplicantsCount());
        JLabel line = new JLabel(String.valueOf(shown));
        line.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        line.setForeground(MoUiTheme.TEXT_PRIMARY);
        JLabel suffix = new JLabel(" applications");
        suffix.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        suffix.setForeground(MoUiTheme.TEXT_SECONDARY);
        JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        countRow.setOpaque(false);
        countRow.add(line);
        countRow.add(suffix);

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.setOpaque(false);
        south.add(countRow);
        south.add(Box.createVerticalStrut(14));
        JButton go = new JButton("View applicants");
        MoUiTheme.styleAccentPrimaryButton(go, 10);
        go.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        go.setFocusPainted(false);
        go.setAlignmentX(Component.LEFT_ALIGNMENT);
        go.addActionListener(e -> {
            dispose();
            new ApplicationReviewPlaceholderFrame(moJobsFrame, job.getId()).setVisible(true);
        });
        south.add(go);

        card.add(top, BorderLayout.NORTH);
        card.add(south, BorderLayout.CENTER);
        return card;
    }

    private JPanel metricRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        row.add(statCard("Module", job.getModuleCode() + " — " + job.getModuleName()));
        row.add(statCard("Quota", String.valueOf(job.getQuota())));
        row.add(statCard("Hours / week", job.getWeeklyHours() + "h"));
        int ac = Math.max(applicationRepository.countApplicationsForJob(job.getId()), job.getApplicantsCount());
        row.add(statCard("Applicants", String.valueOf(ac)));
        return row;
    }

    private JPanel statCard(String label, String value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(true);
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(12, 14, 14, 14)
        ));
        p.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JLabel a = new JLabel(label.toUpperCase(Locale.ENGLISH));
        a.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        a.setForeground(MoUiTheme.TEXT_SECONDARY);

        JLabel b = new JLabel("<html><div style='width:170px;line-height:1.35'>" + escapeHtml(value) + "</div></html>");
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        b.setForeground(MoUiTheme.TEXT_PRIMARY);

        p.add(a);
        p.add(Box.createVerticalStrut(6));
        p.add(b);
        return p;
    }

    /**
     * Status column aligned with the action buttons (hint + pill, centered).
     */
    private JPanel statusPillHero(String normalized) {
        JLabel pill = new JLabel(normalized);
        pill.setOpaque(true);
        pill.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        pill.setBorder(new EmptyBorder(6, 14, 6, 14));
        if ("Open".equalsIgnoreCase(normalized)) {
            pill.setBackground(STATUS_OPEN_BG);
            pill.setForeground(STATUS_OPEN_FG);
        } else if ("Closed".equalsIgnoreCase(normalized)) {
            pill.setBackground(STATUS_CLOSED_BG);
            pill.setForeground(STATUS_CLOSED_FG);
        } else {
            pill.setBackground(STATUS_DRAFT_BG);
            pill.setForeground(STATUS_DRAFT_FG);
        }
        pill.putClientProperty(FlatClientProperties.STYLE, "arc: 999");

        JLabel hint = new JLabel("Status");
        hint.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        hint.setForeground(MoUiTheme.TEXT_SECONDARY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        pill.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        stack.add(hint);
        stack.add(Box.createVerticalStrut(3));
        stack.add(pill);
        return stack;
    }

    private JPanel sectionCard(String title, String subtitle, Component body) {
        JPanel wrap = new JPanel(new BorderLayout(0, 0));
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(16, 18, 18, 18)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setOpaque(false);
        JLabel h = new JLabel(title);
        h.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        h.setForeground(MoUiTheme.TEXT_PRIMARY);
        JLabel s = new JLabel(subtitle);
        s.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        s.setForeground(MoUiTheme.TEXT_SECONDARY);
        head.add(h);
        head.add(Box.createVerticalStrut(4));
        head.add(s);

        card.add(head, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel wrapReadOnlyBody(String text) {
        JTextArea a = readOnlyArea(text);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(a, BorderLayout.CENTER);
        return p;
    }

    private JPanel skillsBody() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        flow.setOpaque(false);
        if (job.getRequiredSkills() == null || job.getRequiredSkills().isEmpty()) {
            JLabel empty = new JLabel("No skills listed for this job.");
            empty.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            empty.setForeground(MoUiTheme.TEXT_MUTED);
            flow.add(empty);
        } else {
            for (String s : job.getRequiredSkills()) {
                JLabel chip = new JLabel(s);
                chip.setOpaque(true);
                chip.setBackground(CHIP_BG);
                chip.setForeground(CHIP_FG);
                chip.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                chip.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                        new EmptyBorder(6, 12, 6, 12)
                ));
                chip.putClientProperty(FlatClientProperties.STYLE, "arc: 999");
                flow.add(chip);
            }
        }
        outer.add(flow, BorderLayout.NORTH);
        return outer;
    }

    private JTextArea readOnlyArea(String text) {
        JTextArea a = new JTextArea(text == null ? "" : text);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        a.setForeground(new Color(0x1F2937));
        a.setBackground(Color.WHITE);
        a.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(12, 14, 14, 14)
        ));
        // Avoid forcing an oversized preferred width, which can trigger horizontal scrolling.
        a.setColumns(1);
        return a;
    }

    private static String emptyToPlaceholder(String raw, String placeholder) {
        if (raw == null || raw.isBlank()) {
            return placeholder;
        }
        return raw;
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void persistSingleJob() {
        java.util.List<Job> mo = jobRepository.loadJobsForMo(com.mojobsystem.MoContext.CURRENT_MO_ID);
        java.util.ArrayList<Job> next = new java.util.ArrayList<>(mo);
        for (int i = 0; i < next.size(); i++) {
            if (job.getId().equals(next.get(i).getId())) {
                next.set(i, job);
                break;
            }
        }
        jobRepository.saveJobsForMo(com.mojobsystem.MoContext.CURRENT_MO_ID, next);
    }

    private void deleteJob() {
        java.util.List<Job> mo = new java.util.ArrayList<>(jobRepository.loadJobsForMo(com.mojobsystem.MoContext.CURRENT_MO_ID));
        mo.removeIf(j -> job.getId().equals(j.getId()));
        jobRepository.saveJobsForMo(com.mojobsystem.MoContext.CURRENT_MO_ID, mo);
        onDataChanged.run();
        dispose();
        moJobsFrame.reloadJobsFromRepository();
        moJobsFrame.setVisible(true);
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "Open";
        }
        String lower = status.trim().toLowerCase(Locale.ENGLISH);
        if ("closed".equals(lower)) {
            return "Closed";
        }
        if ("draft".equals(lower)) {
            return "Draft";
        }
        return "Open";
    }
}
