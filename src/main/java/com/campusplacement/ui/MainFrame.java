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
        bar.setBackground(Theme.INK);
        bar.setPreferredSize(new Dimension(10, 58));
        bar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x2E2A31)),
                BorderFactory.createEmptyBorder(0, 24, 0, 24)));
        JLabel mark = Ui.label("CAMPUS PLACEMENTS", Theme.serif(16), Theme.SURFACE);
        bar.add(mark, BorderLayout.WEST);
        JPanel right = Ui.vstack(0);
        right.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JLabel name = Ui.label(user.displayName(), Theme.sansBold(13), Theme.INK_TEXT);
        name.setAlignmentX(RIGHT_ALIGNMENT);
        String role = user.role() == User.Role.OFFICER ? "Placement Officer" : "Student  " + user.studentId();
        JLabel roleLabel = Ui.label(role + "   |   " + Formats.date(LocalDate.now()), Theme.sans(11), Theme.INK_MUTED);
        roleLabel.setAlignmentX(RIGHT_ALIGNMENT);
        right.add(name);
        right.add(roleLabel);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel sidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Theme.INK);
        side.setPreferredSize(new Dimension(232, 10));
        JPanel list = Ui.vstack(0);
        list.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        String section = null;
        for (Nav n : navs) {
            if (!n.section().isEmpty() && !n.section().equals(section)) {
                section = n.section();
                JLabel s = Ui.label(section, Theme.sansBold(10), Theme.INK_MUTED);
                s.setBorder(BorderFactory.createEmptyBorder(16, 26, 6, 0));
                s.setAlignmentX(LEFT_ALIGNMENT);
                list.add(s);
            }
            NavItem item = new NavItem(n.label(), n.glyph(), () -> show(n.key()));
            items.put(n.key(), item);
            list.add(item);
        }
        side.add(list, BorderLayout.NORTH);
        JPanel bottom = Ui.vstack(0);
        bottom.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x2E2A31)),
                BorderFactory.createEmptyBorder(8, 0, 12, 0)));
        bottom.add(new NavItem("Logout", Glyph.LOGOUT, this::logout));
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

    /** Sidebar entry with icon, hover state and a plum indicator when active. */
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
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            setPreferredSize(new Dimension(232, 38));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            label = Ui.label(text, Theme.sans(13), Theme.INK_TEXT);
            label.setIconTextGap(12);
            label.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
            add(label);
            add(Box.createHorizontalStrut(4), BorderLayout.EAST);
            updateLook();
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { onClick.run(); }

                @Override
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }

                @Override
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setActive(boolean a) {
            active = a;
            updateLook();
            repaint();
        }

        private void updateLook() {
            Color c = active ? Theme.SURFACE : Theme.INK_TEXT;
            label.setForeground(c);
            label.setFont(active ? Theme.sansBold(13) : Theme.sans(13));
            label.setIcon(Icons.of(glyph, 17, active ? Theme.LIGHT_PLUM : Theme.INK_MUTED));
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (active || hover) {
                g.setColor(active ? Theme.INK_RAISED : new Color(0x1F1C22));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            if (active) {
                g.setColor(Theme.PLUM);
                g.fillRect(0, 0, 3, getHeight());
            }
            super.paintComponent(g);
        }
    }
}
