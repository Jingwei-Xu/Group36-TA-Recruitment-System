package com.ta.recruitment.ui;

import com.ta.recruitment.model.Job;
import com.ta.recruitment.service.JobService;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JobManagementFrame extends JFrame {

    private static final String[] TABLE_COLUMNS = {
            "Job Title", "Module", "Hours/Week", "Status", "Applicants", "Actions"
    };

    private final JobService jobService;
    private final JTable jobTable;
    private final DefaultTableModel tableModel;
    private List<Job> currentJobs = new ArrayList<>();

    public JobManagementFrame(JobService jobService) {
        this.jobService = jobService;
        this.tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };
        this.jobTable = new JTable(tableModel);
        initializeUi();
        refreshTable();
    }

    private void initializeUi() {
        setTitle("MO Job System");
        setSize(1100, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 249, 252));

        add(buildTopNav(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
    }

    private JPanel buildTopNav() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Color.WHITE);
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(232, 235, 241)));
        nav.setPreferredSize(new Dimension(1100, 56));

        JLabel brand = new JLabel("MO Job System");
        brand.setBorder(new EmptyBorder(0, 16, 0, 8));
        brand.setFont(new Font("SansSerif", Font.BOLD, 16));
        brand.setForeground(new Color(31, 41, 55));

        JPanel navLinks = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 10));
        navLinks.setOpaque(false);
        navLinks.add(tabButton("Home", false));
        navLinks.add(tabButton("Job Management", true));
        navLinks.add(tabButton("Application Review", false));

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(brand, BorderLayout.WEST);
        left.add(navLinks, BorderLayout.CENTER);

        JButton logout = new JButton("Logout");
        logout.setFocusPainted(false);
        logout.setBorderPainted(false);
        logout.setContentAreaFilled(false);
        logout.setForeground(new Color(55, 65, 81));
        logout.setBorder(new EmptyBorder(0, 0, 0, 16));

        nav.add(left, BorderLayout.WEST);
        nav.add(logout, BorderLayout.EAST);
        return nav;
    }

    private JButton tabButton(String text, boolean active) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(active ? new Color(239, 242, 247) : Color.WHITE);
        button.setForeground(active ? new Color(17, 24, 39) : new Color(107, 114, 128));
        button.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 13));
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        return button;
    }

    private JPanel buildMainContent() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 14));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("My Jobs");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(new Color(17, 24, 39));
        JLabel subtitle = new JLabel("Manage your TA recruitment positions");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(107, 114, 128));
        titleBlock.add(title);
        titleBlock.add(subtitle);

        JButton createButton = new JButton("+  Create New Job");
        createButton.setOpaque(true);
        createButton.setContentAreaFilled(true);
        createButton.setPreferredSize(new Dimension(170, 40));
        createButton.setFocusPainted(false);
        createButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        createButton.setForeground(Color.WHITE);
        createButton.setBackground(new Color(10, 15, 38));
        createButton.setBorder(new EmptyBorder(10, 16, 10, 16));
        createButton.addActionListener(e -> onCreateJob());

        header.add(titleBlock, BorderLayout.WEST);
        header.add(createButton, BorderLayout.EAST);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(232, 235, 241)),
                new EmptyBorder(0, 0, 0, 0)
        ));

        configureTable();
        JScrollPane scrollPane = new JScrollPane(jobTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        card.add(scrollPane, BorderLayout.CENTER);

        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private void configureTable() {
        jobTable.setRowHeight(46);
        jobTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        jobTable.setShowGrid(false);
        jobTable.setIntercellSpacing(new Dimension(0, 0));
        jobTable.setSelectionBackground(new Color(238, 244, 255));
        jobTable.setSelectionForeground(new Color(31, 41, 55));
        jobTable.getTableHeader().setReorderingAllowed(false);
        jobTable.getTableHeader().setBackground(new Color(249, 250, 251));
        jobTable.getTableHeader().setForeground(new Color(55, 65, 81));
        jobTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        DefaultTableCellRenderer baseRenderer = new DefaultTableCellRenderer();
        baseRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        baseRenderer.setForeground(new Color(31, 41, 55));
        jobTable.setDefaultRenderer(Object.class, baseRenderer);

        jobTable.getColumnModel().getColumn(0).setPreferredWidth(260);
        jobTable.getColumnModel().getColumn(1).setPreferredWidth(230);
        jobTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        jobTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        jobTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        jobTable.getColumnModel().getColumn(5).setPreferredWidth(240);

        jobTable.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        JobActionCell actionCell = new JobActionCell();
        jobTable.getColumnModel().getColumn(5).setCellRenderer(actionCell);
        jobTable.getColumnModel().getColumn(5).setCellEditor(actionCell);
    }

    private void onCreateJob() {
        JobFormDialog dialog = new JobFormDialog(this, "Create New Job", null, job -> {
            try {
                jobService.createJob(job);
                refreshTable();
                showInfo("Job created successfully.");
            } catch (IOException | IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });
        dialog.setVisible(true);
    }

    private void onEditJob() {
        Job selected = getSelectedJob(jobTable.getSelectedRow());
        if (selected == null) {
            showError("Please select one job to edit.");
            return;
        }
        JobFormDialog dialog = new JobFormDialog(this, "Edit Job", selected, job -> {
            try {
                jobService.updateJob(job);
                refreshTable();
                showInfo("Job updated successfully.");
            } catch (IOException | IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });
        dialog.setVisible(true);
    }

    private void onDeleteJob() {
        Job selected = getSelectedJob(jobTable.getSelectedRow());
        if (selected == null) {
            showError("Please select one job to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete job " + selected.getJobId() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            jobService.deleteJob(selected.getJobId());
            refreshTable();
            showInfo("Job deleted successfully.");
        } catch (IOException | IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void onToggleStatus(int row) {
        Job selected = getSelectedJob(row);
        if (selected == null) {
            showError("Please select one job to toggle status.");
            return;
        }
        try {
            jobService.toggleOpenClosed(selected.getJobId());
            refreshTable();
            showInfo("Job status toggled.");
        } catch (IOException | IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private Job getSelectedJob(int row) {
        if (row < 0 || row >= currentJobs.size()) {
            return null;
        }
        return currentJobs.get(row);
    }

    private void refreshTable() {
        try {
            currentJobs = jobService.getAllJobs();
            tableModel.setRowCount(0);
            for (Job job : currentJobs) {
                String moduleDisplay = "";
                if (job.getCourse() != null) {
                    moduleDisplay = "<html><b>" + safe(job.getCourse().getCourseCode()) + "</b><br/>"
                            + "<span style='color:#6b7280'>" + safe(job.getCourse().getCourseName()) + "</span></html>";
                }
                String status = job.getLifecycle() != null ? safe(job.getLifecycle().getStatus()) : "";
                String hours = job.getEmployment() != null ? job.getEmployment().getWeeklyHours() + "h" : "0h";
                tableModel.addRow(new Object[]{
                        job.getTitle(),
                        moduleDisplay,
                        hours,
                        status,
                        job.getStats() != null ? job.getStats().getApplicationCount() : 0,
                        ""
                });
            }
        } catch (IOException ex) {
            showError("Failed to load jobs: " + ex.getMessage());
        }
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        protected void setValue(Object value) {
            String status = value == null ? "" : String.valueOf(value);
            String normalized = status.toLowerCase(Locale.ROOT);
            Color bg = "open".equals(normalized) ? new Color(220, 252, 231) : new Color(229, 231, 235);
            Color fg = "open".equals(normalized) ? new Color(22, 101, 52) : new Color(75, 85, 99);
            setHorizontalAlignment(CENTER);
            setText(" " + status + " ");
            setOpaque(true);
            setBackground(bg);
            setForeground(fg);
            setBorder(new EmptyBorder(4, 10, 4, 10));
        }
    }

    private class JobActionCell extends AbstractCellEditor implements javax.swing.table.TableCellRenderer, javax.swing.table.TableCellEditor {
        private final JPanel panel;
        private int editingRow = -1;

        JobActionCell() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
            panel.setOpaque(true);
            panel.setBackground(Color.WHITE);
            panel.add(actionButton("View", e -> onViewJob(editingRow)));
            panel.add(actionButton("TA", e -> showInfo("TA allocation page is planned in next sprint.")));
            panel.add(actionButton("Edit", e -> onEditJobByRow(editingRow)));
            panel.add(actionButton("Toggle", e -> onToggleStatus(editingRow)));
            panel.add(actionButton("Delete", e -> onDeleteJobByRow(editingRow)));
        }

        private JButton actionButton(String text, java.awt.event.ActionListener listener) {
            JButton button = new JButton(text);
            button.setMargin(new java.awt.Insets(3, 6, 3, 6));
            button.setFocusPainted(false);
            button.setFont(new Font("SansSerif", Font.PLAIN, 11));
            button.addActionListener(e -> {
                fireEditingStopped();
                listener.actionPerformed(e);
            });
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
    }

    private void onViewJob(int row) {
        Job selected = getSelectedJob(row);
        if (selected == null) {
            return;
        }
        String detail = "Job ID: " + safe(selected.getJobId())
                + "\nTitle: " + safe(selected.getTitle())
                + "\nModule: " + (selected.getCourse() != null ? safe(selected.getCourse().getCourseCode()) : "")
                + "\nStatus: " + (selected.getLifecycle() != null ? safe(selected.getLifecycle().getStatus()) : "")
                + "\nHours/Week: " + (selected.getEmployment() != null ? selected.getEmployment().getWeeklyHours() : 0)
                + "\n\nDescription:\n" + (selected.getContent() != null ? safe(selected.getContent().getDescription()) : "");
        JOptionPane.showMessageDialog(this, detail, "Job Detail", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onEditJobByRow(int row) {
        jobTable.getSelectionModel().setSelectionInterval(row, row);
        onEditJob();
    }

    private void onDeleteJobByRow(int row) {
        jobTable.getSelectionModel().setSelectionInterval(row, row);
        onDeleteJob();
    }
}
