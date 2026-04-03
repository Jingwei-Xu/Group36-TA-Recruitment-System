

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;



/**
 * 我的申请页面
 * 显示用户所有申请记录的列表
 */
public class Page_MyApplications {
    
    public interface MyApplicationsCallback {
        void onViewStatus(Application application);
    }
    
    private JPanel panel;
    private JTable applicationsTable;
    private DefaultTableModel tableModel;
    private DataService dataService;
    private MyApplicationsCallback callback;
    
    public Page_MyApplications(DataService dataService, MyApplicationsCallback callback) {
        this.dataService = dataService;
        this.callback = callback;
        initPanel();
    }
    
    public JPanel getPanel() {
        return panel;
    }
    
    public void refreshTable() {
        refreshData();
    }
    
    private void initPanel() {
        panel = new JPanel(new BorderLayout());
        panel.setBackground(UI_Constants.BG_COLOR);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        buildHeader();
        buildTable();
    }
    
    private void buildHeader() {
        JPanel northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);
        
        JPanel headerLeft = new JPanel(new BorderLayout());
        headerLeft.setOpaque(false);
        
        JLabel titleLabel = new JLabel("My Applications");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        headerLeft.add(titleLabel, BorderLayout.NORTH);
        
        JLabel subtitleLabel = new JLabel("Track and manage your TA applications");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        headerLeft.add(subtitleLabel, BorderLayout.SOUTH);
        
        northStack.add(headerLeft);
        
        // Summary cards
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(new EmptyBorder(25, 0, 25, 0));
        
        summaryPanel.add(createSummaryCard("Total Applications", 
            String.valueOf(dataService.countApplicationsByStatus("pending") + 
                          dataService.countApplicationsByStatus("under_review") +
                          dataService.countApplicationsByStatus("accepted") +
                          dataService.countApplicationsByStatus("rejected")),
            new Color(219, 234, 254), UI_Constants.INFO_COLOR));
        
        summaryPanel.add(createSummaryCard("Pending", 
            String.valueOf(dataService.countApplicationsByStatus("pending")),
            new Color(254, 243, 199), UI_Constants.WARNING_COLOR));
        
        summaryPanel.add(createSummaryCard("Accepted", 
            String.valueOf(dataService.countApplicationsByStatus("accepted")),
            new Color(209, 250, 229), UI_Constants.SUCCESS_COLOR));
        
        summaryPanel.add(createSummaryCard("Rejected", 
            String.valueOf(dataService.countApplicationsByStatus("rejected")),
            new Color(254, 226, 226), UI_Constants.DANGER_COLOR));
        
        northStack.add(summaryPanel);
        panel.add(northStack, BorderLayout.NORTH);
    }
    
    private void buildTable() {
        String[] columns = {"Job Title", "Course", "Department", "Applied Date", "Status", "Last Updated", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        applicationsTable = new JTable(tableModel);
        applicationsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        applicationsTable.setRowHeight(50);
        applicationsTable.setGridColor(UI_Constants.BORDER_COLOR);
        applicationsTable.setShowGrid(true);
        applicationsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        applicationsTable.getTableHeader().setBackground(UI_Constants.BG_COLOR);
        applicationsTable.getTableHeader().setForeground(UI_Constants.TEXT_PRIMARY);
        applicationsTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UI_Constants.BORDER_COLOR));
        
        // Center align cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < columns.length; i++) {
            applicationsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Status renderer
        applicationsTable.getColumn("Status").setCellRenderer(new StatusRenderer());
        
        // Action renderer
        applicationsTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        
        // Click handler
        final int actionModelIndex = applicationsTable.getColumn("Action").getModelIndex();
        applicationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                Point p = e.getPoint();
                int viewRow = applicationsTable.rowAtPoint(p);
                int viewCol = applicationsTable.columnAtPoint(p);
                if (viewRow < 0 || viewCol < 0) {
                    return;
                }
                int modelCol = applicationsTable.convertColumnIndexToModel(viewCol);
                if (modelCol != actionModelIndex) {
                    return;
                }
                int modelRow = applicationsTable.convertRowIndexToModel(viewRow);
                List<Application> apps = dataService.getUserApplications();
                if (modelRow >= 0 && modelRow < apps.size()) {
                    callback.onViewStatus(apps.get(modelRow));
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(applicationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR));
        scrollPane.getViewport().setBackground(UI_Constants.CARD_BG);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        refreshData();
    }
    
    private void refreshData() {
        tableModel.setRowCount(0);
        
        for (Application app : dataService.getUserApplications()) {
            String status = app.getStatus().getLabel();
            
            Object[] row = {
                app.getJobSnapshot().getTitle(),
                app.getJobSnapshot().getCourseCode(),
                app.getJobSnapshot().getDepartment(),
                app.getMeta().getSubmittedAt().substring(0, 10),
                status,
                app.getStatus().getLastUpdated().substring(0, 16).replace("T", " "),
                "View"
            };
            tableModel.addRow(row);
        }
    }
    
    private JPanel createSummaryCard(String label, String value, Color bg, Color iconColor) {
        JPanel card = UI_Helper.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bg);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(iconColor);
        card.add(valueLabel);
        
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        labelLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        card.add(labelLabel);
        
        return card;
    }
    
    // ==================== Table Renderers ====================
    
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cell.setHorizontalAlignment(JLabel.CENTER);
            
            String status = (String) value;
            Color bg, fg;
            
            switch (status.toLowerCase()) {
                case "pending" -> { bg = new Color(254, 243, 199); fg = UI_Constants.WARNING_COLOR; }
                case "under review" -> { bg = new Color(219, 234, 254); fg = UI_Constants.INFO_COLOR; }
                case "accepted" -> { bg = new Color(209, 250, 229); fg = UI_Constants.SUCCESS_COLOR; }
                case "rejected" -> { bg = new Color(254, 226, 226); fg = UI_Constants.DANGER_COLOR; }
                default -> { bg = UI_Constants.BG_COLOR; fg = UI_Constants.TEXT_SECONDARY; }
            }
            
            cell.setBackground(bg);
            cell.setForeground(fg);
            cell.setOpaque(true);
            cell.setBorder(new EmptyBorder(5, 10, 5, 10));
            
            return cell;
        }
    }
    
    class ButtonRenderer extends JLabel implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText("View");
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setHorizontalAlignment(SwingConstants.CENTER);
            if (isSelected) {
                setForeground(UI_Constants.PRIMARY_COLOR);
                setBackground(new Color(238, 242, 255));
                setBorder(BorderFactory.createLineBorder(UI_Constants.PRIMARY_COLOR));
            } else {
                setForeground(UI_Constants.PRIMARY_COLOR);
                setBackground(UI_Constants.BG_COLOR);
                setBorder(BorderFactory.createLineBorder(UI_Constants.PRIMARY_COLOR));
            }
            setOpaque(true);
            return this;
        }
    }
}
