package com.campusplacement.ui.components;

import com.campusplacement.ui.MainFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Top-Left Card inspired by Figma dashboard:
 * Primary KPI, trend indicator, and custom dual-bar chart by department.
 */
public class DepartmentBarChartCard extends JPanel {
    private final JLabel kpiValue = Ui.label("\u20B9 14.50 LPA", Theme.sansBold(24), Theme.TEXT);
    private final JLabel kpiLabel = Ui.label("Highest Package Offered", Theme.sans(12), Theme.MUTED);
    private final ChartCanvas chart = new ChartCanvas();

    public DepartmentBarChartCard() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Top Header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel titles = Ui.vstack(2);
        titles.setOpaque(false);
        titles.add(Ui.label("Placement Highlights", Theme.sansBold(15), Theme.TEXT));
        titles.add(Ui.label("Department-wise applications & shortlist conversion", Theme.sans(12), Theme.MUTED));
        top.add(titles, BorderLayout.WEST);

        Btn viewReport = new Btn("View Report \u2192", Btn.Variant.GHOST);
        viewReport.addActionListener(e -> MainFrame.navigate("reports"));
        top.add(viewReport, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Middle KPI row
        JPanel kpiRow = new JPanel(new BorderLayout());
        kpiRow.setOpaque(false);
        JPanel kpiBlock = Ui.vstack(2);
        kpiBlock.setOpaque(false);
        kpiBlock.add(kpiValue);
        kpiBlock.add(kpiLabel);
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
        JLabel trendText = Ui.label("\u2191 14% vs last drive", Theme.sansBold(11), trendFg);
        trendPill.add(trendText);
        kpiRow.add(trendPill, BorderLayout.EAST);
        add(kpiRow, BorderLayout.CENTER);

        // Chart canvas
        chart.setPreferredSize(new Dimension(100, 160));
        add(chart, BorderLayout.SOUTH);
    }

    public void updateData(String topPackage, int totalPlaced) {
        updateData(topPackage, totalPlaced, null);
    }

