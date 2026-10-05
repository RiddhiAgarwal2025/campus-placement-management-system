package com.campusplacement.ui.officer;

import com.campusplacement.service.DashboardService;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DepartmentBarChartCard;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.OfferVelocityLineChartCard;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.StatusDonutChartCard;
import com.campusplacement.ui.components.TopCompaniesListCard;
import com.campusplacement.ui.components.Ui;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JPanel;

/**
 * 4-Quadrant Visual Executive Dashboard directly inspired by Dwinawan's Figma design:
 * - Top-Left: Highlights KPI & Engineering Department Bar Chart
 * - Top-Right: Pipeline Conversion Status Donut Chart
 * - Bottom-Left: Top Recruiting Partners & Active Drives List
 * - Bottom-Right: Submission & Offer Velocity Multi-Line Trend Chart
 */
public class OfficerDashboardPage extends Page {
    private final DashboardService service = new DashboardService();
    private final DepartmentBarChartCard barChart = new DepartmentBarChartCard();
    private final StatusDonutChartCard donutChart = new StatusDonutChartCard();
    private final TopCompaniesListCard companiesCard = new TopCompaniesListCard();
    private final OfferVelocityLineChartCard lineChart = new OfferVelocityLineChartCard();

    public OfficerDashboardPage() {
        super("Dashboard", "Placement season at a glance, read live from the database.");
        Btn refresh = new Btn("Refresh", Btn.Variant.SECONDARY, Glyph.REFRESH);
        refresh.addActionListener(e -> refresh());
        addAction(refresh);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 0.5;

        // --- Row 0: Bar Chart (Top-Left 58%) & Donut Chart (Top-Right 42%) ---
        g.gridy = 0;

        g.gridx = 0;
        g.weightx = 0.58;
        g.insets = new Insets(0, 0, 14, 12);
        grid.add(barChart, g);

        g.gridx = 1;
        g.weightx = 0.42;
        g.insets = new Insets(0, 0, 14, 0);
        grid.add(donutChart, g);

        // --- Row 1: Top Companies (Bottom-Left 52%) & Line Chart (Bottom-Right 48%) ---
        g.gridy = 1;

        g.gridx = 0;
        g.weightx = 0.52;
        g.insets = new Insets(0, 0, 0, 12);
        grid.add(companiesCard, g);

        g.gridx = 1;
        g.weightx = 0.48;
        g.insets = new Insets(0, 0, 0, 0);
        grid.add(lineChart, g);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(grid, BorderLayout.CENTER);
        setBody(wrap);
    }

    @Override
    public void refresh() {
        DashboardService.OfficerDashboard d = load(service::officer, null);
        if (d == null) {
            return;
        }

        int stud = d.stats().getInt("students");
        int comp = d.stats().getInt("companies");
        int act = d.stats().getInt("active_drives");
        int apps = d.stats().getInt("applications");
        int sh = d.stats().getInt("shortlisted");
        int off = d.stats().getInt("offers");
        int acc = d.stats().getInt("accepted");

        Number maxPkg = d.stats().get("max_package");
        String maxPkgStr = maxPkg.doubleValue() > 0 ? ("\u20B9 " + maxPkg + " LPA") : "\u20B9 0.00 LPA";

        barChart.updateData(maxPkgStr, acc, d.deptPlacements());
        donutChart.updateData(apps, sh, off, acc);
        companiesCard.updateDrives(d.upcoming());
        lineChart.updateData(apps, off);
    }
}
