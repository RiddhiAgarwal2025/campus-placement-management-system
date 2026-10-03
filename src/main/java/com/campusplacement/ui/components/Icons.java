package com.campusplacement.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/** Minimal line icons drawn with Java2D on a 20×20 grid, so no image assets are required. */
public final class Icons {
    public enum Glyph { DASHBOARD, STUDENTS, DEPARTMENTS, SKILLS, COMPANIES, JOBS, DRIVES, ELIGIBILITY, APPLICATIONS,
        SELECTION, OFFERS, REPORTS, PROFILE, LOGOUT, SEARCH, PLUS, REFRESH, EXPORT, CHECK, EDIT, TRASH, EYE, CLOSE, KEY }

    private Icons() { }

    public static Icon of(Glyph g, int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics graphics, int x, int y) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.translate(x, y);
                double s = size / 20.0;
                g2.scale(s, s);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                draw(g2, g);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }

            @Override
            public int getIconHeight() { return size; }
        };
    }

    private static void draw(Graphics2D g, Glyph glyph) {
        switch (glyph) {
            case DASHBOARD -> {
                g.draw(rr(3, 3, 6, 7)); g.draw(rr(11, 3, 6, 4)); g.draw(rr(11, 9, 6, 8)); g.draw(rr(3, 12, 6, 5));
            }
            case STUDENTS -> {
                g.draw(new Ellipse2D.Double(5, 3, 6, 6)); g.draw(arc(2.5, 11, 11, 16));
                g.draw(new Line2D.Double(14, 4, 14.5, 4)); g.draw(arc(12, 11.5, 18, 15));
            }
            case DEPARTMENTS -> {
                Path2D p = new Path2D.Double(); p.moveTo(2.5, 7.5); p.lineTo(10, 3); p.lineTo(17.5, 7.5); p.closePath(); g.draw(p);
                for (double xx : new double[] {5, 8.5, 11.5, 15}) { g.draw(new Line2D.Double(xx, 9.5, xx, 14.5)); }
                g.draw(new Line2D.Double(2.5, 17, 17.5, 17));
            }
            case SKILLS -> {
                Path2D p = new Path2D.Double(); p.moveTo(10, 2.5); p.lineTo(12.2, 7.4); p.lineTo(17.5, 7.9); p.lineTo(13.5, 11.4);
                p.lineTo(14.7, 16.7); p.lineTo(10, 14); p.lineTo(5.3, 16.7); p.lineTo(6.5, 11.4); p.lineTo(2.5, 7.9);
                p.lineTo(7.8, 7.4); p.closePath(); g.draw(p);
            }
            case COMPANIES -> {
                g.draw(rr(3, 4, 8, 13)); g.draw(rr(11, 8, 6, 9));
                for (double yy : new double[] {7.5, 10.5, 13.5}) { g.draw(new Line2D.Double(5.5, yy, 8.5, yy)); }
            }
            case JOBS -> {
                g.draw(rr(2.5, 6, 15, 10.5)); g.draw(rr(7, 3, 6, 3)); g.draw(new Line2D.Double(2.5, 10.5, 17.5, 10.5));
            }
            case DRIVES -> {
                g.draw(rr(3, 4.5, 14, 12.5)); g.draw(new Line2D.Double(3, 8.5, 17, 8.5));
                g.draw(new Line2D.Double(7, 2.5, 7, 6)); g.draw(new Line2D.Double(13, 2.5, 13, 6));
                g.draw(new Line2D.Double(6.5, 12, 9, 12)); g.draw(new Line2D.Double(11, 12, 13.5, 12));
            }
            case ELIGIBILITY -> {
                Path2D p = new Path2D.Double(); p.moveTo(10, 2.5); p.lineTo(16.5, 5); p.lineTo(16.5, 10);
                p.curveTo(16.5, 14, 13.5, 16.5, 10, 17.5); p.curveTo(6.5, 16.5, 3.5, 14, 3.5, 10); p.lineTo(3.5, 5); p.closePath();
                g.draw(p); check(g, 7, 10, 9.3, 12.3, 13.3, 7.8);
            }
            case APPLICATIONS -> {
                Path2D p = new Path2D.Double(); p.moveTo(5, 2.5); p.lineTo(12, 2.5); p.lineTo(15.5, 6); p.lineTo(15.5, 17.5);
                p.lineTo(5, 17.5); p.closePath(); g.draw(p);
                g.draw(new Line2D.Double(7.5, 9.5, 13, 9.5)); g.draw(new Line2D.Double(7.5, 12.5, 13, 12.5));
                g.draw(new Line2D.Double(7.5, 6.5, 10, 6.5));
            }
            case SELECTION -> {
                g.draw(new Line2D.Double(3, 4.5, 17, 4.5)); g.draw(new Line2D.Double(5.5, 10, 14.5, 10));
                g.draw(new Line2D.Double(8, 15.5, 12, 15.5));
            }
            case OFFERS -> {
                g.draw(new Ellipse2D.Double(5, 2.5, 10, 10));
                Path2D p = new Path2D.Double(); p.moveTo(7, 11.5); p.lineTo(6, 17.5); p.lineTo(10, 15.5); p.lineTo(14, 17.5);
                p.lineTo(13, 11.5); g.draw(p);
            }
            case REPORTS -> {
                g.draw(new Line2D.Double(3, 17, 17, 17)); g.draw(rr(4.5, 10, 2.5, 7)); g.draw(rr(8.75, 5, 2.5, 12));
                g.draw(rr(13, 8, 2.5, 9));
            }
            case PROFILE -> { g.draw(new Ellipse2D.Double(6.5, 3, 7, 7)); g.draw(arc(3.5, 12, 16.5, 17.5)); }
            case LOGOUT -> {
                Path2D p = new Path2D.Double(); p.moveTo(11, 3); p.lineTo(4, 3); p.lineTo(4, 17); p.lineTo(11, 17); g.draw(p);
                g.draw(new Line2D.Double(8.5, 10, 17, 10)); g.draw(new Line2D.Double(14, 7, 17, 10)); g.draw(new Line2D.Double(14, 13, 17, 10));
            }
            case SEARCH -> { g.draw(new Ellipse2D.Double(3, 3, 10, 10)); g.draw(new Line2D.Double(11.5, 11.5, 16.5, 16.5)); }
            case PLUS -> { g.draw(new Line2D.Double(10, 4, 10, 16)); g.draw(new Line2D.Double(4, 10, 16, 10)); }
            case REFRESH -> {
                g.draw(new java.awt.geom.Arc2D.Double(4, 4, 12, 12, 60, 280, java.awt.geom.Arc2D.OPEN));
                g.draw(new Line2D.Double(13, 2.8, 13.2, 5.6)); g.draw(new Line2D.Double(13.2, 5.6, 10.4, 6));
            }
            case EXPORT -> {
                g.draw(new Line2D.Double(10, 3, 10, 12)); g.draw(new Line2D.Double(6.5, 8.5, 10, 12)); g.draw(new Line2D.Double(13.5, 8.5, 10, 12));
                Path2D p = new Path2D.Double(); p.moveTo(3.5, 12.5); p.lineTo(3.5, 16.5); p.lineTo(16.5, 16.5); p.lineTo(16.5, 12.5); g.draw(p);
            }
            case CHECK -> check(g, 4, 10.5, 8, 14.5, 16, 5.5);
            case EDIT -> {
                Path2D p = new Path2D.Double(); p.moveTo(4, 16); p.lineTo(4.5, 12.5); p.lineTo(13, 4); p.lineTo(16, 7);
                p.lineTo(7.5, 15.5); p.closePath(); g.draw(p);
            }
            case TRASH -> {
                g.draw(new Line2D.Double(3.5, 5.5, 16.5, 5.5)); g.draw(rr(5.5, 5.5, 9, 11.5)); g.draw(new Line2D.Double(8, 3, 12, 3));
            }
            case EYE -> {
                Path2D p = new Path2D.Double(); p.moveTo(2.5, 10); p.curveTo(5, 5, 15, 5, 17.5, 10); p.curveTo(15, 15, 5, 15, 2.5, 10);
                g.draw(p); g.draw(new Ellipse2D.Double(7.5, 7.5, 5, 5));
            }
            case CLOSE -> { g.draw(new Line2D.Double(5, 5, 15, 15)); g.draw(new Line2D.Double(15, 5, 5, 15)); }
            case KEY -> {
                g.draw(new Ellipse2D.Double(3, 7, 6, 6)); g.draw(new Line2D.Double(9, 10, 17, 10));
                g.draw(new Line2D.Double(14, 10, 14, 13)); g.draw(new Line2D.Double(16.5, 10, 16.5, 12.5));
            }
            default -> { }
        }
    }

    private static RoundRectangle2D rr(double x, double y, double w, double h) {
        return new RoundRectangle2D.Double(x, y, w, h, 2.5, 2.5);
    }

    private static Path2D arc(double x1, double y1, double x2, double y2) {
        Path2D p = new Path2D.Double();
        p.moveTo(x1, y2);
        p.curveTo(x1, y1 - 1, x2, y1 - 1, x2, y2);
        return p;
    }

    private static void check(Graphics2D g, double x1, double y1, double x2, double y2, double x3, double y3) {
        Path2D p = new Path2D.Double();
        p.moveTo(x1, y1);
        p.lineTo(x2, y2);
        p.lineTo(x3, y3);
        g.draw(p);
    }
}
