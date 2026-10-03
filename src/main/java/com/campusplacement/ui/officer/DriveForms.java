package com.campusplacement.ui.officer;

import com.campusplacement.model.Department;
import com.campusplacement.model.Drive;
import com.campusplacement.model.EligibilityCriteria;
import com.campusplacement.model.JobProfile;
import com.campusplacement.model.Skill;
import com.campusplacement.service.CompanyService;
import com.campusplacement.service.DepartmentService;
import com.campusplacement.service.DriveService;
import com.campusplacement.service.EligibilityService;
import com.campusplacement.service.SkillService;
import com.campusplacement.ui.components.CheckList;
import com.campusplacement.ui.components.FormDialog;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.util.Formats;
import java.awt.Component;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/** Dialogs for creating and editing drives and their eligibility criteria. */
final class DriveForms {
    private DriveForms() { }

    private static final List<String> STATUSES = Arrays.stream(Drive.Status.values()).map(Enum::name).toList();

    static boolean createOrEdit(Component parent, Drive d) {
        DriveService drives = new DriveService();
        List<JobProfile> jobs = new CompanyService().jobs(null, "");
        JComboBox<JobProfile> job = Ui.combo(jobs);
        if (d != null) {
            jobs.stream().filter(j -> j.jobId() == d.jobId()).findFirst().ifPresent(job::setSelectedItem);
        }
        JTextField date = Ui.field(d == null ? Formats.iso(LocalDate.now().plusDays(21)) : Formats.iso(d.driveDate()));
        JTextField deadline = Ui.field(d == null ? Formats.iso(LocalDate.now().plusDays(14)) : Formats.iso(d.deadline()));
        JTextField venue = Ui.field(d == null ? "Placement Cell" : d.venue());
        JComboBox<String> status = Ui.combo(STATUSES);
        status.setSelectedItem(d == null ? "OPEN" : d.status());
        FormDialog f = new FormDialog(parent, d == null ? "New placement drive" : "Edit drive #" + d.driveId(),
                "Dates use YYYY-MM-DD. The deadline must be on or before the drive date.")
                .field("Job profile", job).field("Drive date", date).field("Application deadline", deadline)
                .field("Venue", venue).field("Status", status);
        if (d != null) {
            return f.open("Save changes", () -> drives.update(d.driveId(), new DriveService.DriveForm(
                    (JobProfile) job.getSelectedItem(), date.getText(), deadline.getText(), venue.getText(),
                    (String) status.getSelectedItem())));
        }
        CriteriaFields cf = new CriteriaFields(null);
        f.note("Eligibility criteria");
        cf.addTo(f);
        return f.open("Create drive", () -> drives.create(new DriveService.DriveForm((JobProfile) job.getSelectedItem(),
                date.getText(), deadline.getText(), venue.getText(), (String) status.getSelectedItem()), cf.form()));
    }

    static boolean editCriteria(Component parent, Drive d) {
        EligibilityCriteria cr = new EligibilityService().criteria(d.driveId());
        CriteriaFields cf = new CriteriaFields(cr);
        FormDialog f = new FormDialog(parent, "Eligibility for drive #" + d.driveId(), d.title()
                + ". Leave departments empty to allow all departments.");
        cf.addTo(f);
        return f.open("Save criteria", () -> new DriveService().saveCriteria(d.driveId(), cf.form()));
    }

    private static final class CriteriaFields {
        final JTextField minCgpa;
        final JTextField maxBacklogs;
        final JTextField year;
        final CheckList<Department> depts;
        final CheckList<Skill> skills;

        CriteriaFields(EligibilityCriteria cr) {
            minCgpa = Ui.field(cr == null ? "6.00" : cr.minCgpa().toPlainString());
            maxBacklogs = Ui.field(cr == null ? "0" : String.valueOf(cr.maxBacklogs()));
            year = Ui.field(cr == null ? String.valueOf(LocalDate.now().getYear() + 1)
                    : cr.graduationYear() == null ? "" : String.valueOf(cr.graduationYear()));
            depts = new CheckList<>(new DepartmentService().list(""), Department::code,
                    cr == null ? List.of() : cr.departments().stream().map(Department::deptId).toList(), Department::deptId, 5);
            skills = new CheckList<>(new SkillService().list(""), Skill::name,
                    cr == null ? List.of() : cr.skills().stream().map(Skill::skillId).toList(), Skill::skillId, 3);
        }

        void addTo(FormDialog f) {
            f.field("Minimum CGPA", minCgpa).field("Maximum backlogs", maxBacklogs)
                    .field("Graduation year", year).field("Departments", depts).field("Required skills", skills);
        }

        DriveService.CriteriaForm form() {
            return new DriveService.CriteriaForm(minCgpa.getText(), maxBacklogs.getText(), year.getText(),
                    depts.selected(), skills.selected());
        }
    }
}
