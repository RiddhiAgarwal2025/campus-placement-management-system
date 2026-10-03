package com.campusplacement.dao;

import com.campusplacement.model.AcademicRecord;
import com.campusplacement.model.Student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDao {
    private static final String SELECT = "SELECT s.student_id, s.full_name, s.email, s.phone, s.dept_id, d.dept_code, "
            + "d.dept_name, s.graduation_year, s.cgpa, s.backlogs FROM students s JOIN departments d ON d.dept_id = s.dept_id ";

    private Student map(ResultSet rs) throws SQLException {
        return new Student(rs.getString("student_id"), rs.getString("full_name"), rs.getString("email"),
                rs.getString("phone"), rs.getInt("dept_id"), rs.getString("dept_code"), rs.getString("dept_name"),
                rs.getInt("graduation_year"), rs.getBigDecimal("cgpa"), rs.getInt("backlogs"));
    }

    public List<Student> findAll(Connection c, String search, Integer deptId, Integer gradYear) throws SQLException {
        String sql = SELECT + "WHERE (s.student_id LIKE ? OR s.full_name LIKE ? OR s.email LIKE ?) "
                + "AND (? IS NULL OR s.dept_id = ?) AND (? IS NULL OR s.graduation_year = ?) ORDER BY s.student_id";
        List<Student> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            String q = Jdbc.like(search);
            Jdbc.bind(ps, q, q, q, deptId, deptId, gradYear, gradYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Optional<Student> findById(Connection c, String id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE s.student_id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Integer> graduationYears(Connection c) throws SQLException {
        List<Integer> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT DISTINCT graduation_year FROM students ORDER BY 1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getInt(1));
            }
        }
        return list;
    }

    public Integer userIdOf(Connection c, String studentId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT user_id FROM students WHERE student_id = ?")) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Jdbc.intOrNull(rs, "user_id") : null;
            }
        }
    }

    public void insert(Connection c, Student s, int userId) throws SQLException {
        String sql = "INSERT INTO students (student_id, user_id, full_name, email, phone, dept_id, graduation_year, cgpa, backlogs) "
                + "VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, s.studentId(), userId, s.fullName(), s.email(), s.phone(), s.deptId(), s.graduationYear(),
                    s.cgpa(), s.backlogs());
            ps.executeUpdate();
        }
    }

    public void update(Connection c, Student s) throws SQLException {
        String sql = "UPDATE students SET full_name = ?, email = ?, phone = ?, dept_id = ?, graduation_year = ?, cgpa = ?, "
                + "backlogs = ? WHERE student_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, s.fullName(), s.email(), s.phone(), s.deptId(), s.graduationYear(), s.cgpa(), s.backlogs(),
                    s.studentId());
            ps.executeUpdate();
        }
    }

    public void updateContact(Connection c, String studentId, String email, String phone) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE students SET email = ?, phone = ? WHERE student_id = ?")) {
            Jdbc.bind(ps, email, phone, studentId);
            ps.executeUpdate();
        }
    }

    public void delete(Connection c, String studentId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM students WHERE student_id = ?")) {
            ps.setString(1, studentId);
            ps.executeUpdate();
        }
    }

    // ---- academic records ----
    public List<AcademicRecord> records(Connection c, String studentId) throws SQLException {
        String sql = "SELECT record_id, student_id, semester, sgpa, marks_percentage FROM academic_records "
                + "WHERE student_id = ? ORDER BY semester";
        List<AcademicRecord> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AcademicRecord(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getBigDecimal(4),
                            rs.getBigDecimal(5)));
                }
            }
        }
        return list;
    }

    public void insertRecord(Connection c, AcademicRecord r) throws SQLException {
        String sql = "INSERT INTO academic_records (student_id, semester, sgpa, marks_percentage) VALUES (?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, r.studentId(), r.semester(), r.sgpa(), r.marks());
            ps.executeUpdate();
        }
    }

    public void updateRecord(Connection c, AcademicRecord r) throws SQLException {
        String sql = "UPDATE academic_records SET semester = ?, sgpa = ?, marks_percentage = ? WHERE record_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, r.semester(), r.sgpa(), r.marks(), r.recordId());
            ps.executeUpdate();
        }
    }

    public void deleteRecord(Connection c, int recordId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM academic_records WHERE record_id = ?")) {
            ps.setInt(1, recordId);
            ps.executeUpdate();
        }
    }
}
