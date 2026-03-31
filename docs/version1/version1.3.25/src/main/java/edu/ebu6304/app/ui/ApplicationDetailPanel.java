package edu.ebu6304.app.ui;

import edu.ebu6304.app.model.ApplicationRecord;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class ApplicationDetailPanel extends JPanel {
    private final MainFrame frame;

    private final JLabel appIdValue = new JLabel("-");
    private final JLabel applicantNameValue = new JLabel("-");
    private final JLabel studentIdValue = new JLabel("-");
    private final JLabel courseValue = new JLabel("-");
    private final JLabel majorValue = new JLabel("-");
    private final JLabel gpaValue = new JLabel("-");
    private final JLabel statusValue = new JLabel("-");
    private final JLabel decisionValue = new JLabel("-");

    private final JTextArea skillsArea = new JTextArea();
    private final JTextArea experienceArea = new JTextArea();
    private final JTextArea coverLetterArea = new JTextArea();

    private ApplicationRecord record;

    public ApplicationDetailPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(16, 16));
        setBackground(Style.BG);

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("TA Application Detail");
        title.setFont(Style.FONT_H1);
        title.setForeground(Style.TEXT);

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topButtons.setOpaque(false);

        JButton backBtn = new JButton("Back to List");
        Style.styleSecondaryButton(backBtn);
        backBtn.addActionListener(e -> frame.showApplicationsList());

        JButton reviewBtnTop = new JButton("Review Application");
        Style.stylePrimaryButton(reviewBtnTop);
        reviewBtnTop.addActionListener(e -> {
            if (record != null) {
                frame.showReviewPage(record);
            }
        });

        topButtons.add(reviewBtnTop);
        topButtons.add(backBtn);

        top.add(title, BorderLayout.WEST);
        top.add(topButtons, BorderLayout.EAST);

        JPanel basic = new JPanel(new GridLayout(4, 2, 10, 10));
        Style.stylePanelCard(basic);

        addField(basic, "Application ID", appIdValue);
        addField(basic, "Applicant", applicantNameValue);
        addField(basic, "Student ID", studentIdValue);
        addField(basic, "Course", courseValue);
        addField(basic, "Major", majorValue);
        addField(basic, "GPA", gpaValue);
        addField(basic, "Current Status", statusValue);
        addField(basic, "Review Decision", decisionValue);

        setupReadOnlyText(skillsArea);
        setupReadOnlyText(experienceArea);
        setupReadOnlyText(coverLetterArea);

        JPanel textPanels = new JPanel(new GridLayout(3, 1, 12, 12));
        textPanels.setOpaque(false);
        textPanels.add(textCard("Relevant Skills", skillsArea));
        textPanels.add(textCard("Relevant Experience", experienceArea));
        textPanels.add(textCard("Motivation / Cover Letter", coverLetterArea));

        JPanel bottomActionCard = new JPanel(new BorderLayout(8, 8));
        Style.stylePanelCard(bottomActionCard);
        JLabel actionDesc = new JLabel("Ready to review this application?");
        actionDesc.setFont(Style.FONT_BODY);
        actionDesc.setForeground(Style.MUTED);
        JButton reviewBtnBottom = new JButton("Review Now");
        Style.stylePrimaryButton(reviewBtnBottom);
        reviewBtnBottom.addActionListener(e -> {
            if (record != null) {
                frame.showReviewPage(record);
            }
        });
        bottomActionCard.add(actionDesc, BorderLayout.WEST);
        bottomActionCard.add(reviewBtnBottom, BorderLayout.EAST);

        page.add(top);
        page.add(Box.createVerticalStrut(12));
        page.add(basic);
        page.add(Box.createVerticalStrut(12));
        page.add(textPanels);
        page.add(Box.createVerticalStrut(12));
        page.add(bottomActionCard);

        JScrollPane scroller = new JScrollPane(page);
        scroller.setBorder(null);
        scroller.getViewport().setBackground(Style.BG);
        add(scroller, BorderLayout.CENTER);
    }

    public void setRecord(ApplicationRecord record) {
        this.record = record;
        if (record == null) {
            return;
        }

        appIdValue.setText(safe(record.getApplicationId()));
        applicantNameValue.setText(safe(record.getApplicantName()));
        studentIdValue.setText(safe(record.getStudentId()));
        courseValue.setText(String.format("%s - %s", safe(record.getCourseCode()), safe(record.getCourseName())));
        majorValue.setText(safe(record.getApplicantMajor()));
        gpaValue.setText(record.getApplicantGpa() == null ? "-" : String.valueOf(record.getApplicantGpa()));
        statusValue.setText(safe(record.getStatusLabel()));
        decisionValue.setText((record.getReviewDecision() == null || record.getReviewDecision().isBlank()) ? "-" : record.getReviewDecision());

        skillsArea.setText(record.getRelevantSkills() == null || record.getRelevantSkills().isEmpty()
                ? "-"
                : String.join(", ", record.getRelevantSkills()));
        experienceArea.setText(safe(record.getRelevantExperience()));
        coverLetterArea.setText(safe(record.getMotivationCoverLetter()));
    }

    private JPanel textCard(String title, JTextArea area) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        Style.stylePanelCard(card);

        JLabel label = new JLabel(title);
        label.setFont(Style.FONT_BODY_BOLD);
        label.setForeground(Style.TEXT);

        JScrollPane areaScroll = new JScrollPane(area);
        areaScroll.setBorder(Style.sectionBorder());

        card.add(label, BorderLayout.NORTH);
        card.add(areaScroll, BorderLayout.CENTER);
        return card;
    }

    private void addField(JPanel panel, String label, JLabel value) {
        JLabel l = new JLabel(label + ":");
        l.setFont(Style.FONT_BODY);
        l.setForeground(Style.MUTED);

        value.setFont(Style.FONT_BODY_BOLD);
        value.setForeground(Style.TEXT);

        panel.add(l);
        panel.add(value);
    }

    private void setupReadOnlyText(JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFont(Style.FONT_BODY);
        area.setBackground(Color.WHITE);
        area.setForeground(Style.TEXT);
    }

    private String safe(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }
}
