package com.campusplacement.dao;

import com.campusplacement.model.RoundCandidate;
import com.campusplacement.model.SelectionRound;
import com.campusplacement.model.StudentRoundStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SelectionDao {
    public List<SelectionRound> rounds(Connection c, int driveId) throws SQLException {
        String sql = "SELECT r.round_id, r.drive_id, r.round_name, r.sequence_no, r.round_date, r.status, "
                + "SUM(rr.result = 'PASS') AS passes, SUM(rr.result = 'FAIL') AS fails FROM selection_rounds r "
                + "LEFT JOIN round_results rr ON rr.round_id = r.round_id WHERE r.drive_id = ? "
                + "GROUP BY r.round_id, r.drive_id, r.round_name, r.sequence_no, r.round_date, r.status ORDER BY r.sequence_no";
        List<SelectionRound> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, driveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new SelectionRound(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4),
                            Jdbc.date(rs, "round_date"), rs.getString(6), rs.getInt("passes"), rs.getInt("fails")));
                }
            }
        }
        return list;
    }

    public Optional<SelectionRound> round(Connection c, int roundId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT drive_id FROM selection_rounds WHERE round_id = ?")) {
            ps.setInt(1, roundId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                int driveId = rs.getInt(1);
                return rounds(c, driveId).stream().filter(r -> r.roundId() == roundId).findFirst();
            }
        }
    }

    public void insertRound(Connection c, SelectionRound r) throws SQLException {
        String sql = "INSERT INTO selection_rounds (drive_id, round_name, sequence_no, round_date, status) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, r.driveId(), r.name(), r.sequenceNo(), r.roundDate(), r.status());
            ps.executeUpdate();
        }
    }

    public void updateRound(Connection c, SelectionRound r) throws SQLException {
        String sql = "UPDATE selection_rounds SET round_name = ?, sequence_no = ?, round_date = ?, status = ? WHERE round_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, r.name(), r.sequenceNo(), r.roundDate(), r.status(), r.roundId());
            ps.executeUpdate();
        }
    }

    public void deleteRound(Connection c, int roundId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM selection_rounds WHERE round_id = ?")) {
            ps.setInt(1, roundId);
            ps.executeUpdate();
        }
    }

    /** Candidates of a drive with their result in the previous round (if any) and in this round. */
    public List<RoundCandidate> candidates(Connection c, int driveId, int roundId, int previousRoundId) throws SQLException {
        String sql = "SELECT a.application_id, s.student_id, s.full_name, d.dept_code, a.status, prev.result AS prev_result, "
                + "cur.result AS cur_result, cur.remarks FROM applications a JOIN students s ON s.student_id = a.student_id "
                + "JOIN departments d ON d.dept_id = s.dept_id "
                + "LEFT JOIN round_results prev ON prev.application_id = a.application_id AND prev.round_id = ? "
                + "LEFT JOIN round_results cur ON cur.application_id = a.application_id AND cur.round_id = ? "
                + "WHERE a.drive_id = ? ORDER BY (cur.result IS NULL) DESC, s.full_name";
        List<RoundCandidate> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, previousRoundId, roundId, driveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new RoundCandidate(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getString(5), rs.getString("prev_result"), rs.getString("cur_result"), rs.getString("remarks")));
                }
            }
        }
        return list;
    }

    public String result(Connection c, int roundId, int applicationId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT result FROM round_results WHERE round_id = ? AND application_id = ? FOR UPDATE")) {
            Jdbc.bind(ps, roundId, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public void insertResult(Connection c, int roundId, int applicationId, String result, String remarks) throws SQLException {
        String sql = "INSERT INTO round_results (round_id, application_id, result, remarks) VALUES (?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, roundId, applicationId, result, remarks);
            ps.executeUpdate();
        }
    }

    public void updateResult(Connection c, int roundId, int applicationId, String result, String remarks) throws SQLException {
        String sql = "UPDATE round_results SET result = ?, remarks = ?, recorded_at = CURRENT_TIMESTAMP "
                + "WHERE round_id = ? AND application_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, result, remarks, roundId, applicationId);
            ps.executeUpdate();
        }
    }

    public void deleteResult(Connection c, int roundId, int applicationId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM round_results WHERE round_id = ? AND application_id = ?")) {
            Jdbc.bind(ps, roundId, applicationId);
            ps.executeUpdate();
        }
    }

    /** Number of rounds of the application's drive and how many of them the candidate has passed. */
    public int[] progress(Connection c, int applicationId) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM selection_rounds r JOIN applications a ON a.drive_id = r.drive_id "
                + "WHERE a.application_id = ?), (SELECT COUNT(*) FROM round_results WHERE application_id = ? AND result = 'PASS'), "
                + "(SELECT COUNT(*) FROM round_results WHERE application_id = ? AND result = 'FAIL')";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, applicationId, applicationId, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new int[] {rs.getInt(1), rs.getInt(2), rs.getInt(3)};
            }
        }
    }

    public List<StudentRoundStatus> studentRounds(Connection c, String studentId) throws SQLException {
        String sql = "SELECT a.application_id, va.company_name, va.position, r.sequence_no, r.round_name, r.round_date, "
                + "r.status, rr.result FROM applications a JOIN vw_student_applications va ON va.application_id = a.application_id "
                + "JOIN selection_rounds r ON r.drive_id = a.drive_id "
                + "LEFT JOIN round_results rr ON rr.round_id = r.round_id AND rr.application_id = a.application_id "
                + "WHERE a.student_id = ? ORDER BY a.applied_at DESC, r.sequence_no";
        List<StudentRoundStatus> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudentRoundStatus(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4),
                            rs.getString(5), Jdbc.date(rs, "round_date"), rs.getString(7), rs.getString(8)));
                }
            }
        }
        return list;
    }
}
