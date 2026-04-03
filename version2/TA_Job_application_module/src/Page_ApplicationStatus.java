

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;



/**
 * 申请状态详情：卡片分区 + 纵向时间线，风格与职位详情页一致
 */
public class Page_ApplicationStatus {

    private static final int CARD_PAD = 20;
    private static final int CARD_GAP = 14;
    private static final Color TIMELINE_LINE = new Color(229, 231, 235);
    private static final Color TIMELINE_LINE_RED = new Color(252, 165, 165);
    private static final Color TIMELINE_LINE_BLUE = new Color(147, 197, 253);
    private static final Color TIMELINE_LINE_YELLOW = new Color(253, 230, 138);

    public interface StatusCallback {
        void onBackToApplications();
    }

    private JPanel panel;
    private StatusCallback callback;

    public Page_ApplicationStatus(StatusCallback callback) {
        this.callback = callback;
        initPanel();
    }

    public JPanel getPanel() {
        return panel;
    }

    public void showApplication(Application app) {
        panel.removeAll();
        buildContent(app);
        panel.revalidate();
        panel.repaint();
    }

    private void initPanel() {
        panel = UI_Helper.createPagePanel();
    }

    private void buildContent(Application app) {
        JButton backBtn = new JButton("\u2190 Back to My Applications");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setForeground(UI_Constants.TEXT_SECONDARY);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorder(new EmptyBorder(0, 0, 8, 0));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> callback.onBackToApplications());
        panel.add(backBtn);

        JLabel pageTitle = new JLabel("Application Status");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pageTitle.setForeground(UI_Constants.TEXT_PRIMARY);
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pageTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(pageTitle);

