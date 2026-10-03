package com.campusplacement.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** Flat replacements for the Metal combo box and scroll bar delegates. */
public final class ThemeUI {
    private ThemeUI() { }

    /** Thin, rounded scroll bar thumb without arrow buttons. */
    public static class ScrollBar extends BasicScrollBarUI {
        public static ComponentUI createUI(JComponent c) { return new ScrollBar(); }

        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(0xCFC7C2);
            trackColor = Theme.SURFACE;
        }

        @Override
        protected JButton createDecreaseButton(int o) { return zero(); }

        @Override
        protected JButton createIncreaseButton(int o) { return zero(); }

        private JButton zero() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(c.getParent() != null && c.getParent().getBackground() != null ? c.getParent().getBackground() : trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? new Color(0xB3A9A4) : thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6);
            g2.dispose();
        }
    }

    /** Combo box with a flat surface and a plum chevron. */
    public static class Combo extends BasicComboBoxUI {
        public static ComponentUI createUI(JComponent c) { return new Combo(); }

        @Override
        protected JButton createArrowButton() {
            JButton b = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.SURFACE);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(Theme.PLUM);
                    g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    Path2D p = new Path2D.Double();
                    p.moveTo(cx - 4, cy - 2);
                    p.lineTo(cx, cy + 2);
                    p.lineTo(cx + 4, cy - 2);
                    g2.draw(p);
                    g2.dispose();
                }
            };
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setContentAreaFilled(false);
            b.setFocusable(false);
            b.setPreferredSize(new Dimension(26, 26));
            return b;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(comboBox.isEnabled() ? Theme.SURFACE : Theme.BG);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
            super.paintCurrentValue(g, bounds, false);
        }
    }
}
