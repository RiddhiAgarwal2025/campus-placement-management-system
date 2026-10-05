package com.campusplacement.ui.officer;

import com.campusplacement.model.Department;
import com.campusplacement.service.DepartmentService;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.FilterBar;
import com.campusplacement.ui.components.FormDialog;
import com.campusplacement.ui.components.HintField;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.KpiBanner;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.Searchable;
import com.campusplacement.ui.components.StatTile;
import com.campusplacement.ui.components.Ui;
import java.awt.BorderLayout;
import java.util.Comparator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DepartmentsPage extends Page implements Searchable {
    private final DepartmentService service = new DepartmentService();
    private final DataTable<Department> table = new DataTable<Department>("No departments match",
            "Add a department or change the search.")
            .col("Code", Department::code, 90)
            .col("Department", Department::name, 380)
            .col("Students", Department::studentCount, 90, Kind.NUMBER);
    private final StatTile kpiTotalDepts = new StatTile("Academic Departments", "Engineering & tech disciplines", false);
    private final StatTile kpiTotalStudents = new StatTile("Total Enrolled", "Students mapped to depts", true);
    private final StatTile kpiLargestDept = new StatTile("Largest Department", "Highest enrollment", false);
    private final HintField search;

    public DepartmentsPage() {
        super("Departments", "Academic departments that students belong to and drives can be restricted to.");
        Btn add = new Btn("Add department", Btn.Variant.PRIMARY, Glyph.PLUS);
        add.addActionListener(e -> edit(null));
        addAction(add);
        FilterBar bar = new FilterBar(this::refresh);
        search = bar.search("Search code or name");
        Btn edit = new Btn("Edit", Btn.Variant.SECONDARY, Glyph.EDIT);
        edit.addActionListener(e -> { if (need(table.selected(), "a department")) { edit(table.selected()); } });
        Btn del = new Btn("Delete", Btn.Variant.DANGER, Glyph.TRASH);
        del.addActionListener(e -> delete());

        KpiBanner kpiBanner = new KpiBanner(kpiTotalDepts, kpiTotalStudents, kpiLargestDept);

        JPanel cardContent = new JPanel(new BorderLayout(0, 12));
        cardContent.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(bar, BorderLayout.WEST);
        top.add(Ui.rightRow(edit, del), BorderLayout.EAST);
        table.onDoubleClick(this::edit);

        cardContent.add(top, BorderLayout.NORTH);
        cardContent.add(table, BorderLayout.CENTER);

        JPanel tableCard = Ui.card(cardContent, 18);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(kpiBanner, BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);
        setBody(body);
    }

    private void edit(Department d) {
        JTextField code = Ui.field(d == null ? "" : d.code());
        JTextField name = Ui.field(d == null ? "" : d.name());
        FormDialog f = new FormDialog(this, d == null ? "Add department" : "Edit department", "Codes are short and unique, e.g. CSE.")
                .field("Code", code).field("Name", name);
        if (f.open(d == null ? "Add department" : "Save changes", () -> {
            if (d == null) {
                service.create(code.getText(), name.getText());
            } else {
                service.update(d.deptId(), code.getText(), name.getText());
            }
        })) {
            refresh();
        }
    }

    private void delete() {
        Department d = table.selected();
        if (need(d, "a department") && confirmDelete(d.code()) && run(() -> service.delete(d))) {
            refresh();
        }
    }

    @Override
    public void refresh() {
        List<Department> list = load(() -> service.list(search.getText()), List.of());
        table.setRows(list);

        int totalDepts = list.size();
        long totalStudents = list.stream().mapToInt(Department::studentCount).sum();
        Department largest = list.stream().max(Comparator.comparingInt(Department::studentCount)).orElse(null);

        kpiTotalDepts.setValue(totalDepts);
        kpiTotalStudents.setValue(totalStudents);
        kpiLargestDept.setValue(largest != null ? largest.code() + " (" + largest.studentCount() + ")" : "—");
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
