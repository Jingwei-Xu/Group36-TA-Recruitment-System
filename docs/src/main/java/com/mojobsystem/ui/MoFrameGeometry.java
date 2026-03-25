package com.mojobsystem.ui;

import javax.swing.JFrame;
import java.awt.Rectangle;

/**
 * Keeps window size and screen position stable when replacing one MO screen with another.
 */
public final class MoFrameGeometry {
    public static final int FRAME_W = 1140;
    public static final int FRAME_H = 760;

    private static Rectangle lastBounds;

    private MoFrameGeometry() {
    }

    /**
     * Call from every top-level MO {@link JFrame} constructor (after {@code setDefaultCloseOperation}).
     * First window: centred default size; later: same bounds as the last remembered frame.
     */
    public static void apply(JFrame frame) {
        if (lastBounds != null && lastBounds.width >= 200 && lastBounds.height >= 200) {
            frame.setBounds(lastBounds);
        } else {
            frame.setSize(FRAME_W, FRAME_H);
            frame.setLocationRelativeTo(null);
        }
    }

    /**
     * Child window that stacks over a parent (e.g. create job) should match the parent rectangle.
     */
    public static void applyMatching(JFrame parent, JFrame child) {
        if (parent != null && parent.isDisplayable()) {
            child.setBounds(parent.getBounds());
        } else {
            apply(child);
        }
    }

    public static void rememberFrom(JFrame frame) {
        if (frame != null) {
            lastBounds = frame.getBounds();
        }
    }

    /** Replace the current window with another while preserving bounds. */
    public static void navigateReplace(JFrame from, Runnable openNext) {
        rememberFrom(from);
        from.dispose();
        openNext.run();
    }
}
