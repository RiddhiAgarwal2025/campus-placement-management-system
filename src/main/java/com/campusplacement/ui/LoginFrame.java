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
    private final JTextField username = Ui.field("officer");
    private final JPasswordField password = Ui.password();
    private final JLabel message = Ui.label(" ", Theme.sans(12), Theme.BRICK);

    public LoginFrame() {
        super("Campus Placement Portal — Sign in");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        password.setText("Officer@123");
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Theme.BG);
        root.add(cardPanel());
        setContentPane(root);
        setSize(580, 660);
        setMinimumSize(new Dimension(520, 600));
        setLocationRelativeTo(null);
    }

    private JPanel cardPanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(36, 32, 36, 32)));
        card.setPreferredSize(new Dimension(460, 540));

        JPanel form = Ui.vstack(0);

        JPanel brand = new JPanel(new BorderLayout());
        brand.setOpaque(false);
        JLabel mark = Ui.label("UNIVERSITY PLACEMENT CELL", Theme.sansBold(11), Theme.MUTED);
        brand.add(mark, BorderLayout.WEST);
        form.add(left(brand));
        form.add(Box.createVerticalStrut(16));

        form.add(left(Ui.label("Sign in to Placement Portal", Theme.sansBold(19), Theme.TEXT)));
        form.add(Box.createVerticalStrut(6));
        form.add(left(Ui.muted("Manage drives, review candidates, and process offers.")));
        form.add(Box.createVerticalStrut(24));

        form.add(left(Ui.fieldLabel("Username")));
        form.add(Box.createVerticalStrut(6));
        form.add(left(username));
        form.add(Box.createVerticalStrut(14));

        form.add(left(Ui.fieldLabel("Password")));
        form.add(Box.createVerticalStrut(6));
        form.add(left(password));
        form.add(Box.createVerticalStrut(10));

        form.add(left(message));
        form.add(Box.createVerticalStrut(12));

        Btn signIn = new Btn("Sign in", Btn.Variant.PRIMARY);
        signIn.setPreferredSize(new Dimension(360, 40));
        signIn.setMaximumSize(new Dimension(360, 40));
        signIn.addActionListener(e -> login());
        form.add(left(signIn));
        form.add(Box.createVerticalStrut(10));

        JPanel presets = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        presets.setBackground(Theme.SURFACE);
        presets.setPreferredSize(new Dimension(360, 32));
        presets.setMaximumSize(new Dimension(360, 32));
        Btn offBtn = new Btn("Fill: Officer", Btn.Variant.SECONDARY);
        offBtn.addActionListener(e -> {
            username.setText("officer");
            password.setText("Officer@123");
            message.setText(" ");
        });
        Btn stuBtn = new Btn("Fill: Student", Btn.Variant.SECONDARY);
        stuBtn.addActionListener(e -> {
            username.setText("21CSE001");
            password.setText("Student@123");
            message.setText(" ");
        });
        presets.add(offBtn);
        presets.add(stuBtn);
        form.add(left(presets));
        form.add(Box.createVerticalStrut(12));

        Btn test = new Btn("Test database connection", Btn.Variant.GHOST);
        test.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        test.addActionListener(e -> testConnection());
        form.add(left(test));

        username.setMaximumSize(new Dimension(360, 36));
        password.setMaximumSize(new Dimension(360, 36));
        getRootPane().setDefaultButton(signIn);

        card.add(form, BorderLayout.CENTER);
        return card;
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
