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
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.Ui;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DepartmentsPage extends Page {
    private final DepartmentService service = new DepartmentService();
    private final DataTable<Department> table = new DataTable<Department>("No departments match",
            "Add a department or change the search.")
            .col("Code", Department::code, 90)
            .col("Department", Department::name, 380)
            .col("Students", Department::studentCount, 90, Kind.NUMBER);
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
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(bar, BorderLayout.WEST);
        top.add(Ui.rightRow(edit, del), BorderLayout.EAST);
        table.onDoubleClick(this::edit);
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);
        body.add(table, BorderLayout.CENTER);
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
        table.setRows(load(() -> service.list(search.getText()), List.of()));
    }
}
