package com.campusplacement;

import com.campusplacement.ui.LoginFrame;
import com.campusplacement.ui.components.Theme;
import javax.swing.SwingUtilities;

/** Entry point of the Campus Placement and Recruitment Drive Management System. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            new LoginFrame().setVisible(true);
        });
    }
}
