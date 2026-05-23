package Admin_Module.com.taapp.ui.pages;

import Admin_Module.com.taapp.data.DataStore;
import Admin_Module.com.taapp.model.Statistics;
import Admin_Module.com.taapp.service.AIWorkloadAnalysisService;
import Admin_Module.com.taapp.ui.AppLayout;
import Admin_Module.com.taapp.ui.Dialogs;
import Admin_Module.com.taapp.ui.UI;

import TA_Job_Application_Module.pages.jobs.JobsPortalUi;
import Admin_Module.com.taapp.ui.components.Card;
import Admin_Module.com.taapp.ui.components.Page;
import Admin_Module.com.taapp.ui.components.RoundedActionButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * AI Analysis — spacing and CTAs aligned with MO console (gutters, black primary, outline secondary).
 */
public class AIAnalysisPanel extends Page {
    private static final DateTimeFormatter EXPORT_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.ROOT);
    private static final int NOTE_WRAP_PX = 220;

    private final JLabel statusLabel = new JLabel("Ready to Run AI Analysis");
    private final JProgressBar progress = new JProgressBar();
    private final RoundedActionButton run = new RoundedActionButton("Run Analysis", RoundedActionButton.Scheme.PRIMARY_BLACK);
    private final JButton exportPdf = new JButton("Export Report");
    private final JButton exportExcel = new JButton("Export Data (CSV)");

    private final JLabel healthValue = new JLabel("--/100");
    private final JLabel workloadValue = new JLabel("--/100");
    private final JLabel utilizationValue = new JLabel("--%");
    private final JLabel healthNote = new JLabel("Run analysis to see system health");
    private final JLabel workloadNote = new JLabel("Run analysis to evaluate workload balance");
    private final JLabel utilizationNote = new JLabel("Run analysis to inspect utilization");
    private final JLabel recommendationLabel = new JLabel("Recommendation: Run analysis first.");
    private final JLabel sourceLabel = new JLabel("Source: -");

    private final AIWorkloadAnalysisService analysisService = new AIWorkloadAnalysisService();
    private String healthNoteRaw = "Run analysis to see system health";
    private String workloadNoteRaw = "Run analysis to evaluate workload balance";
    private String utilizationNoteRaw = "Run analysis to inspect utilization";
    private String recommendationRaw = "Run analysis first.";

    public AIAnalysisPanel() {
        super();

        Statistics stats = DataStore.defaultStore().getStatistics();

        run.setFont(UI.moFontBold(13));
        run.setPreferredSize(new Dimension(200, 42));

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(AppLayout.PAGE_INSET_TOP, 0, AppLayout.PAGE_INSET_BOTTOM, 0));

        JPanel headerCard = new JPanel(new BorderLayout(0, 6));
        headerCard.setOpaque(true);
        headerCard.setBackground(Color.WHITE);
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JobsPortalUi.LIGHT_PURPLE_BORDER),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        JLabel h1 = new JLabel("AI Analysis & Insights");
        h1.setFont(UI.moFontBold(26));
        h1.setForeground(UI.palette().text());
        h1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("AI-powered analysis and recommendations for TA management optimization");
        sub.setFont(UI.moFontPlain(14));
        sub.setForeground(UI.palette().textSecondary());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftCol.add(h1);
        leftCol.add(Box.createVerticalStrut(6));
        leftCol.add(sub);
        headerCard.add(leftCol, BorderLayout.CENTER);

        Card control = new Card();
        control.setLayout(new BorderLayout(0, 10));
        control.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UI.palette().border()),
                new EmptyBorder(16, 18, 18, 18)
        ));

        JLabel ct = new JLabel("AI Analysis Engine");
        ct.setFont(UI.moFontBold(17));
        ct.setForeground(UI.palette().text());
        control.add(ct, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        run.addActionListener(e -> runAnalysis());

        actions.add(run);
        control.add(actions, BorderLayout.SOUTH);

        JPanel status = new JPanel(new BorderLayout(0, 6));
        status.setOpaque(false);
        statusLabel.setFont(UI.moFontPlain(14));
        status.add(statusLabel, BorderLayout.NORTH);

        progress.setVisible(false);
        progress.setIndeterminate(true);
        status.add(progress, BorderLayout.SOUTH);
        control.add(status, BorderLayout.CENTER);

        JPanel mainCol = new JPanel();
        mainCol.setLayout(new BoxLayout(mainCol, BoxLayout.Y_AXIS));
        mainCol.setOpaque(false);
        mainCol.add(headerCard);
        mainCol.add(Box.createVerticalStrut(16));
        mainCol.add(control);

        root.add(mainCol, BorderLayout.NORTH);

        JPanel scores = new JPanel(new GridLayout(1, 3, 16, 0));
        scores.setOpaque(false);
        scores.add(scoreCard("System Health Score", healthValue, healthNote));
        scores.add(scoreCard("Workload Balance", workloadValue, workloadNote));
        scores.add(scoreCard("Resource Utilization", utilizationValue, utilizationNote));
        root.add(scores, BorderLayout.CENTER);

        Card exports = new Card();
        exports.setLayout(new BorderLayout(0, 8));
        exports.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UI.palette().border()),
                new EmptyBorder(12, 16, 12, 16)
        ));

        exportPdf.setEnabled(false);
        exportExcel.setEnabled(false);
        UI.styleSecondaryButton(exportPdf);
        UI.styleSecondaryButton(exportExcel);

        exportPdf.addActionListener(e -> exportAnalysisReport());
        exportExcel.addActionListener(e -> exportAsExcelCsv());

        JLabel stat = new JLabel("Applications: " + stats.getTotalApplications() + " | Approval Rate: " + stats.getApprovalRate() + "%");
        stat.setForeground(UI.palette().textSecondary());
        stat.setFont(UI.moFontPlain(13));

        recommendationLabel.setForeground(UI.palette().textSecondary());
        recommendationLabel.setFont(UI.moFontPlain(13));
        sourceLabel.setForeground(UI.palette().textSecondary());
        sourceLabel.setFont(UI.moFontPlain(12));
        applyWrappedLabel(healthNote, compactText(healthNoteRaw, 88), NOTE_WRAP_PX);
        applyWrappedLabel(workloadNote, compactText(workloadNoteRaw, 88), NOTE_WRAP_PX);
        applyWrappedLabel(utilizationNote, compactText(utilizationNoteRaw, 88), NOTE_WRAP_PX);
        applySingleLineLabel(recommendationLabel, "Recommendation: " + compactText(recommendationRaw, 110));

        JPanel exportInfo = new JPanel(new BorderLayout(12, 0));
        exportInfo.setOpaque(false);
        exportInfo.add(sourceLabel, BorderLayout.WEST);
        exportInfo.add(stat, BorderLayout.EAST);

        JPanel recommendationRow = new JPanel(new BorderLayout());
        recommendationRow.setOpaque(false);
        recommendationRow.add(recommendationLabel, BorderLayout.CENTER);

        JPanel exportActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        exportActions.setOpaque(false);
        exportActions.add(exportPdf);
        exportActions.add(exportExcel);

        JPanel exportBody = new JPanel();
        exportBody.setOpaque(false);
        exportBody.setLayout(new BoxLayout(exportBody, BoxLayout.Y_AXIS));
        exportBody.add(exportInfo);
        exportBody.add(Box.createVerticalStrut(6));
        exportBody.add(recommendationRow);
        exports.add(exportBody, BorderLayout.CENTER);
        exports.add(exportActions, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(root, BorderLayout.CENTER);
        wrap.add(exports, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(AppLayout.wrapCentered(wrap));
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        content().add(scroll, BorderLayout.CENTER);
    }

    private void runAnalysis() {
        run.setEnabled(false);
        statusLabel.setText("Analyzing TA data, workload patterns, and application trends...");
        progress.setVisible(true);

        Timer t = new Timer(1200, e -> {
            try {
                AIWorkloadAnalysisService.AnalysisResult r = analysisService.analyze();
                healthValue.setText(r.systemHealthScore() + "/100");
                workloadValue.setText(r.workloadBalanceScore() + "/100");
                utilizationValue.setText(r.resourceUtilizationPercent() + "%");

                healthNoteRaw = r.healthSummary();
                workloadNoteRaw = r.workloadSummary();
                utilizationNoteRaw = r.utilizationSummary();
                recommendationRaw = r.recommendation();

                applyWrappedLabel(healthNote, compactText(healthNoteRaw, 88), NOTE_WRAP_PX);
                applyWrappedLabel(workloadNote, compactText(workloadNoteRaw, 88), NOTE_WRAP_PX);
                applyWrappedLabel(utilizationNote, compactText(utilizationNoteRaw, 88), NOTE_WRAP_PX);
                applySingleLineLabel(recommendationLabel, "Recommendation: " + compactText(recommendationRaw, 110));
                sourceLabel.setText("Source: " + r.analysisSource());

                statusLabel.setText("Analysis completed successfully");
                exportPdf.setEnabled(true);
                exportExcel.setEnabled(true);
                run.setText("Run Again");
            } catch (Exception ex) {
                statusLabel.setText("Analysis failed: " + ex.getMessage());
                Dialogs.showMessage(this, "AI Analysis", "Analysis failed. Please check configuration and try again.", SwingConstants.CENTER);
            } finally {
                progress.setVisible(false);
                run.setEnabled(true);
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private Card scoreCard(String title, JLabel valueLabel, JLabel noteLabel) {
        Card c = new Card();
        c.setLayout(new BorderLayout(0, 6));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UI.palette().border()),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel t = new JLabel(title);
        t.setFont(UI.moFontBold(12));
        t.setForeground(UI.palette().textSecondary());

        valueLabel.setFont(UI.moFontBold(26));
        valueLabel.setForeground(UI.palette().text());

        noteLabel.setFont(UI.moFontPlain(11));
        noteLabel.setForeground(UI.palette().textSecondary());

        c.add(t, BorderLayout.NORTH);
        c.add(valueLabel, BorderLayout.CENTER);
        c.add(noteLabel, BorderLayout.SOUTH);
        return c;
    }

    private void exportAnalysisReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export AI analysis report");
        chooser.setFileFilter(new FileNameExtensionFilter("Markdown report", "md"));
        chooser.setSelectedFile(new File("ai_analysis_report_" + LocalDateTime.now().format(EXPORT_TS) + ".md"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File out = chooser.getSelectedFile();
        if (!out.getName().toLowerCase(Locale.ROOT).endsWith(".md")) {
            out = new File(out.getAbsolutePath() + ".md");
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(out))) {
            String health = healthValue.getText();
            String balance = workloadValue.getText();
            String utilization = utilizationValue.getText();
            int healthNum = parseLeadingInt(health);
            int balanceNum = parseLeadingInt(balance);
            int utilizationNum = parseLeadingInt(utilization);

            w.write("# AI TA Workload Analysis Report\n\n");
            w.write("- **Generated At**: " + LocalDateTime.now() + "\n");
            w.write("- **Analysis Source**: " + stripPrefix(sourceLabel.getText(), "Source:") + "\n");
            w.write("- **Reporting Scope**: Department-level TA workload operations snapshot\n\n");

            w.write("## Executive Summary\n\n");
            w.write("This report provides a structured view of current TA workload operations, combining system-level scoring with narrative insights. " +
                    "It is intended to support short-cycle operational decisions (allocation adjustment, balancing, and follow-up review).\n\n");
            w.write("At the time of export, overall health is rated **" + health + "**, workload balance is **" + balance + "**, and resource utilization is **" + utilization + "**. " +
                    "The combined profile suggests the operation is currently in **" + performanceBand((healthNum + balanceNum + utilizationNum) / 3) + "** condition.\n\n");

            w.write("## Key Metrics\n\n");
            w.write("| Metric | Value | Interpretation |\n");
            w.write("| --- | --- | --- |\n");
            w.write("| System Health Score | " + health + " | " + metricInterpretation(healthNum, "health") + " |\n");
            w.write("| Workload Balance | " + balance + " | " + metricInterpretation(balanceNum, "balance") + " |\n");
            w.write("| Resource Utilization | " + utilization + " | " + metricInterpretation(utilizationNum, "utilization") + " |\n\n");

            w.write("## Detailed AI Insights\n\n");
            w.write("### 1) System Health\n");
            w.write(cleanText(healthNoteRaw) + "\n\n");
            w.write("**Operational meaning**: This score reflects the overall stability of TA operations, combining activity level, balance quality, and effective load distribution. " +
                    "Lower values typically indicate process friction, uneven assignments, or insufficient staffing elasticity.\n\n");

            w.write("### 2) Workload Balance\n");
            w.write(cleanText(workloadNoteRaw) + "\n\n");
            w.write("**Operational meaning**: This score indicates how evenly workload is spread across available TAs. " +
                    "A weak balance score can increase burnout risk for high-load TAs while reducing effectiveness of underutilized members.\n\n");

            w.write("### 3) Resource Utilization\n");
            w.write(cleanText(utilizationNoteRaw) + "\n\n");
            w.write("**Operational meaning**: Utilization estimates how effectively available TA capacity is being used. " +
                    "Very low utilization may imply overstaffing or assignment gaps; very high utilization may reduce flexibility for peak periods.\n\n");

            w.write("## Recommendation and Action Plan\n\n");
            w.write("### Primary Recommendation\n");
            w.write(cleanText(recommendationRaw) + "\n\n");
            w.write("### Suggested 2-Week Actions\n");
            w.write("1. Review top overloaded and underutilized TA records, then propose reallocation scenarios.\n");
            w.write("2. Validate whether course-level demand changes explain current imbalance.\n");
            w.write("3. Apply one controlled adjustment batch and rerun analysis within 3-5 working days.\n");
            w.write("4. Compare before/after score movement and keep the better allocation baseline.\n\n");

            w.write("## Risk Signals to Monitor\n\n");
            w.write("- Persistent low balance score despite reassignment rounds.\n");
            w.write("- Health score decline after assignment increases (possible overload concentration).\n");
            w.write("- Utilization plateau at very low levels, indicating structural allocation inefficiency.\n\n");

            w.write("## Governance Notes\n\n");
            w.write("- This document is an AI-assisted decision-support artifact and should be combined with coordinator judgment.\n");
            w.write("- Rerun analysis after meaningful workload updates to keep this report current.\n");
            w.write("- Keep monthly snapshots to track trend direction across assessment cycles.\n");
        } catch (IOException ex) {
            Dialogs.showMessage(this, "Export Failed", ex.getMessage(), SwingConstants.LEFT);
            return;
        }

        Dialogs.showMessage(this, "Export Complete", "Saved report to:\n" + out.getAbsolutePath(), SwingConstants.LEFT);
    }

    private void exportAsExcelCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export AI analysis (Excel/CSV)");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        chooser.setSelectedFile(new File("ai_analysis_" + LocalDateTime.now().format(EXPORT_TS) + ".csv"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File out = chooser.getSelectedFile();
        if (!out.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            out = new File(out.getAbsolutePath() + ".csv");
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(out))) {
            w.write("Metric,Value");
            w.newLine();
            w.write(csv("System Health Score") + "," + csv(healthValue.getText()));
            w.newLine();
            w.write(csv("Workload Balance") + "," + csv(workloadValue.getText()));
            w.newLine();
            w.write(csv("Resource Utilization") + "," + csv(utilizationValue.getText()));
            w.newLine();
            w.write(csv("Health Summary") + "," + csv(healthNoteRaw));
            w.newLine();
            w.write(csv("Workload Summary") + "," + csv(workloadNoteRaw));
            w.newLine();
            w.write(csv("Utilization Summary") + "," + csv(utilizationNoteRaw));
            w.newLine();
            w.write(csv("Recommendation") + "," + csv("Recommendation: " + recommendationRaw));
            w.newLine();
            w.write(csv("Source") + "," + csv(sourceLabel.getText()));
            w.newLine();
            w.write(csv("Generated At") + "," + csv(LocalDateTime.now().toString()));
            w.newLine();
        } catch (IOException ex) {
            Dialogs.showMessage(this, "Export Failed", ex.getMessage(), SwingConstants.LEFT);
            return;
        }

        Dialogs.showMessage(this, "Export Complete", "Saved report to:\n" + out.getAbsolutePath(), SwingConstants.LEFT);
    }

    private static String cleanText(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\n", " ").replace("\r", " ").trim();
    }

    private static String stripPrefix(String s, String prefix) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        if (t.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
            return t.substring(prefix.length()).trim();
        }
        return t;
    }

    private static void applyWrappedLabel(JLabel label, String text, int widthPx) {
        label.setVerticalAlignment(SwingConstants.TOP);
        String safe = text == null ? "" : text;
        String escaped = safe
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        label.setText("<html><div style='width:" + widthPx + "px'>" + escaped + "</div></html>");
    }

    private static void applySingleLineLabel(JLabel label, String text) {
        String safe = text == null ? "" : text.replace('\n', ' ').replace('\r', ' ').trim();
        label.setText(safe);
        label.setToolTipText(safe);
    }

    private static String compactText(String text, int maxChars) {
        if (text == null) {
            return "";
        }
        String normalized = text.replace('\n', ' ').replace('\r', ' ').replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxChars - 1)).trim() + "\u2026";
    }

    private static String csv(String s) {
        String v = s == null ? "" : s;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    private static int parseLeadingInt(String text) {
        if (text == null) {
            return 0;
        }
        StringBuilder num = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isDigit(c)) {
                num.append(c);
            } else if (num.length() > 0) {
                break;
            }
        }
        if (num.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(num.toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static String performanceBand(int score) {
        if (score >= 85) return "strong";
        if (score >= 70) return "stable";
        if (score >= 55) return "watch-list";
        return "high-risk";
    }

    private static String metricInterpretation(int score, String metric) {
        return switch (metric) {
            case "health" -> score >= 85 ? "High operational stability" : score >= 70 ? "Generally stable" : "Needs intervention";
            case "balance" -> score >= 80 ? "Workload is relatively even" : score >= 60 ? "Mild imbalance present" : "Significant imbalance risk";
            case "utilization" -> score >= 80 ? "Capacity well utilized" : score >= 60 ? "Moderate utilization" : "Low effective utilization";
            default -> "-";
        };
    }
}
