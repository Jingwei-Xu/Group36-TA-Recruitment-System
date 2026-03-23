package taportal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * TA职位申请系统 - Swing桌面应用
 */
public class TAPortalApp extends JFrame {
    
    private DataService dataService;
    private TAUser currentUser;
    
    // Main container
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JPanel navLinksPanel;
    private JButton homeBtn;
    private JButton profileNavBtn;
    private JButton jobsNavBtn;
    
    // Page panels
    private Map<String, JPanel> pages = new HashMap<>();
    
    // Color palette
    private static final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private static final Color PRIMARY_HOVER = new Color(67, 56, 202);
    private static final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color INFO_COLOR = new Color(59, 130, 246);
    private static final Color BG_COLOR = new Color(249, 250, 251);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    /** 原型中的深色主按钮（近黑） */
    private static final Color DARK_BUTTON = new Color(31, 41, 55);
    private static final Color DARK_BUTTON_HOVER = new Color(17, 24, 39);
    private static final Color NAV_ACTIVE_BG = new Color(243, 244, 246);
    
    public TAPortalApp() {
        dataService = DataService.getInstance();
        currentUser = dataService.getCurrentUser();
        
        initFrame();
        initNavigation();
        initDashboard();
        initJobsPage();
        initJobDetailPage();
        initApplyPage();
        initApplicationsPage();
        initStatusPage();
        initProfilePage();
        
        showPage("dashboard");
        setVisible(true);
    }
    
    private void initFrame() {
        setTitle("TA System - Dashboard");
        setSize(1400, 900);
        setMinimumSize(new Dimension(1200, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main layout
        setLayout(new BorderLayout());
        
        // Content panel with CardLayout（各页面在构造函数中初始化一次，避免重复添加）
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BG_COLOR);
        
        add(mainContentPanel, BorderLayout.CENTER);
    }
    
    private void initNavigation() {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(CARD_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        navPanel.setPreferredSize(new Dimension(0, 64));
        
        // Brand
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);
        JLabel logoLabel = new JLabel("\u2709"); // Mail icon
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        logoLabel.setForeground(PRIMARY_COLOR);
        brandPanel.add(logoLabel);
        
        JLabel titleLabel = new JLabel("TA System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        brandPanel.add(titleLabel);
        
        // Navigation links
        navLinksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        navLinksPanel.setOpaque(false);
        
        homeBtn = createNavButton("Home", "dashboard");
        profileNavBtn = createNavButton("Profile Module", "profile");
        jobsNavBtn = createNavButton("Job Application Module", "jobs");
        
        styleNavButtonActive(homeBtn);
        
        navLinksPanel.add(homeBtn);
        navLinksPanel.add(profileNavBtn);
        navLinksPanel.add(jobsNavBtn);
        
        // Logout（与原型一致）
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);
        JButton logoutBtn = new JButton("\u2192 Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logoutBtn.setForeground(TEXT_SECONDARY);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this, "确定要退出吗？", "Logout", JOptionPane.OK_CANCEL_OPTION);
            if (ok == JOptionPane.OK_OPTION) {
                dispose();
                System.exit(0);
            }
        });
        userPanel.add(logoutBtn);
        
        navPanel.add(brandPanel, BorderLayout.WEST);
        navPanel.add(navLinksPanel, BorderLayout.CENTER);
        navPanel.add(userPanel, BorderLayout.EAST);
        
        add(navPanel, BorderLayout.NORTH);
    }
    
