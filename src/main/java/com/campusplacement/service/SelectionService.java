package com.campusplacement.service;

import com.campusplacement.dao.ApplicationDao;
import com.campusplacement.dao.OfferDao;
import com.campusplacement.dao.SelectionDao;
import com.campusplacement.db.Db;
import com.campusplacement.model.Application;
import com.campusplacement.model.RoundCandidate;
import com.campusplacement.model.SelectionRound;
import com.campusplacement.model.StudentRoundStatus;
import com.campusplacement.util.Validators;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class SelectionService {
    public static final List<String> ROUND_STATUSES = List.of("UPCOMING", "IN_PROGRESS", "COMPLETED");

    private final SelectionDao dao = new SelectionDao();
    private final ApplicationDao applications = new ApplicationDao();
    private final OfferDao offers = new OfferDao();

    public List<SelectionRound> rounds(int driveId) {
        Session.requireOfficer();
        return Db.query(c -> dao.rounds(c, driveId));
    }

    private SelectionRound validate(int roundId, int driveId, String name, String seq, String date, String status) {
        String n = Validators.required("Round name", name, 80);
        int s = Validators.integer("Sequence number", seq, 1, 20);
        LocalDate d = Validators.optionalDate("Round date", date);
        if (!ROUND_STATUSES.contains(status)) {
            throw new ServiceException("Invalid round status.");
        }
        return new SelectionRound(roundId, driveId, n, s, d, status, 0, 0);
    }

    public void addRound(int driveId, String name, String seq, String date, String status) {
        Session.requireOfficer();
        SelectionRound r = validate(0, driveId, name, seq, date, status);
        Db.txExec(c -> {
            List<SelectionRound> existing = dao.rounds(c, driveId);
            boolean laterHasResults = existing.stream().anyMatch(x -> x.sequenceNo() > r.sequenceNo()
                    && x.passCount() + x.failCount() > 0);
            if (laterHasResults) {
                throw new ServiceException("A round cannot be inserted before a round that already has results.");
            }
            dao.insertRound(c, r);
        });
    }

    public void updateRound(int roundId, int driveId, String name, String seq, String date, String status) {
        Session.requireOfficer();
        SelectionRound r = validate(roundId, driveId, name, seq, date, status);
        Db.exec(c -> dao.updateRound(c, r));
    }

    public void deleteRound(int roundId) {
        Session.requireOfficer();
        Db.exec(c -> dao.deleteRound(c, roundId));
    }

    /** Returns the round immediately before the given one (by sequence), or null for the first round. */
    public SelectionRound previousRound(List<SelectionRound> rounds, SelectionRound r) {
        SelectionRound prev = null;
        for (SelectionRound x : rounds) {
            if (x.sequenceNo() < r.sequenceNo() && (prev == null || x.sequenceNo() > prev.sequenceNo())) {
                prev = x;
            }
        }
        return prev;
    }

    public List<RoundCandidate> candidates(SelectionRound r) {
        Session.requireOfficer();
        return Db.query(c -> {
            SelectionRound prev = previousRound(dao.rounds(c, r.driveId()), r);
            return dao.candidates(c, r.driveId(), r.roundId(), prev == null ? 0 : prev.roundId());
        });
    }

    /** Records PASS/FAIL and updates the application status in one transaction. Triggers enforce round order too. */
    public void recordResult(SelectionRound round, int applicationId, String result, String remarks) {
        Session.requireOfficer();
        if (!"PASS".equals(result) && !"FAIL".equals(result)) {
            throw new ServiceException("Result must be PASS or FAIL.");
        }
        String rem = Validators.optional("Remarks", remarks, 255);
        Db.txExec(c -> {
            Application a = guard(c, round, applicationId);
            List<SelectionRound> rounds = dao.rounds(c, round.driveId());
            SelectionRound prev = previousRound(rounds, round);
            if (prev != null && !"PASS".equals(dao.result(c, prev.roundId(), applicationId))) {
                throw new ServiceException("Invalid round progression: " + a.studentName() + " has not passed "
                        + prev.name() + " (round " + prev.sequenceNo() + ").");
            }
            if (dao.result(c, round.roundId(), applicationId) == null) {
                dao.insertResult(c, round.roundId(), applicationId, result, rem);
            } else {
                dao.updateResult(c, round.roundId(), applicationId, result, rem);
            }
            syncStatus(c, applicationId, a.status());
        });
    }

    public void clearResult(SelectionRound round, int applicationId) {
        Session.requireOfficer();
        Db.txExec(c -> {
            Application a = guard(c, round, applicationId);
            dao.deleteResult(c, round.roundId(), applicationId);
            syncStatus(c, applicationId, a.status());
        });
    }

    private Application guard(Connection c, SelectionRound round, int applicationId) throws SQLException {
        Application a = applications.lock(c, applicationId)
                .orElseThrow(() -> new ServiceException("Application not found."));
        if (a.driveId() != round.driveId()) {
            throw new ServiceException("Invalid round progression: the candidate did not apply to this drive.");
        }
        if (offers.existsForApplication(c, applicationId)) {
            throw new ServiceException("An offer has already been issued to this candidate; results are locked.");
        }
        return a;
    }

    /** Derives the application status from its round results. */
    private void syncStatus(Connection c, int applicationId, String current) throws SQLException {
        int[] p = dao.progress(c, applicationId);
        int total = p[0];
        int passed = p[1];
        int failed = p[2];
        String status;
        if (failed > 0) {
            status = "REJECTED";
        } else if (total > 0 && passed == total) {
            status = "SELECTED";
        } else if (passed > 0) {
            status = "SHORTLISTED";
        } else {
            status = "SHORTLISTED".equals(current) ? "SHORTLISTED" : "APPLIED";
        }
        if (!status.equals(current)) {
            applications.updateStatus(c, applicationId, status);
        }
    }

    public List<StudentRoundStatus> myRounds() {
        String sid = Session.studentId();
        return Db.query(c -> dao.studentRounds(c, sid));
    }
}
