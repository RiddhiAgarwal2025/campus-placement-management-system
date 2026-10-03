package com.campusplacement.ui.components;

import com.campusplacement.service.ServiceException;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Base class for content pages: header, body, and safe execution of user actions. */
public abstract class Page extends JPanel {
    private final JPanel actions = Ui.rightRow();
    private final JLabel subtitle;

    protected Page(String title, String sub) {
        super(new BorderLayout(0, 18));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(26, 32, 26, 32));
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel titles = Ui.vstack(0);
        titles.add(Ui.title(title));
        subtitle = Ui.muted(sub);
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        titles.add(subtitle);
        head.add(titles, BorderLayout.WEST);
        head.add(actions, BorderLayout.EAST);
        add(head, BorderLayout.NORTH);
    }

    protected void setSubtitle(String text) { subtitle.setText(text); }

    protected void addAction(JComponent c) { actions.add(c); }

    protected void setBody(JComponent c) { add(c, BorderLayout.CENTER); }

    /** Reloads data from the database. Called whenever the page is shown. */
    public abstract void refresh();

    /** Runs an action, showing any failure as a friendly message. Returns true on success. */
    protected boolean run(Runnable action) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            action.run();
            return true;
        } catch (ServiceException e) {
            Dialogs.error(this, "Action not completed", e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            Dialogs.error(this, "Unexpected error", "Something went wrong and the action was not completed. "
                    + "Details were written to the console.");
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
        return false;
    }

    protected <T> T load(Supplier<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (ServiceException e) {
            Dialogs.error(this, "Could not load data", e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            Dialogs.error(this, "Could not load data", "An unexpected error occurred while loading this page.");
        }
        return fallback;
    }

    protected void info(String title, String msg) { Dialogs.info(this, title, msg); }

    protected boolean confirm(String title, String msg, String action) { return Dialogs.confirm(this, title, msg, action); }

    protected boolean confirmDelete(String what) {
        return Dialogs.confirmDanger(this, "Delete " + what + "?", "This permanently removes " + what
                + " from the database.", "Delete");
    }

    /** Shows a message asking the user to select a row first. */
    protected boolean need(Object selection, String what) {
        if (selection == null) {
            Dialogs.info(this, "Nothing selected", "Select " + what + " in the table first.");
            return false;
        }
        return true;
    }
}
