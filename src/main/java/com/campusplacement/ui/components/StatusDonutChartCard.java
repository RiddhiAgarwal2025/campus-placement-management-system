package com.campusplacement.ui.components;

import com.campusplacement.ui.MainFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Top-Right Card inspired by Figma dashboard:
 * Anti-aliased hollow arc Donut chart with center counter and bottom distribution legend.
 */
public class StatusDonutChartCard extends JPanel {
    private final DonutCanvas canvas = new DonutCanvas();

    public StatusDonutChartCard() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Top Header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel titles = Ui.vstack(2);
        titles.setOpaque(false);
        titles.add(Ui.label("Pipeline Distribution", Theme.sansBold(15), Theme.TEXT));
        titles.add(Ui.label("Candidate status and conversion proportions", Theme.sans(12), Theme.MUTED));
        top.add(titles, BorderLayout.WEST);

        Btn viewReport = new Btn("View Report \u2192", Btn.Variant.GHOST);
        viewReport.addActionListener(e -> MainFrame.navigate("applications"));
        top.add(viewReport, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Donut canvas
        canvas.setPreferredSize(new Dimension(100, 200));
        add(canvas, BorderLayout.CENTER);
    }

    public void updateData(int apps, int shortl, int offers, int accepted) {
        canvas.setData(apps, shortl, offers, accepted);
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

    /** Custom Donut Chart Painter */
    private static class DonutCanvas extends JPanel {
        private int valApps = 25;
        private int valShort = 10;
        private int valOffers = 6;
        private int valAccepted = 3;

        DonutCanvas() {
            setOpaque(false);
        }

        void setData(int a, int s, int o, int acc) {
            this.valApps = a;
            this.valShort = s;
            this.valOffers = o;
            this.valAccepted = acc;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int outerDiam = Math.min(130, h - 50);
            int innerDiam = outerDiam - 38;
            int cx = w / 2;
            int cy = (h - 35) / 2 + 5;
            int ox = cx - outerDiam / 2;
            int oy = cy - outerDiam / 2;
            int ix = cx - innerDiam / 2;
            int iy = cy - innerDiam / 2;

            int total = Math.max(1, valApps);
            int angleShort = Math.round(360f * valShort / total);
            int angleOffers = Math.round(360f * valOffers / total);
            int angleAcc = Math.round(360f * valAccepted / total);
            int angleApplied = Math.max(10, 360 - angleShort - angleOffers - angleAcc);

            Color cShort = Theme.isDarkMode ? new Color(0x60, 0xA5, 0xFA) : Theme.PLUM;
            Color cOffers = Theme.isDarkMode ? new Color(0xD9, 0x9B, 0x43) : new Color(0x7A, 0x5A, 0x1E);
            Color cAcc = Theme.isDarkMode ? new Color(0x4A, 0xDE, 0x80) : new Color(0x15, 0x80, 0x3D);
            Color cRem = Theme.isDarkMode ? new Color(0x22, 0x2E, 0x40) : new Color(0xEB, 0xF0, 0xF5);

            int start = 90;

            // Arc 1: Shortlisted
            g2.setColor(cShort);
            g2.fillArc(ox, oy, outerDiam, outerDiam, start, angleShort);
            start += angleShort;

            // Arc 2: Offers
            g2.setColor(cOffers);
            g2.fillArc(ox, oy, outerDiam, outerDiam, start, angleOffers);
            start += angleOffers;

            // Arc 3: Accepted
            g2.setColor(cAcc);
            g2.fillArc(ox, oy, outerDiam, outerDiam, start, angleAcc);
            start += angleAcc;

            // Arc 4: Applied remainder
            g2.setColor(cRem);
            g2.fillArc(ox, oy, outerDiam, outerDiam, start, 360 - (start - 90));

            // Cut out donut center
            g2.setColor(Theme.SURFACE);
            g2.fillOval(ix, iy, innerDiam, innerDiam);

            // Center counter label
            g2.setFont(Theme.sansBold(18));
            g2.setColor(Theme.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            String cntStr = String.valueOf(valApps);
            int sw = fm.stringWidth(cntStr);
            g2.drawString(cntStr, cx - sw / 2, cy + 2);

            g2.setFont(Theme.sans(10));
            g2.setColor(Theme.MUTED);
            FontMetrics fsm = g2.getFontMetrics();
            String subStr = "Total";
            int ssw = fsm.stringWidth(subStr);
            g2.drawString(subStr, cx - ssw / 2, cy + 14);

            // Bottom Legend
            int legY = h - 6;
            int legX = Math.max(10, cx - 145);
            g2.setFont(Theme.sans(11));

            // Shortlisted
            g2.setColor(cShort);
            g2.fillOval(legX, legY - 7, 8, 8);
            g2.setColor(Theme.MUTED);
            int pctShort = Math.round(100f * valShort / total);
            g2.drawString("Shortlisted (" + pctShort + "%)", legX + 11, legY);

            // Offers
            int leg2X = legX + 105;
            g2.setColor(cOffers);
            g2.fillOval(leg2X, legY - 7, 8, 8);
            g2.setColor(Theme.MUTED);
            int pctOff = Math.round(100f * valOffers / total);
            g2.drawString("Offers (" + pctOff + "%)", leg2X + 11, legY);

            // Placed
            int leg3X = leg2X + 85;
            g2.setColor(cAcc);
            g2.fillOval(leg3X, legY - 7, 8, 8);
            g2.setColor(Theme.MUTED);
            int pctAcc = Math.round(100f * valAccepted / total);
            g2.drawString("Placed (" + pctAcc + "%)", leg3X + 11, legY);

            g2.dispose();
        }
    }
}
