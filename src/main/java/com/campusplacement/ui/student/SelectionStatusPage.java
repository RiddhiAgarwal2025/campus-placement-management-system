package com.campusplacement.ui.student;

import com.campusplacement.model.StudentRoundStatus;
import com.campusplacement.service.SelectionService;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.Page;
import java.util.List;

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

    public SelectionStatusPage() {
        super("Selection Status", "Round-by-round results for every drive you applied to.");
        setBody(table);
    }

    @Override
    public void refresh() {
        table.setRows(load(service::myRounds, List.of()));
    }
}
