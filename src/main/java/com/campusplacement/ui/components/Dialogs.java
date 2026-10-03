package com.campusplacement.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/** Themed message and confirmation dialogs. */
public final class Dialogs {
    private Dialogs() { }

    public static void info(Component parent, String title, String message) {
        show(parent, title, message, "Done", null, false);
    }

    public static void error(Component parent, String title, String message) {
        show(parent, title, message, "Close", null, true);
    }

    public static boolean confirm(Component parent, String title, String message, String action) {
        return show(parent, title, message, action, "Cancel", false);
    }

    public static boolean confirmDanger(Component parent, String title, String message, String action) {
        return show(parent, title, message, action, "Cancel", true);
    }

    private static boolean show(Component parent, String title, String message, String ok, String cancel, boolean danger) {
        Window w = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(w, title, JDialog.ModalityType.APPLICATION_MODAL);
        boolean[] result = {false};
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, danger ? Theme.BRICK : Theme.PLUM),
                BorderFactory.createEmptyBorder(20, 24, 18, 24)));
        root.add(Ui.heading(title), BorderLayout.NORTH);
        JTextArea text = new JTextArea(message);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(Theme.sans(13));
        text.setForeground(Theme.TEXT);
        text.setBackground(Theme.SURFACE);
        text.setBorder(null);
        text.setColumns(34);
        root.add(text, BorderLayout.CENTER);
        Btn okBtn = new Btn(ok, cancel != null && danger ? Btn.Variant.DANGER : Btn.Variant.PRIMARY);
        okBtn.addActionListener(e -> { result[0] = true; d.dispose(); });
        JPanel buttons;
        if (cancel != null) {
            Btn c = new Btn(cancel, Btn.Variant.SECONDARY);
            c.addActionListener(e -> d.dispose());
            buttons = Ui.rightRow(c, okBtn);
        } else {
            buttons = Ui.rightRow(okBtn);
        }
        root.add(buttons, BorderLayout.SOUTH);
        d.setContentPane(root);
        d.getRootPane().setDefaultButton(okBtn);
        d.pack();
        d.setMinimumSize(new Dimension(420, d.getHeight()));
        d.setResizable(false);
        d.setLocationRelativeTo(w);
        d.setVisible(true);
        return result[0];
    }
}
