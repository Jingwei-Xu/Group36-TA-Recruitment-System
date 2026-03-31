package edu.ebu6304.app.ui;

import edu.ebu6304.app.data.DataImportService;
import edu.ebu6304.app.model.ApplicationRecord;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private static final String VIEW_DASHBOARD = "dashboard";
    private static final String VIEW_APPLICATION_LIST = "app_list";
    private static final String VIEW_APPLICATION_DETAIL = "app_detail";
    private static final String VIEW_APPLICATION_REVIEW = "app_review";
    private static final String VIEW_REVIEW_RECORDS = "review_records";

    private final DataImportService dataImportService;
    private final String dataRoot;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JPanel rootPanel = new JPanel(new BorderLayout());
    private final TopNavigationBar topNavigationBar;

    private final DashboardPanel dashboardPanel;
    private final ApplicationsListPanel applicationsListPanel;
    private final ApplicationDetailPanel applicationDetailPanel;
    private final ReviewApplicationPanel reviewApplicationPanel;
    private final ReviewRecordsPanel reviewRecordsPanel;

    private final List<ApplicationRecord> applications = new ArrayList<>();
    private ApplicationRecord currentRecord;

    public MainFrame(String dataRoot) {
        super("TA Management System - Standalone");
        this.dataImportService = new DataImportService();
        this.dataRoot = dataRoot;

        setSize(1260, 840);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        topNavigationBar = new TopNavigationBar(this);

        dashboardPanel = new DashboardPanel(this);
        applicationsListPanel = new ApplicationsListPanel(this);
        applicationDetailPanel = new ApplicationDetailPanel(this);
        reviewApplicationPanel = new ReviewApplicationPanel(this);
        reviewRecordsPanel = new ReviewRecordsPanel(this);

        contentPanel.add(dashboardPanel, VIEW_DASHBOARD);
        contentPanel.add(applicationsListPanel, VIEW_APPLICATION_LIST);
        contentPanel.add(applicationDetailPanel, VIEW_APPLICATION_DETAIL);
        contentPanel.add(reviewApplicationPanel, VIEW_APPLICATION_REVIEW);
        contentPanel.add(reviewRecordsPanel, VIEW_REVIEW_RECORDS);

        rootPanel.add(topNavigationBar, BorderLayout.NORTH);
        rootPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(rootPanel);

        reloadData();
        showDashboard();
    }

    public void reloadData() {
        List<ApplicationRecord> loaded = dataImportService.loadApplications(dataRoot);
        applications.clear();
        applications.addAll(loaded);
        refreshAllPanels();
    }

    public void persistReview(ApplicationRecord record, String decision, String notes) {
        dataImportService.persistReviewDecision(record, decision, notes, "u_mo_001");
        reloadData();
    }

    public void refreshAllPanels() {
        dashboardPanel.refresh(applications);
        applicationsListPanel.refresh(applications);
        reviewRecordsPanel.refresh(applications);

        if (currentRecord != null) {
            ApplicationRecord latest = findById(currentRecord.getApplicationId());
            if (latest != null) {
                currentRecord = latest;
                applicationDetailPanel.setRecord(currentRecord);
                reviewApplicationPanel.setRecord(currentRecord);
            }
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    private ApplicationRecord findById(String id) {
        return applications.stream()
                .filter(a -> a.getApplicationId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void showDashboard() {
        topNavigationBar.setActive("home");
        cardLayout.show(contentPanel, VIEW_DASHBOARD);
    }

    public void showApplicationsList() {
        topNavigationBar.setActive("review");
        applicationsListPanel.refresh(applications);
        cardLayout.show(contentPanel, VIEW_APPLICATION_LIST);
    }

    public void showApplicationDetail(ApplicationRecord record) {
        topNavigationBar.setActive("review");
        this.currentRecord = record;
        applicationDetailPanel.setRecord(record);
        cardLayout.show(contentPanel, VIEW_APPLICATION_DETAIL);
    }

    public void showReviewPage(ApplicationRecord record) {
        topNavigationBar.setActive("review");
        this.currentRecord = record;
        reviewApplicationPanel.setRecord(record);
        cardLayout.show(contentPanel, VIEW_APPLICATION_REVIEW);
    }

    public void showReviewRecords() {
        topNavigationBar.setActive("review");
        reviewRecordsPanel.refresh(applications);
        cardLayout.show(contentPanel, VIEW_REVIEW_RECORDS);
    }

    public List<ApplicationRecord> getApplications() {
        return applications;
    }

    public String getDataRoot() {
        return dataRoot;
    }

    public String getDataRootDisplay() {
        return Path.of(dataRoot).toAbsolutePath().toString();
    }
}
