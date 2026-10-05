package com.campusplacement.ui.components;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

/** Sortable, styled table bound to a list of row objects, with an empty-state message. */
public class DataTable<T> extends JPanel {
    public enum Kind { TEXT, BADGE, NUMBER, DATE, MONEY }

    public record Column<T>(String name, Function<T, Object> getter, int width, Kind kind) { }

    private final List<Column<T>> columns = new ArrayList<>();
    private final List<T> rows = new ArrayList<>();
    private final Model model = new Model();
    private final JTable table;
    private final CardLayout cards = new CardLayout();
    private final JLabel emptyTitle;
    private final JLabel emptyText;
    private JScrollPane scroll;

    /** Fills the viewport when columns fit; otherwise keeps preferred widths and scrolls horizontally. */
    private void fitColumns(JScrollPane sp) {
        int vw = sp.getViewport().getWidth();
        if (vw <= 0) {
            return;
        }
        int total = 0;
        for (int i = 0; i < table.getColumnCount(); i++) {
            int w = i < columns.size() && columns.get(i).width() > 0 ? columns.get(i).width() : 120;
            total += w;
        }
        int mode = vw >= total ? JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS : JTable.AUTO_RESIZE_OFF;
        if (table.getAutoResizeMode() != mode) {
            table.setAutoResizeMode(mode);
            for (int i = 0; i < table.getColumnCount() && i < columns.size(); i++) {
                int w = columns.get(i).width() > 0 ? columns.get(i).width() : 120;
                table.getColumnModel().getColumn(i).setPreferredWidth(w);
            }
        }
    }

