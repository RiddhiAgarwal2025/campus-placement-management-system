package com.campusplacement.ui;

import com.campusplacement.model.Company;
import com.campusplacement.model.Drive;
import com.campusplacement.model.Student;
import com.campusplacement.model.User;
import com.campusplacement.service.AuthService;
import com.campusplacement.service.CompanyService;
import com.campusplacement.service.DriveService;
import com.campusplacement.service.StudentService;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.Dialogs;
import com.campusplacement.ui.components.HintField;
import com.campusplacement.ui.components.Icons;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.Searchable;
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
import java.awt.FlowLayout;
import java.awt.FontMetrics;
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
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Application shell: top bar, role-specific sidebar navigation and a card-switched content area. */
public class MainFrame extends JFrame {
    private static MainFrame current;

    private record Nav(String section, String key, String label, Glyph glyph, Supplier<Page> factory) { }

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, Page> pages = new HashMap<>();
    private final Map<String, NavItem> items = new HashMap<>();
    private final List<Nav> navs = new ArrayList<>();
    private JPanel bar;
    private JPanel side;
    private Btn modeBtn;
    private JLabel mark;
    private JLabel name;
    private JLabel roleLabel;
    private HintField topSearch;
    private final JPanel root = new JPanel(new BorderLayout());
    private final Runnable themeListener = this::onThemeChanged;
    private final User currentUser;
    private final StudentService studentService = new StudentService();
    private final CompanyService companyService = new CompanyService();
    private final DriveService driveService = new DriveService();
    private Timer searchDebounce;
    private boolean isSyncingSearch = false;
    private JPopupMenu searchPopup;

    public MainFrame(User user) {
        super("Campus Placements");
        this.currentUser = user;
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

        bar = topBar(user);
        side = sidebar();
        root.add(bar, BorderLayout.NORTH);
        root.add(side, BorderLayout.WEST);
        content.setBackground(Theme.BG);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
        setSize(1360, 840);
        setMinimumSize(new Dimension(1120, 700));
        setLocationRelativeTo(null);

        Theme.addListener(themeListener);

        show("dashboard");
    }

