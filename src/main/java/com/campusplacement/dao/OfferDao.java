package com.campusplacement.dao;

import com.campusplacement.model.Offer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OfferDao {
    private static final String SELECT = "SELECT * FROM vw_student_offers ";

    private Offer map(ResultSet rs) throws SQLException {
        return new Offer(rs.getInt("offer_id"), rs.getInt("application_id"), rs.getString("student_id"),
                rs.getString("full_name"), rs.getString("dept_code"), rs.getString("company_name"), rs.getString("position"),
                rs.getString("location"), rs.getBigDecimal("package_lpa"), Jdbc.date(rs, "offer_date"),
                Jdbc.date(rs, "joining_date"), rs.getString("status"), Jdbc.time(rs, "responded_at"));
    }

    private List<Offer> list(PreparedStatement ps) throws SQLException {
        List<Offer> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public List<Offer> find(Connection c, String search, String status) throws SQLException {
        String sql = SELECT + "WHERE (full_name LIKE ? OR student_id LIKE ? OR company_name LIKE ? OR position LIKE ?) "
                + "AND (? IS NULL OR status = ?) ORDER BY offer_date DESC, offer_id DESC";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            String q = Jdbc.like(search);
            Jdbc.bind(ps, q, q, q, q, status, status);
            return list(ps);
        }
    }

    public List<Offer> byStudent(Connection c, String studentId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE student_id = ? ORDER BY offer_date DESC")) {
            ps.setString(1, studentId);
            return list(ps);
        }
    }

    public List<Offer> recent(Connection c, int limit) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY offer_date DESC, offer_id DESC LIMIT ?")) {
            ps.setInt(1, limit);
            return list(ps);
        }
    }

    /** Locks and returns an offer inside the current transaction. */
    public Optional<Offer> lock(Connection c, int offerId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT offer_id FROM offers WHERE offer_id = ? FOR UPDATE")) {
            ps.setInt(1, offerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
            }
        }
        try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE offer_id = ?")) {
            ps.setInt(1, offerId);
            List<Offer> l = list(ps);
            return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
        }
    }

    public boolean existsForApplication(Connection c, int applicationId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM offers WHERE application_id = ? FOR UPDATE")) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean hasAcceptedOffer(Connection c, String studentId) throws SQLException {
        String sql = "SELECT 1 FROM offers o JOIN applications a ON a.application_id = o.application_id "
                + "WHERE a.student_id = ? AND o.status = 'ACCEPTED' FOR UPDATE";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(Connection c, int applicationId, java.math.BigDecimal pkg, java.time.LocalDate offerDate,
                       java.time.LocalDate joiningDate) throws SQLException {
        String sql = "INSERT INTO offers (application_id, package_lpa, offer_date, joining_date, status) VALUES (?,?,?,?,'PENDING')";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, applicationId, pkg, offerDate, joiningDate);
            ps.executeUpdate();
        }
    }

    public void updateTerms(Connection c, int offerId, java.math.BigDecimal pkg, java.time.LocalDate offerDate,
                            java.time.LocalDate joiningDate) throws SQLException {
        String sql = "UPDATE offers SET package_lpa = ?, offer_date = ?, joining_date = ? WHERE offer_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, pkg, offerDate, joiningDate, offerId);
            ps.executeUpdate();
        }
    }

    public void respond(Connection c, int offerId, String status) throws SQLException {
        String sql = "UPDATE offers SET status = ?, responded_at = CURRENT_TIMESTAMP WHERE offer_id = ? AND status = 'PENDING'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, status, offerId);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("This offer has already been responded to.", "45000", 1644);
            }
        }
    }

    public void rejectOtherPendingOffers(Connection c, String studentId, int acceptedOfferId) throws SQLException {
        String sql = "UPDATE offers o JOIN applications a ON a.application_id = o.application_id "
                + "SET o.status = 'REJECTED', o.responded_at = CURRENT_TIMESTAMP "
                + "WHERE a.student_id = ? AND o.offer_id <> ? AND o.status = 'PENDING'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, acceptedOfferId);
            ps.executeUpdate();
        }
    }

    public void delete(Connection c, int offerId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM offers WHERE offer_id = ?")) {
            ps.setInt(1, offerId);
            ps.executeUpdate();
        }
    }
}
