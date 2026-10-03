package com.campusplacement.ui.components;

import java.awt.FlowLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Row of search field and filter combos that triggers a reload when anything changes. */
public class FilterBar extends JPanel {
    private final Timer debounce;

    public FilterBar(Runnable onChange) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);
        debounce = new Timer(280, e -> onChange.run());
        debounce.setRepeats(false);
        this.onChange = onChange;
    }

    private final Runnable onChange;

    public HintField search(String hint) {
        HintField f = Ui.search(hint);
        f.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { debounce.restart(); }

            public void removeUpdate(DocumentEvent e) { debounce.restart(); }

            public void changedUpdate(DocumentEvent e) { debounce.restart(); }
        });
        add(f);
        return f;
    }

    public <T> JComboBox<T> combo(JComboBox<T> combo) {
        combo.addActionListener(e -> onChange.run());
        add(javax.swing.Box.createHorizontalStrut(8));
        add(combo);
        return combo;
    }

    public void addRight(JComponent c) {
        add(javax.swing.Box.createHorizontalStrut(8));
        add(c);
    }
}
