package Authentication_Module.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Modern purple / white landing page for the TA Recruitment System.
 *
 * Notes:
 * 1) This panel does not change or request any JFrame size.
 * 2) The large hero logo has been removed; only the small header logo is kept.
 * 3) All functional navigation callbacks are preserved: LOGIN / REGISTER.
 */
public class HomePagePanel extends JPanel {

    /** School logo image path provided by the project owner. */
    private static final String SCHOOL_LOGO_PATH =
            "C:\\Users\\35375\\Desktop\\version4_integrate\\resources\\bupt_intl_school_logo.png";

    private static final Color PAGE_TOP = new Color(0xFFFFFF);
    private static final Color PAGE_BOTTOM = new Color(0xF8F6FF);
    private static final Color PRIMARY_PURPLE = new Color(0x6D4DEB);
    private static final Color DEEP_PURPLE = new Color(0x4F35D9);
    private static final Color SOFT_PURPLE = new Color(0x7B5CF6);
    private static final Color LAVENDER = new Color(0xF3EEFF);
    private static final Color LIGHT_PURPLE_BORDER = new Color(0xDED4FF);
    private static final Color DARK_TEXT = new Color(0x111033);
    private static final Color MUTED_TEXT = new Color(0x667085);

    private static final Font FONT_BRAND = new Font("Segoe UI", Font.BOLD, 25);
    private static final Font FONT_EYEBROW = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 34);
    private static final Font FONT_TITLE_PURPLE = new Font("Segoe UI", Font.BOLD, 36);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_PORTAL_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_PORTAL_BODY = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_CTA_READY = new Font("Segoe UI", Font.BOLD, 22);

    private static void applyVectorRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }

    /** High-res icon raster pass: smooth AA, normalized strokes (thin lines stay elegant after downscale). */
    private static void applyIconOffscreenHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }

    public HomePagePanel(AppFrame app) {
        setLayout(new BorderLayout());
        setBackground(PAGE_BOTTOM);

        LandingBackground content = new LandingBackground();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(13, 20, 26, 20));

        content.add(buildHeader(app));
        content.add(Box.createVerticalStrut(8));
        content.add(buildHero(app));
        content.add(Box.createVerticalStrut(28));
        content.add(buildPortalCards());
        content.add(Box.createVerticalStrut(18));
        content.add(buildFooterCta(app));
        content.add(Box.createVerticalStrut(18));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel buildHeader(AppFrame app) {
        JPanel header = new JPanel(new BorderLayout(24, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        header.setPreferredSize(new Dimension(10, 66));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel logoHolder = new LogoImageTile(50, 50, 16, true, 4);
        brand.add(logoHolder);

        JLabel name = new JLabel("TA System");
        name.setFont(FONT_BRAND);
        name.setForeground(PRIMARY_PURPLE);
        name.setVerticalAlignment(SwingConstants.CENTER);
        brand.add(name);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        actions.setOpaque(false);

        JButton loginBtn = gradientButton("Login", 118, 42, VectorIcon.user(Color.WHITE, 18));
        loginBtn.addActionListener(e -> app.showPage("LOGIN"));

        JButton registerBtn = outlineButton("Register", 132, 42, VectorIcon.userPlus(DEEP_PURPLE, 18));
        registerBtn.addActionListener(e -> app.showPage("REGISTER"));

        actions.add(loginBtn);
        actions.add(registerBtn);

        header.add(brand, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel buildHero(AppFrame app) {
        JPanel hero = new JPanel();
        hero.setOpaque(false);
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.setBorder(new EmptyBorder(2, 0, 8, 0));

        JLabel eyebrow = new JLabel("MULTI-ROLE MANAGEMENT PLATFORM");
        eyebrow.setFont(FONT_EYEBROW);
        eyebrow.setForeground(DEEP_PURPLE);
        eyebrow.setBorder(new EmptyBorder(6, 14, 6, 14));
        eyebrow.setAlignmentX(Component.CENTER_ALIGNMENT);
        hero.add(roundedPillWrap(eyebrow, 16, LAVENDER, LIGHT_PURPLE_BORDER, Component.CENTER_ALIGNMENT));

        hero.add(Box.createVerticalStrut(12));

        JLabel titleLine1 = new JLabel("Teaching Assistant");
        titleLine1.setFont(FONT_TITLE);
        titleLine1.setForeground(DARK_TEXT);
        titleLine1.setAlignmentX(Component.CENTER_ALIGNMENT);
        hero.add(titleLine1);

        JLabel titleLine2 = new JLabel("Management System");
        titleLine2.setFont(FONT_TITLE_PURPLE);
        titleLine2.setForeground(PRIMARY_PURPLE);
        titleLine2.setAlignmentX(Component.CENTER_ALIGNMENT);
        hero.add(titleLine2);

        hero.add(Box.createVerticalStrut(12));

        JLabel subtitle = new JLabel("<html><body style='width:400px;text-align:center;line-height:1.36'>"
                + "A simple and efficient platform for TA, MO, and Admin management,<br>"
                + "designed with a clean and modern interface."
                + "</body></html>");
        subtitle.setFont(FONT_BODY);
        subtitle.setForeground(MUTED_TEXT);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        hero.add(subtitle);

        hero.add(Box.createVerticalStrut(17));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startBtn = gradientButton("Get Started", 192, 42, null);
        startBtn.addActionListener(e -> app.showPage("LOGIN"));

        JButton learnBtn = outlineButton("Learn More", 184, 42, VectorIcon.info(DEEP_PURPLE, 18));

        btnRow.add(startBtn);
        btnRow.add(learnBtn);
        hero.add(btnRow);

        return hero;
    }

    private JPanel buildPortalCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(new PortalCard("01", VectorIcon.user(PRIMARY_PURPLE, 34), "TA Portal",
                "Manage personal information, teaching tasks, and assigned work with a clean and focused workflow."));
        row.add(new PortalCard("02", VectorIcon.briefcase(PRIMARY_PURPLE, 34), "MO Portal",
                "Oversee recruitment progress, review applications, and coordinate management tasks efficiently."));
        row.add(new PortalCard("03", VectorIcon.shield(PRIMARY_PURPLE, 34), "Admin Portal",
                "Control users, permissions, and system-level settings through a structured administrative panel."));

        return row;
    }

    private JPanel buildFooterCta(AppFrame app) {
        GradientCtaPanel cta = new GradientCtaPanel();
        cta.setLayout(new GridBagLayout());
        cta.setBorder(new EmptyBorder(17, 20, 17, 20));
        cta.setAlignmentX(Component.LEFT_ALIGNMENT);
        cta.setMinimumSize(new Dimension(10, 122));
        cta.setPreferredSize(new Dimension(10, 134));
        cta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 146));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Ready to get started?");
        title.setFont(FONT_CTA_READY);
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(title);

        center.add(Box.createVerticalStrut(6));

        JLabel hint = new JLabel("Create your account and access the system in a few steps.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(new Color(255, 255, 255, 232));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(hint);

        center.add(Box.createVerticalStrut(13));

        JButton btn = ctaWhiteButton("Register for Free", 228, 42, VectorIcon.userPlus(DEEP_PURPLE, 18));
        btn.addActionListener(e -> app.showPage("REGISTER"));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(btn);

        cta.add(center, gbc);
        return cta;
    }

    private JButton gradientButton(String text, int width, int height, Icon icon) {
        GradientButton btn = new GradientButton(text, icon);
        btn.setPreferredSize(new Dimension(width, height));
        btn.setMinimumSize(new Dimension(width, height));
        btn.setMaximumSize(new Dimension(width, height));
        return btn;
    }

    private JButton outlineButton(String text, int width, int height, Icon icon) {
        OutlineButton btn = new OutlineButton(text, icon);
        btn.setPreferredSize(new Dimension(width, height));
        btn.setMinimumSize(new Dimension(width, height));
        btn.setMaximumSize(new Dimension(width, height));
        return btn;
    }

    private JButton ctaWhiteButton(String text, int width, int height, Icon icon) {
        CtaWhiteButton btn = new CtaWhiteButton(text + "  →", icon);
        btn.setPreferredSize(new Dimension(width, height));
        btn.setMinimumSize(new Dimension(width, height));
        btn.setMaximumSize(new Dimension(width, height));
        return btn;
    }

    private JComponent roundedPillWrap(JComponent inner, int arc, Color fill, Color border, float alignmentX) {
        JPanel wrap = new RoundedSurface(arc, fill, border, false, new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.setOpaque(false);
        wrap.add(inner);
        Dimension pref = wrap.getPreferredSize();
        wrap.setMaximumSize(new Dimension(pref.width, pref.height));
        wrap.setAlignmentX(alignmentX);
        return wrap;
    }

    public void refreshButtonStyles() {
        SwingUtilities.invokeLater(() -> {
            refreshAllButtons(this);
            revalidate();
            repaint();
        });
    }

    private void refreshAllButtons(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                comp.repaint();
            } else if (comp instanceof Container) {
                refreshAllButtons((Container) comp);
            }
        }
    }

    private static Image loadSchoolLogo() {
        try {
            File f = new File(SCHOOL_LOGO_PATH);
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(SCHOOL_LOGO_PATH);
                if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                    return icon.getImage();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static final Image SCHOOL_LOGO_IMAGE = loadSchoolLogo();

    private static class LandingBackground extends JPanel implements Scrollable {
        LandingBackground() {
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL
                    ? Math.max(visibleRect.height - 24, 1)
                    : Math.max(visibleRect.width - 24, 1);
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        /**
         * Keeps content width equal to the scroll viewport so nothing clips horizontally when the
         * horizontal scrollbar is hidden.
         */
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? 18 : 10;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyVectorRenderingHints(g2);
            int w = getWidth();
            int h = getHeight();

            g2.setPaint(new GradientPaint(0, 0, PAGE_TOP, 0, h, PAGE_BOTTOM));
            g2.fillRect(0, 0, w, h);

            // Broad soft lavender wave, matching the supplied reference.
            Path2D wave = new Path2D.Double();
            wave.moveTo(0, 330);
            wave.curveTo(w * 0.17, 285, w * 0.27, 525, w * 0.50, 380);
            wave.curveTo(w * 0.72, 245, w * 0.84, 435, w, 250);
            wave.lineTo(w, 560);
            wave.lineTo(0, 560);
            wave.closePath();
            g2.setColor(new Color(109, 77, 235, 17));
            g2.fill(wave);

            Path2D wave2 = new Path2D.Double();
            wave2.moveTo(0, h - 170);
            wave2.curveTo(w * 0.17, h - 240, w * 0.30, h - 90, w * 0.48, h - 160);
            wave2.curveTo(w * 0.72, h - 245, w * 0.82, h - 80, w, h - 160);
            wave2.lineTo(w, h);
            wave2.lineTo(0, h);
            wave2.closePath();
            g2.setColor(new Color(109, 77, 235, 12));
            g2.fill(wave2);

            paintDots(g2, Math.max(0, w - 245), 120, 205, 175, new Color(109, 77, 235, 38));
            paintDots(g2, 0, 200, 190, 190, new Color(109, 77, 235, 24));
            paintDots(g2, Math.max(0, w - 125), Math.max(0, h - 118), 100, 85, new Color(255, 255, 255, 28));

            g2.dispose();
            super.paintComponent(g);
        }

        private void paintDots(Graphics2D g2, int sx, int sy, int width, int height, Color color) {
            g2.setColor(color);
            for (int x = sx; x < sx + width; x += 14) {
                for (int y = sy; y < sy + height; y += 14) {
                    g2.fillOval(x, y, 3, 3);
                }
            }
        }
    }

    private static class RoundedSurface extends JPanel {
        private final int arc;
        private final Color fill;
        private final Color border;
        private final boolean shadow;

        RoundedSurface(int arc, Color fill, Color border, boolean shadow, LayoutManager layout) {
            super(layout);
            this.arc = arc;
            this.fill = fill;
            this.border = border;
            this.shadow = shadow;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyVectorRenderingHints(g2);
            int w = getWidth();
            int h = getHeight();
            if (shadow) {
                g2.setColor(new Color(79, 53, 217, 18));
                g2.fillRoundRect(2, 5, w - 5, h - 7, arc, arc);
                g2.setColor(new Color(17, 16, 51, 8));
                g2.fillRoundRect(1, 3, w - 3, h - 4, arc, arc);
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 2, h - 2, arc, arc);
            if (border != null) {
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 2, h - 2, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class LogoImageTile extends RoundedSurface {
        private final int imagePadding;

        LogoImageTile(int w, int h, int arc, boolean shadow, int imagePadding) {
            super(arc, Color.WHITE, LIGHT_PURPLE_BORDER, shadow, new BorderLayout());
            this.imagePadding = imagePadding;
            setPreferredSize(new Dimension(w, h));
            setMinimumSize(new Dimension(w, h));
            setMaximumSize(new Dimension(w, h));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            paintLogoImage((Graphics2D) g, this, imagePadding);
        }
    }

    private static void paintLogoImage(Graphics2D original, JComponent comp, int padding) {
        Graphics2D g2 = (Graphics2D) original.create();
        applyVectorRenderingHints(g2);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        int w = comp.getWidth();
        int h = comp.getHeight();
        int availableW = Math.max(1, w - padding * 2);
        int availableH = Math.max(1, h - padding * 2);

        if (SCHOOL_LOGO_IMAGE != null) {
            int iw = SCHOOL_LOGO_IMAGE.getWidth(comp);
            int ih = SCHOOL_LOGO_IMAGE.getHeight(comp);
            if (iw > 0 && ih > 0) {
                double scale = Math.min(availableW / (double) iw, availableH / (double) ih);
                int dw = Math.max(1, (int) Math.round(iw * scale));
                int dh = Math.max(1, (int) Math.round(ih * scale));
                int x = (w - dw) / 2;
                int y = (h - dh) / 2;
                g2.drawImage(SCHOOL_LOGO_IMAGE, x, y, dw, dh, comp);
                g2.dispose();
                return;
            }
        }

        // Fallback: draw a graduation cap, matching the reference if the local logo is unavailable.
        int cx = w / 2;
        int cy = h / 2;
        g2.setColor(LAVENDER);
        g2.fillRoundRect(padding, padding, availableW, availableH, 14, 14);
        g2.setColor(PRIMARY_PURPLE);
        Path2D cap = new Path2D.Double();
        cap.moveTo(cx, cy - 15);
        cap.lineTo(cx + 22, cy - 5);
        cap.lineTo(cx, cy + 6);
        cap.lineTo(cx - 22, cy - 5);
        cap.closePath();
        g2.fill(cap);
        g2.fillRoundRect(cx - 12, cy + 2, 24, 9, 4, 4);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx + 17, cy - 3, cx + 17, cy + 17);
        g2.fillOval(cx + 14, cy + 15, 7, 7);
        g2.dispose();
    }

    private static class PortalCard extends RoundedSurface {
        PortalCard(String number, Icon icon, String title, String description) {
            super(18, Color.WHITE, LIGHT_PURPLE_BORDER, true, new BorderLayout());
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(15, 10, 15, 10));

            JPanel top = new JPanel(new BorderLayout(6, 0));
            top.setOpaque(false);
            top.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel iconTile = new IconTile(icon, 48, 48);
            top.add(iconTile, BorderLayout.WEST);

            JLabel num = new JLabel(number);
            num.setFont(new Font("Segoe UI", Font.BOLD, 34));
            num.setForeground(new Color(109, 77, 235, 28));
            top.add(num, BorderLayout.EAST);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(FONT_PORTAL_TITLE);
            titleLabel.setForeground(DARK_TEXT);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            text.add(titleLabel);
            text.add(Box.createVerticalStrut(5));

            JPanel underline = new JPanel();
            underline.setOpaque(true);
            underline.setBackground(PRIMARY_PURPLE);
            underline.setPreferredSize(new Dimension(40, 2));
            underline.setMaximumSize(new Dimension(40, 2));
            underline.setAlignmentX(Component.LEFT_ALIGNMENT);
            text.add(underline);
            text.add(Box.createVerticalStrut(8));

            JLabel desc = new JLabel("<html><body style='width:220px;margin:0;padding:0;line-height:1.34'>" + description + "</body></html>");
            desc.setFont(FONT_PORTAL_BODY);
            desc.setForeground(MUTED_TEXT);
            desc.setVerticalAlignment(SwingConstants.TOP);
            desc.setAlignmentX(Component.LEFT_ALIGNMENT);
            text.add(desc);

            add(top);
            add(Box.createVerticalStrut(8));
            add(text);
        }
    }

    private static class IconTile extends RoundedSurface {
        private final Icon icon;

        IconTile(Icon icon, int w, int h) {
            super(40, LAVENDER, LIGHT_PURPLE_BORDER, false, new BorderLayout());
            this.icon = icon;
            setPreferredSize(new Dimension(w, h));
            setMinimumSize(new Dimension(w, h));
            setMaximumSize(new Dimension(w, h));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (icon != null) {
                int x = (getWidth() - icon.getIconWidth()) / 2;
                int y = (getHeight() - icon.getIconHeight()) / 2;
                if (g instanceof Graphics2D g2) {
                    applyVectorRenderingHints(g2);
                }
                icon.paintIcon(this, g, x, y);
            }
        }
    }

    private static class GradientCtaPanel extends JPanel {
        GradientCtaPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyVectorRenderingHints(g2);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(new Color(79, 53, 217, 34));
            g2.fillRoundRect(1, 8, w - 3, h - 10, 20, 20);
            g2.setPaint(new GradientPaint(0, 0, DEEP_PURPLE, w, h, SOFT_PURPLE));
            g2.fillRoundRect(0, 0, w - 2, h - 4, 20, 20);

            // subtle decorative circles / dot texture
            g2.setColor(new Color(255, 255, 255, 22));
            g2.fillOval(-70, -40, 260, 230);
            g2.fillOval(w - 150, h - 85, 230, 180);
            for (int x = 24; x < 260; x += 14) {
                for (int y = 20; y < h - 18; y += 14) {
                    g2.fillOval(x, y, 3, 3);
                }
            }
            for (int x = w - 88; x < w - 15; x += 14) {
                for (int y = h - 72; y < h - 16; y += 14) {
                    g2.fillOval(x, y, 3, 3);
                }
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class GradientButton extends JButton {
        private boolean hover;

        GradientButton(String text, Icon icon) {
            super(text, icon);
            setFont(FONT_BUTTON);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setIconTextGap(10);
            setHorizontalTextPosition(SwingConstants.RIGHT);
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
            applyVectorRenderingHints(g2);
            int w = getWidth();
            int h = getHeight();
            Color left = hover ? new Color(0x7B5CF6) : PRIMARY_PURPLE;
            Color right = hover ? new Color(0x3F2BB8) : DEEP_PURPLE;
            g2.setColor(new Color(79, 53, 217, 36));
            g2.fillRoundRect(0, 5, w, h - 5, 14, 14);
            g2.setPaint(new GradientPaint(0, 0, left, w, h, right));
            g2.fillRoundRect(0, 0, w - 1, h - 6, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class OutlineButton extends JButton {
        private boolean hover;

        OutlineButton(String text, Icon icon) {
            super(text, icon);
            setFont(FONT_BUTTON);
            setForeground(DEEP_PURPLE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setIconTextGap(10);
            setHorizontalTextPosition(SwingConstants.RIGHT);
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
            applyVectorRenderingHints(g2);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(hover ? LAVENDER : Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
            g2.setColor(LIGHT_PURPLE_BORDER);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class CtaWhiteButton extends JButton {
        private boolean hover;

        CtaWhiteButton(String text, Icon icon) {
            super(text, icon);
            setFont(FONT_BUTTON);
            setForeground(DEEP_PURPLE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setIconTextGap(10);
            setHorizontalTextPosition(SwingConstants.RIGHT);
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
            applyVectorRenderingHints(g2);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(hover ? new Color(0xF8F6FF) : Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class VectorIcon implements Icon {
        private final int size;
        private final Color color;
        private final int type;

        private static final int USER = 1;
        private static final int USER_PLUS = 2;
        private static final int INFO = 3;
        private static final int BRIEFCASE = 4;
        private static final int SHIELD = 5;

        static Icon user(Color color, int size) {
            return new VectorIcon(USER, color, size);
        }

        static Icon userPlus(Color color, int size) {
            return new VectorIcon(USER_PLUS, color, size);
        }

        static Icon info(Color color, int size) {
            return new VectorIcon(INFO, color, size);
        }

        static Icon briefcase(Color color, int size) {
            return new VectorIcon(BRIEFCASE, color, size);
        }

        static Icon shield(Color color, int size) {
            return new VectorIcon(SHIELD, color, size);
        }

        VectorIcon(int type, Color color, int size) {
            this.type = type;
            this.color = color;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            final int scale = 4;
            BufferedImage img = new BufferedImage(size * scale, size * scale, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            try {
                applyIconOffscreenHints(g2);
                g2.scale(scale, scale);
                g2.setColor(color);
                float sw = iconStrokeWidth(size);
                g2.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                switch (type) {
                    case USER -> paintUser(g2, size);
                    case USER_PLUS -> paintUserPlus(g2, size);
                    case INFO -> paintInfo(g2, size);
                    case BRIEFCASE -> paintBriefcase(g2, size);
                    case SHIELD -> paintShield(g2, size);
                    default -> paintUser(g2, size);
                }
            } finally {
                g2.dispose();
            }
            if (g instanceof Graphics2D og) {
                applyVectorRenderingHints(og);
                og.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                og.drawImage(img, x, y, x + size, y + size, 0, 0, size * scale, size * scale, null);
            } else {
                g.drawImage(img, x, y, size, size, null);
            }
        }

        /** Thin stroke tuned for legibility at small sizes without a “bold” look. */
        private static float iconStrokeWidth(int s) {
            return Math.max(1.35f, s * 0.086f);
        }

        private static void paintUser(Graphics2D g2, int s) {
            float cx = s / 2f;
            float r = s * 0.148f;
            g2.draw(new Ellipse2D.Float(cx - r, s * 0.125f, 2 * r, 2 * r));
            Path2D shoulders = new Path2D.Float();
            shoulders.moveTo(s * 0.19f, s * 0.51f);
            shoulders.curveTo(s * 0.34f, s * 0.745f, s * 0.66f, s * 0.745f, s * 0.81f, s * 0.51f);
            g2.draw(shoulders);
        }

        private static void paintUserPlus(Graphics2D g2, int s) {
            paintUser(g2, s);
            float bx = s * 0.62f;
            float by = s * 0.08f;
            float bd = s * 0.30f;
            g2.draw(new Ellipse2D.Float(bx, by, bd, bd));
            float pcx = bx + bd / 2f;
            float pcy = by + bd / 2f;
            float h = bd * 0.20f;
            g2.drawLine(Math.round(pcx - h), Math.round(pcy), Math.round(pcx + h), Math.round(pcy));
            g2.drawLine(Math.round(pcx), Math.round(pcy - h), Math.round(pcx), Math.round(pcy + h));
        }

        private static void paintInfo(Graphics2D g2, int s) {
            float inset = s * 0.155f;
            float diam = s - 2 * inset;
            g2.draw(new Ellipse2D.Float(inset, inset, diam, diam));
            float cx = s / 2f;
            float dotR = s * 0.055f;
            g2.fill(new Ellipse2D.Float(cx - dotR, s * 0.305f - dotR, dotR * 2, dotR * 2));
            float sw = s * 0.056f;
            g2.fill(new RoundRectangle2D.Float(cx - sw / 2f, s * 0.405f, sw, s * 0.345f, sw * 0.45f, sw * 0.45f));
        }

        private static void paintBriefcase(Graphics2D g2, int s) {
            float x = s * 0.24f;
            float y = s * 0.36f;
            float bw = s * 0.52f;
            float bh = s * 0.36f;
            float arc = Math.max(4f, s * 0.10f);
            g2.draw(new RoundRectangle2D.Float(x, y, bw, bh, arc, arc));
            float hw = s * 0.28f;
            float hx = (s - hw) / 2f;
            float hy = s * 0.195f;
            g2.draw(new Arc2D.Float(hx, hy, hw, s * 0.26f, 0, 180, Arc2D.OPEN));
            float clasp = s * 0.045f;
            g2.drawLine(Math.round(s / 2f - clasp), Math.round(y), Math.round(s / 2f + clasp), Math.round(y));
        }

        private static void paintShield(Graphics2D g2, int s) {
            Path2D p = new Path2D.Float();
            float cx = s / 2f;
            float top = s * 0.115f;
            p.moveTo(cx, top);
            p.curveTo(s * 0.62f, top, s * 0.82f, s * 0.22f, s * 0.78f, s * 0.42f);
            p.curveTo(s * 0.74f, s * 0.62f, s * 0.62f, s * 0.82f, cx, s * 0.895f);
            p.curveTo(s * 0.38f, s * 0.82f, s * 0.26f, s * 0.62f, s * 0.22f, s * 0.42f);
            p.curveTo(s * 0.18f, s * 0.22f, s * 0.38f, top, cx, top);
            p.closePath();
            g2.draw(p);
            float cxMed = s / 2f;
            g2.drawLine(Math.round(cxMed), Math.round(s * 0.36f), Math.round(cxMed), Math.round(s * 0.72f));
            float arm = s * 0.14f;
            g2.drawLine(Math.round(cxMed - arm), Math.round(s * 0.52f), Math.round(cxMed + arm), Math.round(s * 0.52f));
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
