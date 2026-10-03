package com.campusplacement.ui;

import com.campusplacement.config.DatabaseConfig;
import com.campusplacement.db.Db;
import com.campusplacement.model.User;
import com.campusplacement.service.AuthService;
import com.campusplacement.service.ServiceException;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {
    private final AuthService auth = new AuthService();
    private final JTextField username = Ui.field("");
    private final JPasswordField password = Ui.password();
    private final JLabel message = Ui.label(" ", Theme.sans(12), Theme.BRICK);

    public LoginFrame() {
        super("Campus Placements — Sign in");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(brandPanel());
        root.add(formPanel());
        setContentPane(root);
        setSize(980, 600);
        setMinimumSize(new Dimension(860, 540));
        setLocationRelativeTo(null);
    }

    private JPanel brandPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.INK);
        p.setBorder(BorderFactory.createEmptyBorder(48, 52, 44, 52));
        JPanel top = Ui.vstack(0);
        JLabel mark = Ui.label("CAMPUS PLACEMENTS", Theme.serif(15), Theme.INK_TEXT);
        top.add(mark);
        p.add(top, BorderLayout.NORTH);

        JPanel mid = Ui.vstack(0);
        mid.add(Ui.label("<html>The placement<br>office, on one desk.</html>", Theme.serif(34), Theme.SURFACE));
        mid.add(Box.createVerticalStrut(18));
        JPanel rule = new JPanel();
        rule.setBackground(Theme.PLUM);
        rule.setMaximumSize(new Dimension(64, 3));
        rule.setPreferredSize(new Dimension(64, 3));
        rule.setAlignmentX(LEFT_ALIGNMENT);
        mid.add(rule);
        mid.add(Box.createVerticalStrut(18));
        mid.add(Ui.label("<html><div style='width:300px'>Drives, eligibility, selection rounds and offers, kept "
                + "in a single record for officers and students.</div></html>", Theme.sans(14), Theme.INK_MUTED));
        p.add(mid, BorderLayout.CENTER);
        mid.setBorder(BorderFactory.createEmptyBorder(110, 0, 0, 0));
        p.add(Ui.label("Training and Placement Cell", Theme.sans(12), Theme.INK_MUTED), BorderLayout.SOUTH);
        return p;
    }

    private JPanel formPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Theme.BG);
        JPanel form = Ui.vstack(0);
        form.setPreferredSize(new Dimension(340, 400));
        form.add(Ui.label("Sign in", Theme.serif(30), Theme.TEXT));
        form.add(Box.createVerticalStrut(6));
        form.add(Ui.muted("Use your placement office or student ID account."));
        form.add(Box.createVerticalStrut(28));
        form.add(left(Ui.fieldLabel("Username")));
        form.add(Box.createVerticalStrut(6));
        form.add(left(username));
        form.add(Box.createVerticalStrut(16));
        form.add(left(Ui.fieldLabel("Password")));
        form.add(Box.createVerticalStrut(6));
        form.add(left(password));
        form.add(Box.createVerticalStrut(10));
        form.add(left(message));
        form.add(Box.createVerticalStrut(14));
        Btn signIn = new Btn("Sign in", Btn.Variant.PRIMARY);
        signIn.setPreferredSize(new Dimension(340, 40));
        signIn.setMaximumSize(new Dimension(340, 40));
        signIn.addActionListener(e -> login());
        form.add(left(signIn));
        form.add(Box.createVerticalStrut(18));
        Btn test = new Btn("Test database connection", Btn.Variant.GHOST);
        test.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        test.addActionListener(e -> testConnection());
        form.add(left(test));
        username.setMaximumSize(new Dimension(340, 36));
        password.setMaximumSize(new Dimension(340, 36));
        getRootPane().setDefaultButton(signIn);
        outer.add(form);
        return outer;
    }

    private static <T extends javax.swing.JComponent> T left(T c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }

    private void login() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            User u = auth.login(username.getText(), new String(password.getPassword()));
            dispose();
            new MainFrame(u).setVisible(true);
        } catch (ServiceException ex) {
            message.setText("<html><div style='width:320px'>" + Ui.escape(ex.getMessage()) + "</div></html>");
            password.setText("");
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            message.setText("Unexpected error while signing in.");
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void testConnection() {
        try {
            String version = Db.query(c -> c.getMetaData().getDatabaseProductName() + " " + c.getMetaData().getDatabaseProductVersion());
            message.setForeground(Theme.PLUM);
            message.setText("<html><div style='width:320px'>Connected to " + Ui.escape(version) + " using "
                    + Ui.escape(DatabaseConfig.get().source()) + ".</div></html>");
        } catch (ServiceException ex) {
            message.setForeground(Theme.BRICK);
            message.setText("<html><div style='width:320px'>" + Ui.escape(ex.getMessage()) + "</div></html>");
        } catch (RuntimeException ex) {
            message.setForeground(Theme.BRICK);
            message.setText("<html><div style='width:320px'>" + Ui.escape(String.valueOf(ex.getMessage())) + "</div></html>");
        }
        Runnable reset = () -> message.setForeground(Theme.BRICK);
        javax.swing.Timer t = new javax.swing.Timer(6000, e -> reset.run());
        t.setRepeats(false);
        t.start();
    }
}
