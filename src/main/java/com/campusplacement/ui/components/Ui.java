package com.campusplacement.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

/** Factory methods for consistently styled components. */
public final class Ui {
    private Ui() { }

    public static Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(6, 9, 6, 9));
    }

    public static Border FIELD_BORDER = fieldBorder();

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static JLabel title(String text) { return label(text, Theme.sansBold(24), Theme.TEXT); }

    public static JLabel heading(String text) { return label(text, Theme.sansBold(16), Theme.TEXT); }

    public static JLabel muted(String text) { return label(text, Theme.sans(12), Theme.MUTED); }

    public static JLabel body(String text) { return label(text, Theme.sans(13), Theme.TEXT); }

    public static JLabel fieldLabel(String text) { return label(text, Theme.sansBold(11), Theme.MUTED); }

    public static JTextField field(String value) {
        JTextField f = new HintField("");
        f.setText(value == null ? "" : value);
        style(f);
        return f;
    }

    public static HintField search(String hint) {
        HintField f = new HintField(hint);
        style(f);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 30, 6, 9)));
        f.setPreferredSize(new Dimension(260, 34));
        f.setIconGlyph(Icons.Glyph.SEARCH);
        return f;
    }

    public static JPasswordField password() {
        JPasswordField f = new JPasswordField();
        style(f);
        return f;
    }

    public static JTextArea area(String value, int rows) {
        JTextArea a = new JTextArea(value == null ? "" : value, rows, 30);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(Theme.sans(13));
        a.setForeground(Theme.TEXT);
        a.setBackground(Theme.SURFACE);
        a.setBorder(BorderFactory.createEmptyBorder(6, 9, 6, 9));
        return a;
    }

    public static JScrollPane areaScroll(JTextArea a) {
        JScrollPane sp = new JScrollPane(a);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        return sp;
    }

    public static void style(JTextField f) {
        f.setFont(Theme.sans(13));
        f.setForeground(Theme.TEXT);
        f.setBackground(Theme.FIELD_BG);
        f.setCaretColor(Theme.TEXT);
        f.setBorder(fieldBorder());
        f.setPreferredSize(new Dimension(Math.max(f.getPreferredSize().width, 220), 34));
    }

    public static <T> JComboBox<T> combo(List<T> items) {
        JComboBox<T> c = new JComboBox<>();
        items.forEach(c::addItem);
        styleCombo(c);
        return c;
    }

    /** Combo with a leading "All …" entry represented by null. */
    public static <T> JComboBox<T> filterCombo(String allLabel, List<T> items) {
        JComboBox<T> c = new JComboBox<>();
        c.addItem(null);
        items.forEach(c::addItem);
        styleCombo(c);
        c.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value == null ? allLabel : value, index, sel, focus);
                setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
                return this;
            }
        });
        return c;
    }

    public static void styleCombo(JComboBox<?> c) {
        c.setFont(Theme.sans(13));
        c.setBackground(Theme.SURFACE);
        c.setForeground(Theme.TEXT);
        c.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        c.setPreferredSize(new Dimension(Math.max(170, c.getPreferredSize().width), 34));
        c.setMaximumRowCount(14);
        if (!(c.getRenderer() instanceof PaddedRenderer)) {
            c.setRenderer(new PaddedRenderer());
        }
    }

    static class PaddedRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
            super.getListCellRendererComponent(list, value, index, sel, focus);
            setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
            return this;
        }
    }

    /** Apple / Windows 11 Fluent rounded elevated card with hairline border. */
    public static JPanel card(JComponent content, int pad) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Theme.SURFACE);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    /** Card with a serif heading row and optional right-aligned actions. */
    public static JPanel section(String heading, String caption, JComponent content, JComponent... actions) {
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel titles = vstack(0);
        titles.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        titles.add(heading(heading));
        if (caption != null) {
            JLabel cap = muted(caption);
            cap.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
            titles.add(cap);
        }
        head.add(titles, BorderLayout.WEST);
        if (actions.length > 0) {
            head.add(row(actions), BorderLayout.EAST);
        }
        head.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(head, BorderLayout.NORTH);
        body.add(content, BorderLayout.CENTER);
        return card(body, 18);
    }

    public static JPanel row(JComponent... items) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        for (JComponent c : items) {
            p.add(c);
        }
        return p;
    }

    public static JPanel rightRow(JComponent... items) {
        JPanel p = row(items);
        ((FlowLayout) p.getLayout()).setAlignment(FlowLayout.RIGHT);
        return p;
    }

    public static JPanel vstack(int gap) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        if (gap > 0) {
            p.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }
        return p;
    }

    public static JScrollPane scroll(JComponent c) {
        TrackingPanel holder = new TrackingPanel();
        holder.setOpaque(c.isOpaque());
        holder.setBackground(c.getBackground());
        holder.add(c, BorderLayout.CENTER);
        JScrollPane sp = new JScrollPane(holder);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(c.isOpaque() ? c.getBackground() : Theme.BG);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    /** Thin plum rule used under hero headings. */
    public static JComponent rule(int width) {
        return new JComponent() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(width, 3); }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PLUM);
                g2.fillRect(0, 0, width, 2);
            }
        };
    }

    /** Key/value line for detail panels. */
    public static JPanel kv(String key, String value) {
        return kv(key, value, 160);
    }

    /** Key/value line whose value wraps at the given pixel width. */
    public static JPanel kv(String key, String value, int valueWidth) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        JLabel k = muted(key);
        k.setPreferredSize(new Dimension(105, 22));
        k.setVerticalAlignment(JLabel.TOP);
        k.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
        p.add(k, BorderLayout.WEST);
        JLabel v = body("<html><div style='width:" + valueWidth + "px'>" + escape(value == null || value.isBlank() ? "—" : value)
                + "</div></html>");
        v.setVerticalAlignment(JLabel.TOP);
        p.add(v, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 20));
        return p;
    }

    /** Panel that always matches the viewport width, so content wraps instead of scrolling sideways. */
    static class TrackingPanel extends JPanel implements javax.swing.Scrollable {
        TrackingPanel() { super(new BorderLayout()); }

        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }

        public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) { return 18; }

        public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) { return r.height; }

        public boolean getScrollableTracksViewportWidth() { return true; }

        public boolean getScrollableTracksViewportHeight() {
            return getParent() != null && getParent().getHeight() > getPreferredSize().height;
        }
    }

    public static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }
}
