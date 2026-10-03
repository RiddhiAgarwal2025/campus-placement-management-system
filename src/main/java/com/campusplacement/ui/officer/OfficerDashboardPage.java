package com.campusplacement.ui.officer;

import com.campusplacement.model.Application;
import com.campusplacement.model.Drive;
import com.campusplacement.model.Offer;
import com.campusplacement.service.DashboardService;
import com.campusplacement.ui.MainFrame;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.StatTile;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.util.Formats;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.JPanel;

public class OfficerDashboardPage extends Page {
    private final DashboardService service = new DashboardService();
    private final StatTile students = new StatTile("Students", "registered", false);
    private final StatTile companies = new StatTile("Companies", "recruiting partners", false);
    private final StatTile active = new StatTile("Active drives", "currently open", true);
    private final StatTile applications = new StatTile("Applications", "submitted", false);
    private final StatTile shortlisted = new StatTile("Shortlisted", "in process", false);
    private final StatTile offers = new StatTile("Offers issued", "all statuses", false);
    private final StatTile accepted = new StatTile("Accepted", "placed students", false);

    private final DataTable<Drive> upcoming = new DataTable<Drive>("No upcoming drives", "Create a drive to see it here.")
            .col("Company", Drive::companyName, 120)
            .col("Position", Drive::position, 120)
            .col("Deadline", d -> d.deadline(), 80, Kind.DATE)
            .col("Due", d -> Formats.relative(d.deadline()), 75)
            .col("Applied", Drive::applicationCount, 55, Kind.NUMBER)
            .col("Status", Drive::status, 80, Kind.BADGE);
    private final DataTable<Offer> recentOffers = new DataTable<Offer>("No offers yet", "Offers appear after selection.")
            .col("Student", Offer::studentName, 115)
            .col("Company", Offer::companyName, 115)
            .col("Package", Offer::packageLpa, 85, Kind.MONEY)
            .col("Status", Offer::status, 80, Kind.BADGE);
    private final DataTable<Application> recentApps = new DataTable<Application>("No applications yet",
            "Students apply from their own accounts.")
            .col("Applied", Application::appliedAt, 140, Kind.DATE)
            .col("Student", Application::studentName, 150)
            .col("Dept", Application::deptCode, 60)
            .col("Company", Application::companyName, 160)
            .col("Position", Application::position, 160)
            .col("Status", Application::status, 100, Kind.BADGE);

    public OfficerDashboardPage() {
        super("Dashboard", "Placement season at a glance, read live from the database.");
        Btn refresh = new Btn("Refresh", Btn.Variant.SECONDARY, Glyph.REFRESH);
        refresh.addActionListener(e -> refresh());
        addAction(refresh);

        JPanel tiles = new JPanel(new GridLayout(1, 0, 12, 0));
        tiles.setOpaque(false);
        for (StatTile t : new StatTile[] {students, companies, active, applications, shortlisted, offers, accepted}) {
            tiles.add(t);
        }

        Btn allDrives = new Btn("All drives", Btn.Variant.GHOST);
        allDrives.addActionListener(e -> MainFrame.navigate("drives"));
        Btn allOffers = new Btn("All offers", Btn.Variant.GHOST);
        allOffers.addActionListener(e -> MainFrame.navigate("offers"));
        Btn allApps = new Btn("All applications", Btn.Variant.GHOST);
        allApps.addActionListener(e -> MainFrame.navigate("applications"));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 16, 0);
        tiles.setPreferredSize(new Dimension(100, 112));
        grid.add(tiles, g);
        g.gridy = 1;
        g.gridwidth = 1;
        g.weightx = 0.58;
        g.weighty = 0.5;
        g.insets = new Insets(0, 0, 16, 8);
        grid.add(Ui.section("Upcoming drives", "Open and upcoming, nearest deadline first", upcoming, allDrives), g);
        g.gridx = 1;
        g.weightx = 0.42;
        g.insets = new Insets(0, 8, 16, 0);
        grid.add(Ui.section("Recent offers", "Latest offers and responses", recentOffers, allOffers), g);
        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 2;
        g.weighty = 0.5;
        g.insets = new Insets(0, 0, 0, 0);
        grid.add(Ui.section("Recent applications", "Most recent submissions across all drives", recentApps, allApps), g);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(grid);
        setBody(wrap);
    }

    @Override
    public void refresh() {
        DashboardService.OfficerDashboard d = load(service::officer, null);
        if (d == null) {
            return;
        }
        students.setValue(d.stats().get("students"));
        companies.setValue(d.stats().get("companies"));
        active.setValue(d.stats().get("active_drives"));
        applications.setValue(d.stats().get("applications"));
        shortlisted.setValue(d.stats().get("shortlisted"));
        offers.setValue(d.stats().get("offers"));
        accepted.setValue(d.stats().get("accepted"));
        upcoming.setRows(d.upcoming());
        recentOffers.setRows(d.recentOffers());
        recentApps.setRows(d.recentApplications());
    }
}
