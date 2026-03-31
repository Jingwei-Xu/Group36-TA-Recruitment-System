package edu.ebu6304.app.ui;

import edu.ebu6304.app.model.ApplicationRecord;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class ReviewApplicationPanel extends JPanel {
    private final MainFrame frame;

    private final JLabel contextLabel = new JLabel("-");
    private final JRadioButton approveRadio = new JRadioButton("Approve");
    private final JRadioButton rejectRadio = new JRadioButton("Reject");
    private final JTextArea notesArea = new JTextArea(8, 20);

    private ApplicationRecord record;

    public ReviewApplicationPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(16, 16));
        setBackground(Style.BG);

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);

        JLabel title = new JLabel("Review TA Application");
        title.setFont(Style.FONT_H1);
        title.setForeground(Style.TEXT);

        JButton backBtn = new JButton("Back to Detail");
        Style.styleSecondaryButton(backBtn);
        backBtn.addActionListener(e -> {
            if (record != null) {
                frame.showApplicationDetail(record);
            } else {
                frame.showApplicationsList();
            }
        });

        north.add(title, BorderLayout.WEST);
        north.add(backBtn, BorderLayout.EAST);

        JPanel contextCard = new JPanel(new GridLayout(1, 1));
        Style.stylePanelCard(contextCard);
        contextLabel.setFont(Style.FONT_BODY);
        contextLabel.setForeground(Style.TEXT);
        contextCard.add(contextLabel);

        JPanel decisionCard = new JPanel(new GridLayout(2, 1, 0, 10));
        Style.stylePanelCard(decisionCard);

        styleRadio(approveRadio);
        styleRadio(rejectRadio);

        ButtonGroup group = new ButtonGroup();
        group.add(approveRadio);
        group.add(rejectRadio);
        approveRadio.setSelected(true);

        decisionCard.add(approveRadio);
        decisionCard.add(rejectRadio);

        JPanel notesCard = new JPanel(new BorderLayout(8, 8));
        Style.stylePanelCard(notesCard);

        JLabel notesLabel = new JLabel("Review Notes");
        notesLabel.setFont(Style.FONT_BODY_BOLD);
        notesLabel.setForeground(Style.TEXT);

        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(Style.FONT_BODY);
        notesArea.setBackground(Color.WHITE);

        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(Style.sectionBorder());

        notesCard.add(notesLabel, BorderLayout.NORTH);
        notesCard.add(notesScroll, BorderLayout.CENTER);

        JButton submitBtn = new JButton("Submit Review");
        Style.stylePrimaryButton(submitBtn);
        submitBtn.addActionListener(e -> submit());

        JButton cancelBtn = new JButton("Cancel");
        Style.styleSecondaryButton(cancelBtn);
        cancelBtn.addActionListener(e -> {
            if (record != null) {
                frame.showApplicationDetail(record);
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(cancelBtn);
        actions.add(submitBtn);

        page.add(north);
        page.add(Box.createVerticalStrut(12));
        page.add(contextCard);
        page.add(Box.createVerticalStrut(12));
        page.add(decisionCard);
        page.add(Box.createVerticalStrut(12));
        page.add(notesCard);
        page.add(Box.createVerticalStrut(12));
        page.add(actions);

        add(page, BorderLayout.CENTER);
    }

    public void setRecord(ApplicationRecord record) {
        this.record = record;
        if (record == null) {
            return;
        }

        contextLabel.setText(String.format(
                "Application: %s | TA: %s | Student ID: %s | Course: %s - %s",
                safe(record.getApplicationId()), safe(record.getApplicantName()), safe(record.getStudentId()),
                safe(record.getCourseCode()), safe(record.getCourseName())
        ));

        if ("rejected".equalsIgnoreCase(record.getReviewDecision())) {
            rejectRadio.setSelected(true);
        } else {
            approveRadio.setSelected(true);
        }
        notesArea.setText(record.getReviewNotes() == null ? "" : record.getReviewNotes());
    }

    private void submit() {
        if (record == null) {
            return;
        }

        String decision = approveRadio.isSelected() ? "accepted" : "rejected";
        String notes = notesArea.getText() == null ? "" : notesArea.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Confirm to submit this review?",
                "Confirm Review",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        frame.persistReview(record, decision, notes);
        JOptionPane.showMessageDialog(this, "Review submitted and saved to data file.");
        frame.showApplicationsList();
    }

    private void styleRadio(JRadioButton radio) {
        radio.setBackground(Style.CARD_BG);
        radio.setFont(Style.FONT_BODY);
        radio.setForeground(Style.TEXT);
    }

    private String safe(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }
}
