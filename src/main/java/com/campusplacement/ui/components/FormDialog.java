package com.campusplacement.ui.components;

import com.campusplacement.service.ServiceException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Modal form. The save action runs validation and the database operation; if it throws a
 * ServiceException the message is shown inside the dialog and the dialog stays open.
 */
public class FormDialog extends JDialog {
    private final JPanel form = new JPanel(new GridBagLayout());
    private final JLabel error = Ui.label(" ", Theme.sans(12), Theme.BRICK);
    private int row;
    private boolean saved;

    public FormDialog(Component parent, String title, String subtitle) {
        super(parent == null ? null : SwingUtilities.getWindowAncestor(parent), title, ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, Theme.PLUM),
                BorderFactory.createEmptyBorder(20, 24, 18, 24)));
        JPanel head = Ui.vstack(0);
        head.add(Ui.label(title, Theme.serif(20), Theme.TEXT));
        if (subtitle != null) {
            head.add(Ui.muted(subtitle));
        }
        root.add(head, BorderLayout.NORTH);
        form.setOpaque(false);
        root.add(form, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(error, BorderLayout.NORTH);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
        this.south = south;
    }

    private final JPanel south;

    public FormDialog field(String label, JComponent c) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = row;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(10, 0, 0, 16);
        JLabel l = Ui.fieldLabel(label);
        l.setPreferredSize(new Dimension(140, 18));
        form.add(l, g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 0, 0);
        form.add(c, g);
        row++;
        return this;
    }

    public FormDialog note(String text) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(10, 0, 0, 0);
        form.add(Ui.muted(text), g);
        return this;
    }

    /** Shows the dialog; returns true if the save action completed without error. */
    public boolean open(String saveLabel, Runnable onSave) {
        Btn cancel = new Btn("Cancel", Btn.Variant.SECONDARY);
        cancel.addActionListener(e -> dispose());
        Btn save = new Btn(saveLabel, Btn.Variant.PRIMARY);
        save.addActionListener(e -> {
            try {
                onSave.run();
                saved = true;
                dispose();
            } catch (ServiceException ex) {
                error.setText("<html>" + Ui.escape(ex.getMessage()) + "</html>");
                pack();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                error.setText("Something went wrong. The change was not saved.");
            }
        });
        south.add(Ui.rightRow(cancel, save), BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
        pack();
        setMinimumSize(new Dimension(Math.max(520, getWidth()), getHeight()));
        Window owner = getOwner();
        setLocationRelativeTo(owner);
        setVisible(true);
        return saved;
    }
}