    public void updateData(String topPackage, int totalPlaced, Map<String, int[]> dynamicDeptData) {
        if (topPackage != null && !topPackage.isBlank()) {
            kpiValue.setText(topPackage);
            kpiLabel.setText("Highest Package Offered \u2022 " + totalPlaced + " Placed");
        }
        if (dynamicDeptData != null && !dynamicDeptData.isEmpty()) {
            chart.setDeptData(dynamicDeptData);
        }
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

    /** Dual Bar Chart Canvas with interactive hover tooltips */
    private static class ChartCanvas extends JPanel {
        private final Map<String, int[]> deptData = new LinkedHashMap<>();
        private int hoveredIdx = -1;
        private Point mousePt = null;

        void setDeptData(Map<String, int[]> data) {
            deptData.clear();
            deptData.putAll(data);
            repaint();
        }

        ChartCanvas() {
            setOpaque(false);
            // Default distribution matching sample placement data
            deptData.put("CSE", new int[]{9, 5});
            deptData.put("IT", new int[]{6, 3});
            deptData.put("ECE", new int[]{4, 2});
            deptData.put("MECH", new int[]{4, 1});
            deptData.put("EEE", new int[]{2, 1});

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int w = getWidth();
                    int padX = 24;
                    int n = deptData.size();
                    if (n == 0) return;
                    int step = (w - 2 * padX) / n;
                    int mx = e.getX();
                    int my = e.getY();
                    int bottomMargin = 28;
                    int baseline = getHeight() - bottomMargin;
                    int newHovered = -1;
                    if (my >= 6 && my <= baseline + 20 && mx >= padX && mx <= w - padX) {
                        int idx = (mx - padX) / step;
                        if (idx >= 0 && idx < n) {
                            newHovered = idx;
                        }
                    }
                    if (newHovered != hoveredIdx) {
                        hoveredIdx = newHovered;
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
                    hoveredIdx = -1;
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
            int padX = 24;
            int bottomMargin = 28;
            int topMargin = 12;
            int chartH = h - bottomMargin - topMargin;
            int baseline = h - bottomMargin;

            // Subtle dashed grid guidelines
            g2.setColor(Theme.isDarkMode ? new Color(0x1F, 0x2A, 0x3B) : new Color(0xEE, 0xF2, 0xF6));
            for (int i = 1; i <= 3; i++) {
                int gy = baseline - (chartH * i / 3);
                g2.drawLine(padX, gy, w - padX, gy);
            }

            int n = deptData.size();
            int step = (w - 2 * padX) / Math.max(1, n);
            int barW = 12;
            int gap = 4;
            int maxVal = 10;

            Color barPrimary = Theme.isDarkMode ? new Color(0x60, 0xA5, 0xFA) : Theme.PLUM;
            Color barSecondary = Theme.isDarkMode ? new Color(0xD9, 0x9B, 0x43) : new Color(0x7A, 0x5A, 0x1E);

            int idx = 0;
            String hoverDept = null;
            int hoverVal1 = 0;
            int hoverVal2 = 0;
            int hoverCx = 0;

            for (Map.Entry<String, int[]> entry : deptData.entrySet()) {
                String dept = entry.getKey();
                int val1 = entry.getValue()[0];
                int val2 = entry.getValue()[1];

                int h1 = Math.round((float) val1 / maxVal * chartH);
                int h2 = Math.round((float) val2 / maxVal * chartH);

                int cx = padX + idx * step + step / 2;
                int x1 = cx - barW - gap / 2;
                int x2 = cx + gap / 2;

                int y1 = baseline - h1;
                int y2 = baseline - h2;

                boolean isHovered = (idx == hoveredIdx);
                if (isHovered) {
                    hoverDept = dept;
                    hoverVal1 = val1;
                    hoverVal2 = val2;
                    hoverCx = cx;

                    // Highlight column background behind bars
                    g2.setColor(Theme.isDarkMode ? new Color(0x3B, 0x82, 0xF6, 40) : new Color(0x38, 0x58, 0x8C, 25));
                    g2.fillRoundRect(cx - step / 2 + 2, topMargin - 4, step - 4, chartH + bottomMargin - 4, 8, 8);
                }

                // Primary Bar
                g2.setColor(isHovered ? barPrimary.brighter() : barPrimary);
                g2.fillRoundRect(x1, y1, barW, h1, 4, 4);

                // Secondary Bar
                g2.setColor(isHovered ? barSecondary.brighter() : barSecondary);
                g2.fillRoundRect(x2, y2, barW, h2, 4, 4);

                // Dept Label
                g2.setFont(isHovered ? Theme.sansBold(11) : Theme.sans(11));
                g2.setColor(isHovered ? Theme.TEXT : Theme.MUTED);
                FontMetrics fm = g2.getFontMetrics();
                int labelW = fm.stringWidth(dept);
                g2.drawString(dept, cx - labelW / 2, baseline + 16);

                idx++;
            }

            // Legend at bottom left / center
            int legY = h - 2;
            int legX = padX + 4;
            g2.setFont(Theme.sans(11));

            // Dot 1
            g2.setColor(barPrimary);
            g2.fillOval(legX, legY - 7, 8, 8);
            g2.setColor(Theme.MUTED);
            g2.drawString("Applications", legX + 12, legY);

            // Dot 2
            int leg2X = legX + 90;
            g2.setColor(barSecondary);
            g2.fillOval(leg2X, legY - 7, 8, 8);
            g2.setColor(Theme.MUTED);
            g2.drawString("Shortlisted", leg2X + 12, legY);

            // Floating Hover Tooltip Card
            if (hoverDept != null && hoveredIdx >= 0) {
                int tipW = 154;
                int tipH = 74;
                int tipX = Math.max(10, Math.min(w - tipW - 10, hoverCx - tipW / 2));
                int tipY = Math.max(4, topMargin - 2);

                // Tooltip background & shadow border
                g2.setColor(Theme.isDarkMode ? new Color(0x0F, 0x17, 0x2A, 245) : new Color(0x1E, 0x29, 0x3B, 242));
                g2.fillRoundRect(tipX, tipY, tipW, tipH, 8, 8);
                g2.setColor(Theme.isDarkMode ? new Color(0x33, 0x41, 0x55) : new Color(0x47, 0x55, 0x69));
                g2.drawRoundRect(tipX, tipY, tipW, tipH, 8, 8);

                // Tooltip text
                g2.setFont(Theme.sansBold(11));
                g2.setColor(Color.WHITE);
                g2.drawString(hoverDept + " Engineering", tipX + 10, tipY + 16);

                g2.setFont(Theme.sans(10));
                // Dot 1
                g2.setColor(new Color(0x60, 0xA5, 0xFA));
                g2.fillOval(tipX + 10, tipY + 26, 6, 6);
                g2.setColor(new Color(0xDF, 0xE4, 0xEA));
                g2.drawString("Applications: " + hoverVal1, tipX + 20, tipY + 33);

                // Dot 2
                g2.setColor(new Color(0xFB, 0xBF, 0x24));
                g2.fillOval(tipX + 10, tipY + 41, 6, 6);
                g2.setColor(new Color(0xDF, 0xE4, 0xEA));
                g2.drawString("Shortlisted: " + hoverVal2, tipX + 20, tipY + 48);

                // Rate
                int conv = hoverVal1 > 0 ? Math.round(100f * hoverVal2 / hoverVal1) : 0;
                g2.setColor(new Color(0x4A, 0xDE, 0x80));
                g2.drawString("Shortlist Rate: " + conv + "%", tipX + 10, tipY + 63);
            }

            g2.dispose();
        }
    }
}
