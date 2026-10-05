package com.campusplacement.service;

import com.campusplacement.dao.DriveDao;
import com.campusplacement.db.Db;
import com.campusplacement.model.Department;
import com.campusplacement.model.Drive;
import com.campusplacement.model.EligibilityCriteria;
import com.campusplacement.model.JobProfile;
import com.campusplacement.model.Skill;
import com.campusplacement.util.Validators;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DriveService {
    private final DriveDao dao = new DriveDao();

    public record DriveForm(JobProfile job, String driveDate, String deadline, String venue, String status) { }

    public record CriteriaForm(String minCgpa, String maxBacklogs, String graduationYear, List<Department> departments,
                               List<Skill> skills) { }

    public List<Drive> list(String search, String status, Integer companyId) {
        return Db.query(c -> dao.findAll(c, search, status, companyId));
    }

    /** Drives a student may consider: everything except COMPLETED. */
    public List<Drive> listForStudents(String search) {
        return list(search, null, null).stream().filter(d -> !"COMPLETED".equals(d.status())).toList();
    }

    public Drive get(int driveId) {
        return Db.query(c -> dao.findById(c, driveId))
                .orElseThrow(() -> new ServiceException("Drive #" + driveId + " was not found."));
    }

    private Drive validate(int id, DriveForm f, boolean isNew) {
        if (f.job() == null) {
            throw new ServiceException("Job profile is required.");
        }
        LocalDate date = Validators.date("Drive date", f.driveDate());
        LocalDate deadline = Validators.date("Application deadline", f.deadline());
        if (deadline.isAfter(date)) {
            throw new ServiceException("The application deadline must be on or before the drive date.");
        }
        String status = f.status() == null ? "UPCOMING" : f.status();
        try {
            Drive.Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Invalid drive status.");
        }
        if (isNew && "OPEN".equals(status) && deadline.isBefore(LocalDate.now())) {
            throw new ServiceException("An OPEN drive needs an application deadline of today or later.");
        }
        String venue = Validators.required("Venue", f.venue(), 100);
        return new Drive(id, f.job().jobId(), f.job().companyId(), f.job().companyName(), f.job().position(),
                f.job().packageLpa(), f.job().location(), date, deadline, venue, status, null, null, null, 0);
    }

    private EligibilityCriteria validate(int driveId, CriteriaForm f) {
        BigDecimal min = Validators.decimal("Minimum CGPA", f.minCgpa(), 0, 10, true);
        int backlogs = Validators.integer("Maximum backlogs", f.maxBacklogs(), 0, 60);
        Integer year = f.graduationYear() == null || f.graduationYear().isBlank() ? null
                : Validators.integer("Graduation year", f.graduationYear(), 2000, 2100);
        return new EligibilityCriteria(0, driveId, min, backlogs, year,
                f.departments() == null ? List.of() : f.departments(), f.skills() == null ? List.of() : f.skills());
    }

    /** Creates a drive together with its eligibility criteria in a single transaction. */
    public int create(DriveForm form, CriteriaForm criteria) {
        Session.requireOfficer();
        Drive d = validate(0, form, true);
        EligibilityCriteria cr = validate(0, criteria);
        return Db.tx(c -> {
            int id = dao.insert(c, d);
            dao.saveCriteria(c, new EligibilityCriteria(cr.criteriaId(), id, cr.minCgpa(),
                    cr.maxBacklogs(), cr.graduationYear(), cr.departments(), cr.skills()));
            return id;
        });
    }

    public void update(int driveId, DriveForm form) {
        Session.requireOfficer();
        Drive d = validate(driveId, form, false);
        Db.txExec(c -> {
            Drive existing = dao.lock(c, driveId).orElseThrow(() -> new ServiceException("Drive not found."));
            if (existing.jobId() != d.jobId() && existing.applicationCount() > 0) {
                throw new ServiceException("The job profile cannot be changed after students have applied.");
            }
            dao.update(c, d);
        });
    }

    public void saveCriteria(int driveId, CriteriaForm form) {
        Session.requireOfficer();
        EligibilityCriteria cr = validate(driveId, form);
        Db.txExec(c -> dao.saveCriteria(c, cr));
    }

    public void setStatus(int driveId, String status) {
        Session.requireOfficer();
        Drive.Status.valueOf(status);
        Db.exec(c -> dao.updateStatus(c, driveId, status));
    }

    public void delete(Drive d) {
        Session.requireOfficer();
        if (d.applicationCount() > 0) {
            throw new ServiceException("This drive has " + d.applicationCount()
                    + " application(s) and cannot be deleted. Close it instead.");
        }
        Db.exec(c -> dao.delete(c, d.driveId()));
    }
}
