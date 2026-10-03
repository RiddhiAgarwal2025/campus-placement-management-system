package com.campusplacement.ui.officer;

import com.campusplacement.model.Drive;
import com.campusplacement.model.EligibilityCriteria;
import com.campusplacement.model.EligibilityResult;
import com.campusplacement.service.DriveService;
import com.campusplacement.service.EligibilityService;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.StatTile;
import com.campusplacement.ui.components.Ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Select drive → view criteria → check eligibility → list students with reasons. */
public class EligibilityPage extends Page {
    private static Integer preselected;

    private final DriveService drives = new DriveService();
    private final EligibilityService engine = new EligibilityService();
    private final JComboBox<Drive> drive = Ui.combo(List.of());
    private final JComboBox<String> show = Ui.combo(List.of("All students", "Eligible only", "Not eligible only"));
    private final JLabel criteria = Ui.body("Select a drive to see its criteria.");
    private final StatTile eligible = new StatTile("Eligible", "meet every criterion", true);
    private final StatTile notEligible = new StatTile("Not eligible", "fail at least one criterion", false);
    private final StatTile applied = new StatTile("Applications", "already submitted", false);
    private List<EligibilityResult> results = List.of();
    private final DataTable<EligibilityResult> table = new DataTable<EligibilityResult>("No results yet",
            "Choose a drive and select Check eligibility.")
            .col("Student ID", r -> r.student().studentId(), 100)
            .col("Name", r -> r.student().fullName(), 160)
            .col("Dept", r -> r.student().deptCode(), 60)
            .col("CGPA", r -> r.student().cgpa(), 60, Kind.NUMBER)
            .col("Backlogs", r -> r.student().backlogs(), 70, Kind.NUMBER)
            .col("Batch", r -> r.student().graduationYear(), 60, Kind.NUMBER)
            .col("Result", r -> r.eligible() ? "ELIGIBLE" : "NOT ELIGIBLE", 120, Kind.BADGE)
            .col("Reasons", EligibilityResult::reasonText, 420);

    public static void preselect(int driveId) { preselected = driveId; }

    public EligibilityPage() {
        super("Eligibility", "Evaluate every student against a drive's criteria using the shared eligibility engine.");
        drive.setPreferredSize(new Dimension(380, 34));
        drive.addActionListener(e -> showCriteria());
        Btn check = new Btn("Check eligibility", Btn.Variant.PRIMARY, Glyph.ELIGIBILITY);
        check.addActionListener(e -> check());
        show.addActionListener(e -> filter());
        JPanel pick = Ui.row(Ui.fieldLabel("Drive"), drive, check);
        JPanel criteriaCard = Ui.section("Criteria", null, criteria);
        JPanel tiles = new JPanel(new GridLayout(1, 3, 12, 0));
        tiles.setOpaque(false);
        tiles.add(eligible);
        tiles.add(notEligible);
        tiles.add(applied);
        tiles.setPreferredSize(new Dimension(420, 104));
        JPanel summary = new JPanel(new BorderLayout(16, 0));
        summary.setOpaque(false);
        summary.add(criteriaCard, BorderLayout.CENTER);
        summary.add(tiles, BorderLayout.EAST);
        JPanel north = new JPanel(new BorderLayout(0, 14));
        north.setOpaque(false);
        north.add(pick, BorderLayout.NORTH);
        north.add(summary, BorderLayout.CENTER);
        JPanel filterRow = new JPanel(new BorderLayout());
        filterRow.setOpaque(false);
        filterRow.add(Ui.row(Ui.fieldLabel("Show"), show), BorderLayout.WEST);
        north.add(filterRow, BorderLayout.SOUTH);
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(north, BorderLayout.NORTH);
        body.add(table, BorderLayout.CENTER);
        setBody(body);
    }

    private void showCriteria() {
        Drive d = (Drive) drive.getSelectedItem();
        results = List.of();
        table.setRows(List.of());
        eligible.setValue(null);
        notEligible.setValue(null);
        if (d == null) {
            criteria.setText("Select a drive to see its criteria.");
            applied.setValue(null);
            return;
        }
        applied.setValue(d.applicationCount());
        EligibilityCriteria cr = load(() -> engine.criteria(d.driveId()), null);
        criteria.setText(cr == null ? "No criteria defined." : "<html><div style='width:460px'>"
                + Ui.escape(d.title() + " (" + d.status() + ")") + "<br><br>" + Ui.escape(cr.summary()) + "</div></html>");
    }

    private void check() {
        Drive d = (Drive) drive.getSelectedItem();
        if (!need(d, "a drive")) {
            return;
        }
        results = load(() -> engine.checkAll(d.driveId()), List.of());
        long ok = results.stream().filter(EligibilityResult::eligible).count();
        eligible.setValue(ok);
        notEligible.setValue(results.size() - ok);
        filter();
    }

    private void filter() {
        int mode = show.getSelectedIndex();
        table.setRows(results.stream().filter(r -> mode == 0 || (mode == 1) == r.eligible()).toList());
    }

    @Override
    public void refresh() {
        Drive keep = (Drive) drive.getSelectedItem();
        Integer want = preselected != null ? preselected : keep == null ? null : keep.driveId();
        preselected = null;
        List<Drive> all = load(() -> drives.list("", null, null), List.of());
        drive.removeAllItems();
        all.forEach(drive::addItem);
        if (want != null) {
            all.stream().filter(x -> x.driveId() == want).findFirst().ifPresent(drive::setSelectedItem);
        }
        showCriteria();
        if (want != null && drive.getSelectedItem() != null) {
            check();
        }
    }
}
