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

    /** Custom Donut Chart Painter with interactive hover detection and tooltips */
    private static class DonutCanvas extends JPanel {
        private int valApps = 25;
        private int valShort = 10;
        private int valOffers = 6;
        private int valAccepted = 3;
        private int hoveredSlice = -1; // 0=Shortlisted, 1=Offers, 2=Accepted, 3=Applied
        private Point mousePt = null;

        DonutCanvas() {
            setOpaque(false);

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int w = getWidth();
                    int h = getHeight();
                    int outerDiam = Math.min(130, h - 50);
                    int innerDiam = outerDiam - 38;
                    int cx = w / 2;
                    int cy = (h - 35) / 2 + 5;

                    int dx = e.getX() - cx;
                    int dy = e.getY() - cy;
                    double dist = Math.hypot(dx, dy);

                    int total = Math.max(1, valApps);
                    int angleShort = Math.round(360f * valShort / total);
                    int angleOffers = Math.round(360f * valOffers / total);
                    int angleAcc = Math.round(360f * valAccepted / total);

                    int newHovered = -1;
                    if (dist >= (innerDiam / 2.0) - 4 && dist <= (outerDiam / 2.0) + 10) {
                        double theta = Math.toDegrees(Math.atan2(-dy, dx));
                        if (theta < 0) theta += 360;

                        double relAngle = theta - 90;
                        if (relAngle < 0) relAngle += 360;

                        if (relAngle < angleShort) {
                            newHovered = 0; // Shortlisted
                        } else if (relAngle < angleShort + angleOffers) {
                            newHovered = 1; // Offers
                        } else if (relAngle < angleShort + angleOffers + angleAcc) {
                            newHovered = 2; // Accepted
                        } else {
                            newHovered = 3; // Applied / Screening
                        }
                    }

                    if (newHovered != hoveredSlice) {
                        hoveredSlice = newHovered;
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
                    hoveredSlice = -1;
                    mousePt = null;
                    repaint();
                }
            });
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
            int popShort = (hoveredSlice == 0) ? 6 : 0;
            g2.setColor(hoveredSlice == 0 ? cShort.brighter() : cShort);
            g2.fillArc(ox - popShort / 2, oy - popShort / 2, outerDiam + popShort, outerDiam + popShort, start, angleShort);
            start += angleShort;

            // Arc 2: Offers
            int popOff = (hoveredSlice == 1) ? 6 : 0;
            g2.setColor(hoveredSlice == 1 ? cOffers.brighter() : cOffers);
            g2.fillArc(ox - popOff / 2, oy - popOff / 2, outerDiam + popOff, outerDiam + popOff, start, angleOffers);
            start += angleOffers;

            // Arc 3: Accepted
            int popAcc = (hoveredSlice == 2) ? 6 : 0;
            g2.setColor(hoveredSlice == 2 ? cAcc.brighter() : cAcc);
            g2.fillArc(ox - popAcc / 2, oy - popAcc / 2, outerDiam + popAcc, outerDiam + popAcc, start, angleAcc);
            start += angleAcc;

            // Arc 4: Applied remainder
            int popRem = (hoveredSlice == 3) ? 6 : 0;
            g2.setColor(hoveredSlice == 3 ? cRem.brighter() : cRem);
            g2.fillArc(ox - popRem / 2, oy - popRem / 2, outerDiam + popRem, outerDiam + popRem, start, 360 - (start - 90));

            // Cut out donut center
            g2.setColor(Theme.SURFACE);
            g2.fillOval(ix, iy, innerDiam, innerDiam);

            // Center counter label (dynamically updates when slice is hovered)
            String cntStr;
            String subStr;
            Color centerColor = Theme.TEXT;
            if (hoveredSlice == 0) {
                cntStr = String.valueOf(valShort);
                subStr = "Shortlisted";
                centerColor = cShort;
            } else if (hoveredSlice == 1) {
                cntStr = String.valueOf(valOffers);
                subStr = "Offers";
                centerColor = cOffers;
            } else if (hoveredSlice == 2) {
                cntStr = String.valueOf(valAccepted);
                subStr = "Placed";
                centerColor = cAcc;
            } else if (hoveredSlice == 3) {
                cntStr = String.valueOf(Math.max(0, valApps - valShort - valOffers - valAccepted));
                subStr = "In Review";
            } else {
                cntStr = String.valueOf(valApps);
                subStr = "Total";
            }

            g2.setFont(Theme.sansBold(18));
            g2.setColor(centerColor);
            FontMetrics fm = g2.getFontMetrics();
            int sw = fm.stringWidth(cntStr);
            g2.drawString(cntStr, cx - sw / 2, cy + 2);

            g2.setFont(Theme.sans(10));
            g2.setColor(Theme.MUTED);
            FontMetrics fsm = g2.getFontMetrics();
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

            // Floating Tooltip Card
            if (hoveredSlice >= 0 && mousePt != null) {
                int tipW = 148;
                int tipH = 58;
                int tipX = Math.max(10, Math.min(w - tipW - 10, mousePt.x + 12));
                int tipY = Math.max(6, Math.min(h - tipH - 10, mousePt.y - tipH / 2));

                g2.setColor(Theme.isDarkMode ? new Color(0x0F, 0x17, 0x2A, 245) : new Color(0x1E, 0x29, 0x3B, 242));
                g2.fillRoundRect(tipX, tipY, tipW, tipH, 8, 8);
                g2.setColor(Theme.isDarkMode ? new Color(0x33, 0x41, 0x55) : new Color(0x47, 0x55, 0x69));
                g2.drawRoundRect(tipX, tipY, tipW, tipH, 8, 8);

                String sliceName;
                int sliceCount;
                int slicePct;
                Color dotColor;
                if (hoveredSlice == 0) {
                    sliceName = "Shortlisted Candidates";
                    sliceCount = valShort;
                    slicePct = pctShort;
                    dotColor = new Color(0x60, 0xA5, 0xFA);
                } else if (hoveredSlice == 1) {
                    sliceName = "Official Job Offers";
                    sliceCount = valOffers;
                    slicePct = pctOff;
                    dotColor = new Color(0xFB, 0xBF, 0x24);
                } else if (hoveredSlice == 2) {
                    sliceName = "Accepted & Placed";
                    sliceCount = valAccepted;
                    slicePct = pctAcc;
                    dotColor = new Color(0x4A, 0xDE, 0x80);
                } else {
                    sliceName = "Applied / In Review";
                    sliceCount = Math.max(0, valApps - valShort - valOffers - valAccepted);
                    slicePct = 100 - pctShort - pctOff - pctAcc;
                    dotColor = new Color(0x94, 0xA3, 0xB8);
                }

                g2.setFont(Theme.sansBold(10));
                g2.setColor(Color.WHITE);
                g2.drawString(sliceName, tipX + 10, tipY + 16);

                g2.setFont(Theme.sans(11));
                g2.setColor(dotColor);
                g2.fillOval(tipX + 10, tipY + 26, 7, 7);
                g2.setColor(new Color(0xDF, 0xE4, 0xEA));
                g2.drawString(sliceCount + " Students (" + slicePct + "%)", tipX + 22, tipY + 34);

                g2.setFont(Theme.sans(10));
                g2.setColor(new Color(0x94, 0xA3, 0xB8));
                g2.drawString("Share of active pipeline", tipX + 10, tipY + 48);
            }

            g2.dispose();
        }
    }
}
