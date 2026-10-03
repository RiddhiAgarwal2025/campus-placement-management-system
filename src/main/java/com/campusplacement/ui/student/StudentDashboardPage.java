package com.campusplacement.ui.student;

import com.campusplacement.model.Application;
import com.campusplacement.model.Drive;
import com.campusplacement.model.EligibilityResult;
import com.campusplacement.model.Offer;
import com.campusplacement.service.DashboardService;
import com.campusplacement.service.EligibilityService;
import com.campusplacement.service.ServiceException;
import com.campusplacement.service.Session;
import com.campusplacement.ui.MainFrame;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.StatTile;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.util.Formats;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StudentDashboardPage extends Page {
    private final DashboardService service = new DashboardService();
    private final EligibilityService eligibility = new EligibilityService();
    private final StatTile apps = new StatTile("Applications", "submitted", false);
    private final StatTile shortlisted = new StatTile("Shortlisted", "in selection", false);
    private final StatTile offers = new StatTile("Offers", "received", false);
    private final StatTile pending = new StatTile("Awaiting your reply", "pending offers", true);
    private final StatTile open = new StatTile("Open drives", "accepting applications", false);
    private final JPanel todo = Ui.vstack(0);
    private final Map<Integer, String> eligibleMap = new HashMap<>();
    private Set<Integer> appliedDrives = Set.of();
    private final DataTable<Drive> deadlines = new DataTable<Drive>("No upcoming deadlines", "New drives will appear here.")
            .col("Company", Drive::companyName, 150)
            .col("Position", Drive::position, 150)
            .col("Deadline", Drive::deadline, 100, Kind.DATE)
            .col("Due", d -> Formats.relative(d.deadline()), 80)
            .col("You", d -> appliedDrives.contains(d.driveId()) ? "APPLIED" : eligibleMap.getOrDefault(d.driveId(), ""), 110, Kind.BADGE);
    private final DataTable<Application> myApps = new DataTable<Application>("No applications yet",
            "Open Placement Drives to find drives you are eligible for.")
            .col("Company", Application::companyName, 150)
            .col("Position", Application::position, 150)
            .col("Applied", Application::appliedAt, 130, Kind.DATE)
            .col("Status", Application::status, 100, Kind.BADGE);

    public StudentDashboardPage() {
        super("Dashboard", "");
        Btn refresh = new Btn("Refresh", Btn.Variant.SECONDARY, Glyph.REFRESH);
        refresh.addActionListener(e -> refresh());
        addAction(refresh);
        JPanel tiles = new JPanel(new GridLayout(1, 0, 12, 0));
        tiles.setOpaque(false);
        for (StatTile t : new StatTile[] {apps, shortlisted, offers, pending, open}) {
            tiles.add(t);
        }
        tiles.setPreferredSize(new Dimension(100, 112));
        todo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 16, 0);
        grid.add(tiles, g);
        g.gridy = 1;
        g.gridwidth = 1;
        g.weightx = 0.42;
        g.weighty = 1;
        g.insets = new Insets(0, 0, 0, 8);
        grid.add(Ui.section("What to do next", "Actions that need your attention", Ui.scroll(wrap(todo))), g);
        g.gridx = 1;
        g.weightx = 0.58;
        g.insets = new Insets(0, 8, 0, 0);
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 16));
        right.setOpaque(false);
        Btn drives = new Btn("All drives", Btn.Variant.GHOST);
        drives.addActionListener(e -> MainFrame.navigate("drives"));
        Btn all = new Btn("All applications", Btn.Variant.GHOST);
        all.addActionListener(e -> MainFrame.navigate("applications"));
        right.add(Ui.section("Important deadlines", "Open and upcoming drives", deadlines, drives));
        right.add(Ui.section("Application status", "Your applications, newest first", myApps, all));
        grid.add(right, g);
        setBody(grid);
    }

    private static JPanel wrap(JPanel p) {
        JPanel w = new JPanel(new BorderLayout());
        w.setBackground(Theme.SURFACE);
        w.add(p, BorderLayout.NORTH);
        return w;
    }

    @Override
    public void refresh() {
        int h = LocalTime.now().getHour();
        String greet = h < 12 ? "Good morning" : h < 17 ? "Good afternoon" : "Good evening";
        setSubtitle(greet + ", " + Session.user().displayName().split(" ")[0] + ". Here is where your placement stands today.");
        DashboardService.StudentDashboard d = load(service::student, null);
        if (d == null) {
            return;
        }
        apps.setValue(d.stats().get("applications"));
        shortlisted.setValue(d.stats().get("shortlisted"));
        offers.setValue(d.stats().get("offers"));
        pending.setValue(d.stats().get("pending_offers"));
        open.setValue(d.stats().get("open_drives"));
        appliedDrives = d.applications().stream().map(Application::driveId).collect(Collectors.toSet());
        eligibleMap.clear();
        String sid = Session.studentId();
        List<String[]> items = new ArrayList<>();
        for (Offer o : d.offers()) {
            if ("PENDING".equals(o.status())) {
                items.add(new String[] {"Respond to your offer", o.companyName() + ", " + o.position() + " at "
                        + Formats.lpa(o.packageLpa()) + ". Accept or reject it from My Offers.", "offers"});
            }
        }
        for (Drive dr : d.openDrives()) {
            try {
                EligibilityResult r = eligibility.check(sid, dr.driveId());
                eligibleMap.put(dr.driveId(), r.eligible() ? "ELIGIBLE" : "NOT ELIGIBLE");
                if (r.eligible() && dr.acceptingApplications() && !appliedDrives.contains(dr.driveId())) {
                    items.add(new String[] {"Apply to " + dr.companyName(), dr.position() + ". Deadline "
                            + Formats.date(dr.deadline()) + " (" + Formats.relative(dr.deadline()) + ").", "drives"});
                }
            } catch (ServiceException ignored) {
                eligibleMap.put(dr.driveId(), "");
            }
        }
        for (Application a : d.applications()) {
            if ("SHORTLISTED".equals(a.status())) {
                items.add(new String[] {"Prepare for the next round", a.companyName() + ", " + a.position()
                        + ". Check Selection Status for round details.", "selection"});
            }
        }
        todo.removeAll();
        if (items.isEmpty()) {
            todo.add(Ui.muted("<html><div style='width:300px'>Nothing needs your attention right now. New drives you are "
                    + "eligible for will be listed here.</div></html>"));
        }
        for (String[] it : items) {
            todo.add(todoItem(it[0], it[1], it[2]));
        }
        todo.revalidate();
        todo.repaint();
        deadlines.setRows(d.openDrives());
        myApps.setRows(d.applications());
    }

    private JPanel todoItem(String head, String text, String target) {
        JPanel p = new JPanel(new BorderLayout(10, 2));
        p.setBackground(Theme.SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)));
        p.setAlignmentX(LEFT_ALIGNMENT);
        JPanel t = Ui.vstack(0);
        t.add(Ui.label(head, Theme.sansBold(13), Theme.TEXT));
        JLabel l = Ui.muted("<html><div style='width:280px'>" + Ui.escape(text) + "</div></html>");
        t.add(l);
        p.add(t, BorderLayout.CENTER);
        Btn go = new Btn("Open", Btn.Variant.GHOST);
        go.addActionListener(e -> MainFrame.navigate(target));
        JPanel bw = new JPanel(new BorderLayout());
        bw.setOpaque(false);
        bw.add(go, BorderLayout.NORTH);
        p.add(bw, BorderLayout.EAST);
        return p;
    }
}
