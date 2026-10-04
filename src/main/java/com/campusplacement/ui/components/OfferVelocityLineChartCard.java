package com.campusplacement.ui.components;

import com.campusplacement.ui.MainFrame;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Bottom-Right Card inspired by Figma dashboard's multi-line trend chart:
 * Renders dual polyline curves tracking submissions and offers over the season timeline.
 */
public class OfferVelocityLineChartCard extends JPanel {
    private final JLabel headlineCount = Ui.label("25 Submissions", Theme.sansBold(22), Theme.TEXT);
    private final JLabel headlineLabel = Ui.label("Across 5 active placement drives", Theme.sans(12), Theme.MUTED);
    private final LineCanvas canvas = new LineCanvas();

    public OfferVelocityLineChartCard() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Top Header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel titles = Ui.vstack(2);
        titles.setOpaque(false);
        titles.add(Ui.label("Application & Offer Velocity", Theme.sansBold(15), Theme.TEXT));
        titles.add(Ui.label("Milestone progression over the season timeline", Theme.sans(12), Theme.MUTED));
        top.add(titles, BorderLayout.WEST);

        Btn viewOffers = new Btn("All offers \u2192", Btn.Variant.GHOST);
        viewOffers.addActionListener(e -> MainFrame.navigate("offers"));
        top.add(viewOffers, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // KPI Row
        JPanel kpiRow = new JPanel(new BorderLayout());
        kpiRow.setOpaque(false);
        JPanel kpiBlock = Ui.vstack(2);
        kpiBlock.setOpaque(false);
        kpiBlock.add(headlineCount);
        kpiBlock.add(headlineLabel);
        kpiRow.add(kpiBlock, BorderLayout.WEST);

        JPanel trendPill = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 3)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = Theme.isDarkMode ? new Color(0x1B, 0x33, 0x22) : new Color(0xDC, 0xFC, 0xE7);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                Color bdr = Theme.isDarkMode ? new Color(0x2E, 0x55, 0x39) : new Color(0x86, 0xEF, 0xAC);
                g2.setColor(bdr);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        trendPill.setOpaque(false);
        Color trendFg = Theme.isDarkMode ? new Color(0x4A, 0xDE, 0x80) : new Color(0x15, 0x80, 0x3D);
        JLabel trendText = Ui.label("\u2191 5.2% velocity", Theme.sansBold(11), trendFg);
        trendPill.add(trendText);
        kpiRow.add(trendPill, BorderLayout.EAST);
        add(kpiRow, BorderLayout.CENTER);

