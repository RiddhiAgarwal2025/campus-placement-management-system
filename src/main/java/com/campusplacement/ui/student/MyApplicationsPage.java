package com.campusplacement.ui.student;

import com.campusplacement.model.Application;
import com.campusplacement.service.ApplicationService;
import com.campusplacement.ui.MainFrame;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.FilterBar;
import com.campusplacement.ui.components.HintField;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.KpiBanner;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.Searchable;
import com.campusplacement.ui.components.StatTile;
import com.campusplacement.ui.components.Ui;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JPanel;

public class MyApplicationsPage extends Page implements Searchable {
    private final ApplicationService service = new ApplicationService();
    private final DataTable<Application> table = new DataTable<Application>("You have not applied yet",
            "Browse Placement Drives to find drives you are eligible for.")
            .col("Company", Application::companyName, 170)
            .col("Position", Application::position, 170)
            .col("Package", Application::packageLpa, 110, Kind.MONEY)
            .col("Drive date", Application::driveDate, 100, Kind.DATE)
            .col("Applied on", Application::appliedAt, 140, Kind.DATE)
            .col("Drive", Application::driveStatus, 100, Kind.BADGE)
            .col("Application status", Application::status, 130, Kind.BADGE);
    private final StatTile kpiTotal = new StatTile("Total Submitted", "Applications on file", false);
    private final StatTile kpiShortlisted = new StatTile("Shortlisted", "Invited to rounds", false);
    private final StatTile kpiSelected = new StatTile("Final Selections", "Passed all rounds", false);
    private final HintField search;

    public MyApplicationsPage() {
        super("My Applications", "Applications you have submitted and where each one stands.");
        FilterBar bar = new FilterBar(this::refresh);
        search = bar.search("Search company or position");

        Btn rounds = new Btn("Selection status", Btn.Variant.SECONDARY, Glyph.SELECTION);
        rounds.addActionListener(e -> MainFrame.navigate("selection"));
        Btn withdraw = new Btn("Withdraw", Btn.Variant.DANGER, Glyph.TRASH);
        withdraw.addActionListener(e -> {
            Application a = table.selected();
            if (need(a, "an application") && confirm("Withdraw application?", "Your application to " + a.companyName()
                    + " (" + a.position() + ") will be removed. You can apply again before the deadline.", "Withdraw")
                    && run(() -> service.withdraw(a.applicationId()))) {
                refresh();
            }
        });

        KpiBanner kpiBanner = new KpiBanner(kpiTotal, kpiShortlisted, kpiSelected);

        JPanel cardContent = new JPanel(new BorderLayout(0, 12));
        cardContent.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(bar, BorderLayout.WEST);
        top.add(Ui.rightRow(rounds, withdraw), BorderLayout.EAST);

        cardContent.add(top, BorderLayout.NORTH);
        cardContent.add(table, BorderLayout.CENTER);

        JPanel tableCard = Ui.card(cardContent, 18);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(kpiBanner, BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);
        setBody(body);
    }

    @Override
    public void refresh() {
        String q = search == null ? "" : search.getText().trim().toLowerCase();
        List<Application> all = load(service::mine, List.of());
        List<Application> rows = q.isEmpty() ? all : all.stream().filter(a ->
                (a.companyName() != null && a.companyName().toLowerCase().contains(q)) ||
                (a.position() != null && a.position().toLowerCase().contains(q)) ||
                (a.status() != null && a.status().toLowerCase().contains(q))).toList();
        table.setRows(rows);
        Map<String, Long> c = rows.stream().collect(Collectors.groupingBy(Application::status, Collectors.counting()));

        kpiTotal.setValue(rows.size());
        kpiShortlisted.setValue(c.getOrDefault("SHORTLISTED", 0L));
        kpiSelected.setValue(c.getOrDefault("SELECTED", 0L));

        setSubtitle(rows.size() + " application(s) submitted. Monitor your evaluation progress across selection rounds.");
    }

    @Override
    public void setSearch(String text) {
        if (search != null) {
            search.setText(text == null ? "" : text);
            refresh();
        }
    }

    @Override
    public String getSearch() {
        return search == null ? "" : search.getText();
    }
}
