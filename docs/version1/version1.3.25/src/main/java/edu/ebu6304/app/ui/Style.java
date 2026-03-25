package edu.ebu6304.app.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public final class Style {
    public static final Color BG = new Color(246, 247, 251);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER = new Color(224, 227, 234);
    public static final Color TEXT = new Color(17, 24, 39);
    public static final Color MUTED = new Color(107, 114, 128);
    public static final Color PRIMARY = new Color(17, 24, 39);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color WARNING = new Color(217, 119, 6);
    public static final Color DANGER = new Color(220, 38, 38);

    public static final Font FONT_H1 = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_H2 = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private Style() {}

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        );
    }

    public static Border sectionBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        );
    }

    public static void stylePrimaryButton(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        button.setFont(FONT_BODY_BOLD);
    }

    public static void styleSecondaryButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(TEXT);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        button.setFont(FONT_BODY);
    }

    public static void stylePanelCard(JComponent comp) {
        comp.setBackground(CARD_BG);
        comp.setBorder(cardBorder());
    }

    public static void fixedHeight(JComponent c, int h) {
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, h));
    }
}
