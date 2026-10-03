package com.campusplacement.ui.components;

import java.awt.BorderLayout;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** A single statistic: a large serif figure with a label and caption. */
public class StatTile extends JPanel {
    private final JLabel value = Ui.label("—", Theme.serif(32), Theme.TEXT);
    private final boolean accent;

    public StatTile(String label, String caption, boolean accent) {
        super(new BorderLayout(0, 2));
        this.accent = accent;
        setBackground(accent ? Theme.INK : Theme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent ? Theme.INK : Theme.BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel l = Ui.label(label, Theme.sansBold(12), accent ? Theme.INK_TEXT : Theme.TEXT);
        add(l, BorderLayout.NORTH);
        if (accent) {
            value.setForeground(Theme.SURFACE);
        }
        add(value, BorderLayout.CENTER);
        add(Ui.label(caption, Theme.sans(11), accent ? Theme.INK_MUTED : Theme.MUTED), BorderLayout.SOUTH);
    }

    public void setValue(Number n) { value.setText(n == null ? "0" : String.valueOf(n)); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(accent ? Theme.LIGHT_PLUM : Theme.PLUM);
        g.fillRect(0, 0, 3, getHeight());
    }
}
