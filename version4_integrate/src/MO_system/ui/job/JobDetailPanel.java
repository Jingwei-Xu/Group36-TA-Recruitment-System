package MO_system.ui.job;

import MO_system.MoContext;
import MO_system.model.job.Job;
import MO_system.model.job.JobStatusUtil;
import MO_system.repository.ApplicationRepository;
import MO_system.repository.JobRepository;
import MO_system.ui.MoShellFrame;
import MO_system.ui.MoShellHost;
import MO_system.ui.MoUiTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.Locale;
import java.util.Objects;

/**
 * Job Detail — read-only content; actions delegate persistence to repository. Embedded in {@link MoShellFrame}.
 */
public class JobDetailPanel extends JPanel {
    private static final Color STATUS_OPEN_BG = new Color(0xDCFCE7);
    private static final Color STATUS_OPEN_FG = new Color(0x166534);
    private static final Color STATUS_CLOSED_BG = new Color(0xE0E7FF);
    private static final Color STATUS_CLOSED_FG = new Color(0x3730A3);
    private static final Color STATUS_DRAFT_BG = new Color(0xFFFBEB);
    private static final Color STATUS_DRAFT_FG = new Color(0x92400E);

    private static final Color STAT_MODULE_BG = new Color(0xEFF6FF);
    private static final Color STAT_MODULE_BORDER = new Color(0xBFDBFE);
    private static final Color STAT_MODULE_VALUE = new Color(0x1D4ED8);

    private static final Color STAT_QUOTA_BG = new Color(0xF5F3FF);
    private static final Color STAT_QUOTA_BORDER = new Color(0xDDD6FE);
    private static final Color STAT_QUOTA_VALUE = new Color(0x5B21B6);

    private static final Color STAT_HOURS_BG = new Color(0xFFFBEB);
    private static final Color STAT_HOURS_BORDER = new Color(0xFDE68A);
    private static final Color STAT_HOURS_VALUE = new Color(0xB45309);

    private static final Color STAT_APPLICANTS_BG = new Color(0xECFDF5);
    private static final Color STAT_APPLICANTS_BORDER = new Color(0xA7F3D0);
    private static final Color STAT_APPLICANTS_VALUE = new Color(0x047857);

    private static final Color BODY_WELL_BG = new Color(0xF8FAFC);
    private static final Color BODY_WELL_BORDER = new Color(0xE2E8F0);
    private static final Color[] SKILL_CHIP_BGS = {
            new Color(0xEFF6FF),
            new Color(0xF5F3FF),
            new Color(0xECFDF5),
            new Color(0xFFFBEB)
    };
    private static final Color[] SKILL_CHIP_BORDERS = {
            new Color(0xBFDBFE),
            new Color(0xDDD6FE),
            new Color(0xA7F3D0),
            new Color(0xFDE68A)
    };
    private static final Color[] SKILL_CHIP_FGS = {
            new Color(0x1D4ED8),
            new Color(0x5B21B6),
            new Color(0x047857),
            new Color(0xB45309)
    };

    private final MoShellHost host;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final Runnable onDataChanged;
    private JScrollPane bodyScrollPane;
    private Job job;

    public JobDetailPanel(MoShellHost host,
                          JobRepository jobRepository,
                          Job job,
                          Runnable onDataChanged) {
        this.host = host;
        this.jobRepository = jobRepository;
        this.applicationRepository = new ApplicationRepository();
        this.job = job;
        this.onDataChanged = onDataChanged;

        setOpaque(false);
        setBackground(MoUiTheme.PAGE_BG);
        setLayout(new BorderLayout());
        bodyScrollPane = createBodyScrollPane();
        bodyScrollPane.setViewportView(buildBodyContent());
        add(bodyScrollPane, BorderLayout.CENTER);
    }

    /** Reload UI for another job (same panel instance). */
    public void setJob(Job job) {
        this.job = job;
        if (bodyScrollPane == null) {
            bodyScrollPane = createBodyScrollPane();
            add(bodyScrollPane, BorderLayout.CENTER);
        }
        bodyScrollPane.setViewportView(buildBodyContent());
        revalidate();
        repaint();
        scrollToTop();
    }

