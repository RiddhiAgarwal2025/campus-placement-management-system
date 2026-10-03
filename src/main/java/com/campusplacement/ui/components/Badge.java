package com.campusplacement.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/** Small pill showing a status value with restrained, dignified colour coding. */
public class Badge extends JComponent {
    private String text = "";
    private Color bg = Theme.STONE_BG;
    private Color fg = Theme.TEXT;
    private Color rowBg = Theme.SURFACE;

    public Badge(String value) {
        setAlignmentX(LEFT_ALIGNMENT);
        setValue(value);
    }

    public final void setValue(String value) {
        String v = value == null ? "" : value;
        this.text = v.replace('_', ' ');
        switch (v) {
            case "SELECTED", "ACCEPTED", "PASS", "OPEN", "ELIGIBLE", "PLACED" -> {
                bg = Theme.isDarkMode ? new Color(0x22, 0x33, 0x47) : Theme.PLUM;
                fg = Theme.isDarkMode ? new Color(0xF1, 0xF5, 0xF9) : Theme.SURFACE;
            }
            case "SHORTLISTED", "IN_PROGRESS", "YES" -> {
                bg = Theme.LIGHT_PLUM;
                fg = Theme.isDarkMode ? Theme.TEXT : Theme.DEEP_PLUM;
            }
            case "REJECTED", "FAIL", "NOT ELIGIBLE" -> {
                bg = Theme.BRICK_BG;
                fg = Theme.BRICK;
            }
            case "PENDING", "UPCOMING", "AWAITING" -> {
                bg = Theme.OCHRE_BG;
                fg = Theme.OCHRE;
            }
            case "COMPLETED", "CLOSED" -> {
                bg = Theme.STONE_BG;
                fg = Theme.MUTED;
            }
            default -> {
                bg = Theme.STONE_BG;
                fg = Theme.MUTED;
            }
        }
        setToolTipText(text);
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(Theme.sansBold(11));
        return new Dimension(fm.stringWidth(text) + 20, 24);
    }

    @Override
    public Dimension getMaximumSize() { return getPreferredSize(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(rowBg != null ? rowBg : (Theme.isDarkMode ? Theme.SURFACE : Color.WHITE));
        g2.fillRect(0, 0, getWidth(), getHeight());
        if (!text.isEmpty()) {
            g2.setFont(Theme.sansBold(11));
            FontMetrics fm = g2.getFontMetrics();
            int h = 22;
            int w = fm.stringWidth(text) + 20;
            int x = isOpaque() ? 8 : 0;
            int y = (getHeight() - h) / 2;
            g2.setColor(bg);
            g2.fillRoundRect(x, y, w, h, h, h);
            g2.setColor(fg);
            g2.drawString(text, x + 10, y + (h - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2.dispose();
    }

    /** Table renderer that paints values as badges. */
    public static class Renderer implements TableCellRenderer {
        private final Badge badge = new Badge("");

        public Renderer() {
            badge.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus,
                                                       int row, int column) {
            badge.setValue(value == null ? "" : value.toString());
            badge.rowBg = selected ? Theme.ROW_SELECTED : row % 2 == 1 ? Theme.ROW_ALT : Theme.SURFACE;
            return badge;
        }
    }

    /** Badge for use outside tables, on a given background. */
    public static Badge on(String value, Color background) {
        Badge b = new Badge(value);
        b.rowBg = background;
        b.setOpaque(false);
        return b;
    }
}
