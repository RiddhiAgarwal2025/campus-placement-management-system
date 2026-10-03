package com.campusplacement.ui.officer;

import com.campusplacement.model.AcademicRecord;
import com.campusplacement.model.Application;
import com.campusplacement.model.Offer;
import com.campusplacement.model.Skill;
import com.campusplacement.model.Student;
import com.campusplacement.model.StudentSkill;
import com.campusplacement.service.ApplicationService;
import com.campusplacement.service.OfferService;
import com.campusplacement.service.ServiceException;
import com.campusplacement.service.SkillService;
import com.campusplacement.service.StudentService;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.Dialogs;
import com.campusplacement.ui.components.FormDialog;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.Tabs;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Full student record for officers: profile, academic records, skills, applications and offers. */
public class StudentDetailDialog extends JDialog {
    private final StudentService students = new StudentService();
    private final SkillService skills = new SkillService();
    private final String studentId;
    private final JPanel summary = new JPanel(new GridLayout(1, 0, 12, 0));
    private final JLabel heading = Ui.label("", Theme.serif(24), Theme.TEXT);
    private final JLabel sub = Ui.muted("");

    private final DataTable<AcademicRecord> records = new DataTable<AcademicRecord>("No academic records",
            "Add the student's semester results.")
            .col("Semester", AcademicRecord::semester, 80, Kind.NUMBER)
            .col("SGPA", AcademicRecord::sgpa, 80, Kind.NUMBER)
            .col("Marks (%)", AcademicRecord::marks, 90, Kind.NUMBER);
    private final DataTable<StudentSkill> skillTable = new DataTable<StudentSkill>("No skills assigned",
            "Assign skills so the eligibility engine can match required skills.")
            .col("Skill", StudentSkill::skillName, 200)
            .col("Category", StudentSkill::category, 160)
            .col("Proficiency", StudentSkill::proficiency, 120, Kind.BADGE);
    private final DataTable<Application> apps = new DataTable<Application>("No applications", "This student has not applied yet.")
            .col("Company", Application::companyName, 170)
            .col("Position", Application::position, 170)
            .col("Applied", Application::appliedAt, 140, Kind.DATE)
            .col("Status", Application::status, 100, Kind.BADGE);
    private final DataTable<Offer> offers = new DataTable<Offer>("No offers", "Offers appear after selection.")
            .col("Company", Offer::companyName, 160)
            .col("Position", Offer::position, 160)
            .col("Package", Offer::packageLpa, 100, Kind.MONEY)
            .col("Joining", Offer::joiningDate, 100, Kind.DATE)
            .col("Status", Offer::status, 100, Kind.BADGE);

    public StudentDetailDialog(Component parent, String studentId) {
        super(SwingUtilities.getWindowAncestor(parent), "Student record", ModalityType.APPLICATION_MODAL);
        this.studentId = studentId;
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, Theme.PLUM),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        JPanel head = Ui.vstack(0);
        head.add(heading);
        head.add(sub);
        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        summary.setOpaque(false);
        top.add(summary, BorderLayout.CENTER);
        root.add(top, BorderLayout.NORTH);