    /**
     * Whether the current panel already represents the same visible job snapshot.
     * Used to skip expensive rebuilds that can cause navigation flicker.
     */
    public boolean representsJob(Job other) {
        if (job == null || other == null) {
            return false;
        }
        if (!Objects.equals(job.getId(), other.getId())) {
            return false;
        }
        return Objects.equals(job.getTitle(), other.getTitle())
                && Objects.equals(job.getModuleCode(), other.getModuleCode())
                && Objects.equals(job.getModuleName(), other.getModuleName())
                && Objects.equals(job.getStatus(), other.getStatus())
                && job.getQuota() == other.getQuota()
                && job.getWeeklyHours() == other.getWeeklyHours()
                && job.getApplicantsCount() == other.getApplicantsCount();
    }

    private JPanel buildBodyContent() {
        // Rebuilt page body: viewport-tracking root keeps default-window left/right spacing symmetric.
        JPanel root = new ViewportFillPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(16, MoUiTheme.GUTTER, 40, MoUiTheme.GUTTER));

        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);

        column.add(buildHeroCard());
        column.add(Box.createVerticalStrut(16));
        column.add(metricRow());
        column.add(Box.createVerticalStrut(16));

        column.add(sectionCard("Job description",
                "What candidates will do in this role",
                wrapReadOnlyBody(emptyToPlaceholder(job.getDescription(), "No description provided."))));
        column.add(Box.createVerticalStrut(12));

        column.add(sectionCard("Required skills",
                "Skills used to match applicants",
                skillsBody()));
        column.add(Box.createVerticalStrut(12));

        column.add(sectionCard("Additional requirements",
                "Extra criteria or notes",
                wrapReadOnlyBody(emptyToPlaceholder(job.getAdditionalRequirements(),
                        "No additional requirements specified."))));
        column.add(Box.createVerticalStrut(14));

        column.add(applicantSection());
        root.add(column, BorderLayout.CENTER);
        return root;
    }

    private JScrollPane createBodyScrollPane() {
        JScrollPane sp = new JScrollPane();
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(MoUiTheme.PAGE_BG);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setWheelScrollingEnabled(true);
        sp.getVerticalScrollBar().setUnitIncrement(24);
        sp.getVerticalScrollBar().setBlockIncrement(96);
        return sp;
    }

    /** Scroll view that always tracks viewport width for symmetric side insets. */
    private static final class ViewportFillPanel extends JPanel implements Scrollable {
        private ViewportFillPanel(BorderLayout layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(visibleRect.height - 32, 64);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private void scrollToTop() {
        if (bodyScrollPane == null) {
            return;
        }
        bodyScrollPane.getVerticalScrollBar().setValue(0);
        bodyScrollPane.getHorizontalScrollBar().setValue(0);
        bodyScrollPane.getViewport().setViewPosition(new java.awt.Point(0, 0));
    }

    /** Same Back control as {@link CreateJobPanel#buildPageHeaderStrip()}. */
    private JPanel buildBackRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton back = new JButton("Back");
        back.setBorder(new EmptyBorder(4, 0, 4, 0));
        MoUiTheme.styleTextBackLink(back);
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> {
            onDataChanged.run();
            host.showJobList();
        });
        row.add(back, BorderLayout.WEST);
        return row;
    }

    private JPanel buildHeroCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(18, 22, 20, 22)
        ));
        card.putClientProperty("JComponent.style", "arc: 12");
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        card.add(buildBackRow(), BorderLayout.NORTH);
        card.add(buildHero(), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildHero() {
        JPanel hero = new JPanel(new BorderLayout(0, 0));
        hero.setOpaque(false);
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        String displayTitle = emptyToPlaceholder(job.getTitle(), "(Untitled job)");
        JLabel title = new JLabel("<html><div style='width:560px;line-height:1.15;'>"
                + escapeHtml(displayTitle) + "</div></html>");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
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
        String st = JobStatusUtil.display(job.getStatus());
        JPanel titleBand = new JPanel(new BorderLayout(14, 0));
        titleBand.setOpaque(false);
        titleBand.add(left, BorderLayout.CENTER);
        titleBand.add(buildHeroActionsRow(st), BorderLayout.EAST);

        hero.add(titleBand, BorderLayout.CENTER);
        return hero;
    }

    /** Status + Edit / Close / Delete: one row, even spacing, all outline buttons. */
    private JPanel buildHeroActionsRow(String normalizedStatus) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        JButton edit = new JButton("Edit");
        MoUiTheme.styleAccentOutlineButton(edit, 10);
        styleHeroActionButton(edit, 86);
        edit.addActionListener(e -> host.showEditJob(job));

        JButton close = new JButton("Close job");
        MoUiTheme.styleOutlineButton(close, 10);
        styleHeroActionButton(close, 102);
        close.setForeground(MoUiTheme.TEXT_SECONDARY);
        close.addActionListener(e -> {
            job.setStatus("Closed");
            persistSingleJob();
            onDataChanged.run();
            host.showJobList();
        });

        JButton del = new JButton("Delete");
        MoUiTheme.styleDangerOutlineButton(del, 10);
        styleHeroActionButton(del, 90);
        del.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(host.getShellFrame(),
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
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(20, 22, 22, 22)
        ));
        card.putClientProperty("JComponent.style", "arc: 12");
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, MoUiTheme.ACCENT_PRIMARY),
                new EmptyBorder(0, 14, 0, 0)
        ));
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

        int totalPosting = Math.max(applicationRepository.countApplicationsForJob(job.getId()), job.getApplicantsCount());
        int yours = applicationRepository.countApplicationsForJob(job.getId(), MoContext.getCurrentMoUserId());
        JLabel line = new JLabel(String.valueOf(yours));
        line.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        line.setForeground(MoUiTheme.TEXT_PRIMARY);
        JLabel suffix = new JLabel(" assigned to you");
        suffix.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        suffix.setForeground(MoUiTheme.TEXT_SECONDARY);
        JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        countRow.setOpaque(false);
        countRow.add(line);
        countRow.add(suffix);
        if (totalPosting != yours) {
            JLabel totalNote = new JLabel("  ·  " + totalPosting + " total for this posting");
            totalNote.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            totalNote.setForeground(MoUiTheme.TEXT_SECONDARY);
            countRow.add(totalNote);
        }

        JPanel south = new JPanel(new BorderLayout(0, 14));
        south.setOpaque(false);
        JButton go = MoUiTheme.portalGradientPrimary("View applicants",
                new Font(Font.SANS_SERIF, Font.BOLD, 13));
        go.addActionListener(e -> host.showJobApplicantsPlaceholder(job));
        south.add(countRow, BorderLayout.NORTH);
        south.add(go, BorderLayout.WEST);

        card.add(top, BorderLayout.NORTH);
        card.add(south, BorderLayout.CENTER);
        return card;
    }

    private JPanel metricRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));
        int yours = applicationRepository.countApplicationsForJob(job.getId(), MoContext.getCurrentMoUserId());

        row.add(statCard("Module", job.getModuleCode() + " — " + job.getModuleName(),
                STAT_MODULE_BG, STAT_MODULE_BORDER, STAT_MODULE_VALUE));
        row.add(statCard("Quota", String.valueOf(job.getQuota()),
                STAT_QUOTA_BG, STAT_QUOTA_BORDER, STAT_QUOTA_VALUE));
        row.add(statCard("Hours / week", job.getWeeklyHours() + "h",
                STAT_HOURS_BG, STAT_HOURS_BORDER, STAT_HOURS_VALUE));
        row.add(statCard("Your applicants", String.valueOf(yours),
                STAT_APPLICANTS_BG, STAT_APPLICANTS_BORDER, STAT_APPLICANTS_VALUE));
        return row;
    }

    private JPanel statCard(String label, String value, Color bg, Color borderColor, Color valueColor) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(true);
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                new EmptyBorder(16, 14, 18, 14)
        ));
        p.putClientProperty("JComponent.style", "arc: 12");

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel a = new JLabel(label.toUpperCase(Locale.ENGLISH), SwingConstants.CENTER);
        a.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        a.setForeground(new Color(0x1F2937));
        a.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel b = new JLabel("<html><div style='width:168px;text-align:center;line-height:1.35'>"
                + escapeHtml(value) + "</div></html>", SwingConstants.CENTER);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        b.setForeground(valueColor);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(Box.createVerticalGlue());
        inner.add(a);
        inner.add(Box.createVerticalStrut(8));
        inner.add(b);
        inner.add(Box.createVerticalGlue());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        p.add(inner, gbc);
        return p;
    }

    /** Status pill aligned on the same baseline as action buttons. */
    private JPanel statusPillHero(String normalized) {
        JLabel pill = new JLabel(normalized);
        pill.setOpaque(true);
        pill.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        pill.setBorder(new EmptyBorder(6, 12, 6, 12));
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
        pill.putClientProperty("JComponent.style", "arc: 999");
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(pill, BorderLayout.CENTER);
        return wrap;
    }

    private static void styleHeroActionButton(JButton button, int minWidth) {
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        button.setFocusPainted(false);
        Dimension pref = button.getPreferredSize();
        int w = Math.max(minWidth, pref.width + 10);
        int h = Math.max(34, pref.height + 4);
        button.setPreferredSize(new Dimension(w, h));
    }

    /** Called by shell whenever this page is shown. */
    public void ensureScrollTop() {
        scrollToTop();
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
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(16, 18, 18, 18)
        ));
        card.putClientProperty("JComponent.style", "arc: 12");

        JPanel head = new JPanel(new BorderLayout(0, 0));
        head.setOpaque(false);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, MoUiTheme.ACCENT_PRIMARY),
                new EmptyBorder(0, 14, 0, 0)
        ));
        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        JLabel h = new JLabel(title);
        h.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        h.setForeground(MoUiTheme.TEXT_PRIMARY);
        JLabel s = new JLabel(subtitle);
        s.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        s.setForeground(MoUiTheme.TEXT_SECONDARY);
        titles.add(h);
        titles.add(Box.createVerticalStrut(4));
        titles.add(s);
        head.add(titles, BorderLayout.CENTER);

        card.add(head, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel wrapReadOnlyBody(String text) {
        JTextPane a = readOnlyArea(text);
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(MoUiTheme.CONTENT_MAX_W - 80, Integer.MAX_VALUE));
        p.add(a, BorderLayout.CENTER);
        return p;
    }

    private JPanel skillsBody() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        flow.setOpaque(false);
        if (job.getRequiredSkills() == null || job.getRequiredSkills().isEmpty()) {
            JLabel empty = new JLabel("No skills listed for this job.");
            empty.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            empty.setForeground(MoUiTheme.TEXT_MUTED);
            flow.add(empty);
        } else {
            int chipIdx = 0;
            for (String s : job.getRequiredSkills()) {
                JLabel chip = new JLabel(s);
                styleSkillChip(chip, chipIdx++);
                chip.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
                flow.add(chip);
            }
        }
        outer.add(flow, BorderLayout.NORTH);
        return outer;
    }

    private static void styleSkillChip(JLabel chip, int index) {
        int i = Math.floorMod(index, SKILL_CHIP_BGS.length);
        chip.setOpaque(true);
        chip.setBackground(SKILL_CHIP_BGS[i]);
        chip.setForeground(SKILL_CHIP_FGS[i]);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SKILL_CHIP_BORDERS[i]),
                new EmptyBorder(7, 14, 7, 14)
        ));
        chip.putClientProperty("JComponent.style", "arc: 999");
    }

    /** Read-only body: centered text in a tinted well (matches TA Allocation polish). */
    private JTextPane readOnlyArea(String text) {
        JTextPane a = new JTextPane();
        a.setEditable(false);
        a.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        a.setForeground(new Color(0x1F2937));
        a.setBackground(BODY_WELL_BG);
        a.setCaretColor(a.getForeground());
        a.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BODY_WELL_BORDER),
                new EmptyBorder(16, 18, 18, 18)
        ));
        String t = text == null ? "" : text;
        a.setText(t);
        applyCenterParagraphs(a);
        a.setCaretPosition(0);
        a.setFocusable(false);
        return a;
    }

    private static void applyCenterParagraphs(JTextPane pane) {
        StyledDocument doc = pane.getStyledDocument();
        int len = doc.getLength();
        if (len <= 0) {
            return;
        }
        SimpleAttributeSet sa = new SimpleAttributeSet();
        StyleConstants.setAlignment(sa, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, len, sa, false);
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
        java.util.List<Job> mo = jobRepository.loadJobsForMo(MO_system.MoContext.getCurrentMoUserId());
        java.util.ArrayList<Job> next = new java.util.ArrayList<>(mo);
        for (int i = 0; i < next.size(); i++) {
            if (job.getId().equals(next.get(i).getId())) {
                next.set(i, job);
                break;
            }
        }
        jobRepository.saveJobsForMo(MO_system.MoContext.getCurrentMoUserId(), next);
    }

    private void deleteJob() {
        java.util.List<Job> mo = new java.util.ArrayList<>(jobRepository.loadJobsForMo(MO_system.MoContext.getCurrentMoUserId()));
        mo.removeIf(j -> job.getId().equals(j.getId()));
        jobRepository.saveJobsForMo(MO_system.MoContext.getCurrentMoUserId(), mo);
        onDataChanged.run();
        host.showJobList();
    }

}
