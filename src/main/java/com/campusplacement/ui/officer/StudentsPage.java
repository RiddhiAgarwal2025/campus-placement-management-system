package com.campusplacement.ui.officer;

import com.campusplacement.model.Department;
import com.campusplacement.model.Student;
import com.campusplacement.service.DepartmentService;
import com.campusplacement.service.StudentService;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StudentsPage extends Page implements Searchable {
    private final StudentService service = new StudentService();
    private final DepartmentService departments = new DepartmentService();
    private final DataTable<Student> table = new DataTable<Student>("No students match",
            "Add a student or adjust the search and filters.")
            .col("Student ID", Student::studentId, 100)
            .col("Name", Student::fullName, 170)
            .col("Dept", Student::deptCode, 60)
            .col("Batch", Student::graduationYear, 60, Kind.NUMBER)
            .col("CGPA", Student::cgpa, 60, Kind.NUMBER)
            .col("Backlogs", Student::backlogs, 70, Kind.NUMBER)
            .col("Email", Student::email, 230)
            .col("Phone", Student::phone, 110);
    private final StatTile kpiTotal = new StatTile("Registered Students", "Active student profiles", false);
    private final StatTile kpiEligible = new StatTile("Zero Backlogs", "Immediate drive eligibility", false);
    private final StatTile kpiAvgCgpa = new StatTile("Average CGPA", "Batch academic standing", false);
    private final StatTile kpiDepts = new StatTile("Departments", "Academic disciplines", false);
    private final HintField search;
    private final JComboBox<Department> dept;
    private final JComboBox<Integer> year;
    private boolean loadingFilters;

    public StudentsPage() {
        super("Students", "Student records, academic history and skills. Double-click a row for the full record.");
        Btn add = new Btn("Add student", Btn.Variant.PRIMARY, Glyph.PLUS);
        add.addActionListener(e -> edit(null));
        addAction(add);
        FilterBar bar = new FilterBar(() -> { if (!loadingFilters) { reloadTable(); } });
        search = bar.search("Search ID, name or email");
        dept = bar.combo(Ui.filterCombo("All departments", List.of()));
        year = bar.combo(Ui.filterCombo("All batches", List.of()));
        Btn details = new Btn("Details", Btn.Variant.SECONDARY, Glyph.EYE);
        details.addActionListener(e -> { if (need(table.selected(), "a student")) { details(table.selected()); } });
        Btn edit = new Btn("Edit", Btn.Variant.SECONDARY, Glyph.EDIT);
        edit.addActionListener(e -> { if (need(table.selected(), "a student")) { edit(table.selected()); } });
        Btn del = new Btn("Delete", Btn.Variant.DANGER, Glyph.TRASH);
        del.addActionListener(e -> delete());
        table.onDoubleClick(this::details);

        KpiBanner kpiBanner = new KpiBanner(kpiTotal, kpiEligible, kpiAvgCgpa, kpiDepts);

        JPanel cardContent = new JPanel(new BorderLayout(0, 12));
        cardContent.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(bar, BorderLayout.WEST);
        top.add(Ui.rightRow(details, edit, del), BorderLayout.EAST);

        cardContent.add(top, BorderLayout.NORTH);
        cardContent.add(table, BorderLayout.CENTER);

        JPanel tableCard = Ui.card(cardContent, 18);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(kpiBanner, BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);
        setBody(body);
    }

    private void details(Student s) {
        new StudentDetailDialog(this, s.studentId()).setVisible(true);
        reloadTable();
    }

    private void edit(Student s) {
        List<Department> depts = load(() -> departments.list(""), List.of());
        JTextField id = Ui.field(s == null ? "" : s.studentId());
        id.setEditable(s == null);
        JTextField name = Ui.field(s == null ? "" : s.fullName());
        JTextField email = Ui.field(s == null ? "" : s.email());
        JTextField phone = Ui.field(s == null ? "" : s.phone());
        JComboBox<Department> d = Ui.combo(depts);
        if (s != null) {
            depts.stream().filter(x -> x.deptId() == s.deptId()).findFirst().ifPresent(d::setSelectedItem);
        }
        JTextField grad = Ui.field(s == null ? String.valueOf(java.time.Year.now().getValue() + 1) : String.valueOf(s.graduationYear()));
        JTextField cgpa = Ui.field(s == null ? "" : s.cgpa().toPlainString());
        JTextField backlogs = Ui.field(s == null ? "0" : String.valueOf(s.backlogs()));
        FormDialog f = new FormDialog(this, s == null ? "Add student" : "Edit student",
                s == null ? "A login is created with the student ID as username and password "
                        + StudentService.DEFAULT_PASSWORD + "." : "Student ID cannot be changed.")
                .field("Student ID", id).field("Full name", name).field("Email", email).field("Phone", phone)
                .field("Department", d).field("Graduation year", grad).field("CGPA (0–10)", cgpa).field("Backlogs", backlogs);
        if (f.open(s == null ? "Add student" : "Save changes", () -> {
            StudentService.StudentForm form = new StudentService.StudentForm(id.getText(), name.getText(), email.getText(),
                    phone.getText(), (Department) d.getSelectedItem(), grad.getText(), cgpa.getText(), backlogs.getText());
            if (s == null) {
                service.create(form);
            } else {
                service.update(form);
            }
        })) {
            refresh();
        }
    }

    private void delete() {
        Student s = table.selected();
        if (need(s, "a student") && confirmDelete(s.fullName() + " (" + s.studentId()
                + "), their login, academic records and skills") && run(() -> service.delete(s.studentId()))) {
            refresh();
        }
    }

    @Override
    public void refresh() {
        loadingFilters = true;
        Object selDept = dept.getSelectedItem();
        Object selYear = year.getSelectedItem();
        dept.removeAllItems();
        dept.addItem(null);
        load(() -> departments.list(""), List.<Department>of()).forEach(dept::addItem);
        year.removeAllItems();
        year.addItem(null);
        load(service::graduationYears, List.<Integer>of()).forEach(year::addItem);
        if (selDept instanceof Department sd) {
            for (int i = 1; i < dept.getItemCount(); i++) {
                if (dept.getItemAt(i).deptId() == sd.deptId()) {
                    dept.setSelectedIndex(i);
                }
            }
        }
        year.setSelectedItem(selYear);
        loadingFilters = false;
        reloadTable();
    }

    private void reloadTable() {
        Department d = (Department) dept.getSelectedItem();
        Integer y = (Integer) year.getSelectedItem();
        List<Student> rows = load(() -> service.list(search.getText(), d == null ? null : d.deptId(), y), List.of());
        table.setRows(rows);

        int total = rows.size();
        long zeroBacklogs = rows.stream().filter(s -> s.backlogs() == 0).count();
        double avgCgpa = rows.stream().mapToDouble(s -> s.cgpa() != null ? s.cgpa().doubleValue() : 0.0).average().orElse(0.0);
        long distinctDepts = rows.stream().map(Student::deptCode).distinct().count();

        kpiTotal.setValue(total);
        kpiEligible.setValue(zeroBacklogs + " (" + (total > 0 ? (zeroBacklogs * 100 / total) : 0) + "%)");
        kpiAvgCgpa.setValue(String.format("%.2f", avgCgpa));
        kpiDepts.setValue(distinctDepts);

        setSubtitle(total + " student(s) shown. Double-click a row for academic records, skills, applications and offers.");
    }

    @Override
    public void setSearch(String text) {
        if (search != null) {
            search.setText(text == null ? "" : text);
            reloadTable();
        }
    }

    @Override
    public String getSearch() {
        return search == null ? "" : search.getText();
    }
}
