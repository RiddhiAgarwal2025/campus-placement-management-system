package com.campusplacement.service;

import com.campusplacement.dao.ApplicationDao;
import com.campusplacement.dao.DashboardDao;
import com.campusplacement.dao.DriveDao;
import com.campusplacement.dao.OfferDao;
import com.campusplacement.db.Db;
import com.campusplacement.model.Application;
import com.campusplacement.model.DashboardStats;
import com.campusplacement.model.Drive;
import com.campusplacement.model.Offer;
import java.util.List;

public class DashboardService {
    private final DashboardDao dao = new DashboardDao();
    private final DriveDao drives = new DriveDao();
    private final ApplicationDao applications = new ApplicationDao();
    private final OfferDao offers = new OfferDao();

    public record OfficerDashboard(DashboardStats stats, List<Drive> upcoming, List<Application> recentApplications,
                                   List<Offer> recentOffers) { }

    public record StudentDashboard(DashboardStats stats, List<Drive> openDrives, List<Application> applications,
                                   List<Offer> offers) { }

    public OfficerDashboard officer() {
        Session.requireOfficer();
        return Db.query(c -> new OfficerDashboard(new DashboardStats(dao.officerStats(c)), drives.upcoming(c, 6),
                applications.recent(c, 8), offers.recent(c, 6)));
    }

    public StudentDashboard student() {
        String sid = Session.studentId();
        return Db.query(c -> new StudentDashboard(new DashboardStats(dao.studentStats(c, sid)), drives.upcoming(c, 8),
                applications.byStudent(c, sid), offers.byStudent(c, sid)));
    }
}
