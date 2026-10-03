package com.campusplacement.ui.components;

import java.awt.BorderLayout;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** A single statistic: a large serif figure with a label and caption. */
public class StatTile extends JPanel {
    private final JLabel value = Ui.label("—", Theme.sansBold(28), Theme.TEXT);
    private final boolean accent;

    public StatTile(String label, String caption, boolean accent) {
        super(new BorderLayout(0, 4));
        this.accent = accent;
        setBackground(Theme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        JLabel l = Ui.label(label.toUpperCase(), Theme.sansBold(11), Theme.MUTED);
        add(l, BorderLayout.NORTH);
        add(value, BorderLayout.CENTER);
        add(Ui.label(caption, Theme.sans(12), Theme.MUTED), BorderLayout.SOUTH);
    }

    public void setValue(Number n) { value.setText(n == null ? "0" : String.valueOf(n)); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
}
