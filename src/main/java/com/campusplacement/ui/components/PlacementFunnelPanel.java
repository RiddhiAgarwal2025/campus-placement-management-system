package com.campusplacement.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Windows 11 / Apple style elevated Hero Placement Funnel card.
 * Shows end-to-end candidate milestone progression from applications to placements.
 */
public class PlacementFunnelPanel extends JPanel {
    private final StageCard stageApps = new StageCard("Applications", "1. Submitted", 1.0f);
    private final StageCard stageShortlisted = new StageCard("Shortlisted", "2. In Process", 0.40f);
    private final StageCard stageOffers = new StageCard("Offers Extended", "3. Cleared", 0.24f);
    private final StageCard stageAccepted = new StageCard("Placed Students", "4. Accepted", 0.12f);

    private final JLabel headerStats = Ui.label("Batch: — candidates", Theme.sans(12), Theme.MUTED);
    private final JLabel activeDrivesBadge = Ui.label("5 Active Drives", Theme.sansBold(11), Theme.TEXT);
    private final JLabel partnersBadge = Ui.label("9 Companies", Theme.sansBold(11), Theme.TEXT);
    private final JLabel rateBadge = Ui.label("Placement Rate: —", Theme.sansBold(11), Theme.TEXT);

    private int valApps = 0;
    private int valShort = 0;
    private int valOffers = 0;
    private int valAccepted = 0;
    private int valStudents = 0;

    public PlacementFunnelPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // 1. Top Header Row
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel titleBlock = Ui.vstack(2);
        titleBlock.setOpaque(false);
        JLabel title = Ui.label("Placement Funnel", Theme.sansBold(17), Theme.TEXT);
        JLabel sub = Ui.label("Candidate progression from applications to final offers", Theme.sans(12), Theme.MUTED);
        titleBlock.add(title);
        titleBlock.add(sub);
        top.add(titleBlock, BorderLayout.WEST);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badges.setOpaque(false);
        badges.add(wrapChip(activeDrivesBadge));
        badges.add(wrapChip(partnersBadge));
        badges.add(wrapChip(rateBadge));
        top.add(badges, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // 2. Center Funnel Stages Row
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1.0;
        g.insets = new Insets(0, 4, 0, 4);

        g.gridx = 0; g.weightx = 0.25;
        center.add(stageApps, g);

        g.gridx = 1; g.weightx = 0.05;
        center.add(new ArrowConnector(), g);

        g.gridx = 2; g.weightx = 0.25;
        center.add(stageShortlisted, g);

        g.gridx = 3; g.weightx = 0.05;
        center.add(new ArrowConnector(), g);

        g.gridx = 4; g.weightx = 0.25;
        center.add(stageOffers, g);

        g.gridx = 5; g.weightx = 0.05;
        center.add(new ArrowConnector(), g);

        g.gridx = 6; g.weightx = 0.25;
        center.add(stageAccepted, g);

        add(center, BorderLayout.CENTER);
    }

    private JPanel wrapChip(JLabel lbl) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = Theme.isDarkMode ? new Color(0x1B, 0x25, 0x36) : new Color(0xEE, 0xF2, 0xF6);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.add(lbl);
        return p;
    }

    public void update(int students, int companies, int activeDrives, int apps, int shortl, int offers, int accepted) {
        this.valStudents = students;
        this.valApps = apps;
        this.valShort = shortl;
        this.valOffers = offers;
        this.valAccepted = accepted;

        activeDrivesBadge.setText(activeDrives + " Active Drives");
        partnersBadge.setText(companies + " Companies");

        int pctPlaced = students > 0 ? (int) Math.round((double) accepted * 100.0 / students) : 0;
        rateBadge.setText("Placement Rate: " + pctPlaced + "% (" + accepted + "/" + students + ")");

        stageApps.setValue(apps, "100% total", 1.0f);

        float shortRate = apps > 0 ? (float) shortl / (float) apps : 0f;
        stageShortlisted.setValue(shortl, Math.round(shortRate * 100) + "% rate", shortRate);

        float offerRate = apps > 0 ? (float) offers / (float) apps : 0f;
        stageOffers.setValue(offers, Math.round(offerRate * 100) + "% rate", offerRate);

        float acceptRate = offers > 0 ? (float) accepted / (float) offers : 0f;
        stageAccepted.setValue(accepted, Math.round(acceptRate * 100) + "% accept", acceptRate);

        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // Elevated card background
        g2.setColor(Theme.SURFACE);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);

        // Apple / Fluent card hairline border
        g2.setColor(Theme.BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

        g2.dispose();
        super.paintComponent(g);
    }

    /** Single Stage Milestone Card */
    private static class StageCard extends JPanel {
        private final JLabel numLabel = Ui.label("0", Theme.sansBold(24), Theme.TEXT);
        private final JLabel nameLabel;
        private final JLabel subLabel;
        private float ratio = 1.0f;

        StageCard(String title, String subtitle, float initialRatio) {
            this.ratio = initialRatio;
            setLayout(new BorderLayout(0, 4));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

            nameLabel = Ui.label(title, Theme.sansBold(12), Theme.TEXT);
            subLabel = Ui.label(subtitle, Theme.sans(11), Theme.MUTED);

            JPanel textPanel = Ui.vstack(2);
            textPanel.setOpaque(false);
            textPanel.add(subLabel);
            textPanel.add(numLabel);
            textPanel.add(nameLabel);
            add(textPanel, BorderLayout.CENTER);
        }

        void setValue(int count, String sub, float r) {
            numLabel.setText(String.valueOf(count));
            subLabel.setText(sub);
            this.ratio = Math.max(0.08f, Math.min(1.0f, r));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            // Card fill
            Color fill = Theme.isDarkMode ? new Color(0x13, 0x1B, 0x27) : new Color(0xF8, 0xFA, 0xFC);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

            // Hairline border
            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

            // Progress track indicator along the bottom
            int barH = 4;
            int barY = h - barH - 4;
            int barMaxW = w - 16;
            g2.setColor(Theme.isDarkMode ? new Color(0x22, 0x2E, 0x40) : new Color(0xE2, 0xE8, 0xF0));
            g2.fillRoundRect(8, barY, barMaxW, barH, 4, 4);

            int barFillW = Math.max(8, Math.round(barMaxW * ratio));
            Color accentColor = Theme.isDarkMode ? new Color(0x93, 0xC5, 0xFD) : Theme.PLUM;
            g2.setColor(accentColor);
            g2.fillRoundRect(8, barY, barFillW, barH, 4, 4);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Sleek arrow connector between funnel stages */
    private static class ArrowConnector extends JComponent {
        ArrowConnector() {
            setPreferredSize(new Dimension(24, 60));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int midY = h / 2;

            g2.setColor(Theme.isDarkMode ? new Color(0x47, 0x55, 0x69) : new Color(0xCB, 0xD5, 0xE1));
            // Subtle arrow shape
            int cx = w / 2;
            int[] xs = {cx - 4, cx + 4, cx - 4};
            int[] ys = {midY - 6, midY, midY + 6};
            g2.fillPolygon(xs, ys, 3);
            g2.dispose();
        }
    }
}
