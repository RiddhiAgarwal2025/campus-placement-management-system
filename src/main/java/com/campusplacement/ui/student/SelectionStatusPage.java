package com.campusplacement.ui.student;

import com.campusplacement.model.StudentRoundStatus;
import com.campusplacement.service.SelectionService;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.KpiBanner;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.StatTile;
import com.campusplacement.ui.components.Ui;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JPanel;

public class SelectionStatusPage extends Page {
    private final SelectionService service = new SelectionService();
    private final DataTable<StudentRoundStatus> table = new DataTable<StudentRoundStatus>("No selection rounds yet",
            "Rounds appear here once the placement office schedules them for drives you applied to.")
            .col("Company", StudentRoundStatus::companyName, 170)
            .col("Position", StudentRoundStatus::position, 170)
            .col("Round", StudentRoundStatus::sequenceNo, 60, Kind.NUMBER)
            .col("Round name", StudentRoundStatus::roundName, 170)
            .col("Date", StudentRoundStatus::roundDate, 110, Kind.DATE)
            .col("Round status", StudentRoundStatus::roundStatus, 120, Kind.BADGE)
            .col("Your result", r -> r.result() == null ? "AWAITING" : r.result(), 110, Kind.BADGE);
    private final StatTile kpiTotalRounds = new StatTile("Selection Rounds", "Scheduled stages", false);
    private final StatTile kpiPassed = new StatTile("Cleared Rounds", "Successfully passed", true);
    private final StatTile kpiAwaiting = new StatTile("Pending Results", "Under evaluation", false);

    public SelectionStatusPage() {
        super("Selection Status", "Round-by-round results for every drive you applied to.");

        KpiBanner kpiBanner = new KpiBanner(kpiTotalRounds, kpiPassed, kpiAwaiting);
        JPanel tableCard = Ui.card(table, 18);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(kpiBanner, BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);
        setBody(body);
    }

    @Override
    public void refresh() {
        List<StudentRoundStatus> rows = load(service::myRounds, List.of());
        table.setRows(rows);

        long passed = rows.stream().filter(r -> "PASS".equals(r.result())).count();
        long awaiting = rows.stream().filter(r -> r.result() == null || "AWAITING".equals(r.result())).count();

        kpiTotalRounds.setValue(rows.size());
        kpiPassed.setValue(passed);
        kpiAwaiting.setValue(awaiting);
    }
}

