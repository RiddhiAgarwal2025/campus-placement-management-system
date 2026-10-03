package com.campusplacement.ui.components;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Underlined text tabs with a card-switched body. */
public class Tabs extends JPanel {
    private final JPanel strip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final CardLayout cards = new CardLayout();
    private final JPanel body = new JPanel(cards);
    private final List<Tab> tabs = new ArrayList<>();

    public Tabs() {
        super(new BorderLayout(0, 14));
        setOpaque(false);
        strip.setOpaque(false);
        strip.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        body.setOpaque(false);
        add(strip, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    public Tabs tab(String title, JComponent content) {
        Tab t = new Tab(title);
        tabs.add(t);
        strip.add(t);
        body.add(content, title);
        if (tabs.size() == 1) {
            select(t);
        }
        return this;
    }

    private void select(Tab t) {
        tabs.forEach(x -> x.setActive(x == t));
        cards.show(body, t.title);
    }

    private class Tab extends JLabel {
        private final String title;
        private boolean active;

        Tab(String title) {
            super(title);
            this.title = title;
            setFont(Theme.sans(13));
            setForeground(Theme.MUTED);
            setBorder(BorderFactory.createEmptyBorder(8, 2, 10, 2));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { select(Tab.this); }
            });
        }

        void setActive(boolean a) {
            active = a;
            setForeground(a ? Theme.TEXT : Theme.MUTED);
            setFont(a ? Theme.sansBold(13) : Theme.sans(13));
            repaint();
        }

        @Override
        public java.awt.Dimension getPreferredSize() {
            java.awt.Dimension d = super.getPreferredSize();
            return new java.awt.Dimension(d.width + 26, d.height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (active) {
                g.setColor(Theme.PLUM);
                g.fillRect(2, getHeight() - 2, getWidth() - 28, 2);
            }
        }
    }
}
