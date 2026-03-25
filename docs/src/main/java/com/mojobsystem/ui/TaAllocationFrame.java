package com.mojobsystem.ui;

import com.mojobsystem.model.Job;
import com.mojobsystem.repository.ApplicationRepository;
import com.mojobsystem.repository.JobRepository;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spec (6) TA Allocation Results — roster from accepted applications, summary cards from data.
 */
public class TaAllocationFrame extends JFrame {
    private final Job job;
    private final List<ApplicationRepository.AllocatedTaRecord> rows;

    public TaAllocationFrame(JFrame parent, JobRepository jobRepository, Job job) {
        this.job = job;
        ApplicationRepository apps = new ApplicationRepository();
        this.rows = new ArrayList<>(apps.listAcceptedForJob(job.getId()));
        JobRepository.RichJobStats stats = jobRepository.readRichJobStats(job.getId());
        int richAccepted = stats == null ? 0 : stats.acceptedCount();
        if (rows.isEmpty() && richAccepted > 0) {
            JOptionPane.showMessageDialog(parent,
                    "Job stats list " + richAccepted + " accepted hire(s), but no applications in data/applications "
                            + "have status \"accepted\" yet. The table below will populate when those records exist.",
                    "Allocation data",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        setTitle("MO System - TA Allocation Results");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        MoFrameGeometry.applyMatching(parent, this);
        getContentPane().setBackground(MoUiTheme.PAGE_BG);
        setLayout(new BorderLayout());

        add(NavigationPanel.create(NavigationPanel.Tab.JOB_MANAGEMENT, navActions()), BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBackground(MoUiTheme.PAGE_BG);
        main.add(buildPageHeaderStrip(), BorderLayout.NORTH);
        main.add(buildScrollBody(stats), BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
    }

    private NavigationPanel.Actions navActions() {
        return new NavigationPanel.Actions(
                () -> MoFrameGeometry.navigateReplace(this, () -> new MoDashboardFrame().setVisible(true)),
                () -> MoFrameGeometry.navigateReplace(this, () -> new MyJobsFrame().setVisible(true)),
                () -> MoFrameGeometry.navigateReplace(this, () -> new ApplicationReviewPlaceholderFrame(job.getId()).setVisible(true)),
                () -> System.exit(0)
        );
    }

    /**
     * Same pattern as {@link CreateJobFrame#buildPageHeaderStrip()}: text-style Back, then title/subtitle.
     */
    private JPanel buildPageHeaderStrip() {
        JPanel strip = new JPanel(new BorderLayout(20, 0));
        strip.setBackground(Color.WHITE);
        strip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MoUiTheme.BORDER),
                new EmptyBorder(18, 40, 20, 40)
        ));

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        JButton back = new JButton("Back");
        back.setFocusPainted(false);
        back.setContentAreaFilled(false);
        back.setBorder(new EmptyBorder(6, 4, 6, 4));
        back.setForeground(MoUiTheme.TEXT_SECONDARY);
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> dispose());
        leftCol.add(back);
        leftCol.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("TA Allocation Results");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        title.setForeground(MoUiTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCol.add(title);
        leftCol.add(Box.createVerticalStrut(6));

        String subLine = job.getTitle() + " · " + job.getModuleCode() + " — " + job.getModuleName();
        JLabel sub = new JLabel(subLine);
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        sub.setForeground(MoUiTheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCol.add(sub);

        JButton export = new JButton("Export to CSV");
        MoUiTheme.stylePrimaryButton(export, 8);
        export.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        export.setFocusPainted(false);
        export.addActionListener(e -> exportCsv());

        JPanel east = new JPanel(new BorderLayout());
        east.setOpaque(false);
        east.add(export, BorderLayout.NORTH);

        strip.add(leftCol, BorderLayout.CENTER);
        strip.add(east, BorderLayout.EAST);
        return strip;
    }

    private JScrollPane buildScrollBody(JobRepository.RichJobStats stats) {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(24, 40, 36, 40));

        int required = Math.max(0, job.getQuota());
        int allocated = rows.size();
        if (stats != null && stats.acceptedCount() > allocated) {
            allocated = stats.acceptedCount();
        }
        String pct;
        if (required <= 0) {
            pct = "N/A";
        } else if (allocated >= required) {
            pct = "100% Complete";
        } else {
            pct = (int) Math.min(100, (allocated * 100.0 / required)) + "% Complete";
        }

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        statsRow.add(summaryCard("TAs required", String.valueOf(required)));
        statsRow.add(summaryCard("TAs allocated", String.valueOf(Math.max(rows.size(), stats != null ? stats.acceptedCount() : 0))));
        statsRow.add(summaryCard("Allocation status", pct));
        root.add(statsRow);
        root.add(Box.createVerticalStrut(22));

        JLabel sec = new JLabel("Allocated teaching assistants");
        sec.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        sec.setForeground(MoUiTheme.TEXT_PRIMARY);
        root.add(sec);
        root.add(Box.createVerticalStrut(10));

        TaTableModel model = new TaTableModel(rows, job.getWeeklyHours());
        JTable table = new JTable(model);
        table.setRowHeight(52);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        table.setGridColor(MoUiTheme.BORDER);
        table.setShowVerticalLines(false);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(MoUiTheme.BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        root.add(sp);

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(MoUiTheme.PAGE_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel summaryCard(String k, String v) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(MoUiTheme.SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MoUiTheme.BORDER),
                new EmptyBorder(14, 16, 16, 16)
        ));
        JLabel a = new JLabel(k);
        a.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        a.setForeground(MoUiTheme.TEXT_SECONDARY);
        JLabel b = new JLabel(v);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        b.setForeground(MoUiTheme.TEXT_PRIMARY);
        p.add(a);
        p.add(Box.createVerticalStrut(6));
        p.add(b);
        return p;
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("allocated_tas_" + job.getId() + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        StringBuilder sb = new StringBuilder("name,studentId,email,weeklyHours,status,skills\n");
        for (ApplicationRepository.AllocatedTaRecord r : rows) {
            String skills = r.skills().stream().map(s -> s.replace("\"", "\"\"")).collect(Collectors.joining(";"));
            sb.append(csv(r.fullName())).append(',')
                    .append(csv(r.studentId())).append(',')
                    .append(csv(r.email())).append(',')
                    .append(r.weeklyHours() > 0 ? r.weeklyHours() : job.getWeeklyHours()).append(',')
                    .append("Allocated").append(',')
                    .append('"').append(skills).append('"').append('\n');
        }
        try {
            Files.writeString(fc.getSelectedFile().toPath(), sb.toString(), StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(this, "Exported " + rows.size() + " row(s).", "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Export failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String csv(String s) {
        if (s == null) {
            return "\"\"";
        }
        return '"' + s.replace("\"", "\"\"") + '"';
    }

    private static class TaTableModel extends AbstractTableModel {
        private final List<ApplicationRepository.AllocatedTaRecord> data;
        private final int fallbackHours;
        private final String[] cols = {"Name", "Student ID", "Status", "Email", "Hours / week", "Skills"};

        TaTableModel(List<ApplicationRepository.AllocatedTaRecord> data, int fallbackHours) {
            this.data = data;
            this.fallbackHours = fallbackHours;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int column) {
            return cols[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ApplicationRepository.AllocatedTaRecord r = data.get(rowIndex);
            int h = r.weeklyHours() > 0 ? r.weeklyHours() : fallbackHours;
            String skills = String.join(", ", r.skills());
            return switch (columnIndex) {
                case 0 -> r.fullName();
                case 1 -> r.studentId();
                case 2 -> "Allocated";
                case 3 -> r.email();
                case 4 -> h + "h";
                case 5 -> skills;
                default -> "";
            };
        }
    }
}
