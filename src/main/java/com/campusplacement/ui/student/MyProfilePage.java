package com.campusplacement.ui.student;

import com.campusplacement.model.AcademicRecord;
import com.campusplacement.model.Student;
import com.campusplacement.model.StudentSkill;
import com.campusplacement.service.AuthService;
import com.campusplacement.service.Session;
import com.campusplacement.service.SkillService;
import com.campusplacement.service.StudentService;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.FormDialog;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.KpiBanner;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.StatTile;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class MyProfilePage extends Page {
    private final StudentService students = new StudentService();
    private final SkillService skills = new SkillService();
    private final StatTile kpiCgpa = new StatTile("CGPA", "Academic Standing", true);
    private final StatTile kpiBacklogs = new StatTile("Backlogs", "Active course arrears", false);
    private final StatTile kpiGradYear = new StatTile("Graduation Year", "Graduating class", false);
    private final StatTile kpiDept = new StatTile("Department", "Engineering major", false);
    private final JPanel info = Ui.vstack(0);
    private final DataTable<AcademicRecord> records = new DataTable<AcademicRecord>("No academic records",
            "Records are maintained by the placement office.")
            .col("Semester", AcademicRecord::semester, 80, Kind.NUMBER)
            .col("SGPA", AcademicRecord::sgpa, 80, Kind.NUMBER)
            .col("Marks (%)", AcademicRecord::marks, 90, Kind.NUMBER);
    private final DataTable<StudentSkill> skillTable = new DataTable<StudentSkill>("No skills recorded",
            "Ask the placement office to record your skills; drives may require them.")
            .col("Skill", StudentSkill::skillName, 170)
            .col("Category", StudentSkill::category, 140)
            .col("Proficiency", StudentSkill::proficiency, 120, Kind.BADGE);
    private Student me;

    public MyProfilePage() {
        super("My Profile", "Your academic information and skills as held by the placement office.");
        Btn contact = new Btn("Update contact", Btn.Variant.SECONDARY, Glyph.EDIT);
        contact.addActionListener(e -> editContact());
        Btn pwd = new Btn("Change password", Btn.Variant.SECONDARY, Glyph.KEY);
        pwd.addActionListener(e -> changePassword());
        addAction(contact);
        addAction(pwd);

        KpiBanner kpiBanner = new KpiBanner(kpiCgpa, kpiBacklogs, kpiGradYear, kpiDept);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(kpiBanner, BorderLayout.NORTH);
        top.add(Ui.card(info, 18), BorderLayout.CENTER);

        JPanel tables = new JPanel(new GridLayout(1, 2, 16, 0));
        tables.setOpaque(false);
        tables.add(Ui.section("Academic records", "Semester-wise results", records));
        tables.add(Ui.section("Skills", "Used to match drive requirements", skillTable));

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);
        body.add(tables, BorderLayout.CENTER);
        setBody(body);
    }

    private void editContact() {
        if (me == null) {
            return;
        }
        JTextField email = Ui.field(me.email());
        JTextField phone = Ui.field(me.phone());
        if (new FormDialog(this, "Update contact details", "Name, department and academic data are managed by the placement office.")
                .field("Email", email).field("Phone", phone)
                .open("Save changes", () -> students.updateOwnContact(email.getText(), phone.getText()))) {
            refresh();
        }
    }

    private void changePassword() {
        JPasswordField cur = Ui.password();
        JPasswordField next = Ui.password();
        JPasswordField again = Ui.password();
        if (new FormDialog(this, "Change password", "At least 8 characters.")
                .field("Current password", cur).field("New password", next).field("Confirm new password", again)
                .open("Change password", () -> new AuthService().changePassword(new String(cur.getPassword()),
                        new String(next.getPassword()), new String(again.getPassword())))) {
            info("Password changed", "Use your new password the next time you sign in.");
        }
    }

    @Override
    public void refresh() {
        String id = Session.studentId();
        me = load(() -> students.get(id), null);
        if (me == null) {
            return;
        }
        kpiCgpa.setValue(me.cgpa().toPlainString());
        kpiBacklogs.setValue(me.backlogs());
        kpiGradYear.setValue(me.graduationYear());
        kpiDept.setValue(me.deptCode());

        info.removeAll();
        info.add(Ui.label(me.fullName(), Theme.sansBold(20), Theme.TEXT));
        info.add(Box.createVerticalStrut(6));
        info.add(Ui.kv("Student ID", me.studentId()));
        info.add(Ui.kv("Department", me.deptName()));
        info.add(Ui.kv("Email", me.email()));
        info.add(Ui.kv("Phone", me.phone()));
        records.setRows(load(() -> students.records(id), List.of()));
        skillTable.setRows(load(() -> skills.studentSkills(id), List.of()));
        info.revalidate();
        repaint();
    }
}
