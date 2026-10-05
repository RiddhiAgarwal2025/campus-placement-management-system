package com.campusplacement.ui.components;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTextField;

/** Text field that paints a placeholder hint and an optional leading icon with guaranteed non-overlapping insets. */
public class HintField extends JTextField {
    private final String hint;
    private Icons.Glyph glyph;

    public HintField(String hint) {
        this.hint = hint;
    }

    public void setIconGlyph(Icons.Glyph g) {
        this.glyph = g;
        updateBorder();
        repaint();
    }

    public boolean hasIcon() {
        return glyph != null;
    }

    public void updateBorder() {
        int left = hasIcon() ? 36 : 10;
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(6, left, 6, 10)));
    }

    @Override
    public Insets getInsets() {
        Insets ins = super.getInsets();
        if (hasIcon()) {
            return new Insets(ins.top, Math.max(ins.left, 36), ins.bottom, ins.right);
        }
        return ins;
    }

    @Override
    public Insets getInsets(Insets insets) {
        Insets ins = super.getInsets(insets);
        if (hasIcon()) {
            ins.left = Math.max(ins.left, 36);
        }
        return ins;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (glyph != null) {
            Icon icon = Icons.of(glyph, 15, Theme.MUTED);
            int y = (getHeight() - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g2, 11, y);
        }
        if (getText().isEmpty() && hint != null && !hint.isEmpty()) {
            g2.setColor(Theme.MUTED);
            g2.setFont(getFont());
            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            int x = getInsets().left;
            g2.drawString(hint, x, y);
        }
        g2.dispose();
    }
}
