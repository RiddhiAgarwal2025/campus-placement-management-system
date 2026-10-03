package com.campusplacement.ui;

import com.campusplacement.config.DatabaseConfig;
import com.campusplacement.db.Db;
import com.campusplacement.model.User;
import com.campusplacement.service.AuthService;
import com.campusplacement.service.ServiceException;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.SegmentedControl;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
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
    private JPanel root;
    private JPanel card;
    private Btn modeBtn;

    public LoginFrame() {
        super("Campus Placement Portal — Sign in");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        password.setText("Officer@123");
        root = new JPanel(new GridBagLayout());
        root.setBackground(Theme.BG);
        root.add(cardPanel());
        setContentPane(root);
        setSize(580, 660);
        setMinimumSize(new Dimension(520, 600));
        setLocationRelativeTo(null);

        Theme.addListener(() -> {
            String u = username.getText();
            char[] p = password.getPassword();
            Ui.style(username);
            Ui.style(password);
            getContentPane().removeAll();
            root = new JPanel(new GridBagLayout());
            root.setBackground(Theme.BG);
            root.add(cardPanel());
            username.setText(u);
            password.setText(new String(p));
            setContentPane(root);
            revalidate();
            repaint();
        });
    }

    private JPanel cardPanel() {
        card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Theme.SURFACE);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));
        card.setPreferredSize(new Dimension(460, 560));

        JPanel form = Ui.vstack(0);

        JPanel brand = new JPanel(new BorderLayout());
        brand.setOpaque(false);
        JLabel mark = Ui.label("UNIVERSITY PLACEMENT CELL", Theme.sansBold(11), Theme.MUTED);
        brand.add(mark, BorderLayout.WEST);

        modeBtn = new Btn(Theme.isDarkMode ? "Light Mode" : "Dark Mode", Btn.Variant.GHOST);
        modeBtn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        modeBtn.addActionListener(e -> Theme.toggleDarkMode());
        brand.add(modeBtn, BorderLayout.EAST);

        form.add(left(brand));
        form.add(Box.createVerticalStrut(18));

        JLabel titleLbl = Ui.label("Sign in to Placement Portal", Theme.sansBold(20), Theme.TEXT);
        titleLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        form.add(left(titleLbl));
        form.add(Box.createVerticalStrut(6));
        form.add(left(Ui.muted("Manage drives, review candidates, and process offers.")));
        form.add(Box.createVerticalStrut(20));

        // Segmented role switch
        SegmentedControl roleSwitch = new SegmentedControl("Placement Officer", "Student Candidate");
        roleSwitch.setPreferredSize(new Dimension(360, 36));
        roleSwitch.setMaximumSize(new Dimension(360, 36));
        roleSwitch.onSelect(idx -> {
            if (idx == 0) {
                username.setText("officer");
                password.setText("Officer@123");
            } else {
                username.setText("21CSE001");
                password.setText("Student@123");
            }
            message.setText(" ");
        });
        form.add(left(roleSwitch));
        form.add(Box.createVerticalStrut(18));

        form.add(left(Ui.fieldLabel("Username")));
        form.add(Box.createVerticalStrut(6));
        form.add(left(username));
        form.add(Box.createVerticalStrut(14));

        form.add(left(Ui.fieldLabel("Password")));
        form.add(Box.createVerticalStrut(6));
        form.add(left(password));
        form.add(Box.createVerticalStrut(10));

        form.add(left(message));
        form.add(Box.createVerticalStrut(14));

        Btn signIn = new Btn("Sign in", Btn.Variant.PRIMARY);
        signIn.setPreferredSize(new Dimension(360, 40));
        signIn.setMaximumSize(new Dimension(360, 40));
        signIn.addActionListener(e -> login());
        form.add(left(signIn));
        form.add(Box.createVerticalStrut(14));

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
