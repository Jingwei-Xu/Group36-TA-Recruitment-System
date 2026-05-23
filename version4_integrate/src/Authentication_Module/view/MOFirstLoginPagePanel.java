package Authentication_Module.view;

import Authentication_Module.model.User;
import Authentication_Module.util.JsonUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

public class MOFirstLoginPagePanel extends JPanel {
    private static final int CARD_CONTENT_WIDTH = 500;
    /** Fixed width so every input column lines up regardless of label length. */
    private static final int LEADING_COL_FIXED = 196;
    private static final int INPUT_ROW_HEIGHT = 34;
    private static final int LABEL_FIELD_GAP = 6;

    private static final Color PRIMARY_PURPLE = new Color(0x6D4DEB);
    private static final Color DEEP_PURPLE = new Color(0x4F35D9);
    private static final Color LAVENDER = new Color(0xF3EEFF);
    private static final Color LIGHT_PURPLE_BORDER = new Color(0xDED4FF);
    private static final Color DARK_TEXT = new Color(0x111033);
    private static final Color MUTED_TEXT = new Color(0x667085);
    private static final Color INPUT_BORDER = new Color(0xDDE3F0);
    private static final Color PAGE_BG = new Color(0xFAFAFF);

    public MOFirstLoginPagePanel(AppFrame app) {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(PAGE_BG);

        ModernAuthBackground root = new ModernAuthBackground(new GridBagLayout());
        root.setBorder(new EmptyBorder(14, 20, 16, 20));

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        RoundedCard card = new RoundedCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 28, 12, 28));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heroIcon = new JLabel(new PortalIcon("user", PRIMARY_PURPLE, 28));
        heroIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel heroTile = iconCircle(heroIcon, 50);
        heroTile.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Complete Your Profile (MO)", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(DARK_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setMaximumSize(new Dimension(CARD_CONTENT_WIDTH, title.getPreferredSize().height));

        JLabel desc = new JLabel("Please fill in your information", SwingConstants.CENTER);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(MUTED_TEXT);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        desc.setHorizontalAlignment(SwingConstants.CENTER);
        desc.setMaximumSize(new Dimension(CARD_CONTENT_WIDTH, desc.getPreferredSize().height));

        JPanel underline = underline();
        underline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField fullName = createField("Enter your full name");
        JTextField staffId = createField("Enter your staff ID");
        JTextField department = createField("Enter your department");
        JTextField phone = createField("Enter your phone number");
        JTextField school = createField("Enter your school");
        JTextField email = createField("Enter your campus email");
        JTextField campus = createField("Enter your teaching campus");
        JTextField courseTypes = createField("e.g. Math, Physics, Computer Science");
        courseTypes.setToolTipText("Comma-separated, e.g. Math, Physics, Computer Science");

        GradientButton submit = new GradientButton("Save & Continue", new PortalIcon("save", Color.WHITE, 14));
        submit.setAlignmentX(Component.CENTER_ALIGNMENT);
        submit.setMaximumSize(new Dimension(CARD_CONTENT_WIDTH, 36));
        submit.setPreferredSize(new Dimension(CARD_CONTENT_WIDTH, 36));

        JButton back = linkButton("← Back to Home");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> app.showPage("HOME"));

        submit.addActionListener(e -> {
            User user = app.getCurrentUser();
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Session expired. Please login again.");
                app.showPage("LOGIN");
                return;
            }

            if (fullName.getText().isEmpty()
                    || staffId.getText().isEmpty()
                    || department.getText().isEmpty()
                    || phone.getText().isEmpty()
                    || school.getText().isEmpty()
                    || email.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields (*)");
                return;
            }

            JsonUtil.updateMOProfile(
                    user,
                    fullName.getText(),
                    staffId.getText(),
                    department.getText(),
                    phone.getText(),
                    school.getText(),
                    email.getText(),
                    campus.getText(),
                    courseTypes.getText()
            );

            JOptionPane.showMessageDialog(this, "Profile saved!");
            app.showPage("MO");
        });

        card.add(heroTile);
        card.add(Box.createVerticalStrut(6));
        card.add(title);
        card.add(Box.createVerticalStrut(2));
        card.add(desc);
        card.add(Box.createVerticalStrut(6));
        card.add(underline);
        card.add(Box.createVerticalStrut(12));
        int iconSz = 17;
        card.add(createInlineFieldRow(new PortalIcon("user", PRIMARY_PURPLE, iconSz), "Full Name", true, fullName));
        card.add(Box.createVerticalStrut(6));
        card.add(createInlineFieldRow(new PortalIcon("id", PRIMARY_PURPLE, iconSz), "Staff ID", true, staffId));
        card.add(Box.createVerticalStrut(6));
        card.add(createInlineFieldRow(new PortalIcon("dept", PRIMARY_PURPLE, iconSz), "Department", true, department));
        card.add(Box.createVerticalStrut(6));
        card.add(createInlineFieldRow(new PortalIcon("phone", PRIMARY_PURPLE, iconSz), "Phone Number", true, phone));
        card.add(Box.createVerticalStrut(6));
        card.add(createInlineFieldRow(new PortalIcon("school", PRIMARY_PURPLE, iconSz), "School", true, school));
        card.add(Box.createVerticalStrut(6));
        card.add(createInlineFieldRow(new PortalIcon("mail", PRIMARY_PURPLE, iconSz), "Campus Email", true, email));
        card.add(Box.createVerticalStrut(6));
        card.add(createInlineFieldRow(new PortalIcon("pin", PRIMARY_PURPLE, iconSz), "Teaching Campus", false, campus));
        card.add(Box.createVerticalStrut(6));
        card.add(createInlineFieldRow(new PortalIcon("book", PRIMARY_PURPLE, iconSz), "Courses", false, courseTypes));
        card.add(Box.createVerticalStrut(10));
        card.add(submit);
        card.add(Box.createVerticalStrut(4));
        card.add(back);

        GridBagConstraints wrapGbc = new GridBagConstraints();
        wrapGbc.gridx = 0;
        wrapGbc.gridy = 0;
        wrapGbc.weightx = 1;
        wrapGbc.weighty = 1;
        wrapGbc.anchor = GridBagConstraints.CENTER;
        wrapGbc.fill = GridBagConstraints.NONE;
        wrapper.add(card, wrapGbc);

        GridBagConstraints rootGbc = new GridBagConstraints();
        rootGbc.gridx = 0;
        rootGbc.gridy = 0;
        rootGbc.weightx = 1;
        rootGbc.weighty = 1;
        rootGbc.anchor = GridBagConstraints.CENTER;
        rootGbc.fill = GridBagConstraints.BOTH;
        root.add(wrapper, rootGbc);

        JScrollPane scrollPane = new JScrollPane(root);
        scrollPane.setBorder(null);
        scrollPane.setBackground(PAGE_BG);
        scrollPane.getViewport().setBackground(PAGE_BG);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(true);
        add(scrollPane, BorderLayout.CENTER);

        installViewportBackgroundBleed(scrollPane, root);
    }

    /**
     * When the scroll view is taller than the form, extend the painted background to the full viewport
     * height so dots/waves reach the window bottom (no strip of mismatched color).
     */
    private void installViewportBackgroundBleed(JScrollPane scrollPane, JPanel rootPanel) {
        Runnable sync = () -> {
            JViewport vp = scrollPane.getViewport();
            int vw = vp.getWidth();
            int vh = vp.getHeight();
            if (vw <= 0 || vh <= 0) {
                return;
            }
            LayoutManager lm = rootPanel.getLayout();
            Dimension nat = lm != null ? lm.preferredLayoutSize(rootPanel) : rootPanel.getPreferredSize();
            int nw = nat != null && nat.width > 0 ? nat.width : vw;
            int nh = nat != null && nat.height > 0 ? nat.height : vh;
            Dimension d = new Dimension(Math.max(vw, nw), Math.max(vh, nh));
            rootPanel.setPreferredSize(d);
            rootPanel.setMinimumSize(d);
            rootPanel.revalidate();
        };
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                sync.run();
            }
        });
        SwingUtilities.invokeLater(sync);
    }

    private JTextField createField(String placeholder) {
        HintTextField field = new HintTextField(placeholder);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(DARK_TEXT);
        field.setBorder(new EmptyBorder(0, 10, 0, 10));
        field.setOpaque(false);
        return field;
    }

    private JPanel createInlineFieldRow(Icon icon, String label, boolean required, JTextField input) {
        String labelText = required ? label + " *" : label;
        JPanel row = new JPanel(new BorderLayout(LABEL_FIELD_GAP, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        int rowH = INPUT_ROW_HEIGHT + 4;
        row.setMaximumSize(new Dimension(CARD_CONTENT_WIDTH, rowH));
        row.setPreferredSize(new Dimension(CARD_CONTENT_WIDTH, rowH));

        JPanel leadWrap = new JPanel(new BorderLayout());
        leadWrap.setOpaque(false);
        leadWrap.add(compactFieldLeading(icon, labelText), BorderLayout.WEST);
        Dimension ld = new Dimension(LEADING_COL_FIXED, INPUT_ROW_HEIGHT);
        leadWrap.setPreferredSize(ld);
        leadWrap.setMinimumSize(ld);
        leadWrap.setMaximumSize(ld);
        row.add(leadWrap, BorderLayout.WEST);

        JPanel shellInner = new JPanel(new BorderLayout());
        shellInner.setOpaque(false);
        shellInner.add(input, BorderLayout.CENTER);
        row.add(fieldShellStretch(shellInner, INPUT_ROW_HEIGHT), BorderLayout.CENTER);
        return row;
    }

    private JPanel compactFieldLeading(Icon icon, String labelText) {
        JPanel lead = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        lead.setOpaque(false);
        lead.add(mediumIconTile(icon));
        JLabel lab = new JLabel(labelText);
        lab.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lab.setForeground(DARK_TEXT);
        lead.add(lab);
        return lead;
    }

    private JPanel fieldShellStretch(Component inner, int height) {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setOpaque(false);
        shell.setPreferredSize(new Dimension(200, height));
        shell.setMinimumSize(new Dimension(120, height));
        shell.add(inner, BorderLayout.CENTER);
        return new RoundedShell(shell, 8, Color.WHITE, INPUT_BORDER, 1f, new EmptyBorder(0, 0, 0, 0));
    }

    private JPanel mediumIconTile(Icon icon) {
        int sz = 30;
        JPanel tile = new JPanel(new GridBagLayout());
        tile.setOpaque(false);
        tile.setPreferredSize(new Dimension(sz, sz));
        tile.setMinimumSize(new Dimension(sz, sz));
        tile.setMaximumSize(new Dimension(sz, sz));
        tile.add(new JLabel(icon));
        return new RoundedShell(tile, 8, LAVENDER, null, 0, new EmptyBorder(0, 0, 0, 0));
    }

    private JPanel iconCircle(JLabel label, int size) {
        JPanel tile = new JPanel(new GridBagLayout());
        tile.setOpaque(false);
        tile.setPreferredSize(new Dimension(size, size));
        tile.setMinimumSize(new Dimension(size, size));
        tile.setMaximumSize(new Dimension(size, size));
        tile.add(label);
        return new RoundedShell(tile, size, LAVENDER, null, 0, new EmptyBorder(0, 0, 0, 0));
    }

    private JPanel underline() {
        JPanel u = new JPanel();
        u.setBackground(PRIMARY_PURPLE);
        u.setPreferredSize(new Dimension(28, 2));
        u.setMinimumSize(new Dimension(28, 2));
        u.setMaximumSize(new Dimension(28, 2));
        return u;
    }

    private JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(PRIMARY_PURPLE);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setMargin(new Insets(4, 8, 6, 8));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color base = PRIMARY_PURPLE;
        Color hover = new Color(0x5A3FE8);
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setForeground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setForeground(base);
            }
        });
        return b;
    }

    private static class ModernAuthBackground extends JPanel implements Scrollable {
        ModernAuthBackground(LayoutManager lm) {
            super(lm);
            setOpaque(true);
            setBackground(new Color(0xFAFAFF));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, new Color(0xFDFCFF), 0, h, new Color(0xF8F6FF)));
            g2.fillRect(0, 0, w, h);
            paintDots(g2, 40, 150, 250, Math.min(h - 40, 540), 34);
            paintDots(g2, Math.max(0, w - 300), 70, w - 20, 320, 28);
            paintWave(g2, w, h, new Color(109, 77, 235, 28), 0.78, 0);
            paintWave(g2, w, h, new Color(109, 77, 235, 18), 0.86, 90);
            g2.dispose();
        }

        private void paintDots(Graphics2D g2, int x1, int y1, int x2, int y2, int alpha) {
            g2.setColor(new Color(109, 77, 235, alpha));
            for (int x = x1; x < x2; x += 14) {
                for (int y = y1; y < y2; y += 14) {
                    g2.fillOval(x, y, 3, 3);
                }
            }
        }

        private void paintWave(Graphics2D g2, int w, int h, Color c, double base, int phase) {
            Polygon p = new Polygon();
            p.addPoint(0, h);
            int yBase = (int) (h * base);
            for (int x = 0; x <= w; x += 20) {
                p.addPoint(x, yBase + (int) (34 * Math.sin((x + phase) / 120.0)));
            }
            p.addPoint(w, h);
            g2.setColor(c);
            g2.fillPolygon(p);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(visibleRect.height - 10, 10);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static class RoundedCard extends JPanel {
        RoundedCard() {
            setOpaque(false);
        }

        /** Do not stretch vertically past content (avoids empty white inside the card). */
        @Override
        public Dimension getMaximumSize() {
            Dimension p = super.getPreferredSize();
            return new Dimension(Integer.MAX_VALUE, p.height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(new Color(79, 53, 217, 18));
            g2.fillRoundRect(4, 6, w - 8, h - 8, 18, 18);
            g2.setColor(new Color(255, 255, 255, 248));
            g2.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);
            g2.setColor(LIGHT_PURPLE_BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedShell extends JPanel {
        private final int arc;
        private final Color fill;
        private final Color stroke;
        private final float strokeWidth;

        RoundedShell(Component inner, int arc, Color fill, Color stroke, float strokeWidth, EmptyBorder pad) {
            super(new BorderLayout());
            this.arc = arc;
            this.fill = fill;
            this.stroke = stroke;
            this.strokeWidth = strokeWidth;
            setOpaque(false);
            setBorder(pad);
            add(inner, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            if (stroke != null && strokeWidth > 0) {
                g2.setStroke(new BasicStroke(strokeWidth));
                g2.setColor(stroke);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class GradientButton extends JButton {
        private final Icon leadingIcon;
        private boolean hover;

        GradientButton(String text, Icon leadingIcon) {
            super(text);
            this.leadingIcon = leadingIcon;
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color left = hover ? new Color(0x7C5CFF) : PRIMARY_PURPLE;
            Color right = hover ? new Color(0x3F2BB8) : DEEP_PURPLE;
            g2.setPaint(new GradientPaint(0, 0, left, getWidth(), getHeight(), right));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            FontMetrics fm = g2.getFontMetrics(getFont());
            int iw = leadingIcon != null ? leadingIcon.getIconWidth() : 0;
            int gap = leadingIcon != null ? 8 : 0;
            int tw = fm.stringWidth(getText());
            int x = (getWidth() - iw - gap - tw) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            if (leadingIcon != null) {
                leadingIcon.paintIcon(this, g2, x, (getHeight() - leadingIcon.getIconHeight()) / 2);
                x += iw + gap;
            }
            g2.setFont(getFont());
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    private static class HintTextField extends JTextField {
        private final String hint;

        HintTextField(String hint) {
            this.hint = hint;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0x98A2B3));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, 10, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    }

    private static class PortalIcon implements Icon {
        private final String type;
        private final Color color;
        private final int size;

        PortalIcon(String type, Color color, int size) {
            this.type = type;
            this.color = color;
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            float pad = Math.max(1.5f, size / 14f);
            float w = size - 2 * pad;
            float h = size - 2 * pad;
            float ox = x + pad;
            float oy = y + pad;
            float stroke = Math.max(1.35f, size / 16f);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(color);

            switch (type) {
                case "user" -> paintUser(g2, ox, oy, w, h, stroke);
                case "id" -> paintId(g2, ox, oy, w, h, stroke);
                case "dept" -> paintDept(g2, ox, oy, w, h, stroke);
                case "phone" -> paintPhone(g2, ox, oy, w, h, stroke);
                case "school" -> paintSchool(g2, ox, oy, w, h, stroke);
                case "mail" -> paintMail(g2, ox, oy, w, h, stroke);
                case "pin" -> paintPin(g2, ox, oy, w, h, stroke);
                case "book" -> paintBook(g2, ox, oy, w, h, stroke);
                case "save" -> paintSave(g2, ox, oy, w, h, stroke, color);
                default -> g2.draw(new RoundRectangle2D.Float(ox + w * 0.15f, oy + h * 0.15f, w * 0.7f, h * 0.7f, w * 0.12f, h * 0.12f));
            }
            g2.dispose();
        }

        private static void paintUser(Graphics2D g2, float ox, float oy, float w, float h, float stroke) {
            float cx = ox + w / 2;
            float headR = Math.min(w, h) * 0.22f;
            float headCy = oy + h * 0.34f;
            g2.draw(new Ellipse2D.Float(cx - headR, headCy - headR, headR * 2, headR * 2));
            GeneralPath shoulders = new GeneralPath();
            float left = ox + w * 0.14f;
            float right = ox + w * 0.86f;
            float topY = headCy + headR * 0.55f;
            float bottomY = oy + h * 0.92f;
            shoulders.moveTo(left, bottomY);
            shoulders.quadTo(cx, topY + headR * 0.35f, right, bottomY);
            g2.draw(shoulders);
        }

        /** ID badge with lines. */
        private static void paintId(Graphics2D g2, float ox, float oy, float w, float h, float stroke) {
            float ix = ox + w * 0.16f;
            float iy = oy + h * 0.2f;
            float iw = w * 0.68f;
            float ih = h * 0.6f;
            float ar = Math.min(iw, ih) * 0.12f;
            g2.draw(new RoundRectangle2D.Float(ix, iy, iw, ih, ar, ar));
            float lx = ix + iw * 0.12f;
            float rx = ix + iw * 0.88f;
            float y1 = iy + ih * 0.35f;
            float y2 = iy + ih * 0.55f;
            float y3 = iy + ih * 0.75f;
            g2.setStroke(new BasicStroke(stroke * 0.75f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Float(lx, y1, rx, y1));
            g2.draw(new Line2D.Float(lx, y2, rx * 0.72f, y2));
            g2.draw(new Line2D.Float(lx, y3, rx * 0.55f, y3));
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }

        /** Simple org chart / stacked units. */
        private static void paintDept(Graphics2D g2, float ox, float oy, float w, float h, float stroke) {
            float uW = w * 0.38f;
            float uH = h * 0.22f;
            float ar = uH * 0.35f;
            float cx = ox + w / 2;
            float topY = oy + h * 0.14f;
            g2.draw(new RoundRectangle2D.Float(cx - uW / 2, topY, uW, uH, ar, ar));
            float midY = topY + uH + h * 0.08f;
            float gap = w * 0.06f;
            g2.draw(new RoundRectangle2D.Float(cx - uW - gap / 2, midY, uW, uH, ar, ar));
            g2.draw(new RoundRectangle2D.Float(cx + gap / 2, midY, uW, uH, ar, ar));
            g2.draw(new Line2D.Float(cx, topY + uH, cx, midY));
            g2.draw(new Line2D.Float(cx - uW / 2 - gap / 4, midY + uH / 2, cx, topY + uH));
            g2.draw(new Line2D.Float(cx + uW / 2 + gap / 4, midY + uH / 2, cx, topY + uH));
        }

        /** Handset silhouette. */
        private static void paintPhone(Graphics2D g2, float ox, float oy, float w, float h, float stroke) {
            GeneralPath p = new GeneralPath();
            p.moveTo(ox + w * 0.72f, oy + h * 0.22f);
            p.curveTo(ox + w * 0.92f, oy + h * 0.35f, ox + w * 0.88f, oy + h * 0.78f, ox + w * 0.55f, oy + h * 0.88f);
            p.curveTo(ox + w * 0.28f, oy + h * 0.82f, ox + w * 0.18f, oy + h * 0.48f, ox + w * 0.35f, oy + h * 0.28f);
            p.curveTo(ox + w * 0.48f, oy + h * 0.18f, ox + w * 0.62f, oy + h * 0.18f, ox + w * 0.72f, oy + h * 0.22f);
            g2.draw(p);
            g2.draw(new Arc2D.Float(ox + w * 0.38f, oy + h * 0.42f, w * 0.22f, h * 0.18f, 200, 160, Arc2D.OPEN));
        }

        /** Graduation cap. */
        private static void paintSchool(Graphics2D g2, float ox, float oy, float w, float h, float stroke) {
            float cx = ox + w / 2;
            float brimY = oy + h * 0.48f;
            g2.draw(new Line2D.Float(ox + w * 0.12f, brimY, ox + w * 0.88f, brimY));
            GeneralPath cap = new GeneralPath();
            cap.moveTo(ox + w * 0.18f, brimY);
            cap.lineTo(cx, oy + h * 0.2f);
            cap.lineTo(ox + w * 0.82f, brimY);
            g2.draw(cap);
            g2.draw(new Line2D.Float(cx, oy + h * 0.2f, cx + w * 0.12f, oy + h * 0.32f));
            g2.draw(new Ellipse2D.Float(cx - w * 0.06f, brimY - h * 0.04f, w * 0.12f, h * 0.12f));
        }

        /** Envelope. */
        private static void paintMail(Graphics2D g2, float ox, float oy, float w, float h, float stroke) {
            float ix = ox + w * 0.14f;
            float iy = oy + h * 0.3f;
            float iw = w * 0.72f;
            float ih = h * 0.42f;
            float ar = ih * 0.15f;
            g2.draw(new RoundRectangle2D.Float(ix, iy, iw, ih, ar, ar));
            float cx = ix + iw / 2;
            float top = iy;
            g2.draw(new Line2D.Float(ix, top, cx, iy + ih * 0.38f));
            g2.draw(new Line2D.Float(ix + iw, top, cx, iy + ih * 0.38f));
        }

        /** Map pin. */
        private static void paintPin(Graphics2D g2, float ox, float oy, float w, float h, float stroke) {
            float cx = ox + w / 2;
            GeneralPath pin = new GeneralPath();
            pin.moveTo(cx, oy + h * 0.18f);
            pin.curveTo(ox + w * 0.78f, oy + h * 0.22f, ox + w * 0.78f, oy + h * 0.48f, cx, oy + h * 0.88f);
            pin.curveTo(ox + w * 0.22f, oy + h * 0.48f, ox + w * 0.22f, oy + h * 0.22f, cx, oy + h * 0.18f);
            g2.draw(pin);
            g2.draw(new Ellipse2D.Float(cx - w * 0.1f, oy + h * 0.32f, w * 0.2f, h * 0.16f));
        }

        /** Open book. */
        private static void paintBook(Graphics2D g2, float ox, float oy, float w, float h, float stroke) {
            float cx = ox + w / 2;
            float top = oy + h * 0.22f;
            float bot = oy + h * 0.78f;
            g2.draw(new Line2D.Float(cx, top, cx, bot));
            GeneralPath left = new GeneralPath();
            left.moveTo(cx, top);
            left.quadTo(ox + w * 0.12f, oy + h * 0.38f, ox + w * 0.18f, bot);
            left.lineTo(cx, bot);
            g2.draw(left);
            GeneralPath right = new GeneralPath();
            right.moveTo(cx, top);
            right.quadTo(ox + w * 0.88f, oy + h * 0.38f, ox + w * 0.82f, bot);
            right.lineTo(cx, bot);
            g2.draw(right);
        }

        /** Save / confirm — works on light (white) ink for gradient button. */
        private static void paintSave(Graphics2D g2, float ox, float oy, float w, float h, float stroke, Color ink) {
            float cy = oy + h / 2;
            float inset = w * 0.16f;
            float rw = w - 2 * inset;
            float rh = h * 0.48f;
            float ry = oy + h * 0.22f;
            float ar = Math.min(rw, rh) * 0.18f;
            boolean lightInk = (ink.getRed() * 0.299 + ink.getGreen() * 0.587 + ink.getBlue() * 0.114) > 210;
            g2.setColor(ink);
            g2.draw(new RoundRectangle2D.Float(ox + inset, ry, rw, rh, ar, ar));
            g2.setStroke(new BasicStroke(stroke * (lightInk ? 1.15f : 0.9f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            GeneralPath chk = new GeneralPath();
            chk.moveTo(ox + w * 0.28f, cy);
            chk.lineTo(ox + w * 0.44f, cy + h * 0.14f);
            chk.lineTo(ox + w * 0.72f, cy - h * 0.12f);
            g2.draw(chk);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
    }
}
