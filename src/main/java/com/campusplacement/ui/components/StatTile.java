package com.campusplacement.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** A single statistic card styled with 14px rounded corners matching the Figma design system. */
public class StatTile extends JPanel {
    private final JLabel value = Ui.label("—", Theme.sansBold(24), Theme.TEXT);
    private final boolean accent;

    public StatTile(String label, String caption) {
        this(label, caption, false);
    }

    public StatTile(String label, String caption, boolean accent) {
        super(new BorderLayout(0, 4));
        this.accent = false;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        JLabel l = Ui.label(label.toUpperCase(), Theme.sansBold(11), Theme.MUTED);
        add(l, BorderLayout.NORTH);
        add(value, BorderLayout.CENTER);
        add(Ui.label(caption, Theme.sans(12), Theme.MUTED), BorderLayout.SOUTH);
    }

    public void setValue(Object o) {
        value.setText(o == null ? "—" : String.valueOf(o));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // 14px rounded card fill & hairline border (uniform across all tiles)
        g2.setColor(Theme.SURFACE);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
        g2.setColor(Theme.BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

        g2.dispose();
        super.paintComponent(g);
    }
}

