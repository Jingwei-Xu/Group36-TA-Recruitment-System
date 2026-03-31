package edu.ebu6304.app.ui;

import edu.ebu6304.app.model.ApplicationRecord;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ApplicationsListPanel extends JPanel {
    private final MainFrame frame;

    private final JLabel totalLabel = new JLabel("0", SwingConstants.LEFT);
    private final JLabel pendingLabel = new JLabel("0", SwingConstants.LEFT);
    private final JLabel reviewedLabel = new JLabel("0", SwingConstants.LEFT);

    private final JTextField searchField = new JTextField(22);
    private final JComboBox<String> courseFilter = new JComboBox<>();
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All Status", "Pending", "Accepted", "Rejected"});

    private final ApplicationsTableModel tableModel = new ApplicationsTableModel();
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<ApplicationsTableModel> sorter = new TableRowSorter<>(tableModel);

    public ApplicationsListPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(16, 16));
        setBackground(Style.BG);

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("TA Applications");
        title.setFont(Style.FONT_H1);
        title.setForeground(Style.TEXT);

        JButton backBtn = new JButton("Back to Dashboard");
        Style.styleSecondaryButton(backBtn);
        backBtn.addActionListener(e -> frame.showDashboard());

        header.add(title, BorderLayout.WEST);
        header.add(backBtn, BorderLayout.EAST);

        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 14));
        stats.setOpaque(false);
        stats.add(statCard("Total Applications", totalLabel));
        stats.add(statCard("Pending Reviews", pendingLabel));
        stats.add(statCard("Reviewed", reviewedLabel));

        JPanel actions = new JPanel(new GridLayout(2, 1, 0, 8));
        Style.stylePanelCard(actions);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);
        filterRow.add(new JLabel("Search:"));
        searchField.setFont(Style.FONT_BODY);
        Style.fixedHeight(searchField, 32);
        filterRow.add(searchField);

        filterRow.add(new JLabel("Course:"));
        Style.fixedHeight(courseFilter, 32);
        filterRow.add(courseFilter);

        filterRow.add(new JLabel("Status:"));
        Style.fixedHeight(statusFilter, 32);
        filterRow.add(statusFilter);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setOpaque(false);

        JButton detailBtn = new JButton("◉ Detail");
        Style.styleSecondaryButton(detailBtn);
        detailBtn.addActionListener(e -> openSelectedDetail());

        JButton reviewBtn = new JButton("✓ Review");
        Style.stylePrimaryButton(reviewBtn);
        reviewBtn.addActionListener(e -> openSelectedReview());

        JButton recordsBtn = new JButton("☰ Records");
        Style.styleSecondaryButton(recordsBtn);
        recordsBtn.addActionListener(e -> frame.showReviewRecords());

        actionRow.add(detailBtn);
        actionRow.add(reviewBtn);
        actionRow.add(recordsBtn);

        actions.add(filterRow);
        actions.add(actionRow);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        courseFilter.addActionListener(e -> applyFilter());
        statusFilter.addActionListener(e -> applyFilter());

        table.setRowSorter(sorter);
        table.setFillsViewportHeight(true);
        table.setRowHeight(30);
        table.getTableHeader().setFont(Style.FONT_BODY_BOLD);
        table.getTableHeader().setBackground(new Color(243, 244, 246));

        TableCellRenderer badgeRenderer = new BadgeRenderer();
        table.getColumnModel().getColumn(5).setCellRenderer(badgeRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(badgeRenderer);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(Style.sectionBorder());

        page.add(header);
        page.add(Box.createVerticalStrut(12));
        page.add(stats);
        page.add(Box.createVerticalStrut(12));
        page.add(actions);
        page.add(Box.createVerticalStrut(12));
        page.add(tableScroll);

        add(page, BorderLayout.CENTER);
    }

    public void refresh(List<ApplicationRecord> records) {
        tableModel.setData(records);
        long total = records.size();
        long pending = records.stream().filter(r -> !r.isReviewed()).count();
        long reviewed = records.stream().filter(ApplicationRecord::isReviewed).count();

        totalLabel.setText(String.valueOf(total));
        pendingLabel.setText(String.valueOf(pending));
        reviewedLabel.setText(String.valueOf(reviewed));

        reloadCourseFilter(records);
        applyFilter();
    }

    private void reloadCourseFilter(List<ApplicationRecord> records) {
        Object selected = courseFilter.getSelectedItem();
        Set<String> courseSet = new LinkedHashSet<>();
        courseSet.add("All Courses");
        for (ApplicationRecord r : records) {
            String c = String.format("%s - %s", safe(r.getCourseCode()), safe(r.getCourseName())).trim();
            if (!c.isBlank() && !"- -".equals(c)) {
                courseSet.add(c);
            }
        }
        courseFilter.removeAllItems();
        for (String c : courseSet) {
            courseFilter.addItem(c);
        }
        if (selected != null) {
            courseFilter.setSelectedItem(selected);
        }
        if (courseFilter.getSelectedIndex() < 0) {
            courseFilter.setSelectedIndex(0);
        }
    }

    private void applyFilter() {
        String keyword = searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedCourse = (String) courseFilter.getSelectedItem();
        String selectedStatus = ((String) statusFilter.getSelectedItem()).toLowerCase(Locale.ROOT);

        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends ApplicationsTableModel, ? extends Integer> entry) {
                int modelRow = entry.getIdentifier();
                ApplicationRecord r = tableModel.getAt(modelRow);

                boolean textMatch = keyword.isEmpty() || contains(r.getApplicantName(), keyword)
                        || contains(r.getStudentId(), keyword)
                        || contains(r.getCourseCode(), keyword)
                        || contains(r.getCourseName(), keyword)
                        || contains(r.getApplicationId(), keyword);

                String rowCourse = String.format("%s - %s", safe(r.getCourseCode()), safe(r.getCourseName())).trim();
                boolean courseMatch = selectedCourse == null || "All Courses".equals(selectedCourse) || selectedCourse.equals(rowCourse);

                String decision = r.getReviewDecision() == null ? "" : r.getReviewDecision().toLowerCase(Locale.ROOT);
                String status = r.getStatusLabel() == null ? "" : r.getStatusLabel().toLowerCase(Locale.ROOT);
                boolean statusMatch = "all status".equals(selectedStatus)
                        || status.contains(selectedStatus)
                        || decision.contains(selectedStatus);

                return textMatch && courseMatch && statusMatch;
            }
        });
    }

    private boolean contains(String text, String keyword) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private void openSelectedDetail() {
        ApplicationRecord selected = getSelectedRecord();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an application first.");
            return;
        }
        frame.showApplicationDetail(selected);
    }

    private void openSelectedReview() {
        ApplicationRecord selected = getSelectedRecord();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an application first.");
            return;
        }
        frame.showReviewPage(selected);
    }

    private ApplicationRecord getSelectedRecord() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return tableModel.getAt(modelRow);
    }

    private JPanel statCard(String title, JLabel value) {
        JPanel card = new JPanel(new BorderLayout());
        Style.stylePanelCard(card);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Style.FONT_BODY);
        titleLabel.setForeground(Style.MUTED);

        value.setFont(Style.FONT_H2);
        value.setForeground(Style.TEXT);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private static class BadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = value == null ? "-" : value.toString();
            String key = text.toLowerCase(Locale.ROOT);

            Color fg = new Color(55, 65, 81);
            Color bg = new Color(229, 231, 235);

            if (key.contains("pending")) {
                fg = new Color(146, 64, 14);
                bg = new Color(254, 243, 199);
            } else if (key.contains("accepted") || key.contains("approved")) {
                fg = new Color(22, 101, 52);
                bg = new Color(220, 252, 231);
            } else if (key.contains("rejected")) {
                fg = new Color(153, 27, 27);
                bg = new Color(254, 226, 226);
            }

            label.setText("  " + text + "  ");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);
            label.setForeground(fg);
            label.setBackground(isSelected ? bg.darker() : bg);
            label.setBorder(new EmptyBorder(3, 8, 3, 8));
            return label;
        }
    }

    private static class ApplicationsTableModel extends AbstractTableModel {
        private final String[] cols = {
                "Application ID", "TA Name", "Student ID", "Course", "Department", "Status", "Decision"
        };
        private final List<ApplicationRecord> data = new ArrayList<>();

        public void setData(List<ApplicationRecord> records) {
            data.clear();
            data.addAll(records);
            fireTableDataChanged();
        }

        public ApplicationRecord getAt(int row) {
            return data.get(row);
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
            ApplicationRecord r = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> r.getApplicationId();
                case 1 -> r.getApplicantName();
                case 2 -> r.getStudentId();
                case 3 -> String.format("%s - %s", safe(r.getCourseCode()), safe(r.getCourseName()));
                case 4 -> r.getDepartment();
                case 5 -> r.getStatusLabel();
                case 6 -> (r.getReviewDecision() == null || r.getReviewDecision().isBlank()) ? "-" : r.getReviewDecision();
                default -> "";
            };
        }

        private String safe(String text) {
            return text == null ? "" : text;
        }
    }
}
