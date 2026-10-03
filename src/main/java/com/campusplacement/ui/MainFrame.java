package com.campusplacement.ui;

import com.campusplacement.model.User;
import com.campusplacement.service.AuthService;
import com.campusplacement.ui.components.Dialogs;
import com.campusplacement.ui.components.Icons;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.ui.officer.ApplicationsPage;
import com.campusplacement.ui.officer.CompaniesPage;
import com.campusplacement.ui.officer.DepartmentsPage;
import com.campusplacement.ui.officer.DrivesPage;
import com.campusplacement.ui.officer.EligibilityPage;
import com.campusplacement.ui.officer.JobProfilesPage;
import com.campusplacement.ui.officer.OfferPage;
import com.campusplacement.ui.officer.OfficerDashboardPage;
import com.campusplacement.ui.officer.ReportsPage;
import com.campusplacement.ui.officer.SelectionPage;
import com.campusplacement.ui.officer.SkillsPage;
import com.campusplacement.ui.officer.StudentsPage;
import com.campusplacement.ui.student.MyApplicationsPage;
import com.campusplacement.ui.student.MyOffersPage;
import com.campusplacement.ui.student.MyProfilePage;
import com.campusplacement.ui.student.SelectionStatusPage;
import com.campusplacement.ui.student.StudentDashboardPage;
import com.campusplacement.ui.student.StudentDrivesPage;
import com.campusplacement.util.Formats;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Application shell: top bar, role-specific sidebar navigation and a card-switched content area. */
public class MainFrame extends JFrame {
    private static MainFrame current;

    private record Nav(String section, String key, String label, Glyph glyph, Supplier<Page> factory) { }

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, Page> pages = new HashMap<>();
    private final Map<String, NavItem> items = new HashMap<>();
    private final List<Nav> navs = new ArrayList<>();

    public MainFrame(User user) {
        super("Campus Placements");
        current = this;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        boolean officer = user.role() == User.Role.OFFICER;
        if (officer) {
            navs.add(new Nav("OVERVIEW", "dashboard", "Dashboard", Glyph.DASHBOARD, OfficerDashboardPage::new));
            navs.add(new Nav("PEOPLE", "students", "Students", Glyph.STUDENTS, StudentsPage::new));
            navs.add(new Nav("PEOPLE", "departments", "Departments", Glyph.DEPARTMENTS, DepartmentsPage::new));
            navs.add(new Nav("PEOPLE", "skills", "Skills", Glyph.SKILLS, SkillsPage::new));
            navs.add(new Nav("RECRUITMENT", "companies", "Companies", Glyph.COMPANIES, CompaniesPage::new));
            navs.add(new Nav("RECRUITMENT", "jobs", "Job Profiles", Glyph.JOBS, JobProfilesPage::new));
            navs.add(new Nav("RECRUITMENT", "drives", "Drives", Glyph.DRIVES, DrivesPage::new));
            navs.add(new Nav("RECRUITMENT", "eligibility", "Eligibility", Glyph.ELIGIBILITY, EligibilityPage::new));
            navs.add(new Nav("RECRUITMENT", "applications", "Applications", Glyph.APPLICATIONS, ApplicationsPage::new));
            navs.add(new Nav("RECRUITMENT", "selection", "Selection", Glyph.SELECTION, SelectionPage::new));
            navs.add(new Nav("RECRUITMENT", "offers", "Offers", Glyph.OFFERS, OfferPage::new));
            navs.add(new Nav("ANALYTICS", "reports", "Reports", Glyph.REPORTS, ReportsPage::new));
        } else {
            navs.add(new Nav("", "dashboard", "Dashboard", Glyph.DASHBOARD, StudentDashboardPage::new));
            navs.add(new Nav("", "drives", "Placement Drives", Glyph.DRIVES, StudentDrivesPage::new));
            navs.add(new Nav("", "applications", "My Applications", Glyph.APPLICATIONS, MyApplicationsPage::new));
            navs.add(new Nav("", "selection", "Selection Status", Glyph.SELECTION, SelectionStatusPage::new));
            navs.add(new Nav("", "offers", "My Offers", Glyph.OFFERS, MyOffersPage::new));
            navs.add(new Nav("", "profile", "My Profile", Glyph.PROFILE, MyProfilePage::new));
        }

        JPanel root = new JPanel(new BorderLayout());
        root.add(topBar(user), BorderLayout.NORTH);
        root.add(sidebar(), BorderLayout.WEST);
        content.setBackground(Theme.BG);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
        setSize(1360, 840);
        setMinimumSize(new Dimension(1120, 700));
        setLocationRelativeTo(null);
        show("dashboard");
    }

    /** Navigates the current window to a page by key (used for cross-page links). */
    public static void navigate(String key) {
        if (current != null) {
            current.show(key);
        }
    }

