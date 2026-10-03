package com.campusplacement.ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/** Custom-painted button in four variants; never uses the default Swing button rendering. */
public class Btn extends JButton {
    public enum Variant { PRIMARY, SECONDARY, GHOST, DANGER }

    private final Variant variant;
    private boolean hover;

    public Btn(String text, Variant variant) {
        this(text, variant, null);
    }

    public Btn(String text, Variant variant, Icons.Glyph glyph) {
        super(text);
        this.variant = variant;
        setFont(Theme.sansBold(12));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        setForeground(fg());
        if (glyph != null) {
            setIcon(Icons.of(glyph, 15, fg()));
            setIconTextGap(7);
        }
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { hover = true; repaint(); }

            @Override
            public void mouseExited(MouseEvent e) { hover = false; repaint(); }
        });
    }

    private Color fg() {
        return switch (variant) {
            case PRIMARY -> Theme.SURFACE;
            case SECONDARY -> Theme.TEXT;
            case GHOST -> Theme.PLUM;
            case DANGER -> Theme.BRICK;
        };
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 34));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        boolean enabled = isEnabled();
        switch (variant) {
            case PRIMARY -> {
                g2.setColor(!enabled ? new Color(0xB9A8B4) : hover ? Theme.DEEP_PLUM : Theme.PLUM);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 6, 6);
            }
            case SECONDARY, DANGER -> {
                g2.setColor(hover && enabled ? (variant == Variant.DANGER ? Theme.BRICK_BG : Theme.BG) : Theme.SURFACE);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 6, 6);
                g2.setColor(variant == Variant.DANGER && enabled ? new Color(0xD9B8B2) : Theme.BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);
            }
            case GHOST -> {
                if (hover && enabled) {
                    g2.setColor(Theme.LIGHT_PLUM);
                    g2.fillRoundRect(0, 0, w - 1, h - 1, 6, 6);
                }
            }
        }
        g2.dispose();
        setForeground(enabled ? fg() : Theme.MUTED);
        super.paintComponent(g);
    }
}
