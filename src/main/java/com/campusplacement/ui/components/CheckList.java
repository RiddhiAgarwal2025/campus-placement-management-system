package com.campusplacement.ui.components;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/** Multi-select list of check boxes in a scrollable grid. */
public class CheckList<T> extends JScrollPane {
    private final Map<JCheckBox, T> boxes = new LinkedHashMap<>();

    public CheckList(List<T> items, Function<T, String> label, Collection<?> selectedKeys, Function<T, Object> key, int columns) {
        JPanel p = new JPanel(new GridLayout(0, columns, 6, 2));
        p.setBackground(Theme.SURFACE);
        p.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        for (T item : items) {
            JCheckBox b = new JCheckBox(label.apply(item));
            b.setFont(Theme.sans(12));
            b.setBackground(Theme.SURFACE);
            b.setForeground(Theme.TEXT);
            b.setFocusPainted(false);
            b.setSelected(selectedKeys.contains(key.apply(item)));
            boxes.put(b, item);
            p.add(b);
        }
        setViewportView(p);
        setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        getVerticalScrollBar().setUnitIncrement(14);
        int rows = (items.size() + columns - 1) / columns;
        setPreferredSize(new Dimension(360, Math.min(150, Math.max(40, rows * 26 + 14))));
    }

    public List<T> selected() {
        List<T> list = new ArrayList<>();
        boxes.forEach((b, t) -> {
            if (b.isSelected()) {
                list.add(t);
            }
        });
        return list;
    }
}
