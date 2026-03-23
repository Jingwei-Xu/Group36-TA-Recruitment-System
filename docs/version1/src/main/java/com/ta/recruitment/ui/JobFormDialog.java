package com.ta.recruitment.ui;

import com.ta.recruitment.model.Job;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JobFormDialog extends JDialog {

    public interface SaveListener {
        void onSave(Job job);
    }

    private final JTextField jobIdField = new JTextField(20);
    private final JTextField titleField = new JTextField(20);
    private final JTextField moduleCodeField = new JTextField(20);
    private final JTextField moduleNameField = new JTextField(20);
    private final JTextField instructorField = new JTextField(20);
    private final JTextField departmentField = new JTextField(20);
    private final JTextField hoursField = new JTextField(20);
    private final JTextField employmentTypeField = new JTextField(20);
    private final JTextArea descriptionArea = new JTextArea(5, 20);
    private final JTextField requiredSkillsField = new JTextField(20);
    private final JTextField additionalRequirementsField = new JTextField(20);
    private final JCheckBox publishNowCheck = new JCheckBox("Publish Immediately");

    private final Job originalJob;
    private final SaveListener saveListener;

    public JobFormDialog(JFrame parent, String title, Job existingJob, SaveListener saveListener) {
        super(parent, title, true);
        this.originalJob = existingJob;
        this.saveListener = saveListener;
        initializeUi();
        if (existingJob != null) {
            fillForm(existingJob);
            jobIdField.setEditable(false);
        }
    }

    private void initializeUi() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        int row = 0;
        addRow(formPanel, gbc, row++, "Job ID *", jobIdField);
        addRow(formPanel, gbc, row++, "Job Title *", titleField);
        addRow(formPanel, gbc, row++, "Module Code *", moduleCodeField);
        addRow(formPanel, gbc, row++, "Module Name *", moduleNameField);
        addRow(formPanel, gbc, row++, "Instructor *", instructorField);
        addRow(formPanel, gbc, row++, "Department", departmentField);
        addRow(formPanel, gbc, row++, "Hours / Week *", hoursField);
        addRow(formPanel, gbc, row++, "Employment Type", employmentTypeField);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Job Description *"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setPreferredSize(new Dimension(300, 90));
        formPanel.add(descScroll, gbc);
        row++;

        addRow(formPanel, gbc, row++, "Required Skills (comma separated)", requiredSkillsField);
        addRow(formPanel, gbc, row++, "Additional Requirements", additionalRequirementsField);

        gbc.gridx = 1;
        gbc.gridy = row;
        formPanel.add(publishNowCheck, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton(originalJob == null ? "Publish Job" : "Save Changes");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> handleSave());

        pack();
        setLocationRelativeTo(getParent());
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private void fillForm(Job job) {
        jobIdField.setText(job.getJobId());
        titleField.setText(job.getTitle());
        if (job.getCourse() != null) {
            moduleCodeField.setText(job.getCourse().getCourseCode());
            moduleNameField.setText(job.getCourse().getCourseName());
        }
        if (job.getInstructor() != null) {
            instructorField.setText(job.getInstructor().getName());
        }
        departmentField.setText(job.getDepartment());
        if (job.getEmployment() != null) {
            hoursField.setText(String.valueOf(job.getEmployment().getWeeklyHours()));
            employmentTypeField.setText(job.getEmployment().getEmploymentType());
        }
        if (job.getContent() != null) {
            descriptionArea.setText(job.getContent().getDescription());
            requiredSkillsField.setText(String.join(", ", safeList(job.getContent().getRequirements())));
            additionalRequirementsField.setText(job.getContent().getSummary());
        }
        if (job.getPublication() != null) {
            publishNowCheck.setSelected("published".equalsIgnoreCase(job.getPublication().getStatus()));
        }
    }

    private void handleSave() {
        try {
            Job job = originalJob == null ? new Job() : originalJob;
            job.setJobId(jobIdField.getText().trim());
            job.setTitle(titleField.getText().trim());
            job.setDepartment(departmentField.getText().trim());

            if (job.getCourse() == null) {
                job.setCourse(new Job.Course());
            }
            job.getCourse().setCourseCode(moduleCodeField.getText().trim());
            job.getCourse().setCourseName(moduleNameField.getText().trim());

            if (job.getInstructor() == null) {
                job.setInstructor(new Job.Instructor());
            }
            job.getInstructor().setName(instructorField.getText().trim());

            if (job.getEmployment() == null) {
                job.setEmployment(new Job.Employment());
            }
            job.getEmployment().setWeeklyHours(parseHours(hoursField.getText().trim()));
            job.getEmployment().setEmploymentType(employmentTypeField.getText().trim());
            if (job.getEmployment().getLocationMode() == null) {
                job.getEmployment().setLocationMode("On-Campus");
            }
            if (job.getEmployment().getJobType() == null) {
                job.getEmployment().setJobType("TA");
            }

            if (job.getContent() == null) {
                job.setContent(new Job.Content());
            }
            job.getContent().setDescription(descriptionArea.getText().trim());
            job.getContent().setRequirements(parseSkills(requiredSkillsField.getText().trim()));
            job.getContent().setSummary(additionalRequirementsField.getText().trim());

            if (job.getPublication() == null) {
                job.setPublication(new Job.Publication());
            }
            job.getPublication().setStatus(publishNowCheck.isSelected() ? "published" : "draft");

            if (job.getLifecycle() == null) {
                job.setLifecycle(new Job.Lifecycle());
            }
            if (job.getLifecycle().getStatus() == null || job.getLifecycle().getStatus().isBlank()) {
                job.getLifecycle().setStatus("open");
            }

            saveListener.onSave(job);
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int parseHours(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Hours / Week must be an integer.");
        }
    }

    private List<String> parseSkills(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> safeList(List<String> values) {
        return values == null ? new ArrayList<>() : values;
    }
}
