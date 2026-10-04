package com.campusplacement.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Apple macOS / Windows 11 Fluent style segmented control (pill tab switch).
 * Automatically balances segments into equal widths spanning the full container.
 */
public class SegmentedControl extends JPanel {
    private final List<Segment> segments = new ArrayList<>();
    private int selectedIndex = 0;
    private Consumer<Integer> onSelect;

    public SegmentedControl(String... items) {
        setLayout(new GridLayout(1, items.length, 4, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        for (int i = 0; i < items.length; i++) {
            final int index = i;
            Segment seg = new Segment(items[i], i == 0, () -> select(index));
            segments.add(seg);
            add(seg);
        }
    }

    public void onSelect(Consumer<Integer> listener) {
        this.onSelect = listener;
    }

    public void select(int index) {
        if (index < 0 || index >= segments.size()) return;
        this.selectedIndex = index;
        for (int i = 0; i < segments.size(); i++) {
            segments.get(i).setSelected(i == index);
        }
        repaint();
        if (onSelect != null) {
            onSelect.accept(index);
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        Color bg = Theme.isDarkMode ? new Color(0x11, 0x18, 0x24) : new Color(0xEB, 0xF0, 0xF5);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
        g2.setColor(Theme.BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
        g2.dispose();
        super.paintComponent(g);
    }

    private static class Segment extends JPanel {
        private final JLabel label;
        private boolean selected;
        private boolean hover;

        Segment(String text, boolean initialSelected, Runnable onClick) {
            super(new BorderLayout());
            this.selected = initialSelected;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

            label = new JLabel(text, SwingConstants.CENTER);
            label.setFont(Theme.sans(12));
            updateLook();
            add(label, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override
                public void mouseClicked(MouseEvent e) { onClick.run(); }
            });
        }

        void setSelected(boolean sel) {
            this.selected = sel;
            updateLook();
            repaint();
        }

        private void updateLook() {
            if (selected) {
                label.setFont(Theme.sansBold(12));
                label.setForeground(Theme.TEXT);
            } else {
                label.setFont(Theme.sans(12));
                label.setForeground(Theme.MUTED);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (selected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Color fill = Theme.isDarkMode ? new Color(0x22, 0x2E, 0x42) : Color.WHITE;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 8, 8);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
                g2.dispose();
            } else if (hover) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = Theme.isDarkMode ? new Color(0x18, 0x22, 0x33) : new Color(0xDF, 0xE5, 0xEC);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
