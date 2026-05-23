package Authentication_Module.view;

import Authentication_Module.session.SessionManager;
import Authentication_Module.controller.AuthController;
import Authentication_Module.model.User;
import Authentication_Module.util.JsonUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

public class LoginPagePanel extends JPanel {
    /** Inner content width inside the login card (fields + primary button). */
    private static final int CARD_CONTENT_WIDTH = 500;
    private static final int INPUT_ROW_HEIGHT = 42;
    /** Horizontal gap between label column and input shell. */
    private static final int LABEL_FIELD_GAP = 8;

    private static final Color PRIMARY_PURPLE = new Color(0x6D4DEB);
    private static final Color DEEP_PURPLE = new Color(0x4F35D9);
    private static final Color LAVENDER = new Color(0xF3EEFF);
    private static final Color LIGHT_PURPLE_BORDER = new Color(0xDED4FF);
    private static final Color DARK_TEXT = new Color(0x111033);
    private static final Color MUTED_TEXT = new Color(0x667085);
    private static final Color INPUT_BORDER = new Color(0xDDE3F0);

    private final JTextField user;
    private final JPasswordField pass;

    public LoginPagePanel(AppFrame app) {
        setLayout(new BorderLayout());
        setOpaque(false);

        ModernAuthBackground root = new ModernAuthBackground(new BorderLayout());
        root.setBorder(new EmptyBorder(20, 24, 28, 24));

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        RoundedCard card = new RoundedCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 40, 36, 40));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(CARD_CONTENT_WIDTH + 80, Integer.MAX_VALUE));

        JLabel heroIcon = new JLabel(new PortalIcon("enter", PRIMARY_PURPLE, 38));
        heroIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel heroTile = iconCircle(heroIcon, 64);
        heroTile.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Welcome Back", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(DARK_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setMaximumSize(new Dimension(CARD_CONTENT_WIDTH, title.getPreferredSize().height));

        JLabel desc = new JLabel("Login to your account", SwingConstants.CENTER);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc.setForeground(MUTED_TEXT);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        desc.setHorizontalAlignment(SwingConstants.CENTER);
        desc.setMaximumSize(new Dimension(CARD_CONTENT_WIDTH, desc.getPreferredSize().height));

        JPanel underline = underline();
        underline.setAlignmentX(Component.CENTER_ALIGNMENT);

        user = styledField("Enter your username");
        pass = styledPasswordField("Enter your password");

        JPanel userBlock = createInlineLabeledField(new PortalIcon("user", PRIMARY_PURPLE, 20), "Username", user);
        JPanel passBlock = createInlinePasswordRow(new PortalIcon("lock", PRIMARY_PURPLE, 20), "Password", pass);

        GradientButton login = new GradientButton("Login", new PortalIcon("enter", Color.WHITE, 16));
        login.setAlignmentX(Component.CENTER_ALIGNMENT);
        login.setMaximumSize(new Dimension(CARD_CONTENT_WIDTH, 44));
        login.setPreferredSize(new Dimension(CARD_CONTENT_WIDTH, 44));

        AuthController controller = new AuthController();

        login.addActionListener(e -> {
            String username = user.getText().trim();
            char[] pw = pass.getPassword();
            String password = new String(pw);
            java.util.Arrays.fill(pw, '\0');

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username and password");
                return;
            }

            User loginUser = controller.handleLogin(username, password);

            if (loginUser == null) {

                JOptionPane.showMessageDialog(
                        this,
                        controller.getLoginMessage()
                );

                return;
            }

            SessionManager.login(loginUser);
            app.setCurrentUser(loginUser);
            String role = loginUser.getRole().toLowerCase();
            switch (role) {
                case "mo":
                    boolean firstLogin = JsonUtil.isFirstLogin(loginUser);
                    if (firstLogin) {
                        app.showPage("MO_FIRST");
                    } else {
                        app.showPage("MO");
                    }
                    break;
                case "ta":
                    app.showPage("TA");
                    break;
                case "admin":
                    app.showPage("ADMIN");
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Unknown role: " + role);
                    SessionManager.logout();
                    return;
            }
        });

        JButton back = linkButton("← Back to Home");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> app.showPage("HOME"));

        card.add(heroTile);
        card.add(Box.createVerticalStrut(10));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(desc);
        card.add(Box.createVerticalStrut(10));
        card.add(underline);
        card.add(Box.createVerticalStrut(28));
        card.add(userBlock);
        card.add(Box.createVerticalStrut(20));
        card.add(passBlock);
        card.add(Box.createVerticalStrut(28));
        card.add(login);
        card.add(Box.createVerticalStrut(16));
        card.add(back);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        wrapper.add(card, gbc);
        root.add(wrapper, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            SwingUtilities.invokeLater(() -> {
                Container parent = getParent();
                if (parent != null) {
                    parent.revalidate();
                    parent.repaint();
                }
            });
        }
    }

    public void clearCredentials() {
        user.setText("");
        char[] pw = pass.getPassword();
        java.util.Arrays.fill(pw, '\0');
        pass.setText("");
    }

    private JTextField styledField(String placeholder) {
        HintTextField field = new HintTextField(placeholder);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setForeground(DARK_TEXT);
        field.setBorder(new EmptyBorder(0, 14, 0, 14));
        field.setOpaque(false);
        return field;
    }

    private JPasswordField styledPasswordField(String placeholder) {
        HintPasswordField field = new HintPasswordField(placeholder);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setForeground(DARK_TEXT);
        field.setBorder(new EmptyBorder(0, 14, 0, 8));
        field.setOpaque(false);
        return field;
    }

    /** Icon + label on the left; input stretches on the same row. */
    private JPanel createInlineLabeledField(Icon icon, String labelText, JTextField input) {
        JPanel row = new JPanel(new BorderLayout(LABEL_FIELD_GAP, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(CARD_CONTENT_WIDTH, INPUT_ROW_HEIGHT + 8));
        row.setPreferredSize(new Dimension(CARD_CONTENT_WIDTH, INPUT_ROW_HEIGHT + 8));

        row.add(compactFieldLeading(icon, labelText), BorderLayout.WEST);

        JPanel shellInner = new JPanel(new BorderLayout());
        shellInner.setOpaque(false);
        shellInner.add(input, BorderLayout.CENTER);
        row.add(fieldShellStretch(shellInner, INPUT_ROW_HEIGHT), BorderLayout.CENTER);
        return row;
    }

    private JPanel createInlinePasswordRow(Icon icon, String labelText, JPasswordField input) {
        char maskEcho = input.getEchoChar();
        if (maskEcho == 0 || Character.isWhitespace(maskEcho)) {
            maskEcho = '\u2022';
            input.setEchoChar(maskEcho);
        }
        final char maskChar = maskEcho;

        JButton eye = new JButton();
        eye.setIcon(new PortalIcon("eye-closed", MUTED_TEXT, 15));
        eye.setToolTipText("Show password");
        eye.setFocusable(false);
        eye.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 10));
        eye.setContentAreaFilled(false);
        eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eye.addActionListener(e -> {
            if (input.getEchoChar() == 0) {
                input.setEchoChar(maskChar);
                eye.setIcon(new PortalIcon("eye-closed", MUTED_TEXT, 15));
                eye.setToolTipText("Show password");
            } else {
                input.setEchoChar((char) 0);
                eye.setIcon(new PortalIcon("eye-open", MUTED_TEXT, 15));
                eye.setToolTipText("Hide password");
            }
        });

        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(input, BorderLayout.CENTER);
        holder.add(eye, BorderLayout.EAST);

        JPanel row = new JPanel(new BorderLayout(LABEL_FIELD_GAP, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(CARD_CONTENT_WIDTH, INPUT_ROW_HEIGHT + 8));
        row.setPreferredSize(new Dimension(CARD_CONTENT_WIDTH, INPUT_ROW_HEIGHT + 8));

        row.add(compactFieldLeading(icon, labelText), BorderLayout.WEST);
        row.add(fieldShellStretch(holder, INPUT_ROW_HEIGHT), BorderLayout.CENTER);
        return row;
    }

    /** Icon + label packed to natural width (no fixed trailing gap before the field). */
    private JPanel compactFieldLeading(Icon icon, String labelText) {
        JPanel lead = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        lead.setOpaque(false);
        lead.add(mediumIconTile(icon));
        JLabel lab = new JLabel(labelText);
        lab.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lab.setForeground(DARK_TEXT);
        lead.add(lab);
        return lead;
    }

    /** Rounded input shell that grows horizontally within the row. */
    private JPanel fieldShellStretch(Component inner, int height) {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setOpaque(false);
        shell.setPreferredSize(new Dimension(220, height));
        shell.setMinimumSize(new Dimension(160, height));
        shell.add(inner, BorderLayout.CENTER);
        return new RoundedShell(shell, 10, Color.WHITE, INPUT_BORDER, 1f, new EmptyBorder(0, 0, 0, 0));
    }

    private JPanel mediumIconTile(Icon icon) {
        int sz = 38;
        JPanel tile = new JPanel(new GridBagLayout());
        tile.setOpaque(false);
        tile.setPreferredSize(new Dimension(sz, sz));
        tile.setMinimumSize(new Dimension(sz, sz));
        tile.setMaximumSize(new Dimension(sz, sz));
        tile.add(new JLabel(icon));
        return new RoundedShell(tile, 10, LAVENDER, null, 0, new EmptyBorder(0, 0, 0, 0));
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
        u.setPreferredSize(new Dimension(28, 3));
        u.setMinimumSize(new Dimension(28, 3));
        u.setMaximumSize(new Dimension(28, 3));
        return u;
    }

    private JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setForeground(PRIMARY_PURPLE);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setMargin(new Insets(10, 12, 12, 12));
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

    private static class ModernAuthBackground extends JPanel {
        ModernAuthBackground(LayoutManager lm) { super(lm); setOpaque(true); setBackground(new Color(0xFAFAFF)); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight();
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
            for (int x = x1; x < x2; x += 14) for (int y = y1; y < y2; y += 14) g2.fillOval(x, y, 3, 3);
        }
        private void paintWave(Graphics2D g2, int w, int h, Color c, double base, int phase) {
            Polygon p = new Polygon(); p.addPoint(0, h);
            int yBase = (int) (h * base);
            for (int x = 0; x <= w; x += 20) p.addPoint(x, yBase + (int) (34 * Math.sin((x + phase) / 120.0)));
            p.addPoint(w, h); g2.setColor(c); g2.fillPolygon(p);
        }
    }

    private static class RoundedCard extends JPanel {
        RoundedCard() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(79, 53, 217, 18)); g2.fillRoundRect(4, 6, w - 8, h - 8, 18, 18);
            g2.setColor(new Color(255, 255, 255, 248)); g2.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);
            g2.setColor(LIGHT_PURPLE_BORDER); g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);
            g2.dispose(); super.paintComponent(g);
        }
    }

    private static class RoundedShell extends JPanel {
        private final int arc; private final Color fill; private final Color stroke; private final float strokeWidth;
        RoundedShell(Component inner, int arc, Color fill, Color stroke, float strokeWidth, EmptyBorder pad) { super(new BorderLayout()); this.arc=arc; this.fill=fill; this.stroke=stroke; this.strokeWidth=strokeWidth; setOpaque(false); setBorder(pad); add(inner, BorderLayout.CENTER); }
        @Override protected void paintComponent(Graphics g) { Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(fill); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,arc,arc); if(stroke!=null&&strokeWidth>0){g2.setStroke(new BasicStroke(strokeWidth)); g2.setColor(stroke); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,arc,arc);} g2.dispose(); super.paintComponent(g); }
    }

    private static class GradientButton extends JButton {
        private final Icon leadingIcon; private boolean hover;
        GradientButton(String text, Icon leadingIcon) { super(text); this.leadingIcon=leadingIcon; setFont(new Font("Segoe UI", Font.BOLD, 14)); setForeground(Color.WHITE); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){hover=true;repaint();}@Override public void mouseExited(MouseEvent e){hover=false;repaint();}}); }
        @Override protected void paintComponent(Graphics g) { Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); Color left=hover?new Color(0x7C5CFF):PRIMARY_PURPLE; Color right=hover?new Color(0x3F2BB8):DEEP_PURPLE; g2.setPaint(new GradientPaint(0,0,left,getWidth(),getHeight(),right)); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); FontMetrics fm=g2.getFontMetrics(getFont()); int iw=leadingIcon!=null?leadingIcon.getIconWidth():0; int gap=leadingIcon!=null?8:0; int tw=fm.stringWidth(getText()); int x=(getWidth()-iw-gap-tw)/2; int y=(getHeight()-fm.getHeight())/2+fm.getAscent(); if(leadingIcon!=null){leadingIcon.paintIcon(this,g2,x,(getHeight()-leadingIcon.getIconHeight())/2); x+=iw+gap;} g2.setFont(getFont()); g2.setColor(Color.WHITE); g2.drawString(getText(),x,y); g2.dispose(); }
    }

    private static class HintTextField extends JTextField { private final String hint; HintTextField(String hint){this.hint=hint;} @Override protected void paintComponent(Graphics g){super.paintComponent(g); if(getText().isEmpty()&&!isFocusOwner()){Graphics2D g2=(Graphics2D)g.create(); g2.setColor(new Color(0x98A2B3)); g2.setFont(getFont()); FontMetrics fm=g2.getFontMetrics(); g2.drawString(hint,14,(getHeight()-fm.getHeight())/2+fm.getAscent()); g2.dispose();}} }
    private static class HintPasswordField extends JPasswordField { private final String hint; HintPasswordField(String hint){this.hint=hint;} @Override protected void paintComponent(Graphics g){super.paintComponent(g); if(getPassword().length==0&&!isFocusOwner()){Graphics2D g2=(Graphics2D)g.create(); g2.setColor(new Color(0x98A2B3)); g2.setFont(getFont()); FontMetrics fm=g2.getFontMetrics(); g2.drawString(hint,14,(getHeight()-fm.getHeight())/2+fm.getAscent()); g2.dispose();}} }

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
                case "lock" -> paintLock(g2, ox, oy, w, h, stroke);
                case "enter" -> paintEnter(g2, ox, oy, w, h, stroke);
                case "eye-open" -> paintEyeOpen(g2, ox, oy, w, h, stroke, color);
                case "eye-closed", "eye" -> paintEyeClosed(g2, ox, oy, w, h, stroke);
                default -> g2.draw(new RoundRectangle2D.Float(ox + w * 0.15f, oy + h * 0.15f, w * 0.7f, h * 0.7f, w * 0.12f, h * 0.12f));
            }
            g2.dispose();
        }

        /** Rounded head + smooth shoulder curve. */
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

        /** Padlock with open shackle curve and keyhole hint. */
        private static void paintLock(Graphics2D g2, float ox, float oy, float w, float h, float stroke) {
            float bodyW = w * 0.48f;
            float bodyH = h * 0.42f;
            float bodyX = ox + (w - bodyW) / 2;
            float bodyY = oy + h * 0.48f;
            float arcBody = Math.min(bodyW, bodyH) * 0.28f;
            g2.draw(new RoundRectangle2D.Float(bodyX, bodyY, bodyW, bodyH, arcBody, arcBody));

            float shW = bodyW * 0.62f;
            float shH = h * 0.36f;
            float shX = ox + (w - shW) / 2;
            float shY = oy + h * 0.16f;
            Arc2D shackle = new Arc2D.Float(shX, shY, shW, shH, 0, 180, Arc2D.OPEN);
            g2.draw(shackle);

            float kx = bodyX + bodyW / 2;
            float ky = bodyY + bodyH * 0.38f;
            g2.setStroke(new BasicStroke(stroke * 0.85f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Float(kx, ky, kx, ky + bodyH * 0.32f));
            g2.draw(new Ellipse2D.Float(kx - stroke * 0.55f, ky - stroke * 0.9f, stroke * 1.1f, stroke * 1.1f));
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }

        /** Arrow into a door frame — sign-in metaphor. */
        private static void paintEnter(Graphics2D g2, float ox, float oy, float w, float h, float stroke) {
            float cy = oy + h / 2;
            float frameR = ox + w * 0.88f;
            float top = oy + h * 0.22f;
            float bot = oy + h * 0.78f;
            g2.draw(new Line2D.Float(frameR, top, frameR, bot));
            g2.draw(new Line2D.Float(frameR, top, frameR - w * 0.12f, top));
            g2.draw(new Line2D.Float(frameR, bot, frameR - w * 0.12f, bot));

            float tipX = frameR - w * 0.22f;
            float shaftEnd = ox + w * 0.18f;
            g2.draw(new Line2D.Float(shaftEnd, cy, tipX, cy));
            float ah = Math.min(w, h) * 0.14f;
            GeneralPath head = new GeneralPath();
            head.moveTo(tipX, cy);
            head.lineTo(tipX - ah * 1.1f, cy - ah);
            head.moveTo(tipX, cy);
            head.lineTo(tipX - ah * 1.1f, cy + ah);
            g2.draw(head);
        }

        /** Open eye: clear sclera outline, iris ring, pupil, highlight. */
        private static void paintEyeOpen(Graphics2D g2, float ox, float oy, float w, float h, float stroke, Color tint) {
            float cx = ox + w / 2;
            float cy = oy + h / 2;
            float ew = w * 0.78f;
            float eh = h * 0.38f;
            g2.draw(new Ellipse2D.Float(cx - ew / 2, cy - eh / 2, ew, eh));

            float pr = Math.min(ew, eh) * 0.22f;
            g2.setStroke(new BasicStroke(stroke * 0.75f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Ellipse2D.Float(cx - pr * 1.35f, cy - pr * 1.35f, pr * 2.7f, pr * 2.7f));
            g2.setColor(tint);
            g2.fill(new Ellipse2D.Float(cx - pr, cy - pr, pr * 2, pr * 2));

            float hx = cx + pr * 0.35f;
            float hy = cy - pr * 0.35f;
            g2.setColor(new Color(255, 255, 255, 210));
            g2.fill(new Ellipse2D.Float(hx, hy, pr * 0.55f, pr * 0.55f));
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }

        /** Closed eye: soft almond + upper/lower lids meeting (hidden password). */
        private static void paintEyeClosed(Graphics2D g2, float ox, float oy, float w, float h, float stroke) {
            float cx = ox + w / 2;
            float cy = oy + h / 2;
            float ew = w * 0.78f;
            float eh = h * 0.36f;

            g2.setStroke(new BasicStroke(stroke * 0.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Ellipse2D.Float(cx - ew / 2, cy - eh / 2, ew, eh));

            g2.setStroke(new BasicStroke(stroke * 1.05f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            GeneralPath upper = new GeneralPath();
            upper.moveTo(ox + w * 0.09f, cy + eh * 0.06f);
            upper.quadTo(cx, cy - eh * 0.42f, ox + w * 0.91f, cy + eh * 0.06f);
            g2.draw(upper);

            GeneralPath lower = new GeneralPath();
            lower.moveTo(ox + w * 0.11f, cy - eh * 0.02f);
            lower.quadTo(cx, cy + eh * 0.38f, ox + w * 0.89f, cy - eh * 0.02f);
            g2.draw(lower);

            g2.setStroke(new BasicStroke(stroke * 0.45f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = -2; i <= 2; i++) {
                float t = i / 3f;
                float lx = cx + t * ew * 0.28f;
                g2.draw(new Line2D.Float(lx, cy - eh * 0.02f, lx + ew * 0.04f, cy - eh * 0.18f));
            }
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
    }
}