    private JButton createNavButton(String text, String page) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        styleNavButtonInactive(btn);
        btn.addActionListener(e -> showPage(page));
        return btn;
    }
    
    private void updateNavButtons(JButton activeBtn) {
        for (Component c : navLinksPanel.getComponents()) {
            if (c instanceof JButton btn) {
                if (btn == activeBtn) {
                    styleNavButtonActive(btn);
                } else {
                    styleNavButtonInactive(btn);
                }
            }
        }
    }
    
    private void styleNavButtonInactive(JButton btn) {
        btn.setForeground(TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBackground(null);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
    }
    
    private void styleNavButtonActive(JButton btn) {
        btn.setForeground(TEXT_PRIMARY);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBackground(NAV_ACTIVE_BG);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(9, 15, 9, 15)
        ));
    }
    
    // ==================== DASHBOARD PAGE ====================
    private void initDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(BG_COLOR);
        dashboard.setBorder(new EmptyBorder(24, 48, 32, 48));
        pages.put("dashboard", dashboard);
        mainContentPanel.add(dashboard, "dashboard");
        
        // 顶部标题（靠左，与原型一致）
        JPanel topTitle = new JPanel(new BorderLayout());
        topTitle.setOpaque(false);
        JLabel titleMain = new JLabel("TA Dashboard");
        titleMain.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleMain.setForeground(TEXT_PRIMARY);
        topTitle.add(titleMain, BorderLayout.NORTH);
        JLabel subtitle = new JLabel("Welcome! Please select a function module to get started.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setBorder(new EmptyBorder(8, 0, 0, 0));
        topTitle.add(subtitle, BorderLayout.SOUTH);
        dashboard.add(topTitle, BorderLayout.NORTH);
        
        // 中部：两个功能模块在可视区域水平+垂直居中
        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        cardsRow.setOpaque(false);
        
        // Profile 模块卡片
        JPanel profileCard = createDashboardModuleCard(
            "\uD83D\uDC64",
            "Profile Module",
            "Manage personal information, skills, and CV",
            new Color(219, 234, 254),
            INFO_COLOR
        );
        JButton goProfile = createDarkPrimaryButton("Go to Profile");
        goProfile.setPreferredSize(new Dimension(300, 44));
        goProfile.addActionListener(e -> showPage("profile"));
        JPanel profileBtnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        profileBtnWrap.setOpaque(false);
        profileBtnWrap.add(goProfile);
        profileCard.add(profileBtnWrap, BorderLayout.SOUTH);
        cardsRow.add(profileCard);
        
        // Job Application 模块卡片
        JPanel jobCard = createDashboardModuleCard(
            "\uD83D\uDCBC",
            "Job Application Module",
            "Browse jobs, apply, and track application status",
            new Color(209, 250, 229),
            SUCCESS_COLOR
        );
        // 使用 GridLayout 并排固定两列，避免 FlowLayout 换行后第二行被裁切；Windows L&F 下按钮需 opaque 才绘制背景
        JPanel jobActions = new JPanel(new GridLayout(1, 2, 12, 0));
        jobActions.setOpaque(false);
        JButton browseJobsBtn = createDarkPrimaryButton("Browse Jobs");
        browseJobsBtn.addActionListener(e -> showPage("jobs"));
        JButton myAppsBtn = createOutlineButton("My Applications");
        myAppsBtn.addActionListener(e -> showPage("applications"));
        jobActions.add(browseJobsBtn);
        jobActions.add(myAppsBtn);
        jobCard.add(jobActions, BorderLayout.SOUTH);
        // Job 卡片略加宽，容纳两个按钮文案（高 DPI 下更稳）
        jobCard.setPreferredSize(new Dimension(420, 280));
        jobCard.setMinimumSize(new Dimension(400, 260));
        cardsRow.add(jobCard);
        
        centerWrap.add(cardsRow, gbc);
        dashboard.add(centerWrap, BorderLayout.CENTER);
        
        // 底部 Quick Status Overview
        JPanel statusSection = new JPanel();
        statusSection.setLayout(new BoxLayout(statusSection, BoxLayout.Y_AXIS));
        statusSection.setOpaque(false);
        statusSection.setBorder(new EmptyBorder(16, 0, 0, 0));
        
        JLabel statusTitle = new JLabel("Quick Status Overview");
        statusTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusTitle.setForeground(TEXT_PRIMARY);
        statusTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusTitle.setBorder(new EmptyBorder(0, 0, 16, 0));
        statusSection.add(statusTitle);
        
        JPanel statusRow = new JPanel(new GridLayout(1, 3, 20, 0));
        statusRow.setOpaque(false);
        TAUser.ApplicationSummary summary = currentUser.getApplicationSummary();
        statusRow.add(createQuickStatusItem("\u2713", SUCCESS_COLOR, "Profile Completion",
            currentUser.getProfileCompletion() + "%"));
        statusRow.add(createQuickStatusItem("\u2191", INFO_COLOR, "CV Upload Status",
            currentUser.getCv().isUploaded() ? "Uploaded" : "Not Uploaded"));
        statusRow.add(createQuickStatusItem("\uD83D\uDCC4", PRIMARY_COLOR, "Number of Applications",
            String.valueOf(summary.getTotalApplications())));
        statusSection.add(statusRow);
        
        dashboard.add(statusSection, BorderLayout.SOUTH);
    }
    
    /** 仪表盘功能模块卡片（固定宽度，白底描边） */
    private JPanel createDashboardModuleCard(String icon, String title, String desc,
            Color iconBg, Color iconFg) {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(28, 28, 28, 28)
        ));
        card.setPreferredSize(new Dimension(380, 260));
        card.setMaximumSize(new Dimension(420, 320));
        
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        header.setOpaque(false);
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        iconPanel.setPreferredSize(new Dimension(52, 52));
        iconPanel.setLayout(new GridBagLayout());
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        iconLbl.setForeground(iconFg);
        iconPanel.add(iconLbl);
        header.add(iconPanel);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(TEXT_PRIMARY);
        header.add(titleLbl);
        card.add(header, BorderLayout.NORTH);
        
        JLabel descLbl = new JLabel("<html><div style='width:300px'>" + desc + "</div></html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLbl.setForeground(TEXT_SECONDARY);
        card.add(descLbl, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createQuickStatusItem(String icon, Color iconColor, String label, String value) {
        JPanel p = new JPanel(new BorderLayout(12, 8));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLbl.setForeground(iconColor);
        p.add(iconLbl, BorderLayout.WEST);
        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(TEXT_PRIMARY);
        text.add(val, BorderLayout.NORTH);
        JLabel lab = new JLabel(label);
        lab.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lab.setForeground(TEXT_SECONDARY);
        text.add(lab, BorderLayout.SOUTH);
        p.add(text, BorderLayout.CENTER);
        return p;
    }
    
    private JButton createDarkPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(DARK_BUTTON);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(DARK_BUTTON_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(DARK_BUTTON);
            }
        });
        return btn;
    }
    
    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_COLOR);               // 浅灰背景，与卡片白底区分开
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 65, 81), 1),
            new EmptyBorder(10, 12, 10, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(PRIMARY_COLOR);
                btn.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(BG_COLOR);
                btn.setForeground(TEXT_PRIMARY);
            }
        });
        return btn;
    }
    
    // ==================== JOBS PAGE ====================
    private JPanel jobsPage;
    private JTextField searchField;
    private JComboBox<String> departmentFilter;
    private JComboBox<String> jobTypeFilter;
    private JPanel jobsListPanel;
    private List<Job> filteredJobs;
    private JLabel jobListCountLabel;
    
    private void initJobsPage() {
        jobsPage = new JPanel(new BorderLayout(0, 0));
        jobsPage.setBackground(BG_COLOR);
        jobsPage.setBorder(new EmptyBorder(16, 48, 32, 48));
        pages.put("jobs", jobsPage);
        mainContentPanel.add(jobsPage, "jobs");
        filteredJobs = new ArrayList<>(dataService.getOpenJobs());
        
        JPanel northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);
        
        // ← Back to Home
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        JButton backHome = new JButton("\u2190 Back to Home");
        backHome.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backHome.setForeground(TEXT_SECONDARY);
        backHome.setContentAreaFilled(false);
        backHome.setBorder(new EmptyBorder(0, 0, 16, 0));
        backHome.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backHome.addActionListener(e -> showPage("dashboard"));
        backRow.add(backHome);
        northStack.add(backRow);
        
        // 标题行：左标题 + 右 My Applications（白底描边）
        JPanel titleRow = new JPanel(new BorderLayout(24, 0));
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(0, 0, 20, 0));
        JPanel titleLeft = new JPanel(new BorderLayout(0, 6));
        titleLeft.setOpaque(false);
        JLabel titleLabel = new JLabel("Available Jobs");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLeft.add(titleLabel, BorderLayout.NORTH);
        JLabel subtitleLabel = new JLabel("Browse all open TA positions.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        titleLeft.add(subtitleLabel, BorderLayout.SOUTH);
        titleRow.add(titleLeft, BorderLayout.WEST);
        JButton myAppsBtn = createJobsPageOutlineButton("\uD83D\uDCCB  My Applications");
        myAppsBtn.addActionListener(e -> showPage("applications"));
        JPanel myAppsWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        myAppsWrap.setOpaque(false);
        myAppsWrap.add(myAppsBtn);
        titleRow.add(myAppsWrap, BorderLayout.EAST);
        northStack.add(titleRow);
        
        // 搜索与筛选：白底卡片一行
        JPanel searchCard = new JPanel(new BorderLayout(16, 0));
        searchCard.setBackground(CARD_BG);
        searchCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(14, 18, 14, 18)
        ));
        
        JPanel searchWithIcon = new JPanel(new BorderLayout(10, 0));
        searchWithIcon.setOpaque(false);
        JLabel magIcon = new JLabel("\uD83D\uDD0D");
        magIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        magIcon.setForeground(TEXT_SECONDARY);
        magIcon.setBorder(new EmptyBorder(0, 4, 0, 0));
        searchWithIcon.add(magIcon, BorderLayout.WEST);
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        searchField.putClientProperty("JTextField.placeholderText", "Search by job title or course...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterJobs(); }
            public void removeUpdate(DocumentEvent e) { filterJobs(); }
            public void insertUpdate(DocumentEvent e) { filterJobs(); }
        });
        searchWithIcon.add(searchField, BorderLayout.CENTER);
        
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        filters.setOpaque(false);
        JLabel funnel = new JLabel("\u25BC ");
        funnel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        funnel.setForeground(TEXT_SECONDARY);
        filters.add(funnel);
        departmentFilter = new JComboBox<>(new String[]{"All Departments", "Computer Science", "Mathematics", "Physics", "Chemistry", "Biology"});
        styleJobsFilterCombo(departmentFilter);
        departmentFilter.addActionListener(e -> filterJobs());
        filters.add(departmentFilter);
        jobTypeFilter = new JComboBox<>(new String[]{"All Job Types", "TA", "Lab TA", "Grading TA", "Part-time TA"});
        styleJobsFilterCombo(jobTypeFilter);
        jobTypeFilter.addActionListener(e -> filterJobs());
        filters.add(jobTypeFilter);
        
        searchCard.add(searchWithIcon, BorderLayout.CENTER);
        searchCard.add(filters, BorderLayout.EAST);
        northStack.add(searchCard);
        
        jobListCountLabel = new JLabel(" ");
        jobListCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jobListCountLabel.setForeground(TEXT_SECONDARY);
        jobListCountLabel.setBorder(new EmptyBorder(12, 4, 16, 0));
        jobListCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        northStack.add(jobListCountLabel);
        
        jobsPage.add(northStack, BorderLayout.NORTH);
        
        jobsListPanel = new JPanel();
        jobsListPanel.setLayout(new BoxLayout(jobsListPanel, BoxLayout.Y_AXIS));
        jobsListPanel.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(jobsListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        jobsPage.add(scrollPane, BorderLayout.CENTER);
        
        refreshJobsList();
    }
    
    /** 职位列表页右上角「My Applications」白底按钮 */
    private JButton createJobsPageOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(CARD_BG);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 18, 10, 18)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BG_COLOR);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(CARD_BG);
            }
        });
        return btn;
    }
    
    private void styleJobsFilterCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(CARD_BG);
        combo.setPreferredSize(new Dimension(168, 36));
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(4, 10, 4, 10)
        ));
    }
    
    private void filterJobs() {
        filteredJobs.clear();
        String search = searchField.getText().toLowerCase();
        String dept = (String) departmentFilter.getSelectedItem();
        String type = (String) jobTypeFilter.getSelectedItem();
        
        for (Job job : dataService.getOpenJobs()) {
            boolean match = true;
            
            if (!search.isEmpty()) {
                String title = job.getTitle().toLowerCase();
                String course = job.getCourseCode().toLowerCase();
                if (!title.contains(search) && !course.contains(search)) {
                    match = false;
                }
            }
            
            if (dept != null && !dept.equals("All Departments")) {
                if (!job.getDepartment().equals(dept)) {
                    match = false;
                }
            }
            
            if (type != null && !type.equals("All Job Types")) {
                if (!job.getEmploymentType().contains(type)) {
                    match = false;
                }
            }
            
            if (match) {
                filteredJobs.add(job);
            }
        }
        
        refreshJobsList();
    }
    
    private void refreshJobsList() {
        jobsListPanel.removeAll();
        
        int total = dataService.getOpenJobs().size();
        int shown = filteredJobs.size();
        if (jobListCountLabel != null) {
            jobListCountLabel.setText("Showing " + shown + " of " + total + " positions");
        }
        
        if (filteredJobs.isEmpty()) {
            JLabel emptyLabel = new JLabel("No jobs found matching your criteria");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            emptyLabel.setForeground(TEXT_SECONDARY);
            emptyLabel.setAlignmentX(CENTER_ALIGNMENT);
            emptyLabel.setBorder(new EmptyBorder(50, 0, 50, 0));
            jobsListPanel.add(emptyLabel);
        } else {
            for (Job job : filteredJobs) {
                jobsListPanel.add(createJobCard(job));
                jobsListPanel.add(Box.createVerticalStrut(20));
            }
        }
        
        jobsListPanel.revalidate();
        jobsListPanel.repaint();
    }
    
    /** 截止日期展示为 March 25, 2026 */
    private String formatDeadlinePretty(Job job) {
        String raw = job.getDeadlineDisplay();
        if (raw == null || raw.length() < 10) {
            return raw != null ? raw : "";
        }
        String ymd = raw.substring(0, 10);
        String[] p = ymd.split("-");
        if (p.length != 3) {
            return "Deadline: " + raw;
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
        return raw;
    }
    
    /**
     * 职位卡片：左（标题 / 课程·系·教师 / 摘要 / 底部图标行），右深色「View Details &gt;」垂直居中
     */
    private JPanel createJobCard(Job job) {
        JPanel card = new JPanel(new BorderLayout(28, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(22, 26, 22, 26)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        
        JLabel titleLabel = new JLabel(job.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(titleLabel);
        
        left.add(Box.createVerticalStrut(6));
        String meta = job.getCourseCode() + "  \u2022  " + job.getDepartment() + "  \u2022  " + job.getInstructorName();
        JLabel metaLabel = new JLabel(meta);
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        metaLabel.setForeground(TEXT_SECONDARY);
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(metaLabel);
        
        left.add(Box.createVerticalStrut(8));
        String summary = job.getSummary();
        if (summary == null || summary.isEmpty()) {
            summary = job.getDescription();
        }
        if (summary != null && summary.length() > 120) {
            summary = summary.substring(0, 117) + "...";
        }
        JLabel sumLabel = new JLabel("<html><div style='width:720px'>" + (summary != null ? summary : "") + "</div></html>");
        sumLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sumLabel.setForeground(new Color(75, 85, 99));
        sumLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(sumLabel);
        
        left.add(Box.createVerticalStrut(14));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(createJobMetaChip("\u23F0", job.getWeeklyHoursDisplay()));
        footer.add(createJobMetaChip("\uD83D\uDCC5", "Deadline: " + formatDeadlinePretty(job)));
        footer.add(createJobMetaChip("\uD83D\uDCCD", job.getLocationMode()));
        left.add(footer);
        
        card.add(left, BorderLayout.CENTER);
        
        JButton viewBtn = createDarkPrimaryButton("View Details  >");
        viewBtn.setPreferredSize(new Dimension(160, 44));
        viewBtn.addActionListener(e -> showJobDetail(job));
        JPanel btnCol = new JPanel(new GridBagLayout());
        btnCol.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        btnCol.add(viewBtn, c);
        card.add(btnCol, BorderLayout.EAST);
        
        return card;
    }
    
    private JLabel createJobMetaChip(String icon, String text) {
        JLabel l = new JLabel(icon + "  " + text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_SECONDARY);
        return l;
    }
    
    private JLabel createDetailItem(String icon, String text) {
        JLabel label = new JLabel(icon + " " + text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_SECONDARY);
        return label;
    }
    
    // ==================== JOB DETAIL PAGE ====================
    private void initJobDetailPage() {
        JPanel page = createPagePanel();
        pages.put("job-detail", page);
        mainContentPanel.add(page, "job-detail");
    }
    
    private void showJobDetail(Job job) {
        JPanel page = pages.get("job-detail");
        page.removeAll();
        
        // Back button
        JButton backBtn = new JButton("\u2190 Back to Jobs");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setForeground(TEXT_SECONDARY);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorder(new EmptyBorder(0, 0, 20, 0));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> showPage("jobs"));
        page.add(backBtn);
        
        // Main container
        JPanel content = new JPanel(new BorderLayout(30, 0));
        content.setOpaque(false);
        
        // Left column - Job Info
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        
        JLabel title = new JLabel(job.getTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        leftPanel.add(title);
        
        JPanel tags = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        tags.setOpaque(false);
        tags.add(createTag(job.getCourseCode(), new Color(219, 234, 254), INFO_COLOR));
        tags.add(Box.createHorizontalStrut(8));
        tags.add(createTag(job.getDepartment(), new Color(209, 250, 229), SUCCESS_COLOR));
        leftPanel.add(tags);
        
        // Instructor
        addSection(leftPanel, "Instructor", job.getInstructorName(), TEXT_PRIMARY);
        addSection(leftPanel, "Email", job.getInstructorEmail(), TEXT_SECONDARY);
        
        // Description
        addSection(leftPanel, "Job Description", job.getDescription(), TEXT_PRIMARY);
        
        // Responsibilities
        JLabel respTitle = new JLabel("Responsibilities");
        respTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        respTitle.setForeground(TEXT_PRIMARY);
        respTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        leftPanel.add(respTitle);
        
        for (String resp : job.getResponsibilities()) {
            JLabel item = new JLabel("\u2022 " + resp);
            item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            item.setForeground(TEXT_PRIMARY);
            item.setBorder(new EmptyBorder(3, 0, 3, 0));
            leftPanel.add(item);
        }
        
        // Requirements
        JLabel reqTitle = new JLabel("Requirements");
        reqTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        reqTitle.setForeground(TEXT_PRIMARY);
        reqTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        leftPanel.add(reqTitle);
        
        for (String req : job.getRequirements()) {
            JLabel item = new JLabel("\u2022 " + req);
            item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            item.setForeground(TEXT_PRIMARY);
            item.setBorder(new EmptyBorder(3, 0, 3, 0));
            leftPanel.add(item);
        }
        
        // Preferred Skills
        JLabel skillsTitle = new JLabel("Preferred Skills");
        skillsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        skillsTitle.setForeground(TEXT_PRIMARY);
        skillsTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        leftPanel.add(skillsTitle);
        
        JPanel skillsTags = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        skillsTags.setOpaque(false);
        for (String skill : job.getPreferredSkills()) {
            skillsTags.add(createSkillTag(skill));
            skillsTags.add(Box.createHorizontalStrut(8));
        }
        leftPanel.add(skillsTags);
        
        content.add(leftPanel, BorderLayout.CENTER);
        
        // Right column - Summary
        JPanel summaryPanel = createCard("");
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JLabel summaryTitle = new JLabel("Position Summary");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        summaryTitle.setForeground(TEXT_PRIMARY);
        summaryTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        summaryPanel.add(summaryTitle);
        
        addSummaryItem(summaryPanel, "Employment Type", job.getEmploymentType());
        addSummaryItem(summaryPanel, "Weekly Hours", job.getWeeklyHoursDisplay());
        addSummaryItem(summaryPanel, "Application Deadline", job.getDeadlineDisplay());
        addSummaryItem(summaryPanel, "Work Mode", job.getLocationMode());
        
        JButton applyBtn = createPrimaryButton("Apply Now");
        applyBtn.setPreferredSize(new Dimension(0, 45));
        applyBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        applyBtn.addActionListener(e -> showApplyPage(job));
        summaryPanel.add(Box.createVerticalStrut(20));
        summaryPanel.add(applyBtn);
        
        content.add(summaryPanel, BorderLayout.EAST);
        content.setPreferredSize(new Dimension(0, 600));
        
        page.add(content);
        
        showPage("job-detail");
    }
    
    private void addSection(JPanel panel, String label, String value, Color valueColor) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setBorder(new EmptyBorder(12, 0, 5, 0));
        panel.add(lbl);
        
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        val.setForeground(valueColor);
        panel.add(val);
    }
    
    private void addSummaryItem(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 0, 8, 0));
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_SECONDARY);
        row.add(lbl, BorderLayout.WEST);
        
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        val.setForeground(TEXT_PRIMARY);
        row.add(val, BorderLayout.EAST);
        
        panel.add(row);
    }
    
    // ==================== APPLY PAGE ====================
    private void initApplyPage() {
        JPanel page = createPagePanel();
        pages.put("apply", page);
        mainContentPanel.add(page, "apply");
    }
    
    private void showApplyPage(Job job) {
        JPanel page = pages.get("apply");
        page.removeAll();
        
        // Back button
        JButton backBtn = new JButton("\u2190 Back to Job Details");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setForeground(TEXT_SECONDARY);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorder(new EmptyBorder(0, 0, 20, 0));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> showJobDetail(job));
        page.add(backBtn);
        
        // Header
        JLabel title = new JLabel("Apply for Position");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        page.add(title);
        
        // Position info
        JPanel posInfo = createCard("");
        posInfo.setLayout(new BoxLayout(posInfo, BoxLayout.Y_AXIS));
        posInfo.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel posTitle = new JLabel(job.getTitle());
        posTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        posTitle.setForeground(TEXT_PRIMARY);
        posInfo.add(posTitle);
        
        JPanel posDetails = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        posDetails.setOpaque(false);
        posDetails.add(createDetailItem("\uD83D\uDCC4", job.getCourseCode() + " - " + job.getCourse().getCourseName()));
        posDetails.add(Box.createHorizontalStrut(20));
        posDetails.add(createDetailItem("\uD83D\uDC64", job.getInstructorName()));
        posDetails.add(Box.createHorizontalStrut(20));
        JLabel deadlineLbl = createDetailItem("\u23F0", "Deadline: " + job.getDeadlineDisplay());
        deadlineLbl.setForeground(WARNING_COLOR);
        posDetails.add(deadlineLbl);
        posInfo.add(posDetails);
        
        page.add(posInfo);
        
        // Form
        JPanel form = createCard("");
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Pre-filled info
        JLabel section1 = new JLabel("Your Information (from Profile)");
        section1.setFont(new Font("Segoe UI", Font.BOLD, 16));
        section1.setForeground(TEXT_PRIMARY);
        section1.setBorder(new EmptyBorder(0, 0, 15, 0));
        form.add(section1);
        
        JPanel grid1 = new JPanel(new GridLayout(3, 2, 15, 15));
        grid1.setOpaque(false);
        grid1.add(createReadOnlyField("Full Name", currentUser.getProfile().getFullName()));
        grid1.add(createReadOnlyField("Student ID", currentUser.getProfile().getStudentId()));
        grid1.add(createReadOnlyField("Email", currentUser.getAccount().getEmail()));
        grid1.add(createReadOnlyField("Phone", currentUser.getProfile().getPhoneNumber()));
        grid1.add(createReadOnlyField("Program/Major", currentUser.getProfile().getProgramMajor()));
        grid1.add(createReadOnlyField("Year", currentUser.getProfile().getYear()));
        form.add(grid1);
        
        // Application Details
        JLabel section2 = new JLabel("Application Details");
        section2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        section2.setForeground(TEXT_PRIMARY);
        section2.setBorder(new EmptyBorder(25, 0, 15, 0));
        form.add(section2);
        
        JTextField skillsField = createTextField("Relevant Skills (comma separated) *");
        JTextArea experienceArea = createTextArea("Relevant Experience *", 4);
        JTextField availabilityField = createTextField("Availability *");
        JTextArea motivationArea = createTextArea("Motivation Letter *", 5);
        
        form.add(skillsField);
        form.add(Box.createVerticalStrut(10));
        form.add(experienceArea);
        form.add(Box.createVerticalStrut(10));
        form.add(availabilityField);
        form.add(Box.createVerticalStrut(10));
        form.add(motivationArea);
        
        // Submit buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(25, 0, 0, 0));
        
        JButton cancelBtn = createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> showJobDetail(job));
        
        JButton submitBtn = createPrimaryButton("Submit Application");
        submitBtn.addActionListener(e -> {
            // Create and submit application
            Application app = new Application();
            
            // Job snapshot
            Application.JobSnapshot jobSnap = new Application.JobSnapshot();
            jobSnap.setTitle(job.getTitle());
            jobSnap.setCourseCode(job.getCourseCode());
            jobSnap.setCourseName(job.getCourse().getCourseName());
            jobSnap.setDepartment(job.getDepartment());
            jobSnap.setInstructorName(job.getInstructorName());
            jobSnap.setInstructorEmail(job.getInstructorEmail());
            jobSnap.setDeadline(job.getDeadlineDisplay());
            jobSnap.setEmploymentType(job.getEmploymentType());
            jobSnap.setWeeklyHours(job.getEmployment().getWeeklyHours());
            jobSnap.setLocationMode(job.getLocationMode());
            app.setJobSnapshot(jobSnap);
            
            // Applicant snapshot
            Application.ApplicantSnapshot appSnap = new Application.ApplicantSnapshot();
            appSnap.setFullName(currentUser.getProfile().getFullName());
            appSnap.setStudentId(currentUser.getProfile().getStudentId());
            appSnap.setEmail(currentUser.getAccount().getEmail());
            appSnap.setPhoneNumber(currentUser.getProfile().getPhoneNumber());
            appSnap.setProgramMajor(currentUser.getProfile().getProgramMajor());
            appSnap.setYear(currentUser.getProfile().getYear());
            appSnap.setGpa(currentUser.getAcademic().getGpa());
            app.setApplicantSnapshot(appSnap);
            
            // Form data
            Application.ApplicationForm appForm = new Application.ApplicationForm();
            String[] skills = skillsField.getText().split(",");
            appForm.setRelevantSkills(Arrays.stream(skills).map(String::trim).toList());
            appForm.setRelevantExperience(experienceArea.getText());
            appForm.setAvailability(availabilityField.getText());
            appForm.setMotivationCoverLetter(motivationArea.getText());
            app.setApplicationForm(appForm);
            
            app.setJobId(job.getJobId());
            
            dataService.addApplication(app);
            
            JOptionPane.showMessageDialog(this, 
                "Application submitted successfully!\n\nYou can track your application status in 'My Applications'.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            showPage("applications");
            refreshApplicationsTable();
        });
        
        buttons.add(cancelBtn);
        buttons.add(submitBtn);
        form.add(buttons);
        
        page.add(form);
        
        showPage("apply");
    }
    
    // ==================== APPLICATIONS PAGE ====================
    private JPanel applicationsPage;
    private JTable applicationsTable;
    private DefaultTableModel tableModel;
    
    private void initApplicationsPage() {
        // 必须用 BorderLayout：createPagePanel() 是 BoxLayout，若混用 BorderLayout 约束会导致表格区域异常、点击无响应
        applicationsPage = new JPanel(new BorderLayout());
        applicationsPage.setBackground(BG_COLOR);
        applicationsPage.setBorder(new EmptyBorder(30, 40, 30, 40));
        pages.put("applications", applicationsPage);
        mainContentPanel.add(applicationsPage, "applications");
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        JPanel headerLeft = new JPanel(new BorderLayout());
        headerLeft.setOpaque(false);
        
        JLabel titleLabel = new JLabel("My Applications");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerLeft.add(titleLabel, BorderLayout.NORTH);
        
        JLabel subtitleLabel = new JLabel("Track and manage your TA applications");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        headerLeft.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Summary cards
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(new EmptyBorder(0, 0, 25, 0));
        
        summaryPanel.add(createSummaryCard("Total Applications", 
            String.valueOf(dataService.countApplicationsByStatus("pending") + 
                          dataService.countApplicationsByStatus("under_review") +
                          dataService.countApplicationsByStatus("accepted") +
                          dataService.countApplicationsByStatus("rejected")),
            new Color(219, 234, 254), INFO_COLOR));
        
        summaryPanel.add(createSummaryCard("Pending", 
            String.valueOf(dataService.countApplicationsByStatus("pending")),
            new Color(254, 243, 199), WARNING_COLOR));
        
        summaryPanel.add(createSummaryCard("Accepted", 
            String.valueOf(dataService.countApplicationsByStatus("accepted")),
            new Color(209, 250, 229), SUCCESS_COLOR));
        
        summaryPanel.add(createSummaryCard("Rejected", 
            String.valueOf(dataService.countApplicationsByStatus("rejected")),
            new Color(254, 226, 226), DANGER_COLOR));
        
        JPanel northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);
        northStack.add(headerLeft);
        northStack.add(summaryPanel);
        applicationsPage.add(northStack, BorderLayout.NORTH);
        
        // Table
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
        applicationsTable.setGridColor(BORDER_COLOR);
        applicationsTable.setShowGrid(true);
        applicationsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        applicationsTable.getTableHeader().setBackground(BG_COLOR);
        applicationsTable.getTableHeader().setForeground(TEXT_PRIMARY);
        applicationsTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        // Center align cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < columns.length; i++) {
            applicationsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(applicationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(CARD_BG);

        applicationsPage.add(scrollPane, BorderLayout.CENTER);

        // 监听器和渲染器只需注册一次（用 mousePressed：Windows 上 mouseClicked 常因轻微位移不触发）
        applicationsTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        applicationsTable.getColumn("Status").setCellRenderer(new StatusRenderer());
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
                    System.out.println("[DEBUG] View clicked! row=" + modelRow + ", app=" + apps.get(modelRow).getApplicationId());
                    showStatusPage(apps.get(modelRow));
                }
            }
        });
    }
    
    private JPanel createSummaryCard(String label, String value, Color bg, Color iconColor) {
        JPanel card = createCard("");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bg);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(iconColor);
        card.add(valueLabel);
        
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelLabel.setForeground(TEXT_PRIMARY);
        labelLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        card.add(labelLabel);
        
        return card;
    }
    
    private void refreshApplicationsTable() {
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
    
    private Color getStatusColor(String color) {
        if (color == null) {
            return TEXT_SECONDARY;
        }
        return switch (color) {
            case "yellow" -> WARNING_COLOR;
            case "blue" -> INFO_COLOR;
            case "green" -> SUCCESS_COLOR;
            case "red" -> DANGER_COLOR;
            default -> TEXT_SECONDARY;
        };
    }
    
    // ==================== STATUS PAGE ====================
    private void initStatusPage() {
        JPanel page = createPagePanel();
        pages.put("status", page);
        mainContentPanel.add(page, "status");
    }
    
    private void showStatusPage(Application app) {
        JPanel page = pages.get("status");
        page.removeAll();
        
        try {
        
        // Back button
        JButton backBtn = new JButton("\u2190 Back to My Applications");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setForeground(TEXT_SECONDARY);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorder(new EmptyBorder(0, 0, 20, 0));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            showPage("applications");
            refreshApplicationsTable();
        });
        page.add(backBtn);
        
        // Header
        JPanel header = createCard("");
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JLabel title = new JLabel(app.getJobSnapshot().getTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        header.add(title);
        
        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        meta.setOpaque(false);
        meta.add(createMetaItem("Course: ", app.getJobSnapshot().getCourseCode()));
        meta.add(Box.createHorizontalStrut(20));
        meta.add(createMetaItem("Applicant: ", app.getApplicantSnapshot().getFullName()));
        meta.add(Box.createHorizontalStrut(20));
        meta.add(createMetaItem("Applied: ", app.getMeta().getSubmittedAt().substring(0, 10)));
        meta.add(Box.createHorizontalStrut(20));
        
        JLabel statusLbl = new JLabel(app.getStatus().getLabel());
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLbl.setForeground(getStatusColor(app.getStatus().getColor()));
        statusLbl.setOpaque(true);
        statusLbl.setBackground(getStatusColor(app.getStatus().getColor()).brighter().brighter());
        statusLbl.setBorder(new EmptyBorder(5, 12, 5, 12));
        statusLbl.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                ((JLabel)c).setVerticalTextPosition(JLabel.CENTER);
                super.paint(g, c);
            }
        });
        meta.add(statusLbl);
        
        header.add(meta);
        page.add(header);
        
        // Timeline
        JPanel timelineSection = createCard("");
        timelineSection.setLayout(new BoxLayout(timelineSection, BoxLayout.Y_AXIS));
        timelineSection.setBorder(new EmptyBorder(20, 25, 20, 25));
        timelineSection.setPreferredSize(new Dimension(0, 180));
        
        JLabel timelineTitle = new JLabel("Application Progress");
        timelineTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timelineTitle.setForeground(TEXT_PRIMARY);
        timelineTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        timelineSection.add(timelineTitle);
        
        // Timeline steps
        JPanel timeline = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        timeline.setOpaque(false);
        
        String[] steps = {"Application Submitted", "Under Review", "Interview Scheduled", "Decision"};
        String[] stepKeys = {"submitted", "under_review", "interview_scheduled", "decision"};
        
        List<Application.TimelineEvent> tl = app.getTimeline();
        if (tl == null) {
            tl = List.of();
        }
        for (int i = 0; i < steps.length; i++) {
            boolean completed = false;
            boolean current = false;
            
            for (Application.TimelineEvent event : tl) {
                if (event.getStepKey().equals(stepKeys[i]) || 
                    (i == 0 && event.getStepKey().equals("submitted")) ||
                    (i == 3 && (app.getStatus().getCurrent().equals("accepted") || app.getStatus().getCurrent().equals("rejected")))) {
                    completed = true;
                    if (i == steps.length - 1) {
                        current = app.getStatus().getCurrent().equals("accepted") || app.getStatus().getCurrent().equals("rejected");
                    } else {
                        current = app.getStatus().getCurrent().equals("under_review") && i == 1;
                    }
                }
            }
            
            timeline.add(createTimelineStep(steps[i], completed, current));
            
            if (i < steps.length - 1) {
                final boolean isCompleted = completed;
                JPanel connector = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setStroke(new BasicStroke(2));
                        g2.setColor(isCompleted ? SUCCESS_COLOR : BORDER_COLOR);
                        g2.drawLine(0, 15, 60, 15);
                    }
                };
                connector.setPreferredSize(new Dimension(60, 30));
                connector.setOpaque(false);
                timeline.add(connector);
            }
        }
        
        timelineSection.add(timeline);
        page.add(timelineSection);
        
        // Status Details（review 可能为 null，避免 NPE 导致界面不切换）
        Application.Review review = app.getReview();
        JPanel details = new JPanel(new GridLayout(1, 3, 20, 20));
        details.setOpaque(false);
        details.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        details.add(createDetailSection("Status Information", 
            review != null && review.getStatusMessage() != null && !review.getStatusMessage().isEmpty() 
                ? review.getStatusMessage() 
                : "Your application is being processed."));
        
        details.add(createDetailSection("Reviewer Notes",
            review != null && review.getReviewerNotes() != null && !review.getReviewerNotes().isEmpty()
                ? review.getReviewerNotes()
                : "No notes available yet."));
        
        details.add(createDetailSection("Next Steps",
            review != null && review.getNextSteps() != null && !review.getNextSteps().isEmpty()
                ? review.getNextSteps()
                : "Please wait for further updates."));
        
        page.add(details);
        
        showPage("status");
        
        } catch (Exception ex) {
            System.err.println("[ERROR] Exception in showStatusPage: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private JLabel createMetaItem(String label, String value) {
        JLabel lbl = new JLabel("<html><b>" + label + "</b> " + value + "</html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }
    
    private JPanel createTimelineStep(String label, boolean completed, boolean current) {
        JPanel step = new JPanel();
        step.setLayout(new BoxLayout(step, BoxLayout.Y_AXIS));
        step.setOpaque(false);
        
        JPanel circle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (current) {
                    g2.setColor(PRIMARY_COLOR);
                    g2.fillOval(3, 3, getWidth() - 6, getHeight() - 6);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(8, 8, getWidth() - 16, getHeight() - 16);
                } else if (completed) {
                    g2.setColor(SUCCESS_COLOR);
                    g2.fillOval(3, 3, getWidth() - 6, getHeight() - 6);
                } else {
                    g2.setColor(BORDER_COLOR);
                    g2.fillOval(3, 3, getWidth() - 6, getHeight() - 6);
                }
            }
        };
        circle.setPreferredSize(new Dimension(32, 32));
        circle.setMaximumSize(new Dimension(32, 32));
        circle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel stepLabel = new JLabel("<html><center>" + label + "</center></html>");
        stepLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        stepLabel.setForeground(completed || current ? TEXT_PRIMARY : TEXT_SECONDARY);
        stepLabel.setMaximumSize(new Dimension(80, 40));
        stepLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        step.add(circle);
        step.add(Box.createVerticalStrut(8));
        step.add(stepLabel);
        
        return step;
    }
    
    private JPanel createDetailSection(String title, String content) {
        JPanel section = createCard("");
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        section.add(titleLabel);
        
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentArea.setForeground(TEXT_SECONDARY);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setOpaque(false);
        contentArea.setRows(3);
        section.add(contentArea);
        
        return section;
    }
    
    // ==================== PROFILE PAGE ====================
    private void initProfilePage() {
        JPanel page = createPagePanel();
        pages.put("profile", page);
        pages.put("profile", page);
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        JLabel titleLabel = new JLabel("Profile Module");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        header.add(titleLabel, BorderLayout.WEST);
        
        JLabel subtitleLabel = new JLabel("Manage your personal and academic information");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        header.add(subtitleLabel, BorderLayout.SOUTH);
        
        page.add(header);
        
        // Profile card
        JPanel profileCard = createCard("");
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBorder(new EmptyBorder(30, 30, 30, 30));
        profileCard.setMaximumSize(new Dimension(600, 800));
        
        // Avatar and name
        JPanel profileHeader = new JPanel(new BorderLayout(20, 0));
        profileHeader.setOpaque(false);
        
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_COLOR, getWidth(), getHeight(), new Color(129, 140, 248));
                g2.setPaint(gradient);
                g2.fillOval(0, 0, getWidth(), getHeight());
            }
        };
        avatar.setPreferredSize(new Dimension(80, 80));
        avatar.setLayout(new GridBagLayout());
        JLabel initials = new JLabel(getInitials(currentUser.getProfile().getFullName()));
        initials.setFont(new Font("Segoe UI", Font.BOLD, 28));
        initials.setForeground(Color.WHITE);
        avatar.add(initials);
        
        JPanel nameInfo = new JPanel();
        nameInfo.setLayout(new BoxLayout(nameInfo, BoxLayout.Y_AXIS));
        nameInfo.setOpaque(false);
        
        JLabel name = new JLabel(currentUser.getProfile().getFullName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 22));
        name.setForeground(TEXT_PRIMARY);
        nameInfo.add(name);
        
        JLabel program = new JLabel(currentUser.getProfile().getProgramMajor() + " - " + currentUser.getProfile().getYear());
        program.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        program.setForeground(TEXT_SECONDARY);
        program.setBorder(new EmptyBorder(5, 0, 0, 0));
        nameInfo.add(program);
        
        JLabel studentId = new JLabel("Student ID: " + currentUser.getProfile().getStudentId());
        studentId.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        studentId.setForeground(TEXT_SECONDARY);
        studentId.setBorder(new EmptyBorder(3, 0, 0, 0));
        nameInfo.add(studentId);
        
        profileHeader.add(avatar, BorderLayout.WEST);
        profileHeader.add(nameInfo, BorderLayout.CENTER);
        profileCard.add(profileHeader);
        
        // Details
        addProfileRow(profileCard, "Email", currentUser.getAccount().getEmail());
        addProfileRow(profileCard, "Phone", currentUser.getProfile().getPhoneNumber());
        addProfileRow(profileCard, "GPA", String.valueOf(currentUser.getAcademic().getGpa()));
        addProfileRow(profileCard, "Bio", currentUser.getProfile().getShortBio());
        
        // Completion bar
        JPanel completionSection = new JPanel();
        completionSection.setLayout(new BoxLayout(completionSection, BoxLayout.Y_AXIS));
        completionSection.setOpaque(false);
        completionSection.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JPanel completionHeader = new JPanel(new BorderLayout());
        completionHeader.setOpaque(false);
        JLabel compLabel = new JLabel("Profile Completion");
        compLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        compLabel.setForeground(TEXT_PRIMARY);
        completionHeader.add(compLabel, BorderLayout.WEST);
        
        JLabel compValue = new JLabel(currentUser.getProfileCompletion() + "%");
        compValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        compValue.setForeground(PRIMARY_COLOR);
        completionHeader.add(compValue, BorderLayout.EAST);
        
        completionSection.add(completionHeader);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setValue(currentUser.getProfileCompletion());
        progressBar.setStringPainted(false);
        progressBar.setBackground(BORDER_COLOR);
        progressBar.setForeground(PRIMARY_COLOR);
        progressBar.setPreferredSize(new Dimension(0, 8));
        progressBar.setBorderPainted(false);
        progressBar.setBorder(null);
        completionSection.add(progressBar);
        
        profileCard.add(completionSection);
        
        // Edit button
        JButton editBtn = createSecondaryButton("Edit Profile");
        editBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        editBtn.setBorder(new EmptyBorder(20, 0, 0, 0));
        profileCard.add(editBtn);
        
        page.add(profileCard);
        mainContentPanel.add(page, "profile");
    }
    
    private void addProfileRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 0, 12, 0));
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_SECONDARY);
        row.add(lbl, BorderLayout.WEST);
        
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        val.setForeground(TEXT_PRIMARY);
        val.setHorizontalAlignment(JLabel.RIGHT);
        row.add(val, BorderLayout.EAST);
        
        panel.add(row);
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void showPage(String pageName) {
        if (pageName.equals("applications")) {
            refreshApplicationsTable();
        }
        
        // 导航高亮与当前页一致
        switch (pageName) {
            case "dashboard" -> updateNavButtons(homeBtn);
            case "profile" -> updateNavButtons(profileNavBtn);
            case "jobs", "job-detail", "apply" -> updateNavButtons(jobsNavBtn);
            case "applications", "status" -> updateNavButtons(jobsNavBtn);
            default -> { /* 保持当前 */ }
        }
        
        cardLayout.show(mainContentPanel, pageName);
    }
    
    private JPanel createPagePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        return panel;
    }
    
    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(0, 0, 0, 0)
        ));
        return card;
    }
    
    private JLabel createTag(String text, Color bg, Color fg) {
        JLabel tag = new JLabel(text);
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tag.setForeground(fg);
        tag.setOpaque(true);
        tag.setBackground(bg);
        tag.setBorder(new EmptyBorder(5, 10, 5, 10));
        return tag;
    }
    
    private JLabel createSkillTag(String text) {
        JLabel tag = new JLabel(text);
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tag.setForeground(TEXT_SECONDARY);
        tag.setOpaque(true);
        tag.setBackground(BG_COLOR);
        tag.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        tag.setBorder(new EmptyBorder(5, 10, 5, 10));
        return tag;
    }
    
    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_COLOR);
            }
        });
        
        return btn;
    }
    
    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT_SECONDARY);
        btn.setBackground(CARD_BG);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(BG_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(CARD_BG);
            }
        });
        
        return btn;
    }
    
    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(10, 12, 10, 12)
        ));
        return field;
    }
    
    private JTextArea createTextArea(String label, int rows) {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setRows(rows);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(10, 12, 10, 12)
        ));
        return area;
    }
    
    private JPanel createReadOnlyField(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel lbl = new JLabel("<html><b>" + label + "</b></html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_SECONDARY);
        panel.add(lbl, BorderLayout.NORTH);
        
        JTextField field = new JTextField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setEditable(false);
        field.setBackground(BG_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)
        ));
        panel.add(field, BorderLayout.CENTER);
        
        return panel;
    }
    
    private String getInitials(String name) {
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0);
        }
        return name.substring(0, 2).toUpperCase();
    }
    
    // Table renderers
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cell.setHorizontalAlignment(JLabel.CENTER);
            
            String status = (String) value;
            Color bg, fg;
            
            switch (status.toLowerCase()) {
                case "pending" -> { bg = new Color(254, 243, 199); fg = WARNING_COLOR; }
                case "under review" -> { bg = new Color(219, 234, 254); fg = INFO_COLOR; }
                case "accepted" -> { bg = new Color(209, 250, 229); fg = SUCCESS_COLOR; }
                case "rejected" -> { bg = new Color(254, 226, 226); fg = DANGER_COLOR; }
                default -> { bg = BG_COLOR; fg = TEXT_SECONDARY; }
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
                setForeground(PRIMARY_COLOR);
                setBackground(new Color(238, 242, 255));
                setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR));
            } else {
                setForeground(PRIMARY_COLOR);
                setBackground(BG_COLOR);
                setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR));
            }
            setOpaque(true);
            return this;
        }
    }
    
    // Main method
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new TAPortalApp());
    }
}
