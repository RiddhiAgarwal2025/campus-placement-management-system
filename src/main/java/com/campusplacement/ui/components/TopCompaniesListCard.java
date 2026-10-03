package com.campusplacement.ui.components;

import com.campusplacement.model.Drive;
import com.campusplacement.ui.MainFrame;
import com.campusplacement.util.Formats;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Bottom-Left Card inspired by Figma dashboard's "Most Ordered" list:
 * Shows top partner companies with circular initial avatars, position details, and package pills.
 */
public class TopCompaniesListCard extends JPanel {
    private final JPanel listContainer = new JPanel();

    public TopCompaniesListCard() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Top Header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel titles = Ui.vstack(2);
        titles.setOpaque(false);
        titles.add(Ui.label("Top Recruiting Partners", Theme.sansBold(15), Theme.TEXT));
        titles.add(Ui.label("Active campus drives and package offerings", Theme.sans(12), Theme.MUTED));
        top.add(titles, BorderLayout.WEST);

        Btn allDrives = new Btn("All drives \u2192", Btn.Variant.GHOST);
        allDrives.addActionListener(e -> MainFrame.navigate("drives"));
        top.add(allDrives, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // List Container
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);
        add(listContainer, BorderLayout.CENTER);
    }

    public void updateDrives(List<Drive> drives) {
        listContainer.removeAll();
        if (drives == null || drives.isEmpty()) {
            JLabel empty = Ui.muted("No active drives scheduled.");
            empty.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            listContainer.add(empty);
        } else {
            int count = 0;
            for (Drive d : drives) {
                if (count++ >= 4) break;
                listContainer.add(createCompanyRow(d));
                if (count < Math.min(4, drives.size())) {
                    listContainer.add(createDivider());
                }
            }
        }
        revalidate();
        repaint();
    }

    private JPanel createCompanyRow(Drive d) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        // Circular initial avatar
        String initial = d.companyName().isEmpty() ? "C" : d.companyName().substring(0, 1).toUpperCase();
        JPanel avatar = new JPanel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(34, 34); }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = Theme.isDarkMode ? new Color(0x22, 0x30, 0x47) : new Color(0xEE, 0xF2, 0xF6);
                g2.setColor(bg);
                g2.fillOval(0, 0, 33, 33);
                g2.setColor(Theme.isDarkMode ? new Color(0x93, 0xC5, 0xFD) : Theme.PLUM);
                g2.setFont(Theme.sansBold(14));
                FontMetrics fm = g2.getFontMetrics();
                int sw = fm.stringWidth(initial);
                g2.drawString(initial, (34 - sw) / 2, 22);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        row.add(avatar, BorderLayout.WEST);

        // Center Company & Role
        JPanel textBlock = Ui.vstack(2);
        textBlock.setOpaque(false);
        JLabel name = Ui.label(d.companyName(), Theme.sansBold(13), Theme.TEXT);
        String sub = d.position() + " \u2022 " + d.applicationCount() + " applied";
        JLabel role = Ui.label(sub, Theme.sans(11), Theme.MUTED);
        textBlock.add(name);
        textBlock.add(role);
        row.add(textBlock, BorderLayout.CENTER);

        // Right Package pill or Deadline
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        right.setOpaque(false);
        String pkgStr = d.status() != null ? d.status() : "OPEN";
        Badge b = new Badge(pkgStr);
        b.setOpaque(false);
        right.add(b);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    private JPanel createDivider() {
        JPanel div = new JPanel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(10, 1); }
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Theme.BORDER);
                g.fillRect(46, 0, getWidth() - 46, 1);
            }
        };
        div.setOpaque(false);
        return div;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // 14px rounded card
        g2.setColor(Theme.SURFACE);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
        g2.setColor(Theme.BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

        g2.dispose();
        super.paintComponent(g);
    }
}