    public DataTable(String emptyHeading, String emptyMessage) {
        super();
        setLayout(cards);
        setOpaque(false);
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row) && !(c instanceof Badge)) {
                    c.setBackground(row % 2 == 1 ? Theme.ROW_ALT : Theme.SURFACE);
                }
                return c;
            }
        };
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFont(Theme.sans(13));
        table.setForeground(Theme.TEXT);
        table.setBackground(Theme.SURFACE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(false);
        TableRowSorter<Model> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(10, 36));
        header.setDefaultRenderer(new HeaderRenderer());
        header.setBackground(Theme.isDarkMode ? new Color(0x10, 0x17, 0x24) : new Color(0xF8, 0xFA, 0xFC));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(300, 200));
        sp.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) { fitColumns(sp); }
        });
        this.scroll = sp;
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        sp.getViewport().setBackground(Theme.SURFACE);
        add(sp, "table");

        JPanel empty = new JPanel();
        empty.setLayout(new javax.swing.BoxLayout(empty, javax.swing.BoxLayout.Y_AXIS));
        empty.setBackground(Theme.SURFACE);
        empty.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(48, 24, 48, 24)));
        emptyTitle = Ui.heading(emptyHeading);
        emptyText = Ui.muted(emptyMessage);
        emptyTitle.setAlignmentX(CENTER_ALIGNMENT);
        emptyText.setAlignmentX(CENTER_ALIGNMENT);
        empty.add(javax.swing.Box.createVerticalGlue());
        empty.add(emptyTitle);
        empty.add(javax.swing.Box.createVerticalStrut(6));
        empty.add(emptyText);
        empty.add(javax.swing.Box.createVerticalGlue());
        add(empty, "empty");
        cards.show(this, "empty");
    }

    public DataTable<T> col(String name, Function<T, Object> getter, int width) {
        return col(name, getter, width, Kind.TEXT);
    }

    public DataTable<T> col(String name, Function<T, Object> getter, int width, Kind kind) {
        columns.add(new Column<>(name, getter, width, kind));
        model.fireTableStructureChanged();
        applyColumns();
        return this;
    }

    public void clearColumns() {
        columns.clear();
        rows.clear();
        model.fireTableStructureChanged();
    }

    private void applyColumns() {
        @SuppressWarnings("unchecked")
        TableRowSorter<Model> sorter = (TableRowSorter<Model>) table.getRowSorter();
        for (int i = 0; i < columns.size(); i++) {
            Column<T> c = columns.get(i);
            var tc = table.getColumnModel().getColumn(i);
            tc.setPreferredWidth(c.width() > 0 ? c.width() : 120);
            switch (c.kind()) {
                case BADGE -> tc.setCellRenderer(new Badge.Renderer());
                case NUMBER, MONEY -> tc.setCellRenderer(new CellRenderer(SwingConstants.RIGHT, c.kind()));
                default -> tc.setCellRenderer(new CellRenderer(SwingConstants.LEFT, c.kind()));
            }
            sorter.setComparator(i, VALUE_ORDER);
        }
        if (scroll != null) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            fitColumns(scroll);
        }
    }

    private static final Comparator<Object> VALUE_ORDER = (a, b) -> {
        if (a == null) {
            return b == null ? 0 : -1;
        }
        if (b == null) {
            return 1;
        }
        if (a instanceof Number x && b instanceof Number y) {
            return new BigDecimal(x.toString()).compareTo(new BigDecimal(y.toString()));
        }
        if (a instanceof Comparable && a.getClass() == b.getClass()) {
            @SuppressWarnings("unchecked")
            Comparable<Object> ca = (Comparable<Object>) a;
            return ca.compareTo(b);
        }
        return a.toString().compareToIgnoreCase(b.toString());
    };

    public void setRows(List<T> data) {
        T keep = selected();
        rows.clear();
        rows.addAll(data);
        model.fireTableDataChanged();
        cards.show(this, rows.isEmpty() ? "empty" : "table");
        if (keep != null) {
            int idx = rows.indexOf(keep);
            if (idx >= 0) {
                int view = table.convertRowIndexToView(idx);
                table.setRowSelectionInterval(view, view);
            }
        }
    }

    public void setEmptyMessage(String heading, String message) {
        emptyTitle.setText(heading);
        emptyText.setText(message);
    }

    public List<T> rows() { return List.copyOf(rows); }

    public T selected() {
        int v = table.getSelectedRow();
        if (v < 0) {
            return null;
        }
        int m = table.convertRowIndexToModel(v);
        return (m >= 0 && m < rows.size()) ? rows.get(m) : null;
    }

    public JTable table() { return table; }

    /** Enables selecting several rows (Ctrl/Shift click). */
    public DataTable<T> multiSelect() {
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        return this;
    }

    public List<T> selectedRows() {
        List<T> list = new ArrayList<>();
        for (int v : table.getSelectedRows()) {
            list.add(rows.get(table.convertRowIndexToModel(v)));
        }
        return list;
    }

    public AbstractTableModel model() { return model; }

    public void onSelect(Consumer<T> handler) {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handler.accept(selected());
            }
        });
    }

    public void onDoubleClick(Consumer<T> handler) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && selected() != null) {
                    handler.accept(selected());
                }
            }
        });
    }

    private class Model extends AbstractTableModel {
        @Override
        public int getRowCount() { return rows.size(); }

        @Override
        public int getColumnCount() { return columns.size(); }

        @Override
        public String getColumnName(int c) { return columns.get(c).name(); }

        @Override
        public Object getValueAt(int r, int c) { return columns.get(c).getter().apply(rows.get(r)); }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            setFont(Theme.sansBold(11));
            setForeground(Theme.MUTED);
            setBackground(Theme.isDarkMode ? new Color(0x10, 0x17, 0x24) : new Color(0xF8, 0xFA, 0xFC));
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setHorizontalAlignment(LEFT);
            return this;
        }
    }

    private static class CellRenderer extends DefaultTableCellRenderer {
        private final Kind kind;

        CellRenderer(int align, Kind kind) {
            this.kind = kind;
            setHorizontalAlignment(align);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            Object shown = v;
            if (v instanceof LocalDate d) {
                shown = com.campusplacement.util.Formats.date(d);
            } else if (v instanceof LocalDateTime d) {
                shown = com.campusplacement.util.Formats.dateTime(d);
            } else if (kind == Kind.MONEY && v instanceof BigDecimal b) {
                shown = com.campusplacement.util.Formats.lpa(b);
            } else if (v == null) {
                shown = "—";
            }
            super.getTableCellRendererComponent(t, shown, s, false, r, c);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setForeground(Theme.TEXT);
            setToolTipText(shown != null && shown.toString().length() > 28 ? shown.toString() : null);
            return this;
        }
    }

    /** Wraps the table in a BorderLayout panel with the given minimum height. */
    public JPanel withHeight(int h) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(200, h));
        p.add(this);
        return p;
    }
}