        panel.add(buildSummaryCard(app));
        panel.add(Box.createVerticalStrut(CARD_GAP));
        panel.add(buildTimelineCard(app));
        panel.add(Box.createVerticalStrut(CARD_GAP));
        panel.add(buildStatusInfoCard(app));
        panel.add(Box.createVerticalStrut(CARD_GAP));
        panel.add(buildTextCard("Reviewer Notes", defaultReviewerNotes(app), colorKey));
        panel.add(Box.createVerticalStrut(CARD_GAP));
        panel.add(buildTextCard("Next Steps", defaultNextSteps(app), colorKey));
    }

    private String colorKey(Application app) {
        return resolveStatusColorKey(app);
    }

    private JPanel buildSummaryCard(Application app) {
        Application.JobSnapshot job = app.getJobSnapshot();
        Application.ApplicantSnapshot who = app.getApplicantSnapshot();
        Application.Meta meta = app.getMeta();

        String titleText = job != null && job.getTitle() != null ? job.getTitle() : "Application";
        String courseDept = "";
        if (job != null) {
            String cc = job.getCourseCode() != null ? job.getCourseCode() : "";
            String dept = job.getDepartment() != null ? job.getDepartment() : "";
            courseDept = cc + "  \u2022  " + dept;
        }
        String applicant = who != null && who.getFullName() != null ? who.getFullName() : "";
        String applied = "";
        if (meta != null && meta.getSubmittedAt() != null) {
            applied = "Applied " + formatDateTime(meta.getSubmittedAt());
        }

        JPanel card = createCardShell();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UI_Constants.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);

        card.add(Box.createVerticalStrut(10));
        String metaLine = courseDept + "  \u2022  " + applicant + "  \u2022  " + applied;
        JLabel metaLbl = new JLabel("<html><body style='width:720px'>" + escapeHtml(metaLine) + "</body></html>");
        metaLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        metaLbl.setForeground(UI_Constants.TEXT_SECONDARY);
        metaLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(metaLbl);

        card.add(Box.createVerticalStrut(16));
        card.add(buildCurrentStatusBar(app));

        return card;
    }

    private JPanel buildCurrentStatusBar(Application app) {
        Application.Status st = app.getStatus();
        String label = st != null && st.getLabel() != null ? st.getLabel() : "Unknown";
        String colorKey = resolveStatusColorKey(app);

        Color bg;
        Color fg;
        Color border;
        switch (colorKey) {
            case "green" -> {
                bg = new Color(209, 250, 229);
                fg = new Color(5, 122, 85);
                border = new Color(167, 243, 208);
            }
            case "red" -> {
                bg = new Color(254, 226, 226);
                fg = new Color(185, 28, 28);
                border = new Color(252, 165, 165);
            }
            case "blue" -> {
                bg = new Color(219, 234, 254);
                fg = new Color(29, 78, 216);
                border = new Color(147, 197, 253);
            }
            default -> {
                bg = new Color(254, 252, 232);
                fg = new Color(161, 98, 7);
                border = new Color(253, 230, 138);
            }
        }

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(true);
        bar.setBackground(bg);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border, 1),
            new EmptyBorder(14, 16, 14, 16)
        ));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JLabel text = new JLabel("Current Status: " + label);
        text.setFont(new Font("Segoe UI", Font.BOLD, 15));
        text.setForeground(fg);
        bar.add(text, BorderLayout.WEST);

        return bar;
    }

    private String resolveStatusColorKey(Application app) {
        Application.Status st = app.getStatus();
        if (st == null) {
            return "yellow";
        }
        String c = st.getColor();
        if (c != null && !c.isEmpty()) {
            return c;
        }
        String cur = st.getCurrent();
        if (cur == null) {
            return "yellow";
        }
        return switch (cur.toLowerCase()) {
            case "accepted" -> "green";
            case "rejected" -> "red";
            case "under_review", "pending" -> "blue";
            default -> "yellow";
        };
    }

    private JPanel buildTimelineCard(Application app) {
        String ck = resolveStatusColorKey(app);
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel h = sectionHeading("Application Progress");
        inner.add(h);
        inner.add(Box.createVerticalStrut(16));

        List<Application.TimelineEvent> events = orderedTimeline(app);
        if (events.isEmpty()) {
            JLabel empty = new JLabel("No timeline events yet.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(UI_Constants.TEXT_SECONDARY);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            inner.add(empty);
        } else {
            for (int i = 0; i < events.size(); i++) {
                inner.add(buildTimelineRow(events.get(i), i < events.size() - 1, ck));
                if (i < events.size() - 1) {
                    inner.add(Box.createVerticalStrut(4));
                }
            }
        }

        JPanel card = wrapInCard(inner);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private List<Application.TimelineEvent> orderedTimeline(Application app) {
        List<Application.TimelineEvent> raw = app.getTimeline();
        List<Application.TimelineEvent> list = new ArrayList<>();
        if (raw != null) {
            list.addAll(raw);
        }
        list.sort(Comparator.comparing(e -> e.getTimestamp() != null ? e.getTimestamp() : ""));
        return list;
    }

    private JPanel buildTimelineRow(Application.TimelineEvent ev, boolean drawLineBelow) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel rail = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                int r = 7;
                boolean done = ev.getStatus() == null || "completed".equalsIgnoreCase(ev.getStatus());
                g2.setColor(done ? UI_Constants.SUCCESS_COLOR : UI_Constants.BORDER_COLOR);
                g2.fillOval(cx - r, 6, r * 2, r * 2);
                if (done) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx - 3, 13, cx - 1, 16);
                    g2.drawLine(cx - 1, 16, cx + 4, 10);
                }
                if (drawLineBelow) {
                    g2.setColor(TIMELINE_LINE);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(cx, 6 + r * 2 + 2, cx, getHeight() - 2);
                }
                g2.dispose();
            }
        };
        rail.setPreferredSize(new Dimension(36, 0));
        rail.setOpaque(false);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        String stepTitle = ev.getStepLabel() != null && !ev.getStepLabel().isEmpty()
            ? ev.getStepLabel()
            : ev.getStepKey();
        JLabel title = new JLabel(stepTitle);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(UI_Constants.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        textCol.add(title);

        String ts = formatDateTime(ev.getTimestamp());
        if (ts != null && !ts.isEmpty()) {
            textCol.add(Box.createVerticalStrut(4));
            JLabel time = new JLabel(ts);
            time.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            time.setForeground(UI_Constants.TEXT_SECONDARY);
            time.setAlignmentX(Component.LEFT_ALIGNMENT);
            textCol.add(time);
        }

        row.add(rail, BorderLayout.WEST);
        row.add(textCol, BorderLayout.CENTER);
        return row;
    }

    private JPanel buildStatusInfoCard(Application app) {
        Application.Review review = app.getReview();
        String msg = review != null && review.getStatusMessage() != null && !review.getStatusMessage().isEmpty()
            ? review.getStatusMessage()
            : "Your application is being processed.";

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.add(sectionHeading("Status Information"));
        inner.add(Box.createVerticalStrut(12));

        JPanel box = new JPanel();
        box.setLayout(new BorderLayout());
        box.setOpaque(true);
        box.setBackground(STATUS_INFO_BG);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(STATUS_INFO_BORDER, 1),
            new EmptyBorder(16, 16, 16, 16)
        ));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea ta = new JTextArea(msg);
        ta.setEditable(false);
        ta.setOpaque(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ta.setForeground(STATUS_INFO_TEXT);
        ta.setBorder(BorderFactory.createEmptyBorder());
        box.add(ta, BorderLayout.CENTER);

        inner.add(box);

        JPanel card = wrapInCard(inner);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private JPanel buildTextCard(String heading, String body) {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.add(sectionHeading(heading));
        inner.add(Box.createVerticalStrut(10));

        JTextArea ta = new JTextArea(body == null ? "" : body);
        ta.setEditable(false);
        ta.setOpaque(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ta.setForeground(UI_Constants.TEXT_PRIMARY);
        ta.setBorder(BorderFactory.createEmptyBorder());
        ta.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(ta);

        JPanel card = wrapInCard(inner);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private String defaultReviewerNotes(Application app) {
        Application.Review r = app.getReview();
        if (r != null && r.getReviewerNotes() != null && !r.getReviewerNotes().isEmpty()) {
            return r.getReviewerNotes();
        }
        return "No reviewer notes yet.";
    }

    private String defaultNextSteps(Application app) {
        Application.Review r = app.getReview();
        if (r != null && r.getNextSteps() != null && !r.getNextSteps().isEmpty()) {
            return r.getNextSteps();
        }
        return "Please wait for further updates from the hiring team.";
    }

    private JLabel sectionHeading(String text) {
        JLabel h = new JLabel(text);
        h.setFont(new Font("Segoe UI", Font.BOLD, 16));
        h.setForeground(UI_Constants.TEXT_PRIMARY);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        return h;
    }

    private JPanel createCardShell() {
        JPanel c = new JPanel();
        c.setBackground(UI_Constants.CARD_BG);
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR, 1),
            new EmptyBorder(CARD_PAD, CARD_PAD, CARD_PAD, CARD_PAD)
        ));
        return c;
    }

    private JPanel wrapInCard(JComponent inner) {
        JPanel shell = createCardShell();
        shell.setLayout(new BorderLayout());
        shell.add(inner, BorderLayout.CENTER);
        return shell;
    }

    private String formatDateTime(String iso) {
        if (iso == null || iso.length() < 10) {
            return iso != null ? iso : "";
        }
        String ymd = iso.substring(0, 10);
        String nice = formatDateYmd(ymd);
        if (iso.length() >= 16) {
            String hm = iso.substring(11, 16);
            return nice + " at " + hm;
        }
        return nice;
    }

    private String formatDateYmd(String ymd) {
        String[] p = ymd.split("-");
        if (p.length != 3) {
            return ymd;
        }
        String[] months = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
        try {
            int m = Integer.parseInt(p[1]);
            int d = Integer.parseInt(p[2]);
            if (m >= 1 && m <= 12) {
                return months[m - 1] + " " + d + ", " + p[0];
            }
        } catch (NumberFormatException ignored) { }
        return ymd;
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
