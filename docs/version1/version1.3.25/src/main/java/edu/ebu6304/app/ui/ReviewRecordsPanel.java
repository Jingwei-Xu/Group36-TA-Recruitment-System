package edu.ebu6304.app.ui;

import edu.ebu6304.app.model.ApplicationRecord;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReviewRecordsPanel extends JPanel {
    private final MainFrame frame;

    private final RecordsTableModel tableModel = new RecordsTableModel();
    private final JTable table = new JTable(tableModel);

    public ReviewRecordsPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(12, 12));
        setBackground(Style.BG);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("My Review Records");
        title.setFont(Style.FONT_H1);

        JButton backBtn = new JButton("Back to Applications");
        Style.styleSecondaryButton(backBtn);
        backBtn.addActionListener(e -> frame.showApplicationsList());

        top.add(title, BorderLayout.WEST);
        top.add(backBtn, BorderLayout.EAST);

        table.setRowHeight(28);
        table.getTableHeader().setFont(Style.FONT_BODY_BOLD);
        table.getTableHeader().setBackground(new java.awt.Color(243, 244, 246));

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(Style.sectionBorder());

        add(top, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
    }

    public void refresh(List<ApplicationRecord> all) {
        List<ApplicationRecord> reviewed = all.stream()
                .filter(ApplicationRecord::isReviewed)
                .sorted(Comparator.comparing(ApplicationRecord::reviewedAtTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        tableModel.setData(reviewed);
    }

    private static class RecordsTableModel extends AbstractTableModel {
        private final String[] cols = {
                "Application ID", "Course", "TA Name", "Student ID", "Decision", "Reviewed By", "Reviewed At"
        };
        private final List<ApplicationRecord> data = new ArrayList<>();

        public void setData(List<ApplicationRecord> reviewed) {
            data.clear();
            data.addAll(reviewed);
            fireTableDataChanged();
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
                case 1 -> String.format("%s - %s", safe(r.getCourseCode()), safe(r.getCourseName()));
                case 2 -> r.getApplicantName();
                case 3 -> r.getStudentId();
                case 4 -> r.getReviewDecision();
                case 5 -> r.getReviewedBy();
                case 6 -> r.getReviewedAt();
                default -> "";
            };
        }

        private String safe(String s) {
            return s == null ? "" : s;
        }
    }
}
