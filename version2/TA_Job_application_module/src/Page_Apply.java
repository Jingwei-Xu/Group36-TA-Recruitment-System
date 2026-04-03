

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * 申请页面
 * 用户填写并提交职位申请
 */
public class Page_Apply {
    
    /**
     * CV 上传保存路径: data/uploads/profile_cv/{studentId}/{filename}
     * data 与 version2 同级，都在 src 下，所以要从 user.dir 往上两级
     */
    private static String getCvUploadBase() {
        String userDir = System.getProperty("user.dir");
        return userDir + File.separator + ".." + File.separator + ".."
            + File.separator + "data" + File.separator + "uploads"
            + File.separator + "profile_cv";
    }

    public interface ApplyCallback {
        void onBackToJobDetail(Job job);
        void onSubmitSuccess();
    }
    
    private JPanel panel;
    private ApplyCallback callback;
    private Job currentJob;
    private TAUser currentUser;
    private DataService dataService;
    
    public Page_Apply(TAUser currentUser, DataService dataService, ApplyCallback callback) {
        this.currentUser = currentUser;
        this.dataService = dataService;
        this.callback = callback;
        initPanel();
    }
    
    public JPanel getPanel() {
        return panel;
    }
    
    public void showJob(Job job) {
        this.currentJob = job;
        buildContent(job);
    }
    
    private void initPanel() {
        panel = new JPanel(new BorderLayout());
        panel.setBackground(UI_Constants.BG_COLOR);
        panel.setBorder(new EmptyBorder(0, 48, 30, 48));
    }
    
    private void buildContent(Job job) {
        panel.removeAll();
        
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);
        scrollContent.setBorder(new EmptyBorder(16, 0, 30, 0));
        
        // Top section
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.setBorder(new EmptyBorder(0, 0, 16, 0));
        