        // Chart canvas
        canvas.setPreferredSize(new Dimension(100, 150));
        add(canvas, BorderLayout.SOUTH);
    }

    public void updateData(int apps, int offers) {
        headlineCount.setText(apps + " Submissions");
        headlineLabel.setText(offers + " offers extended to date");
        repaint();
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

    /** Dual Line Trend Chart Canvas with interactive hover tooltips */
    private static class LineCanvas extends JPanel {
        private final String[] dates = {"24 Sep", "27 Sep", "01 Oct", "02 Oct", "03 Oct"};
        private final int[] lineApps = {3, 7, 14, 20, 25};
        private final int[] lineOffers = {0, 1, 3, 5, 6};
        private int hoveredNode = -1;
        private Point mousePt = null;

        LineCanvas() {
            setOpaque(false);

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int w = getWidth();
                    int padX = 26;
                    int n = dates.length;
                    if (n <= 1) return;
                    int step = (w - 2 * padX) / (n - 1);
                    int mx = e.getX();
                    int my = e.getY();
                    int bottomMargin = 26;
                    int baseline = getHeight() - bottomMargin;

                    int newHovered = -1;
                    if (my >= 6 && my <= baseline + 20 && mx >= padX - step / 2 && mx <= w - padX + step / 2) {
                        int idx = Math.round((float) (mx - padX) / step);
                        if (idx >= 0 && idx < n) {
                            newHovered = idx;
                        }
                    }

                    if (newHovered != hoveredNode) {
                        hoveredNode = newHovered;
                        mousePt = (newHovered >= 0) ? e.getPoint() : null;
                        repaint();
                    } else if (newHovered >= 0) {
                        mousePt = e.getPoint();
                        repaint();
                    }
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoveredNode = -1;
                    mousePt = null;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padX = 26;
            int bottomMargin = 26;
            int topMargin = 12;
            int chartH = h - bottomMargin - topMargin;
            int baseline = h - bottomMargin;

            // Horizontal grid guidelines
            g2.setColor(Theme.isDarkMode ? new Color(0x1F, 0x2A, 0x3B) : new Color(0xEE, 0xF2, 0xF6));
            for (int i = 1; i <= 3; i++) {
                int gy = baseline - (chartH * i / 3);
                g2.drawLine(padX, gy, w - padX, gy);
            }

            int n = dates.length;
            int step = (w - 2 * padX) / (n - 1);
            int maxVal = 28;

            int[] xs = new int[n];
            int[] ysApps = new int[n];
            int[] ysOffers = new int[n];

            for (int i = 0; i < n; i++) {
                xs[i] = padX + i * step;
                ysApps[i] = baseline - Math.round((float) lineApps[i] / maxVal * chartH);
                ysOffers[i] = baseline - Math.round((float) lineOffers[i] / maxVal * chartH);
            }

            Color colorApps = Theme.isDarkMode ? new Color(0x60, 0xA5, 0xFA) : Theme.PLUM;
            Color colorOffers = Theme.isDarkMode ? new Color(0xD9, 0x9B, 0x43) : new Color(0x7A, 0x5A, 0x1E);

            // Draw vertical dashed guide line for hovered node
            if (hoveredNode >= 0 && hoveredNode < n) {
                int hx = xs[hoveredNode];
                Stroke oldStroke = g2.getStroke();
                Stroke dashed = new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f, 4.0f}, 0.0f);
                g2.setStroke(dashed);
                g2.setColor(Theme.isDarkMode ? new Color(0x3B, 0x82, 0xF6, 120) : new Color(0x38, 0x58, 0x8C, 100));
                g2.drawLine(hx, topMargin - 4, hx, baseline);
                g2.setStroke(oldStroke);
            }

            // Draw Submissions Line
            g2.setColor(colorApps);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(xs[i], ysApps[i], xs[i + 1], ysApps[i + 1]);
            }
            // Dots
            for (int i = 0; i < n; i++) {
                boolean isHov = (i == hoveredNode);
                if (isHov) {
                    g2.setColor(Theme.isDarkMode ? new Color(0x60, 0xA5, 0xFA, 70) : new Color(0x38, 0x58, 0x8C, 60));
                    g2.fillOval(xs[i] - 9, ysApps[i] - 9, 18, 18);
                }
                g2.setColor(Theme.SURFACE);
                g2.fillOval(xs[i] - (isHov ? 6 : 5), ysApps[i] - (isHov ? 6 : 5), isHov ? 12 : 10, isHov ? 12 : 10);
                g2.setColor(colorApps);
                g2.fillOval(xs[i] - (isHov ? 4 : 3), ysApps[i] - (isHov ? 4 : 3), isHov ? 8 : 6, isHov ? 8 : 6);
            }

            // Draw Offers Line
            g2.setColor(colorOffers);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(xs[i], ysOffers[i], xs[i + 1], ysOffers[i + 1]);
            }
            // Dots
            for (int i = 0; i < n; i++) {
                boolean isHov = (i == hoveredNode);
                if (isHov) {
                    g2.setColor(Theme.isDarkMode ? new Color(0xFB, 0xBF, 0x24, 70) : new Color(0x7A, 0x5A, 0x1E, 60));
                    g2.fillOval(xs[i] - 8, ysOffers[i] - 8, 16, 16);
                }
                g2.setColor(Theme.SURFACE);
                g2.fillOval(xs[i] - (isHov ? 5 : 4), ysOffers[i] - (isHov ? 5 : 4), isHov ? 10 : 8, isHov ? 10 : 8);
                g2.setColor(colorOffers);
                g2.fillOval(xs[i] - (isHov ? 3 : 2), ysOffers[i] - (isHov ? 3 : 2), isHov ? 6 : 4, isHov ? 6 : 4);
            }

            // X-Axis Date Labels
            g2.setFont(Theme.sans(10));
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < n; i++) {
                boolean isHov = (i == hoveredNode);
                g2.setFont(isHov ? Theme.sansBold(10) : Theme.sans(10));
                g2.setColor(isHov ? Theme.TEXT : Theme.MUTED);
                int dw = fm.stringWidth(dates[i]);
                g2.drawString(dates[i], xs[i] - dw / 2, baseline + 15);
            }

            // Bottom Legend
            int legY = h - 1;
            int legX = padX;
            g2.setFont(Theme.sans(11));

            // Dot 1
            g2.setColor(colorApps);
            g2.fillOval(legX, legY - 7, 8, 8);
            g2.setColor(Theme.MUTED);
            g2.drawString("Submissions", legX + 12, legY);

            // Dot 2
            int leg2X = legX + 95;
            g2.setColor(colorOffers);
            g2.fillOval(leg2X, legY - 7, 8, 8);
            g2.setColor(Theme.MUTED);
            g2.drawString("Offers Extended", leg2X + 12, legY);

            // Floating Tooltip Card
            if (hoveredNode >= 0 && hoveredNode < n) {
                int tipW = 152;
                int tipH = 64;
                int hx = xs[hoveredNode];
                int tipX = Math.max(10, Math.min(w - tipW - 10, hx - tipW / 2));
                int tipY = Math.max(4, topMargin - 4);

                g2.setColor(Theme.isDarkMode ? new Color(0x0F, 0x17, 0x2A, 245) : new Color(0x1E, 0x29, 0x3B, 242));
                g2.fillRoundRect(tipX, tipY, tipW, tipH, 8, 8);
                g2.setColor(Theme.isDarkMode ? new Color(0x33, 0x41, 0x55) : new Color(0x47, 0x55, 0x69));
                g2.drawRoundRect(tipX, tipY, tipW, tipH, 8, 8);

                // Date Title
                g2.setFont(Theme.sansBold(10));
                g2.setColor(Color.WHITE);
                g2.drawString(dates[hoveredNode] + " Milestone", tipX + 10, tipY + 16);

                // Apps row
                g2.setFont(Theme.sans(10));
                g2.setColor(new Color(0x60, 0xA5, 0xFA));
                g2.fillOval(tipX + 10, tipY + 26, 6, 6);
                g2.setColor(new Color(0xDF, 0xE4, 0xEA));
                g2.drawString("Submissions: " + lineApps[hoveredNode], tipX + 20, tipY + 33);

                // Offers row
                g2.setColor(new Color(0xFB, 0xBF, 0x24));
                g2.fillOval(tipX + 10, tipY + 41, 6, 6);
                g2.setColor(new Color(0xDF, 0xE4, 0xEA));
                g2.drawString("Offers: " + lineOffers[hoveredNode], tipX + 20, tipY + 48);
            }

            g2.dispose();
        }
    }
}
