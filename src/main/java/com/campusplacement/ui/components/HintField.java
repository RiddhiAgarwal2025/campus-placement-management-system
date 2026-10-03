package com.campusplacement.ui.components;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;
import javax.swing.JTextField;

/** Text field that paints a placeholder hint and an optional leading icon. */
public class HintField extends JTextField {
    private final String hint;
    private Icon icon;

    public HintField(String hint) {
        this.hint = hint;
    }

    public void setIconGlyph(Icons.Glyph g) {
        this.icon = Icons.of(g, 15, Theme.MUTED);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (icon != null) {
            icon.paintIcon(this, g2, 9, (getHeight() - icon.getIconHeight()) / 2);
        }
        if (getText().isEmpty() && hint != null && !hint.isEmpty()) {
            g2.setColor(Theme.MUTED);
            g2.setFont(getFont());
            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(hint, getInsets().left, y);
        }
        g2.dispose();
    }
}