    private void onThemeChanged() {
        if (current != this || !isDisplayable()) {
            Theme.removeListener(themeListener);
            return;
        }
        root.setBackground(Theme.BG);
        content.setBackground(Theme.BG);
        if (bar != null) {
            bar.setBackground(Theme.SURFACE);
            bar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                    BorderFactory.createEmptyBorder(0, 24, 0, 24)));
        }
        if (side != null) {
            side.setBackground(Theme.SURFACE);
            side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));
        }
        if (modeBtn != null) {
            modeBtn.setText(Theme.isDarkMode ? "Light Mode" : "Dark Mode");
            modeBtn.repaint();
        }
        if (mark != null) {
            mark.setForeground(Theme.TEXT);
            mark.setIcon(Icons.of(Glyph.DASHBOARD, 18, Theme.TEXT));
        }
        if (name != null) name.setForeground(Theme.TEXT);
        if (roleLabel != null) roleLabel.setForeground(Theme.MUTED);
        if (searchPopup != null) {
            searchPopup.setVisible(false);
        }
        if (topSearch != null) {
            Ui.style(topSearch);
            topSearch.repaint();
        }
        items.values().forEach(NavItem::updateLook);

        try {
            content.removeAll();
            pages.clear();
            show(currentKey);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        content.revalidate();
        root.revalidate();
        revalidate();
        repaint();
    }

    private String currentKey = "dashboard";

    /** Navigates the current window to a page by key (used for cross-page links). */
    public static void navigate(String key) {
        if (current != null) {
            current.show(key);
        }
    }

    public static void navigate(String key, String query) {
        if (current != null) {
            current.navigateToSearch(key, query);
        }
    }

    private JPanel topBar(User user) {
        JPanel b = new JPanel(new BorderLayout());
        b.setBackground(Theme.SURFACE);
        b.setPreferredSize(new Dimension(10, 58));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 24, 0, 24)));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 17));
        brand.setOpaque(false);
        mark = Ui.label("CAMPUS PLACEMENTS", Theme.sansBold(14), Theme.TEXT);
        mark.setIcon(Icons.of(Glyph.DASHBOARD, 18, Theme.TEXT));
        mark.setIconTextGap(10);
        brand.add(mark);
        b.add(brand, BorderLayout.WEST);

        // Center Pill Search Bar (inspired by Figma)
        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        centerWrap.setOpaque(false);
        topSearch = Ui.search(user.role() == User.Role.OFFICER
                ? "Search students, companies, drives..."
                : "Search drives, companies, jobs...");
        topSearch.setPreferredSize(new Dimension(360, 34));

        searchDebounce = new Timer(220, e -> onSearchTextChanged());
        searchDebounce.setRepeats(false);

        topSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { scheduleSearch(); }
            public void removeUpdate(DocumentEvent e) { scheduleSearch(); }
            public void changedUpdate(DocumentEvent e) { scheduleSearch(); }
            private void scheduleSearch() {
                if (!isSyncingSearch) {
                    searchDebounce.restart();
                }
            }
        });

        topSearch.addActionListener(e -> executeSearch(topSearch.getText().trim()));
        centerWrap.add(topSearch);
        b.add(centerWrap, BorderLayout.CENTER);

        // Right Profile Avatar Pill & Dark Mode Toggle
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 11));
        right.setOpaque(false);

        String initial = user.displayName().isEmpty() ? "U" : user.displayName().substring(0, 1).toUpperCase();
        JPanel userPill = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = Theme.isDarkMode ? new Color(0x1B, 0x25, 0x36) : new Color(0xF1, 0xF5, 0xF9);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        userPill.setOpaque(false);
        JLabel userAvatar = new JLabel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(24, 24); }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.isDarkMode ? new Color(0x3B, 0x82, 0xF6) : Theme.PLUM);
                g2.fillOval(0, 0, 23, 23);
                g2.setColor(Color.WHITE);
                g2.setFont(Theme.sansBold(11));
                FontMetrics fm = g2.getFontMetrics();
                int sw = fm.stringWidth(initial);
                g2.drawString(initial, (24 - sw) / 2, 16);
                g2.dispose();
            }
        };
        userPill.add(userAvatar);
        name = Ui.label(user.displayName(), Theme.sansBold(12), Theme.TEXT);
        userPill.add(name);
        right.add(userPill);

        modeBtn = new Btn(Theme.isDarkMode ? "Light Mode" : "Dark Mode", Btn.Variant.SECONDARY);
        modeBtn.setPreferredSize(new Dimension(96, 32));
        modeBtn.addActionListener(e -> Theme.toggleDarkMode());
        right.add(modeBtn);

        b.add(right, BorderLayout.EAST);
        return b;
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
        this.currentKey = key;
        Page page = pages.get(key);
        if (page == null) {
            page = nav.factory().get();
            pages.put(key, page);
            content.add(page, key);
        }
        items.forEach((k, item) -> item.setActive(k.equals(key)));
        cards.show(content, key);
        page.refresh();

        if (searchPopup != null) {
            searchPopup.setVisible(false);
        }

        isSyncingSearch = true;
        if (page instanceof Searchable s) {
            topSearch.setText(s.getSearch());
        } else {
            topSearch.setText("");
        }
        isSyncingSearch = false;

        content.revalidate();
        content.repaint();
    }

    public void navigateToSearch(String key, String query) {
        show(key);
        Page page = pages.get(key);
        if (page instanceof Searchable s) {
            isSyncingSearch = true;
            topSearch.setText(query);
            isSyncingSearch = false;
            s.setSearch(query);
        }
    }

    private void onSearchTextChanged() {
        if (isSyncingSearch) return;
        String q = topSearch.getText().trim();
        Page current = pages.get(currentKey);
        if (current instanceof Searchable s) {
            s.setSearch(q);
            if (searchPopup != null) {
                searchPopup.setVisible(false);
            }
        } else {
            if (q.length() >= 2) {
                showGlobalSearchPopup(q);
            } else if (searchPopup != null) {
                searchPopup.setVisible(false);
            }
        }
    }

    private void executeSearch(String q) {
        if (searchPopup != null) {
            searchPopup.setVisible(false);
        }
        if (q.isEmpty()) {
            Page current = pages.get(currentKey);
            if (current instanceof Searchable s) {
                s.setSearch("");
            }
            return;
        }

        Page current = pages.get(currentKey);
        if (current instanceof Searchable s) {
            s.setSearch(q);
            return;
        }

        if (currentUser.role() == User.Role.OFFICER) {
            List<Company> companies = companyService.list(q);
            if (!companies.isEmpty()) {
                navigateToSearch("companies", q);
                return;
            }
            List<Student> students = studentService.list(q, null, null);
            if (!students.isEmpty()) {
                navigateToSearch("students", q);
                return;
            }
            List<Drive> drives = driveService.list(q, null, null);
            if (!drives.isEmpty()) {
                navigateToSearch("drives", q);
                return;
            }
            navigateToSearch("students", q);
        } else {
            navigateToSearch("drives", q);
        }
    }

    private void showGlobalSearchPopup(String q) {
        if (!topSearch.isShowing() || !topSearch.hasFocus()) {
            return;
        }
        if (searchPopup == null) {
            searchPopup = new JPopupMenu();
            searchPopup.setFocusable(false);
        }
        searchPopup.removeAll();
        searchPopup.setBackground(Theme.SURFACE);
        searchPopup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));

        boolean hasResults = false;
        boolean isOfficer = currentUser.role() == User.Role.OFFICER;

        if (isOfficer) {
            List<Student> students = studentService.list(q, null, null);
            if (!students.isEmpty()) {
                hasResults = true;
                addPopupHeader("STUDENTS (" + students.size() + ")");
                int limit = Math.min(students.size(), 3);
                for (int i = 0; i < limit; i++) {
                    Student s = students.get(i);
                    addPopupItem(s.fullName() + " (" + s.studentId() + ") • " + s.deptCode(),
                            Glyph.STUDENTS, () -> navigateToSearch("students", s.fullName()));
                }
            }

            List<Company> companies = companyService.list(q);
            if (!companies.isEmpty()) {
                hasResults = true;
                addPopupHeader("COMPANIES (" + companies.size() + ")");
                int limit = Math.min(companies.size(), 3);
                for (int i = 0; i < limit; i++) {
                    Company c = companies.get(i);
                    addPopupItem(c.name() + " • " + (c.industry() == null ? "Company" : c.industry()),
                            Glyph.COMPANIES, () -> navigateToSearch("companies", c.name()));
                }
            }

            List<Drive> drives = driveService.list(q, null, null);
            if (!drives.isEmpty()) {
                hasResults = true;
                addPopupHeader("PLACEMENT DRIVES (" + drives.size() + ")");
                int limit = Math.min(drives.size(), 3);
                for (int i = 0; i < limit; i++) {
                    Drive d = drives.get(i);
                    addPopupItem(d.companyName() + " — " + d.position(),
                            Glyph.DRIVES, () -> navigateToSearch("drives", d.companyName()));
                }
            }
        } else {
            List<Drive> drives = driveService.listForStudents(q);
            if (!drives.isEmpty()) {
                hasResults = true;
                addPopupHeader("AVAILABLE DRIVES (" + drives.size() + ")");
                int limit = Math.min(drives.size(), 4);
                for (int i = 0; i < limit; i++) {
                    Drive d = drives.get(i);
                    addPopupItem(d.companyName() + " — " + d.position(),
                            Glyph.DRIVES, () -> navigateToSearch("drives", d.companyName()));
                }
            }
        }

        if (!hasResults) {
            JLabel empty = new JLabel("  No matching records for \"" + q + "\"");
            empty.setFont(Theme.sans(12));
            empty.setForeground(Theme.MUTED);
            empty.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            searchPopup.add(empty);
        }

        searchPopup.pack();
        searchPopup.show(topSearch, 0, topSearch.getHeight() + 4);
        topSearch.requestFocusInWindow();
    }

    private void addPopupHeader(String text) {
        JLabel h = new JLabel(text);
        h.setFont(Theme.sansBold(10));
        h.setForeground(Theme.MUTED);
        h.setBorder(BorderFactory.createEmptyBorder(6, 8, 3, 8));
        searchPopup.add(h);
    }

    private void addPopupItem(String label, Glyph glyph, Runnable onSelect) {
        JMenuItem item = new JMenuItem(label);
        item.setFont(Theme.sans(12));
        item.setForeground(Theme.TEXT);
        item.setBackground(Theme.SURFACE);
        item.setIcon(Icons.of(glyph, 14, Theme.PLUM));
        item.setIconTextGap(8);
        item.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        item.addActionListener(e -> {
            if (searchPopup != null) {
                searchPopup.setVisible(false);
            }
            onSelect.run();
        });
        searchPopup.add(item);
    }

    @Override
    public void dispose() {
        if (themeListener != null) {
            Theme.removeListener(themeListener);
        }
        if (current == this) {
            current = null;
        }
        super.dispose();
    }

    private void logout() {
        if (Dialogs.confirm(this, "Sign out?", "You will return to the sign-in screen.", "Sign out")) {
            if (themeListener != null) {
                Theme.removeListener(themeListener);
            }
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
            Color c = active ? Theme.TEXT : (hover ? (Theme.isDarkMode ? Color.WHITE : new Color(0x1E, 0x29, 0x3B)) : Theme.MUTED);
            label.setForeground(c);
            label.setFont(active ? Theme.sansBold(13) : Theme.sans(13));
            Color iconColor = active ? Theme.TEXT : (hover ? (Theme.isDarkMode ? Color.WHITE : new Color(0x33, 0x41, 0x55)) : (Theme.isDarkMode ? new Color(0x64, 0x74, 0x8B) : new Color(0x94, 0xA3, 0xB8)));
            label.setIcon(Icons.of(glyph, 16, iconColor));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (active) {
                g2.setColor(Theme.isDarkMode ? new Color(0x22, 0x2E, 0x42) : new Color(0xF1, 0xF5, 0xF9));
                g2.fillRoundRect(0, 0, w, h, 6, 6);
                g2.setColor(Theme.isDarkMode ? new Color(0x93, 0xC5, 0xFD) : Theme.PLUM);
                g2.fillRoundRect(0, 6, 3, h - 12, 2, 2);
            } else if (hover) {
                g2.setColor(Theme.isDarkMode ? new Color(0x19, 0x23, 0x35) : new Color(0xF8, 0xFA, 0xFC));
                g2.fillRoundRect(0, 0, w, h, 6, 6);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