        Tabs tabs = new Tabs()
                .tab("Academic records", withButtons(records, recordButtons()))
                .tab("Skills", withButtons(skillTable, skillButtons()))
                .tab("Applications", apps)
                .tab("Offers", offers);
        root.add(tabs, BorderLayout.CENTER);
        Btn close = new Btn("Close", Btn.Variant.SECONDARY);
        close.addActionListener(e -> dispose());
        root.add(Ui.rightRow(close), BorderLayout.SOUTH);
        setContentPane(root);
        reload();
        setSize(940, 660);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(getOwner());
    }

    private JPanel withButtons(JComponent table, JComponent buttons) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.add(buttons, BorderLayout.NORTH);
        p.add(table, BorderLayout.CENTER);
        return p;
    }

    private JComponent recordButtons() {
        Btn add = new Btn("Add record", Btn.Variant.PRIMARY, Glyph.PLUS);
        add.addActionListener(e -> editRecord(null));
        Btn edit = new Btn("Edit", Btn.Variant.SECONDARY, Glyph.EDIT);
        edit.addActionListener(e -> {
            if (records.selected() == null) {
                Dialogs.info(this, "Nothing selected", "Select a record first.");
            } else {
                editRecord(records.selected());
            }
        });
        Btn del = new Btn("Delete", Btn.Variant.DANGER, Glyph.TRASH);
        del.addActionListener(e -> {
            AcademicRecord r = records.selected();
            if (r == null) {
                Dialogs.info(this, "Nothing selected", "Select a record first.");
            } else if (Dialogs.confirmDanger(this, "Delete semester " + r.semester() + " record?",
                    "This removes the record from the database.", "Delete")) {
                act(() -> students.deleteRecord(r.recordId()));
            }
        });
        records.onDoubleClick(this::editRecord);
        return Ui.row(add, edit, del);
    }

    private void editRecord(AcademicRecord r) {
        JTextField sem = Ui.field(r == null ? "" : String.valueOf(r.semester()));
        JTextField sgpa = Ui.field(r == null ? "" : r.sgpa().toPlainString());
        JTextField marks = Ui.field(r == null ? "" : r.marks().toPlainString());
        FormDialog f = new FormDialog(this, r == null ? "Add academic record" : "Edit academic record",
                "Semester 1–10, SGPA 0–10, marks 0–100. One record per semester.")
                .field("Semester", sem).field("SGPA", sgpa).field("Marks (%)", marks);
        if (f.open("Save record", () -> {
            if (r == null) {
                students.addRecord(studentId, sem.getText(), sgpa.getText(), marks.getText());
            } else {
                students.updateRecord(r.recordId(), studentId, sem.getText(), sgpa.getText(), marks.getText());
            }
        })) {
            reload();
        }
    }

    private JComponent skillButtons() {
        Btn assign = new Btn("Assign skill", Btn.Variant.PRIMARY, Glyph.PLUS);
        assign.addActionListener(e -> assignSkill());
        Btn change = new Btn("Change proficiency", Btn.Variant.SECONDARY, Glyph.EDIT);
        change.addActionListener(e -> {
            StudentSkill s = skillTable.selected();
            if (s == null) {
                Dialogs.info(this, "Nothing selected", "Select a skill first.");
                return;
            }
            JComboBox<String> p = Ui.combo(SkillService.PROFICIENCIES);
            p.setSelectedItem(s.proficiency());
            if (new FormDialog(this, "Proficiency in " + s.skillName(), null).field("Proficiency", p)
                    .open("Save", () -> skills.changeProficiency(studentId, s.skillId(), (String) p.getSelectedItem()))) {
                reload();
            }
        });
        Btn remove = new Btn("Remove", Btn.Variant.DANGER, Glyph.TRASH);
        remove.addActionListener(e -> {
            StudentSkill s = skillTable.selected();
            if (s == null) {
                Dialogs.info(this, "Nothing selected", "Select a skill first.");
            } else if (Dialogs.confirmDanger(this, "Remove " + s.skillName() + "?",
                    "The skill will no longer count towards eligibility for this student.", "Remove")) {
                act(() -> skills.unassign(studentId, s.skillId()));
            }
        });
        return Ui.row(assign, change, remove);
    }

    private void assignSkill() {
        Set<Integer> have = skillTable.rows().stream().map(StudentSkill::skillId).collect(Collectors.toSet());
        List<Skill> available;
        try {
            available = skills.list("").stream().filter(s -> !have.contains(s.skillId())).toList();
        } catch (ServiceException ex) {
            Dialogs.error(this, "Could not load skills", ex.getMessage());
            return;
        }
        if (available.isEmpty()) {
            Dialogs.info(this, "All skills assigned", "Every skill in the catalogue is already assigned. Add new skills from the Skills page.");
            return;
        }
        JComboBox<Skill> skill = Ui.combo(available);
        JComboBox<String> prof = Ui.combo(SkillService.PROFICIENCIES);
        prof.setSelectedItem("INTERMEDIATE");
        if (new FormDialog(this, "Assign skill", null).field("Skill", skill).field("Proficiency", prof)
                .open("Assign skill", () -> skills.assign(studentId, (Skill) skill.getSelectedItem(),
                        (String) prof.getSelectedItem()))) {
            reload();
        }
    }

    private void act(Runnable r) {
        try {
            r.run();
            reload();
        } catch (ServiceException ex) {
            Dialogs.error(this, "Action not completed", ex.getMessage());
        }
    }

    private void reload() {
        try {
            Student s = students.get(studentId);
            heading.setText(s.fullName());
            sub.setText(s.studentId() + "   |   " + s.deptName() + "   |   " + s.email()
                    + (s.phone() == null ? "" : "   |   " + s.phone()));
            summary.removeAll();
            summary.add(fact("CGPA", s.cgpa().toPlainString()));
            summary.add(fact("Backlogs", String.valueOf(s.backlogs())));
            summary.add(fact("Graduation year", String.valueOf(s.graduationYear())));
            List<Application> a = new ApplicationService().forStudent(studentId);
            List<Offer> o = new OfferService().forStudent(studentId);
            summary.add(fact("Applications", String.valueOf(a.size())));
            summary.add(fact("Offers", String.valueOf(o.size())));
            records.setRows(students.records(studentId));
            skillTable.setRows(skills.studentSkills(studentId));
            apps.setRows(a);
            offers.setRows(o);
            summary.revalidate();
            summary.repaint();
        } catch (ServiceException ex) {
            Dialogs.error(this, "Could not load student", ex.getMessage());
        }
    }

    private JPanel fact(String label, String value) {
        JPanel p = Ui.vstack(0);
        p.setOpaque(true);
        p.setBackground(Theme.SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        p.add(Ui.muted(label));
        p.add(Ui.label(value, Theme.serif(22), Theme.TEXT));
        return p;
    }
}
