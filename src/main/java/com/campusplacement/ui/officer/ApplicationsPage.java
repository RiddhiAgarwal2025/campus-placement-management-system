package com.campusplacement.ui.officer;

import com.campusplacement.model.Application;
import com.campusplacement.model.Company;
import com.campusplacement.model.Department;
import com.campusplacement.model.Drive;
import com.campusplacement.service.ApplicationService;
import com.campusplacement.service.CompanyService;
import com.campusplacement.service.DepartmentService;
import com.campusplacement.service.DriveService;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.FilterBar;
import com.campusplacement.ui.components.HintField;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.KpiBanner;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.StatTile;
import com.campusplacement.ui.components.Ui;
import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class ApplicationsPage extends Page {
    private final ApplicationService service = new ApplicationService();
    private final DataTable<Application> table = new DataTable<Application>("No applications match",
            "Adjust the filters, or wait for students to apply.")
            .col("Applied", Application::appliedAt, 140, Kind.DATE)
            .col("Student ID", Application::studentId, 90)
            .col("Student", Application::studentName, 150)
            .col("Dept", Application::deptCode, 55)
            .col("CGPA", Application::cgpa, 55, Kind.NUMBER)
            .col("Drive", Application::driveId, 50, Kind.NUMBER)
            .col("Company", Application::companyName, 160)
            .col("Position", Application::position, 160)
            .col("Status", Application::status, 110, Kind.BADGE)
            .multiSelect();
    private final StatTile kpiTotal = new StatTile("Applications", "Total submitted", false);
    private final StatTile kpiShort = new StatTile("Shortlisted", "In active evaluation", false);
    private final StatTile kpiSelect = new StatTile("Selected", "Cleared selection", true);
    private final StatTile kpiReject = new StatTile("Rejected", "Archived files", false);
    private final HintField search;
    private final JComboBox<Drive> drive;
    private final JComboBox<Company> company;
    private final JComboBox<Department> dept;
    private final JComboBox<String> status;
    private boolean loading;

    public ApplicationsPage() {
        super("Applications", "Every application with filters by drive, company, department and status.");
        FilterBar bar = new FilterBar(() -> { if (!loading) { reloadTable(); } });
        search = bar.search("Search student, company, position");
        drive = bar.combo(Ui.filterCombo("All drives", List.of()));
        company = bar.combo(Ui.filterCombo("All companies", List.of()));
        dept = bar.combo(Ui.filterCombo("All departments", List.of()));
        status = bar.combo(Ui.filterCombo("All statuses",
                Arrays.stream(Application.Status.values()).map(Enum::name).toList()));
        drive.setPreferredSize(new java.awt.Dimension(220, 34));
        Btn shortlist = new Btn("Shortlist", Btn.Variant.SECONDARY);
        shortlist.addActionListener(e -> change("SHORTLISTED"));
        Btn select = new Btn("Select", Btn.Variant.SECONDARY, Glyph.CHECK);
        select.addActionListener(e -> change("SELECTED"));
        Btn reject = new Btn("Reject", Btn.Variant.DANGER);
        reject.addActionListener(e -> change("REJECTED"));
        Btn record = new Btn("Student record", Btn.Variant.SECONDARY, Glyph.EYE);
        record.addActionListener(e -> {
            Application a = table.selected();
            if (need(a, "an application")) {
                new StudentDetailDialog(this, a.studentId()).setVisible(true);
            }
        });
        Btn export = new Btn("Export CSV", Btn.Variant.SECONDARY, Glyph.EXPORT);
        export.addActionListener(e -> ReportsPage.exportCsv(this, table.model(), "applications.csv"));
        addAction(export);
        table.onDoubleClick(a -> new StudentDetailDialog(this, a.studentId()).setVisible(true));

        // Top KPI Banner
        KpiBanner kpiBanner = new KpiBanner(kpiTotal, kpiShort, kpiSelect, kpiReject);

        // Elevated Main Table Card
        JPanel cardContent = new JPanel(new BorderLayout(0, 12));
        cardContent.setOpaque(false);

        JPanel topToolbar = new JPanel(new BorderLayout(0, 10));
        topToolbar.setOpaque(false);
        topToolbar.add(bar, BorderLayout.NORTH);

        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.add(Ui.muted("Select one or more rows (Ctrl/Shift-click) to change status. Round results update statuses automatically."),
                BorderLayout.WEST);
        actions.add(Ui.rightRow(record, shortlist, select, reject), BorderLayout.EAST);
        topToolbar.add(actions, BorderLayout.SOUTH);

        cardContent.add(topToolbar, BorderLayout.NORTH);
        cardContent.add(table, BorderLayout.CENTER);

        JPanel tableCard = Ui.card(cardContent, 18);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(kpiBanner, BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);
        setBody(body);
    }

    private void change(String newStatus) {
        List<Application> sel = table.selectedRows();
        if (!need(sel.isEmpty() ? null : sel, "one or more applications")) {
            return;
        }
        if (!confirm("Mark " + sel.size() + " application(s) " + newStatus + "?",
                sel.stream().map(a -> a.studentName() + " — " + a.companyName()).collect(Collectors.joining("\n")),
                "Mark " + newStatus.toLowerCase())) {
            return;
        }
        for (Application a : sel) {
            if (!run(() -> service.changeStatus(a.applicationId(), newStatus))) {
                break;
            }
        }
        reloadTable();
    }

    @Override
    public void refresh() {
        loading = true;
        refill(drive, load(() -> new DriveService().list("", null, null), List.of()), Drive::driveId);
        refill(company, load(() -> new CompanyService().list(""), List.of()), Company::companyId);
        refill(dept, load(() -> new DepartmentService().list(""), List.of()), Department::deptId);
        loading = false;
        reloadTable();
    }

    private static <T> void refill(JComboBox<T> combo, List<T> items, Function<T, Integer> id) {
        @SuppressWarnings("unchecked")
        T sel = (T) combo.getSelectedItem();
        combo.removeAllItems();
        combo.addItem(null);
        items.forEach(combo::addItem);
        if (sel != null) {
            items.stream().filter(x -> id.apply(x).equals(id.apply(sel))).findFirst().ifPresent(combo::setSelectedItem);
        }
    }

    private void reloadTable() {
        Drive d = (Drive) drive.getSelectedItem();
        Company c = (Company) company.getSelectedItem();
        Department dp = (Department) dept.getSelectedItem();
        List<Application> rows = load(() -> service.list(search.getText(), d == null ? null : d.driveId(),
                c == null ? null : c.companyId(), dp == null ? null : dp.deptId(), (String) status.getSelectedItem()), List.of());
        table.setRows(rows);
        Map<String, Long> counts = rows.stream().collect(Collectors.groupingBy(Application::status, Collectors.counting()));
        kpiTotal.setValue(rows.size());
        kpiShort.setValue(counts.getOrDefault("SHORTLISTED", 0L));
        kpiSelect.setValue(counts.getOrDefault("SELECTED", 0L));
        kpiReject.setValue(counts.getOrDefault("REJECTED", 0L));
        setSubtitle("Showing " + rows.size() + " active candidate records filtered across drives.");
    }
}