        JButton backBtn = new JButton("\u2190 Back to Job Details");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setForeground(UI_Constants.TEXT_SECONDARY);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorder(new EmptyBorder(0, 0, 12, 0));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> callback.onBackToJobDetail(job));
        topSection.add(backBtn);
        
        JLabel title = new JLabel("Apply for Job");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UI_Constants.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        topSection.add(title);
        
        scrollContent.add(topSection);
        
        // Two-column layout
        JPanel content = new JPanel(new BorderLayout(30, 0));
        content.setOpaque(false);
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // LEFT COLUMN: Form
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Info banner
        JPanel hintCard = UI_Helper.createCard();
        hintCard.setLayout(new BorderLayout(12, 0));
        hintCard.setBackground(new Color(239, 246, 255));
        hintCard.setOpaque(true);
        hintCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(191, 219, 254)),
            new EmptyBorder(14, 16, 14, 16)
        ));
        hintCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        hintCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        JLabel hintIcon = new JLabel("\u2139");
        hintIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hintIcon.setForeground(UI_Constants.INFO_COLOR);
        hintCard.add(hintIcon, BorderLayout.WEST);
        JLabel hintText = new JLabel("Your profile data, skills, and CV have been auto-filled from your saved profile. You may modify any information before submitting.");
        hintText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hintText.setForeground(UI_Constants.TEXT_SECONDARY);
        hintCard.add(hintText, BorderLayout.CENTER);
        leftPanel.add(hintCard);
        
        leftPanel.add(Box.createVerticalStrut(16));
        
        // Application Info card
        JPanel appInfoCard = UI_Helper.createCard();
        appInfoCard.setLayout(new BoxLayout(appInfoCard, BoxLayout.Y_AXIS));
        appInfoCard.setBorder(new EmptyBorder(24, 24, 24, 24));
        appInfoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        appInfoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        JLabel sectionTitle = new JLabel("Application Information");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(UI_Constants.TEXT_PRIMARY);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 18, 0));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        appInfoCard.add(sectionTitle);
        
        // Grid for basic info
        JPanel grid = new JPanel(new GridLayout(3, 2, 18, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextField fullNameField = createEditableField(currentUser.getProfile().getFullName());
        JTextField studentIdField = createEditableField(currentUser.getProfile().getStudentId());
        JTextField emailField = createEditableField(currentUser.getAccount().getEmail());
        JTextField phoneField = createEditableField(currentUser.getProfile().getPhoneNumber());
        JTextField programField = createEditableField(currentUser.getProfile().getProgramMajor());
        JTextField gpaField = createEditableField(String.valueOf(currentUser.getAcademic().getGpa()));
        
        fullNameField.setPreferredSize(new Dimension(0, 48));
        studentIdField.setPreferredSize(new Dimension(0, 48));
        emailField.setPreferredSize(new Dimension(0, 48));
        phoneField.setPreferredSize(new Dimension(0, 48));
        programField.setPreferredSize(new Dimension(0, 48));
        gpaField.setPreferredSize(new Dimension(0, 48));
        
        grid.add(createFieldPanel("Full Name *", fullNameField));
        grid.add(createFieldPanel("Student ID *", studentIdField));
        grid.add(createFieldPanel("Email *", emailField));
        grid.add(createFieldPanel("Phone Number *", phoneField));
        grid.add(createFieldPanel("Program / Major *", programField));
        grid.add(createFieldPanel("GPA (Optional)", gpaField));
        appInfoCard.add(grid);
        
        appInfoCard.add(Box.createVerticalStrut(16));
        
        // Skills
        JTextArea skillsArea = UI_Helper.createTextArea(4);
        JPanel skillsWrap = createLabeledArea("Relevant Skills *", skillsArea,
            "e.g., Java, Python, Data Structures, Machine Learning");
        skillsWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        skillsArea.setText(formatUserSkills(currentUser));
        appInfoCard.add(skillsWrap);
        
        appInfoCard.add(Box.createVerticalStrut(14));
        
        // Experience
        JTextArea experienceArea = UI_Helper.createTextArea(5);
        JPanel expWrap = createLabeledArea("Relevant Experience *", experienceArea,
            "Describe your relevant work experience, TA/grading experience, or projects...");
        expWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        appInfoCard.add(expWrap);
        
        appInfoCard.add(Box.createVerticalStrut(14));
        
        // Availability
        JTextArea availabilityArea = UI_Helper.createTextArea(3);
        JPanel availWrap = createLabeledArea("Availability *", availabilityArea,
            "e.g., Monday/Wednesday 10am-12pm, Tuesday 2pm-4pm");
        availWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        appInfoCard.add(availWrap);
        
        appInfoCard.add(Box.createVerticalStrut(14));
        
        // Motivation
        JTextArea motivationArea = UI_Helper.createTextArea(6);
        JPanel motivWrap = createLabeledArea("Motivation / Cover Letter *", motivationArea,
            "Explain why you are interested in this TA position and what makes you a good fit...");
        motivWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        appInfoCard.add(motivWrap);
        
        appInfoCard.add(Box.createVerticalStrut(18));
        
        // CV upload
        JLabel resumeTitle = new JLabel("Resume / CV *");
        resumeTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resumeTitle.setForeground(UI_Constants.TEXT_SECONDARY);
        resumeTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        resumeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        appInfoCard.add(resumeTitle);
        
        JPanel resumeBox = createUploadBox(
            currentUser.getProfile().getFullName() + "_CV.pdf is attached from your profile",
            "Click to upload a different file"
        );
        resumeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        resumeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        appInfoCard.add(resumeBox);
        
        appInfoCard.add(Box.createVerticalStrut(14));
        
        JLabel supportTitle = new JLabel("Supporting Documents (Optional)");
        supportTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        supportTitle.setForeground(UI_Constants.TEXT_SECONDARY);
        supportTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        supportTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        appInfoCard.add(supportTitle);
        
        JPanel supportBox = createUploadBox(
            "Upload transcripts, certificates, or other documents",
            "Click to upload"
        );
        supportBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        supportBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        appInfoCard.add(supportBox);
        
        leftPanel.add(appInfoCard);
        content.add(leftPanel, BorderLayout.CENTER);
        
        // RIGHT COLUMN: Summary
        JPanel summaryPanel = UI_Helper.createCard();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        summaryPanel.setAlignmentX(Component.TOP_ALIGNMENT);
        
        JLabel summaryTitle = new JLabel("Position Summary");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        summaryTitle.setForeground(UI_Constants.TEXT_PRIMARY);
        summaryTitle.setBorder(new EmptyBorder(0, 0, 18, 0));
        summaryPanel.add(summaryTitle);
        
        JPanel summaryItems = new JPanel();
        summaryItems.setLayout(new BoxLayout(summaryItems, BoxLayout.Y_AXIS));
        summaryItems.setOpaque(false);
        
        summaryItems.add(createSummaryRow("Position", job.getTitle()));
        summaryItems.add(Box.createVerticalStrut(10));
        summaryItems.add(createSummaryRow("Course", job.getCourseCode() + " " + job.getCourse().getCourseName()));
        summaryItems.add(Box.createVerticalStrut(10));
        summaryItems.add(createSummaryRow("Instructor", job.getInstructorName()));
        summaryItems.add(Box.createVerticalStrut(10));
        summaryItems.add(createSummaryRow("Department", job.getDepartment()));
        summaryItems.add(Box.createVerticalStrut(10));
        summaryItems.add(createSummaryRow("Employment Type", job.getEmploymentType()));
        summaryItems.add(Box.createVerticalStrut(10));
        summaryItems.add(createSummaryRow("Weekly Hours", job.getWeeklyHoursDisplay()));
        summaryItems.add(Box.createVerticalStrut(10));
        
        JPanel deadlineRow = new JPanel(new BorderLayout());
        deadlineRow.setOpaque(false);
        JLabel dlLbl = new JLabel("Application Deadline");
        dlLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dlLbl.setForeground(UI_Constants.TEXT_SECONDARY);
        deadlineRow.add(dlLbl, BorderLayout.WEST);
        JLabel dlVal = new JLabel(job.getDeadlineDisplay());
        dlVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dlVal.setForeground(UI_Constants.WARNING_COLOR);
        deadlineRow.add(dlVal, BorderLayout.EAST);
        summaryItems.add(deadlineRow);
        
        summaryItems.add(Box.createVerticalStrut(10));
        summaryItems.add(createSummaryRow("Work Mode", job.getLocationMode()));
        
        summaryPanel.add(summaryItems);
        
        // Divider
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(UI_Constants.BORDER_COLOR);
                g.drawLine(0, 0, getWidth(), 0);
            }
        };
        divider.setBorder(new EmptyBorder(16, 0, 16, 0));
        divider.setOpaque(false);
        summaryPanel.add(divider);
        
        JLabel noteLbl = new JLabel("Please review all information before submitting your application.");
        noteLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noteLbl.setForeground(UI_Constants.TEXT_SECONDARY);
        noteLbl.setBorder(new EmptyBorder(0, 0, 16, 0));
        summaryPanel.add(noteLbl);
        
        // Submit button
        JButton submitBtn = new JButton("Submit Application");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(UI_Constants.DARK_BUTTON);
        submitBtn.setOpaque(true);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.setBorder(new EmptyBorder(14, 20, 14, 20));
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        submitBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { submitBtn.setBackground(UI_Constants.DARK_BUTTON_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent evt) { submitBtn.setBackground(UI_Constants.DARK_BUTTON); }
        });
        summaryPanel.add(submitBtn);
        
        // Cancel button
        JButton cancelBtn = UI_Helper.createSecondaryButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancelBtn.setBorder(new EmptyBorder(10, 0, 0, 0));
        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cancelBtn.addActionListener(e -> callback.onBackToJobDetail(job));
        summaryPanel.add(cancelBtn);
        
        content.add(summaryPanel, BorderLayout.EAST);
        summaryPanel.setPreferredSize(new Dimension(320, 700));
        
        scrollContent.add(content);
        
        // File selection
        final String[] selectedCvPath = {null};
        final List<String> selectedSupportPaths = new ArrayList<>();
        
        JButton resumePickBtn = (JButton) resumeBox.getClientProperty("pickButton");
        JLabel resumeHintLbl = (JLabel) resumeBox.getClientProperty("hintLabel");
        resumePickBtn.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(panel);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                selectedCvPath[0] = f.getAbsolutePath();
                resumeHintLbl.setText(f.getName());
            }
        });
        
        JButton supportPickBtn = (JButton) supportBox.getClientProperty("pickButton");
        JLabel supportHintLbl = (JLabel) supportBox.getClientProperty("hintLabel");
        supportPickBtn.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            int res = chooser.showOpenDialog(panel);
            if (res == JFileChooser.APPROVE_OPTION) {
                selectedSupportPaths.clear();
                File[] files = chooser.getSelectedFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) selectedSupportPaths.add(f.getAbsolutePath());
                    supportHintLbl.setText(files.length + " file(s) selected");
                }
            }
        });
        
        // Submit action
        submitBtn.addActionListener(e -> {
            if (fullNameField.getText().trim().isEmpty()
                || studentIdField.getText().trim().isEmpty()
                || emailField.getText().trim().isEmpty()
                || phoneField.getText().trim().isEmpty()
                || programField.getText().trim().isEmpty()
                || skillsArea.getText().trim().isEmpty()
                || experienceArea.getText().trim().isEmpty()
                || availabilityArea.getText().trim().isEmpty()
                || motivationArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill in all required fields (*) before submitting.", "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
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
            appSnap.setFullName(fullNameField.getText().trim());
            appSnap.setStudentId(studentIdField.getText().trim());
            appSnap.setEmail(emailField.getText().trim());
            appSnap.setPhoneNumber(phoneField.getText().trim());
            appSnap.setProgramMajor(programField.getText().trim());
            appSnap.setYear(currentUser.getProfile().getYear());
            try {
                String gpaTxt = gpaField.getText().trim();
                if (!gpaTxt.isEmpty()) appSnap.setGpa(Double.parseDouble(gpaTxt));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "GPA must be a number (e.g., 3.8).", "Invalid GPA", JOptionPane.WARNING_MESSAGE);
                return;
            }
            app.setApplicantSnapshot(appSnap);
            
            // Application form
            Application.ApplicationForm appForm = new Application.ApplicationForm();
            String[] skills = skillsArea.getText().split(",");
            appForm.setRelevantSkills(Arrays.stream(skills).map(String::trim).filter(s -> !s.isEmpty()).toList());
            appForm.setRelevantExperience(experienceArea.getText().trim());
            appForm.setAvailability(availabilityArea.getText().trim());
            appForm.setMotivationCoverLetter(motivationArea.getText().trim());
            app.setApplicationForm(appForm);
            
            // Attachments
            Application.Attachments at = new Application.Attachments();
            if (selectedCvPath[0] != null && !selectedCvPath[0].isEmpty()) {
                File sourceFile = new File(selectedCvPath[0]);
                String studentId = studentIdField.getText().trim();
                File studentDir = new File(getCvUploadBase() + File.separator + studentId);
                if (!studentDir.exists()) {
                    studentDir.mkdirs();
                }
                File destFile = new File(studentDir, sourceFile.getName());
                try {
                    java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (java.io.IOException ex) {
                    System.err.println("Error copying CV file: " + ex.getMessage());
                }
                Application.CVInfo cv = new Application.CVInfo();
                cv.setFileName(sourceFile.getName());
                cv.setFilePath(destFile.getAbsolutePath());
                String lower = sourceFile.getName().toLowerCase();
                cv.setFileType(lower.contains(".") ? lower.substring(lower.lastIndexOf('.') + 1) : "");
                at.setCv(cv);
            }
            if (!selectedSupportPaths.isEmpty()) {
                List<Application.Document> docs = new ArrayList<>();
                for (String p : selectedSupportPaths) {
                    File f = new File(p);
                    Application.Document d = new Application.Document();
                    d.setFileName(f.getName());
                    d.setFilePath(f.getAbsolutePath());
                    String lower = f.getName().toLowerCase();
                    d.setFileType(lower.contains(".") ? lower.substring(lower.lastIndexOf('.') + 1) : "");
                    docs.add(d);
                }
                at.setSupportingDocuments(docs);
            }
            app.setAttachments(at);
            app.setJobId(job.getJobId());
            
            dataService.addApplication(app);
            
            JOptionPane.showMessageDialog(panel,
                "Application submitted successfully!\n\nYou can track your application status in 'My Applications'.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            callback.onSubmitSuccess();
        });
        
        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UI_Constants.BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }
    
    private String formatUserSkills(TAUser user) {
        if (user == null || user.getSkills() == null) return "";
        List<String> all = new ArrayList<>();
        TAUser.Skills s = user.getSkills();
        addSkillNames(all, s.getProgramming());
        addSkillNames(all, s.getTeaching());
        addSkillNames(all, s.getCommunication());
        addSkillNames(all, s.getOther());
        return String.join(", ", all);
    }
    
    private void addSkillNames(List<String> out, List<TAUser.Skill> skills) {
        if (skills == null) return;
        for (TAUser.Skill sk : skills) {
            if (sk != null && sk.getName() != null && !sk.getName().trim().isEmpty()) {
                out.add(sk.getName().trim());
            }
        }
    }
    
    private JTextField createEditableField(String value) {
        JTextField field = new JTextField(value == null ? "" : value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }
    
    private JPanel createFieldPanel(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel lbl = new JLabel("<html><b>" + label + "</b></html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(UI_Constants.TEXT_SECONDARY);
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createLabeledArea(String label, JTextArea area, String placeholder) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel("<html><b>" + label + "</b></html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(UI_Constants.TEXT_SECONDARY);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        wrap.add(lbl);
        
        JPanel areaContainer = new JPanel(new BorderLayout());
        areaContainer.setOpaque(false);
        
        JLabel placeholderLabel = new JLabel(placeholder);
        placeholderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        placeholderLabel.setForeground(new Color(156, 163, 175));
        placeholderLabel.setBorder(new EmptyBorder(10, 12, 10, 12));
        placeholderLabel.setName("placeholder");
        areaContainer.add(placeholderLabel, BorderLayout.NORTH);
        
        area.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                placeholderLabel.setVisible(false);
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (area.getText().isEmpty()) {
                    placeholderLabel.setVisible(true);
                }
            }
        });
        
        area.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                placeholderLabel.setVisible(false);
            }
        });
        
        areaContainer.add(area, BorderLayout.CENTER);
        
        JScrollPane sp = new JScrollPane(areaContainer);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setOpaque(false);
        wrap.add(sp);
        return wrap;
    }
    
    private JPanel createUploadBox(String topLine, String bottomLine) {
        JPanel box = UI_Helper.createCard();
        box.setLayout(new BorderLayout());
        box.setOpaque(false);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createDashedBorder(UI_Constants.BORDER_COLOR, 6, 6),
            new EmptyBorder(14, 14, 14, 14)
        ));
        
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        
        JLabel hint = new JLabel(topLine);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(UI_Constants.TEXT_SECONDARY);
        center.add(hint);
        
        JLabel action = new JLabel(bottomLine);
        action.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        action.setForeground(UI_Constants.TEXT_SECONDARY);
        action.setBorder(new EmptyBorder(4, 0, 0, 0));
        center.add(action);
        
        box.add(center, BorderLayout.CENTER);
        
        JButton pick = UI_Helper.createSecondaryButton("Upload");
        pick.setFont(new Font("Segoe UI", Font.BOLD, 13));
        box.add(pick, BorderLayout.EAST);
        
        box.putClientProperty("pickButton", pick);
        box.putClientProperty("hintLabel", hint);
        return box;
    }
    
    private JPanel createSummaryRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(UI_Constants.TEXT_SECONDARY);
        row.add(lbl, BorderLayout.WEST);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        val.setForeground(UI_Constants.TEXT_PRIMARY);
        row.add(val, BorderLayout.EAST);
        return row;
    }
}
