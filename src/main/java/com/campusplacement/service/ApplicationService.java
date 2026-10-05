package com.campusplacement.service;

import com.campusplacement.dao.ApplicationDao;
import com.campusplacement.dao.DriveDao;
import com.campusplacement.dao.OfferDao;
import com.campusplacement.dao.SelectionDao;
import com.campusplacement.db.Db;
import com.campusplacement.model.Application;
import com.campusplacement.model.Drive;
import com.campusplacement.model.EligibilityResult;
import com.campusplacement.util.Formats;
import java.time.LocalDate;
import java.util.List;

public class ApplicationService {
    private final ApplicationDao dao = new ApplicationDao();
    private final DriveDao drives = new DriveDao();
    private final SelectionDao selection = new SelectionDao();
    private final OfferDao offers = new OfferDao();
    private final EligibilityService eligibility = new EligibilityService();

    public List<Application> list(String search, Integer driveId, Integer companyId, Integer deptId, String status) {
        Session.requireOfficer();
        return Db.query(c -> dao.find(c, search, driveId, companyId, deptId, status));
    }

    public List<Application> mine() {
        String id = Session.studentId();
        return Db.query(c -> dao.byStudent(c, id));
    }

    public List<Application> forStudent(String studentId) {
        if (!Session.isOfficer() && !Session.studentId().equals(studentId)) {
            throw new ServiceException("Unauthorized: You cannot view applications belonging to another student.");
        }
        return Db.query(c -> dao.byStudent(c, studentId));
    }

    /**
     * Application transaction: lock drive, validate status and deadline, evaluate eligibility,
     * check for a duplicate, insert. Any failure rolls back.
     */
    public int apply(String studentId, int driveId) {
        if (!Session.isOfficer() && !Session.studentId().equals(studentId)) {
            throw new ServiceException("You can only apply on your own behalf.");
        }
        return Db.tx(c -> {
            Drive d = drives.lock(c, driveId).orElseThrow(() -> new ServiceException("The selected drive no longer exists."));
            switch (d.status()) {
                case "UPCOMING" -> throw new ServiceException("Applications for this drive have not opened yet.");
                case "CLOSED" -> throw new ServiceException("This drive is closed and no longer accepts applications.");
                case "COMPLETED" -> throw new ServiceException("This drive has already been completed.");
                default -> { }
            }
            if (d.deadline().isBefore(LocalDate.now())) {
                throw new ServiceException("The application deadline (" + Formats.date(d.deadline()) + ") has passed.");
            }
            EligibilityResult r = eligibility.check(c, studentId, driveId);
            if (!r.eligible()) {
                throw new ServiceException("NOT ELIGIBLE\n\n" + String.join("\n", r.reasons()));
            }
            if (dao.exists(c, studentId, driveId)) {
                throw new ServiceException("You have already applied to this drive.");
            }
            return dao.insert(c, studentId, driveId);
        });
    }

    /** A student may withdraw an application while it is still APPLIED and the drive is accepting applications. */
    public void withdraw(int applicationId) {
        String sid = Session.studentId();
        Db.txExec(c -> {
            Application a = dao.lock(c, applicationId).orElseThrow(() -> new ServiceException("Application not found."));
            if (!a.studentId().equals(sid)) {
                throw new ServiceException("You can only withdraw your own applications.");
            }
            if (!"APPLIED".equals(a.status())) {
                throw new ServiceException("Only applications that are still APPLIED can be withdrawn.");
            }
            if (!"OPEN".equals(a.driveStatus()) || a.deadline().isBefore(LocalDate.now())) {
                throw new ServiceException("Applications can be withdrawn only before the deadline of an open drive.");
            }
            dao.delete(c, applicationId);
        });
    }

    /** Officer status change, consistent with recorded round results. */
    public void changeStatus(int applicationId, String status) {
        Session.requireOfficer();
        Application.Status.valueOf(status);
        Db.txExec(c -> {
            Application a = dao.lock(c, applicationId).orElseThrow(() -> new ServiceException("Application not found."));
            if (a.status().equals(status)) {
                return;
            }
            if (offers.existsForApplication(c, applicationId)) {
                throw new ServiceException("An offer has been issued for this application; its status cannot change.");
            }
            int[] p = selection.progress(c, applicationId);
            int total = p[0];
            int passed = p[1];
            int failed = p[2];
            switch (status) {
                case "SELECTED" -> {
                    if (failed > 0) {
                        throw new ServiceException("This candidate failed a selection round and cannot be selected.");
                    }
                    if (total > 0 && passed < total) {
                        throw new ServiceException("The candidate must pass all " + total
                                + " selection rounds before being selected (passed " + passed + ").");
                    }
                }
                case "SHORTLISTED" -> {
                    if (failed > 0) {
                        throw new ServiceException("This candidate failed a selection round and cannot be shortlisted.");
                    }
                }
                case "APPLIED" -> {
                    if (passed + failed > 0) {
                        throw new ServiceException("Round results exist for this candidate; clear them before resetting to APPLIED.");
                    }
                }
                default -> { }
            }
            dao.updateStatus(c, applicationId, status);
        });
    }
}
