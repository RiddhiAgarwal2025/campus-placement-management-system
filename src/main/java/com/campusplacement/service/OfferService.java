package com.campusplacement.service;

import com.campusplacement.dao.ApplicationDao;
import com.campusplacement.dao.OfferDao;
import com.campusplacement.db.Db;
import com.campusplacement.model.Application;
import com.campusplacement.model.Offer;
import com.campusplacement.util.Validators;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OfferService {
    private final OfferDao dao = new OfferDao();
    private final ApplicationDao applications = new ApplicationDao();

    public List<Offer> list(String search, String status) {
        Session.requireOfficer();
        return Db.query(c -> dao.find(c, search, status));
    }

    public List<Offer> mine() {
        String sid = Session.studentId();
        return Db.query(c -> dao.byStudent(c, sid));
    }

    public List<Offer> forStudent(String studentId) {
        return Db.query(c -> dao.byStudent(c, studentId));
    }

    /** SELECTED applications that do not yet hold an offer. */
    public List<Application> candidatesForOffer() {
        Session.requireOfficer();
        return Db.query(c -> {
            Set<Integer> withOffer = dao.find(c, "", null).stream().map(Offer::applicationId).collect(Collectors.toSet());
            return applications.find(c, "", null, null, null, "SELECTED").stream()
                    .filter(a -> !withOffer.contains(a.applicationId())).toList();
        });
    }

    public void issue(int applicationId, String pkg, String offerDate, String joiningDate) {
        Session.requireOfficer();
        BigDecimal p = Validators.decimal("Package (LPA)", pkg, 0, 500, false);
        LocalDate od = Validators.date("Offer date", offerDate);
        LocalDate jd = Validators.date("Joining date", joiningDate);
        if (jd.isBefore(od)) {
            throw new ServiceException("The joining date must be on or after the offer date.");
        }
        Db.txExec(c -> {
            Application a = applications.lock(c, applicationId)
                    .orElseThrow(() -> new ServiceException("Application not found."));
            if (!"SELECTED".equals(a.status())) {
                throw new ServiceException("Offers can only be issued to SELECTED candidates. "
                        + a.studentName() + " is currently " + a.status() + ".");
            }
            if (dao.existsForApplication(c, applicationId)) {
                throw new ServiceException("An offer has already been issued for this application.");
            }
            dao.insert(c, applicationId, p, od, jd);
        });
    }

    public void updateTerms(Offer o, String pkg, String offerDate, String joiningDate) {
        Session.requireOfficer();
        BigDecimal p = Validators.decimal("Package (LPA)", pkg, 0, 500, false);
        LocalDate od = Validators.date("Offer date", offerDate);
        LocalDate jd = Validators.date("Joining date", joiningDate);
        if (jd.isBefore(od)) {
            throw new ServiceException("The joining date must be on or after the offer date.");
        }
        Db.txExec(c -> {
            Offer cur = dao.lock(c, o.offerId()).orElseThrow(() -> new ServiceException("Offer not found."));
            if (!"PENDING".equals(cur.status())) {
                throw new ServiceException("Only pending offers can be edited.");
            }
            dao.updateTerms(c, o.offerId(), p, od, jd);
        });
    }

    public void withdraw(Offer o) {
        Session.requireOfficer();
        Db.txExec(c -> {
            Offer cur = dao.lock(c, o.offerId()).orElseThrow(() -> new ServiceException("Offer not found."));
            if (!"PENDING".equals(cur.status())) {
                throw new ServiceException("Only pending offers can be withdrawn.");
            }
            dao.delete(c, o.offerId());
        });
    }

    /** Student accepts or rejects their own pending offer atomically. */
    public void respond(int offerId, boolean accept) {
        String sid = Session.studentId();
        Db.txExec(c -> {
            Offer o = dao.lock(c, offerId).orElseThrow(() -> new ServiceException("Offer not found."));
            if (!o.studentId().equals(sid)) {
                throw new ServiceException("This offer does not belong to you.");
            }
            if (!"PENDING".equals(o.status())) {
                throw new ServiceException("This offer has already been " + o.status().toLowerCase() + ".");
            }
            if (accept && dao.hasAcceptedOffer(c, sid)) {
                throw new ServiceException("You have already accepted another offer. Only one offer can be accepted.");
            }
            dao.respond(c, offerId, accept ? "ACCEPTED" : "REJECTED");
        });
    }
}
