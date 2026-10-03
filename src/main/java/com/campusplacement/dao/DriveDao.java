package com.campusplacement.dao;

import com.campusplacement.model.Department;
import com.campusplacement.model.Drive;
import com.campusplacement.model.EligibilityCriteria;
import com.campusplacement.model.Skill;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DriveDao {
    private static final String SELECT = "SELECT * FROM vw_drive_summary ";

    private Drive map(ResultSet rs) throws SQLException {
        return new Drive(rs.getInt("drive_id"), rs.getInt("job_id"), rs.getInt("company_id"), rs.getString("company_name"),
                rs.getString("position"), rs.getBigDecimal("package_lpa"), rs.getString("location"),
                Jdbc.date(rs, "drive_date"), Jdbc.date(rs, "application_deadline"), rs.getString("venue"),
                rs.getString("status"), rs.getBigDecimal("min_cgpa"), Jdbc.intOrNull(rs, "max_backlogs"),
                Jdbc.intOrNull(rs, "graduation_year"), rs.getInt("application_count"));
    }

    public List<Drive> findAll(Connection c, String search, String status, Integer companyId) throws SQLException {
        String sql = SELECT + "WHERE (company_name LIKE ? OR position LIKE ? OR location LIKE ?) "
                + "AND (? IS NULL OR status = ?) AND (? IS NULL OR company_id = ?) ORDER BY drive_date DESC, drive_id DESC";
        List<Drive> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            String q = Jdbc.like(search);
            Jdbc.bind(ps, q, q, q, status, status, companyId, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<Drive> upcoming(Connection c, int limit) throws SQLException {
        String sql = SELECT + "WHERE status IN ('UPCOMING','OPEN') ORDER BY application_deadline, drive_date LIMIT ?";
        List<Drive> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Optional<Drive> findById(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE drive_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    /** Locks the drive row for the remainder of the transaction and returns its status and deadline. */
    public Optional<Drive> lock(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT drive_id FROM placement_drives WHERE drive_id = ? FOR UPDATE")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
            }
        }
        return findById(c, id);
    }

    public int insert(Connection c, Drive d) throws SQLException {
        String sql = "INSERT INTO placement_drives (job_id, drive_date, application_deadline, venue, status) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Jdbc.bind(ps, d.jobId(), d.driveDate(), d.deadline(), d.venue(), d.status());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                return k.getInt(1);
            }
        }
    }

    public void update(Connection c, Drive d) throws SQLException {
        String sql = "UPDATE placement_drives SET job_id = ?, drive_date = ?, application_deadline = ?, venue = ?, status = ? "
                + "WHERE drive_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, d.jobId(), d.driveDate(), d.deadline(), d.venue(), d.status(), d.driveId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection c, int driveId, String status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE placement_drives SET status = ? WHERE drive_id = ?")) {
            Jdbc.bind(ps, status, driveId);
            ps.executeUpdate();
        }
    }

    public void delete(Connection c, int driveId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM placement_drives WHERE drive_id = ?")) {
            ps.setInt(1, driveId);
            ps.executeUpdate();
        }
    }

    // ---- eligibility criteria ----
    public Optional<EligibilityCriteria> criteria(Connection c, int driveId) throws SQLException {
        int criteriaId;
        java.math.BigDecimal minCgpa;
        int maxBacklogs;
        Integer gradYear;
        String sql = "SELECT criteria_id, min_cgpa, max_backlogs, graduation_year FROM eligibility_criteria WHERE drive_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, driveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                criteriaId = rs.getInt(1);
                minCgpa = rs.getBigDecimal(2);
                maxBacklogs = rs.getInt(3);
                gradYear = Jdbc.intOrNull(rs, "graduation_year");
            }
        }
        List<Department> depts = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT d.dept_id, d.dept_code, d.dept_name FROM eligibility_departments ed "
                + "JOIN departments d ON d.dept_id = ed.dept_id WHERE ed.criteria_id = ? ORDER BY d.dept_code")) {
            ps.setInt(1, criteriaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    depts.add(new Department(rs.getInt(1), rs.getString(2), rs.getString(3), 0));
                }
            }
        }
        List<Skill> skills = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT k.skill_id, k.skill_name, k.category FROM eligibility_skills es "
                + "JOIN skills k ON k.skill_id = es.skill_id WHERE es.criteria_id = ? ORDER BY k.skill_name")) {
            ps.setInt(1, criteriaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    skills.add(new Skill(rs.getInt(1), rs.getString(2), rs.getString(3), 0));
                }
            }
        }
        return Optional.of(new EligibilityCriteria(criteriaId, driveId, minCgpa, maxBacklogs, gradYear, depts, skills));
    }

    /** Inserts or replaces the criteria of a drive, including department and skill lists. */
    public void saveCriteria(Connection c, EligibilityCriteria cr) throws SQLException {
        String upsert = "INSERT INTO eligibility_criteria (drive_id, min_cgpa, max_backlogs, graduation_year) VALUES (?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE min_cgpa = VALUES(min_cgpa), max_backlogs = VALUES(max_backlogs), "
                + "graduation_year = VALUES(graduation_year)";
        try (PreparedStatement ps = c.prepareStatement(upsert)) {
            Jdbc.bind(ps, cr.driveId(), cr.minCgpa(), cr.maxBacklogs(), cr.graduationYear());
            ps.executeUpdate();
        }
        int criteriaId;
        try (PreparedStatement ps = c.prepareStatement("SELECT criteria_id FROM eligibility_criteria WHERE drive_id = ?")) {
            ps.setInt(1, cr.driveId());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                criteriaId = rs.getInt(1);
            }
        }
        for (String t : new String[] {"eligibility_departments", "eligibility_skills"}) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + t + " WHERE criteria_id = ?")) {
                ps.setInt(1, criteriaId);
                ps.executeUpdate();
            }
        }
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO eligibility_departments (criteria_id, dept_id) VALUES (?, ?)")) {
            for (Department d : cr.departments()) {
                Jdbc.bind(ps, criteriaId, d.deptId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO eligibility_skills (criteria_id, skill_id) VALUES (?, ?)")) {
            for (Skill s : cr.skills()) {
                Jdbc.bind(ps, criteriaId, s.skillId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
