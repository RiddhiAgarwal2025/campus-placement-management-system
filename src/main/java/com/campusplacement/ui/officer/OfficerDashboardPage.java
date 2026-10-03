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
import com.campusplacement.ui.components.PlacementFunnelPanel;
import com.campusplacement.ui.components.SegmentedControl;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.util.Formats;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class OfficerDashboardPage extends Page {
    private final DashboardService service = new DashboardService();
    private final PlacementFunnelPanel funnel = new PlacementFunnelPanel();

    private final DataTable<Drive> upcoming = new DataTable<Drive>("No upcoming drives", "Create a drive to see it here.")
            .col("Company", Drive::companyName, 140)
            .col("Position", Drive::position, 140)
            .col("Deadline", Drive::deadline, 90, Kind.DATE)
            .col("Due", d -> Formats.relative(d.deadline()), 80)
            .col("Applied", Drive::applicationCount, 55, Kind.NUMBER)
            .col("Status", Drive::status, 80, Kind.BADGE);

    private final DataTable<Application> recentApps = new DataTable<Application>("No applications yet",
            "Students apply from their own accounts.")
            .col("Applied", Application::appliedAt, 125, Kind.DATE)
            .col("Student ID", Application::studentId, 85)
            .col("Student", Application::studentName, 130)
            .col("Dept", Application::deptCode, 60)
            .col("Company", Application::companyName, 140)
            .col("Position", Application::position, 150)
            .col("Status", Application::status, 90, Kind.BADGE);

    private final DataTable<Offer> recentOffers = new DataTable<Offer>("No offers yet", "Offers appear after selection.")
            .col("Student", Offer::studentName, 115)
            .col("Company", Offer::companyName, 115)
            .col("Package", Offer::packageLpa, 85, Kind.MONEY)
            .col("Status", Offer::status, 85, Kind.BADGE);

    public OfficerDashboardPage() {
        super("Dashboard", "Placement season at a glance, read live from the database.");
        Btn refresh = new Btn("Refresh", Btn.Variant.SECONDARY, Glyph.REFRESH);
        refresh.addActionListener(e -> refresh());
        addAction(refresh);

        // Main Vertical Container
        JPanel main = Ui.vstack(16);
        main.setOpaque(false);

        // 1. Top Hero: Placement Funnel Component
        funnel.setPreferredSize(new Dimension(100, 150));
        main.add(funnel);

        // 2. Bottom 65 / 35 Split Container
        JPanel splitGrid = new JPanel(new GridBagLayout());
        splitGrid.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1.0;

        // --- Left 65% Primary Card: Upcoming Drives & Recent Submissions with SegmentedControl ---
        CardLayout cardLayout = new CardLayout();
        JPanel switchableTables = new JPanel(cardLayout);
        switchableTables.setOpaque(false);
        switchableTables.add(upcoming, "drives");
        switchableTables.add(recentApps, "apps");

        SegmentedControl seg = new SegmentedControl("Upcoming Drives", "Recent Applications");
        Btn viewAllBtn = new Btn("All drives \u2192", Btn.Variant.GHOST);
        viewAllBtn.addActionListener(e -> {
            if (seg.getSelectedIndex() == 0) {
                MainFrame.navigate("drives");
            } else {
                MainFrame.navigate("applications");
            }
        });

        seg.onSelect(idx -> {
            if (idx == 0) {
                cardLayout.show(switchableTables, "drives");
                viewAllBtn.setText("All drives \u2192");
            } else {
                cardLayout.show(switchableTables, "apps");
                viewAllBtn.setText("All applications \u2192");
            }
        });

        JPanel leftHead = new JPanel(new BorderLayout());
        leftHead.setOpaque(false);
        leftHead.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        leftHead.add(seg, BorderLayout.WEST);
        leftHead.add(viewAllBtn, BorderLayout.EAST);

        JPanel leftCardBody = new JPanel(new BorderLayout());
        leftCardBody.setOpaque(false);
        leftCardBody.add(leftHead, BorderLayout.NORTH);
        leftCardBody.add(switchableTables, BorderLayout.CENTER);
        JPanel leftCard = Ui.card(leftCardBody, 18);

        g.gridx = 0;
        g.weightx = 0.64;
        g.insets = new Insets(0, 0, 0, 10);
        splitGrid.add(leftCard, g);

        // --- Right 35% Activity Card: Recent Offers & Placements ---
        Btn allOffersBtn = new Btn("All offers \u2192", Btn.Variant.GHOST);
        allOffersBtn.addActionListener(e -> MainFrame.navigate("offers"));

        JPanel rightHead = new JPanel(new BorderLayout());
        rightHead.setOpaque(false);
        JPanel rightTitles = Ui.vstack(1);
        rightTitles.setOpaque(false);
        rightTitles.add(Ui.heading("Recent Offers"));
        rightTitles.add(Ui.muted("Latest compensation & acceptance"));
        rightHead.add(rightTitles, BorderLayout.WEST);
        rightHead.add(allOffersBtn, BorderLayout.EAST);
        rightHead.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        // Quick action shortcuts footer
        JPanel shortcuts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        shortcuts.setOpaque(false);
        shortcuts.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        Btn newDriveBtn = new Btn("+ New Drive", Btn.Variant.SECONDARY);
        newDriveBtn.addActionListener(e -> MainFrame.navigate("drives"));
        Btn reportsBtn = new Btn("Reports", Btn.Variant.SECONDARY);
        reportsBtn.addActionListener(e -> MainFrame.navigate("reports"));
        shortcuts.add(newDriveBtn);
        shortcuts.add(reportsBtn);

        JPanel rightCardBody = new JPanel(new BorderLayout());
        rightCardBody.setOpaque(false);
        rightCardBody.add(rightHead, BorderLayout.NORTH);
        rightCardBody.add(recentOffers, BorderLayout.CENTER);
        rightCardBody.add(shortcuts, BorderLayout.SOUTH);
        JPanel rightCard = Ui.card(rightCardBody, 18);

        g.gridx = 1;
        g.weightx = 0.36;
        g.insets = new Insets(0, 0, 0, 0);
        splitGrid.add(rightCard, g);

        main.add(splitGrid);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(main, BorderLayout.CENTER);
        setBody(wrap);
    }

    @Override
    public void refresh() {
        DashboardService.OfficerDashboard d = load(service::officer, null);
        if (d == null) {
            return;
        }

        int stud = d.stats().get("students").intValue();
        int comp = d.stats().get("companies").intValue();
        int act = d.stats().get("active_drives").intValue();
        int apps = d.stats().get("applications").intValue();
        int sh = d.stats().get("shortlisted").intValue();
        int off = d.stats().get("offers").intValue();
        int acc = d.stats().get("accepted").intValue();

        funnel.update(stud, comp, act, apps, sh, off, acc);
        upcoming.setRows(d.upcoming());
        recentOffers.setRows(d.recentOffers());
        recentApps.setRows(d.recentApplications());
    }
}
