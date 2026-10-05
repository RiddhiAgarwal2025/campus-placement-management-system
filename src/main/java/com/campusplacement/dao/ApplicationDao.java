package com.campusplacement.dao;

import com.campusplacement.model.Application;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApplicationDao {
    private static final String SELECT = "SELECT * FROM vw_student_applications ";

    private Application map(ResultSet rs) throws SQLException {
        return new Application(rs.getInt("application_id"), rs.getString("student_id"), rs.getString("full_name"),
                rs.getString("dept_code"), rs.getBigDecimal("cgpa"), rs.getInt("drive_id"), rs.getInt("company_id"),
                rs.getString("company_name"), rs.getString("position"), rs.getBigDecimal("package_lpa"),
                Jdbc.date(rs, "drive_date"), Jdbc.date(rs, "application_deadline"), rs.getString("drive_status"),
                rs.getString("status"), Jdbc.time(rs, "applied_at"));
    }

    private List<Application> list(PreparedStatement ps) throws SQLException {
        List<Application> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public List<Application> find(Connection c, String search, Integer driveId, Integer companyId, Integer deptId,
                                  String status) throws SQLException {
        String sql = SELECT + "WHERE (student_id LIKE ? OR full_name LIKE ? OR company_name LIKE ? OR position LIKE ?) "
                + "AND (? IS NULL OR drive_id = ?) AND (? IS NULL OR company_id = ?) AND (? IS NULL OR dept_id = ?) "
                + "AND (? IS NULL OR status = ?) ORDER BY applied_at DESC";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            String q = Jdbc.like(search);
            Jdbc.bind(ps, q, q, q, q, driveId, driveId, companyId, companyId, deptId, deptId, status, status);
            return list(ps);
        }
    }

    public List<Application> byStudent(Connection c, String studentId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE student_id = ? ORDER BY applied_at DESC")) {
            ps.setString(1, studentId);
            return list(ps);
        }
    }

    public List<Application> recent(Connection c, int limit) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY applied_at DESC LIMIT ?")) {
            ps.setInt(1, limit);
            return list(ps);
        }
    }

    public Optional<Application> findById(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE application_id = ?")) {
            ps.setInt(1, id);
            List<Application> l = list(ps);
            return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
        }
    }

    /** Locks the application row inside the current transaction. */
    public Optional<Application> lock(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT application_id FROM applications WHERE application_id = ? FOR UPDATE")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
            }
        }
        return findById(c, id);
    }

    public boolean exists(Connection c, String studentId, int driveId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM applications WHERE student_id = ? AND drive_id = ? FOR UPDATE")) {
            Jdbc.bind(ps, studentId, driveId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insert(Connection c, String studentId, int driveId) throws SQLException {
        String sql = "INSERT INTO applications (student_id, drive_id, status) VALUES (?, ?, 'APPLIED')";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Jdbc.bind(ps, studentId, driveId);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (!k.next()) {
                    throw new SQLException("Failed to obtain generated application ID");
                }
                return k.getInt(1);
            }
        }
    }

    public List<Application> selectedWithoutOffers(Connection c) throws SQLException {
        String sql = "SELECT a.* FROM vw_student_applications a "
                + "LEFT JOIN offers o ON o.application_id = a.application_id "
                + "WHERE a.status = 'SELECTED' AND o.offer_id IS NULL "
                + "ORDER BY a.applied_at DESC";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            return list(ps);
        }
    }

    public void withdrawPending(Connection c, String studentId) throws SQLException {
        String sql = "UPDATE applications SET status = 'WITHDRAWN' WHERE student_id = ? AND status = 'APPLIED'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection c, int applicationId, String status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE applications SET status = ? WHERE application_id = ?")) {
            Jdbc.bind(ps, status, applicationId);
            ps.executeUpdate();
        }
    }

    public void delete(Connection c, int applicationId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM applications WHERE application_id = ?")) {
            ps.setInt(1, applicationId);
            ps.executeUpdate();
        }
    }
}
