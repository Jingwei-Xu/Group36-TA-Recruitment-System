package com.example.tasystem.ui.screens;

import com.example.tasystem.data.ProfileData;
import com.example.tasystem.data.SkillItem;
import com.example.tasystem.ui.AppFrame;
import com.example.tasystem.ui.Theme;
import com.example.tasystem.ui.Ui;
import com.example.tasystem.ui.components.Chip;
import com.example.tasystem.ui.components.PrimaryButton;
import com.example.tasystem.ui.components.SecondaryButton;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;

public final class ProfileScreen extends JPanel {
    private final AppFrame app;

    private final JLabel nameValue = Ui.body("—");
    private final JLabel majorValue = Ui.body("—");
    private final JLabel emailValue = Ui.body("—");
    private final JLabel studentIdValue = Ui.body("—");
    private final JLabel yearValue = Ui.body("—");
    private final JLabel phoneValue = Ui.body("—");

    private final JPanel skillsWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));

    private final JLabel cvFileValue = Ui.body("—");
    private final JLabel cvMetaValue = Ui.muted("—");

    public ProfileScreen(AppFrame app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        add(buildNavBar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    public void refresh() {
        ProfileData p = app.profile();

        nameValue.setText(nonEmpty(p.fullName));
        majorValue.setText(nonEmpty(p.programMajor));
        emailValue.setText(nonEmpty(p.email));
        studentIdValue.setText(nonEmpty(p.studentId));
        yearValue.setText(nonEmpty(p.year));
        phoneValue.setText(nonEmpty(p.phoneNumber));

        skillsWrap.removeAll();
        if (p.skills != null) {
            for (SkillItem s : p.skills) {
                Chip chip = chipForCategory(s.category, s.name + " (" + s.proficiency + ")");
                chip.setEnabled(false);
                skillsWrap.add(chip);
            }
        }
        if (skillsWrap.getComponentCount() == 0) {
            skillsWrap.add(Ui.muted("No skills added yet."));
        }

        if (p.cv != null && p.cv.fileName != null && !p.cv.fileName.isBlank()) {
            cvFileValue.setText(p.cv.fileName);
            String meta = "Last Updated: " + nonEmpty(p.cv.lastUpdated);
            if (p.cv.sizeLabel != null && !p.cv.sizeLabel.isBlank()) meta += " · Size: " + p.cv.sizeLabel;
            cvMetaValue.setText(meta);
        } else {
            cvFileValue.setText("No CV uploaded");
            cvMetaValue.setText("—");
        }

        revalidate();
        repaint();
    }

    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Theme.SURFACE);
        nav.setBorder(Ui.empty(10, 18, 10, 18));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        left.setOpaque(false);
        JLabel brand = new JLabel("TA System");
        brand.setFont(Theme.BODY_BOLD.deriveFont(14f));
        brand.setForeground(Theme.TEXT);
        left.add(brand);

        JButton home = navLink("Home");
        JButton profile = navLink("Profile Module");
        JButton job = navLink("Job Application Module");
        home.addActionListener(e -> app.showRoute(AppFrame.ROUTE_DASHBOARD));
        profile.addActionListener(e -> app.showRoute(AppFrame.ROUTE_PROFILE));
        left.add(home);
        left.add(profile);
        left.add(job);

        nav.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        right.setOpaque(false);
        JButton logout = navLink("Logout");
        right.add(logout);
        nav.add(right, BorderLayout.EAST);
        return nav;
    }

    private JButton navLink(String text) {
        JButton b = new JButton(text);
        b.setFont(Theme.BODY);
        b.setForeground(Theme.TEXT);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel buildBody() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(Ui.empty(22, 28, 28, 28));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(Ui.h1("My Profile"));
        head.add(Box.createVerticalStrut(6));
        head.add(Ui.muted("View your complete personal information, skills, and CV status"));
        wrap.add(head, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(Box.createVerticalStrut(18));
        content.add(buildPersonalInfoCard());
        content.add(Box.createVerticalStrut(14));
        content.add(buildSkillsCard());
        content.add(Box.createVerticalStrut(14));
        content.add(buildCvCard());
        content.add(Box.createVerticalGlue());

        wrap.add(new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildPersonalInfoCard() {
        Ui.RoundedPanel card = new Ui.RoundedPanel(18, Theme.SURFACE, Theme.BORDER, 1);
        card.setLayout(new BorderLayout());
        card.setBorder(Ui.empty(16, 16, 16, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(Ui.h3("Personal Information"), BorderLayout.WEST);
        SecondaryButton edit = new SecondaryButton("Edit Profile");
        edit.setPreferredSize(new java.awt.Dimension(130, 38));
        edit.addActionListener(e -> openEditProfileDialog());
        JPanel editWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        editWrap.setOpaque(false);
        editWrap.add(edit);
        top.add(editWrap, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 24, 0));
        grid.setOpaque(false);
        grid.setBorder(Ui.empty(14, 0, 0, 0));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(kv("Full Name", nameValue));
        left.add(Box.createVerticalStrut(8));
        left.add(kv("Program / Major", majorValue));
        left.add(Box.createVerticalStrut(8));
        left.add(kv("Email", emailValue));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(kv("Student ID", studentIdValue));
        right.add(Box.createVerticalStrut(8));
        right.add(kv("Year", yearValue));
        right.add(Box.createVerticalStrut(8));
        right.add(kv("Phone Number", phoneValue));

        grid.add(left);
        grid.add(right);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSkillsCard() {
        Ui.RoundedPanel card = new Ui.RoundedPanel(18, Theme.SURFACE, Theme.BORDER, 1);
        card.setLayout(new BorderLayout());
        card.setBorder(Ui.empty(16, 16, 16, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(Ui.h3("Skills Information"), BorderLayout.WEST);
        SecondaryButton edit = new SecondaryButton("Edit Skills");
        edit.setPreferredSize(new java.awt.Dimension(120, 38));
        edit.addActionListener(e -> openEditSkillsDialog());
        JPanel editWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        editWrap.setOpaque(false);
        editWrap.add(edit);
        top.add(editWrap, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        skillsWrap.setOpaque(false);
        skillsWrap.setBorder(Ui.empty(14, 0, 0, 0));
        card.add(skillsWrap, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCvCard() {
        Ui.RoundedPanel card = new Ui.RoundedPanel(18, Theme.SURFACE, Theme.BORDER, 1);
        card.setLayout(new BorderLayout());
        card.setBorder(Ui.empty(16, 16, 16, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(Ui.h3("CV Information"), BorderLayout.WEST);
        SecondaryButton manage = new SecondaryButton("Manage CV");
        manage.setPreferredSize(new java.awt.Dimension(120, 38));
        manage.addActionListener(e -> openManageCvDialog());
        JPanel manageWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        manageWrap.setOpaque(false);
        manageWrap.add(manage);
        top.add(manageWrap, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel line = new JPanel();
        line.setOpaque(false);
        line.setLayout(new BoxLayout(line, BoxLayout.Y_AXIS));
        line.setBorder(Ui.empty(14, 0, 0, 0));
        cvFileValue.setFont(Theme.BODY_BOLD);
        line.add(cvFileValue);
        line.add(Box.createVerticalStrut(6));
        line.add(cvMetaValue);
        card.add(line, BorderLayout.CENTER);
        return card;
    }

    private JPanel kv(String k, JLabel v) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel key = Ui.muted(k);
        v.setFont(Theme.BODY_BOLD);
        p.add(key);
        p.add(Box.createVerticalStrut(2));
        p.add(v);
        return p;
    }

    private String nonEmpty(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    private Chip chipForCategory(String category, String text) {
        if (category == null) return Chip.blue(text, false);
        String c = category.toLowerCase();
        if (c.contains("program")) return Chip.blue(text, false);
        if (c.contains("teach")) return Chip.green(text, false);
        if (c.contains("comm")) return Chip.purple(text, false);
        return Chip.blue(text, false);
    }

    private void openEditProfileDialog() {
        ProfileData p = app.profile();

        JDialog d = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(this), "Edit Profile");
        d.setModal(true);
        d.setSize(720, 620);
        d.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.setBorder(Ui.empty(18, 18, 18, 18));

        JPanel card = new Ui.RoundedPanel(18, Theme.SURFACE, Theme.BORDER, 1);
        card.setLayout(new BorderLayout());
        card.setBorder(Ui.empty(18, 18, 18, 18));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        JTextField fullName = Ui.textField("");
        fullName.setText(p.fullName);
        JTextField studentId = Ui.textField("");
        studentId.setText(p.studentId);
        JComboBox<String> year = new JComboBox<>(new String[]{"1st Year", "2nd Year", "3rd Year", "4th Year", "Graduate"});
        year.setSelectedItem(p.year == null || p.year.isBlank() ? "3rd Year" : p.year);
        JTextField program = Ui.textField("");
        program.setText(p.programMajor);
        JTextField email = Ui.textField("");
        email.setText(p.email);
        JTextField phone = Ui.textField("");
        phone.setText(p.phoneNumber);
        JTextField address = Ui.textField("123 Main St, City, State, ZIP");
        address.setText(p.address);
        JTextArea bio = new JTextArea(5, 30);
        bio.setText(p.shortBio);
        bio.setLineWrap(true);
        bio.setWrapStyleWord(true);
        bio.setFont(Theme.BODY);
        bio.setBorder(Ui.empty(10, 12, 10, 12));
        Ui.RoundedPanel bioWrap = new Ui.RoundedPanel(12, Theme.SURFACE, Theme.BORDER, 1);
        bioWrap.setLayout(new BorderLayout());
        bioWrap.add(new JScrollPane(bio), BorderLayout.CENTER);

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        form.add(Ui.h2("Edit Profile"), c);

        c.gridy++;
        form.add(Ui.muted("Update your personal information"), c);

        c.gridy++;
        form.add(labeled("Full Name *", new Ui.RoundedTextField(fullName)), c);

        c.gridy++; c.gridwidth = 1;
        form.add(labeled("Student ID *", new Ui.RoundedTextField(studentId)), c);
        c.gridx = 1;
        Ui.RoundedPanel yearWrap = new Ui.RoundedPanel(12, Theme.SURFACE, Theme.BORDER, 1);
        yearWrap.setLayout(new BorderLayout());
        yearWrap.add(year, BorderLayout.CENTER);
        yearWrap.setPreferredSize(new java.awt.Dimension(240, 40));
        form.add(labeled("Year *", yearWrap), c);

        c.gridx = 0; c.gridy++; c.gridwidth = 2;
        form.add(labeled("Program / Major *", new Ui.RoundedTextField(program)), c);

        c.gridy++; c.gridwidth = 1;
        form.add(labeled("Email *", new Ui.RoundedTextField(email)), c);
        c.gridx = 1;
        form.add(labeled("Phone Number *", new Ui.RoundedTextField(phone)), c);

        c.gridx = 0; c.gridy++; c.gridwidth = 2;
        form.add(labeled("Address (Optional)", new Ui.RoundedTextField(address)), c);

        c.gridy++;
        form.add(labeled("Short Bio (Optional)", bioWrap), c);

        card.add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(Ui.empty(14, 0, 0, 0));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        SecondaryButton cancel = new SecondaryButton("Cancel");
        cancel.addActionListener(e -> d.dispose());
        PrimaryButton save = new PrimaryButton("Save Changes");
        save.addActionListener(e -> {
            ProfileData next = app.profile();
            next.fullName = fullName.getText().trim();
            next.studentId = studentId.getText().trim();
            next.year = String.valueOf(year.getSelectedItem());
            next.programMajor = program.getText().trim();
            next.email = email.getText().trim();
            next.phoneNumber = phone.getText().trim();
            next.address = address.getText().trim();
            next.shortBio = bio.getText().trim();
            app.updateProfile(next);
            d.dispose();
        });
        right.add(save);
        right.add(cancel);
        bottom.add(right, BorderLayout.EAST);

        root.add(card, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        d.setContentPane(root);
        d.setVisible(true);
    }

    private JPanel labeled(String label, Component field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(Theme.BODY);
        l.setForeground(Theme.TEXT);
        p.add(l);
        p.add(Box.createVerticalStrut(6));
        p.add(field);
        return p;
    }

    private void openEditSkillsDialog() {
        JDialog d = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(this), "Skills Information");
        d.setModal(true);
        d.setSize(820, 620);
        d.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.setBorder(Ui.empty(18, 18, 18, 18));

        Ui.RoundedPanel card = new Ui.RoundedPanel(18, Theme.SURFACE, Theme.BORDER, 1);
        card.setLayout(new BorderLayout());
        card.setBorder(Ui.empty(18, 18, 18, 18));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(Ui.h2("Skills Information"));
        head.add(Box.createVerticalStrut(6));
        head.add(Ui.muted("Manage your skills and competencies"));

        card.add(head, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(1, 4, 10, 0));
        form.setOpaque(false);
        form.setBorder(Ui.empty(14, 0, 12, 0));

        JTextField name = Ui.textField("e.g., React, Data Analysis");
        JComboBox<String> category = new JComboBox<>(new String[]{"Programming", "Teaching / Tutoring", "Communication", "Other Skills"});
        JComboBox<String> prof = new JComboBox<>(new String[]{"Beginner", "Intermediate", "Advanced"});
        PrimaryButton add = new PrimaryButton("+");
        add.setPreferredSize(new java.awt.Dimension(52, 44));

        Ui.RoundedPanel catWrap = new Ui.RoundedPanel(12, Theme.SURFACE, Theme.BORDER, 1);
        catWrap.setLayout(new BorderLayout());
        catWrap.add(category, BorderLayout.CENTER);
        Ui.RoundedPanel profWrap = new Ui.RoundedPanel(12, Theme.SURFACE, Theme.BORDER, 1);
        profWrap.setLayout(new BorderLayout());
        profWrap.add(prof, BorderLayout.CENTER);

        form.add(new Ui.RoundedTextField(name));
        form.add(catWrap);
        form.add(profWrap);
        form.add(add);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        chips.setOpaque(false);

        final Runnable[] rebuild = new Runnable[1];
        rebuild[0] = () -> {
            chips.removeAll();
            ProfileData p = app.profile();
            if (p.skills != null) {
                for (int i = 0; i < p.skills.size(); i++) {
                    SkillItem s = p.skills.get(i);
                    int idx = i;
                    Chip closable = chipForCategoryClosable(s.category, s.name + " (" + s.proficiency + ")");
                    closable.addActionListener(e -> {
                        ProfileData next = app.profile();
                        if (next.skills != null && idx >= 0 && idx < next.skills.size()) {
                            next.skills.remove(idx);
                            app.updateProfile(next);
                            rebuild[0].run();
                        }
                    });
                    chips.add(closable);
                }
            }
            if (chips.getComponentCount() == 0) {
                chips.add(Ui.muted("No skills added yet."));
            }
            chips.revalidate();
            chips.repaint();
        };

        add.addActionListener(e -> {
            String n = name.getText().trim();
            if (n.isEmpty()) return;
            ProfileData next = app.profile();
            next.addSkill(n, String.valueOf(category.getSelectedItem()), String.valueOf(prof.getSelectedItem()));
            app.updateProfile(next);
            name.setText("");
            rebuild[0].run();
        });

        list.add(Ui.h3("Programming Skills"));
        list.add(Box.createVerticalStrut(8));
        list.add(chips);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(list, BorderLayout.CENTER);

        card.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);
        bottom.setBorder(Ui.empty(12, 0, 0, 0));
        PrimaryButton save = new PrimaryButton("Save Skills");
        save.addActionListener(e -> d.dispose());
        SecondaryButton cancel = new SecondaryButton("Cancel");
        cancel.addActionListener(e -> d.dispose());
        bottom.add(save);
        bottom.add(cancel);

        root.add(card, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        d.setContentPane(root);
        rebuild[0].run();
        d.setVisible(true);
    }

    private Chip chipForCategoryClosable(String category, String text) {
        if (category == null) return Chip.blue(text, true);
        String c = category.toLowerCase();
        if (c.contains("program")) return Chip.blue(text, true);
        if (c.contains("teach")) return Chip.green(text, true);
        if (c.contains("comm")) return Chip.purple(text, true);
        return Chip.blue(text, true);
    }

    private void openManageCvDialog() {
        JDialog d = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(this), "CV Upload");
        d.setModal(true);
        d.setSize(820, 640);
        d.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.setBorder(Ui.empty(18, 18, 18, 18));

        Ui.RoundedPanel card = new Ui.RoundedPanel(18, Theme.SURFACE, Theme.BORDER, 1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(Ui.empty(18, 18, 18, 18));

        card.add(Ui.h2("CV Upload"));
        card.add(Box.createVerticalStrut(6));
        card.add(Ui.muted("Upload or manage your curriculum vitae"));
        card.add(Box.createVerticalStrut(16));

        Ui.RoundedPanel status = new Ui.RoundedPanel(16, Theme.GREEN_BG, Theme.BORDER, 1);
        status.setLayout(new BorderLayout());
        status.setBorder(Ui.empty(12, 12, 12, 12));

        JLabel statusText = new JLabel();
        statusText.setFont(Theme.BODY_BOLD);
        statusText.setForeground(Theme.TEXT);
        ProfileData p = app.profile();
        if (p.cv != null && p.cv.fileName != null && !p.cv.fileName.isBlank()) {
            statusText.setText(p.cv.fileName + "  (" + p.cv.status + ")");
        } else {
            statusText.setText("No CV uploaded");
        }
        status.add(statusText, BorderLayout.WEST);

        SecondaryButton remove = new SecondaryButton("Remove");
        remove.setPreferredSize(new java.awt.Dimension(110, 38));
        remove.addActionListener(e -> {
            ProfileData next = app.profile();
            next.cv.fileName = "";
            next.cv.status = "";
            next.cv.lastUpdated = "";
            next.cv.sizeLabel = "";
            app.updateProfile(next);
            statusText.setText("No CV uploaded");
        });
        JPanel removeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        removeWrap.setOpaque(false);
        removeWrap.add(remove);
        status.add(removeWrap, BorderLayout.EAST);

        card.add(status);
        card.add(Box.createVerticalStrut(16));

        Ui.RoundedPanel drop = new Ui.RoundedPanel(18, new java.awt.Color(0xFA, 0xFB, 0xFC), Theme.BORDER, 1);
        drop.setLayout(new BoxLayout(drop, BoxLayout.Y_AXIS));
        drop.setBorder(Ui.empty(18, 18, 18, 18));
        drop.setPreferredSize(new java.awt.Dimension(10, 260));

        JLabel drag = new JLabel("Drag and drop your CV here", JLabel.CENTER);
        drag.setAlignmentX(Component.CENTER_ALIGNMENT);
        drag.setFont(Theme.BODY_BOLD);

        PrimaryButton browse = new PrimaryButton("Browse File");
        browse.setAlignmentX(Component.CENTER_ALIGNMENT);
        browse.setPreferredSize(new java.awt.Dimension(140, 44));
        browse.addActionListener(e -> {
            javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
            chooser.setDialogTitle("Select CV (PDF/DOC/DOCX)");
            int res = chooser.showOpenDialog(d);
            if (res != javax.swing.JFileChooser.APPROVE_OPTION) return;
            java.io.File f = chooser.getSelectedFile();
            if (f == null) return;
            ProfileData next = app.profile();
            next.cv.fileName = f.getName();
            next.cv.status = "Uploaded";
            next.cv.lastUpdated = LocalDate.now().toString();
            next.cv.sizeLabel = (Math.max(1, f.length() / 1024)) + " KB";
            app.updateProfile(next);
            statusText.setText(next.cv.fileName + "  (" + next.cv.status + ")");
        });

        drop.add(Box.createVerticalGlue());
        drop.add(drag);
        drop.add(Box.createVerticalStrut(10));
        drop.add(browse);
        drop.add(Box.createVerticalGlue());

        card.add(drop);
        card.add(Box.createVerticalStrut(16));

        JTextArea req = new JTextArea(
                "- Accepted file types: PDF, DOC, DOCX\n" +
                        "- Maximum file size: 5 MB\n" +
                        "- File name should not contain special characters\n" +
                        "- Recommended: Use a clear naming format (e.g., FirstName_LastName_CV.pdf)"
        );
        req.setEditable(false);
        req.setOpaque(false);
        req.setFont(Theme.BODY);
        req.setForeground(Theme.MUTED);
        card.add(Ui.h3("File Requirements"));
        card.add(Box.createVerticalStrut(6));
        card.add(req);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);
        bottom.setBorder(Ui.empty(12, 0, 0, 0));
        SecondaryButton cancel = new SecondaryButton("Cancel");
        cancel.addActionListener(e -> d.dispose());
        bottom.add(cancel);

        root.add(card, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        d.setContentPane(root);
        d.setVisible(true);
    }
}