    private JPanel topBar(User user) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.SURFACE);
        bar.setPreferredSize(new Dimension(10, 58));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 24, 0, 24)));

        JPanel brand = Ui.row();
        brand.setOpaque(false);
        JLabel mark = Ui.label("CAMPUS PLACEMENTS", Theme.sansBold(14), Theme.TEXT);
        mark.setIcon(Icons.of(Glyph.DASHBOARD, 18, Theme.TEXT));
        mark.setIconTextGap(10);
        brand.add(mark);
        bar.add(brand, BorderLayout.WEST);

        JPanel right = Ui.vstack(2);
        right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JLabel name = Ui.label(user.displayName(), Theme.sansBold(13), Theme.TEXT);
        name.setAlignmentX(RIGHT_ALIGNMENT);
        String role = user.role() == User.Role.OFFICER ? "Placement Officer" : "Student • " + user.studentId();
        JLabel roleLabel = Ui.label(role + "   •   " + Formats.date(LocalDate.now()), Theme.sans(11), Theme.MUTED);
        roleLabel.setAlignmentX(RIGHT_ALIGNMENT);
        right.add(name);
        right.add(roleLabel);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel sidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Theme.SURFACE);
        side.setPreferredSize(new Dimension(236, 10));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));

        JPanel list = Ui.vstack(2);
        list.setOpaque(false);
        list.setBorder(BorderFactory.createEmptyBorder(14, 12, 14, 12));
        String section = null;
        for (Nav n : navs) {
            if (!n.section().isEmpty() && !n.section().equals(section)) {
                section = n.section();
                JLabel s = Ui.label(section, Theme.sansBold(10), Theme.MUTED);
                s.setBorder(BorderFactory.createEmptyBorder(14, 12, 6, 0));
                s.setAlignmentX(LEFT_ALIGNMENT);
                list.add(s);
            }
            NavItem item = new NavItem(n.label(), n.glyph(), () -> show(n.key()));
            items.put(n.key(), item);
            list.add(item);
        }
        side.add(list, BorderLayout.NORTH);

        JPanel bottom = Ui.vstack(0);
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(8, 12, 12, 12)));
        bottom.add(new NavItem("Sign out", Glyph.LOGOUT, this::logout));
        side.add(bottom, BorderLayout.SOUTH);
        return side;
    }

    private void show(String key) {
        Nav nav = navs.stream().filter(n -> n.key().equals(key)).findFirst().orElse(null);
        if (nav == null) {
            return;
        }
        Page page = pages.get(key);
        if (page == null) {
            page = nav.factory().get();
            pages.put(key, page);
            content.add(page, key);
        }
        items.forEach((k, item) -> item.setActive(k.equals(key)));
        cards.show(content, key);
        page.refresh();
    }

    private void logout() {
        if (Dialogs.confirm(this, "Sign out?", "You will return to the sign-in screen.", "Sign out")) {
            new AuthService().logout();
            current = null;
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    /** Sidebar entry with icon, hover state and a clean active pill. */
    private static class NavItem extends JPanel {
        private final JLabel label;
        private final Glyph glyph;
        private boolean active;
        private boolean hover;

        NavItem(String text, Glyph glyph, Runnable onClick) {
            super(new BorderLayout());
            this.glyph = glyph;
            setOpaque(false);
            setAlignmentX(LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            setPreferredSize(new Dimension(212, 36));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            label = Ui.label(text, Theme.sans(13), Theme.MUTED);
            label.setIconTextGap(10);
            label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
            add(label, BorderLayout.CENTER);
            updateLook();
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { onClick.run(); }

                @Override
                public void mouseEntered(MouseEvent e) { hover = true; updateLook(); repaint(); }

                @Override
                public void mouseExited(MouseEvent e) { hover = false; updateLook(); repaint(); }
            });
        }

        void setActive(boolean a) {
            active = a;
            updateLook();
            repaint();
        }

        private void updateLook() {
            Color c = active ? Theme.TEXT : (hover ? new Color(0x1E, 0x29, 0x3B) : Theme.MUTED);
            label.setForeground(c);
            label.setFont(active ? Theme.sansBold(13) : Theme.sans(13));
            Color iconColor = active ? Theme.TEXT : (hover ? new Color(0x33, 0x41, 0x55) : new Color(0x94, 0xA3, 0xB8));
            label.setIcon(Icons.of(glyph, 16, iconColor));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (active) {
                g2.setColor(new Color(0xF1, 0xF5, 0xF9));
                g2.fillRoundRect(0, 0, w, h, 6, 6);
                g2.setColor(Theme.PLUM);
                g2.fillRoundRect(0, 6, 3, h - 12, 2, 2);
            } else if (hover) {
                g2.setColor(new Color(0xF8, 0xFA, 0xFC));
                g2.fillRoundRect(0, 0, w, h, 6, 6);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
