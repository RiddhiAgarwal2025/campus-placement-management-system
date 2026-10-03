package com.campusplacement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardDao {
    private Map<String, Number> single(Connection c, String sql, Object... params) throws SQLException {
        Map<String, Number> m = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                if (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        Object v = rs.getObject(i);
                        m.put(md.getColumnLabel(i), v instanceof Number n ? n : 0);
                    }
                }
            }
        }
        return m;
    }

    public Map<String, Number> officerStats(Connection c) throws SQLException {
        return single(c, "SELECT (SELECT COUNT(*) FROM students) AS students, "
                + "(SELECT COUNT(*) FROM companies) AS companies, "
                + "(SELECT COUNT(*) FROM placement_drives WHERE status = 'OPEN' AND application_deadline >= CURDATE()) AS active_drives, "
                + "(SELECT COUNT(*) FROM applications) AS applications, "
                + "(SELECT COUNT(DISTINCT student_id) FROM applications WHERE status IN ('SHORTLISTED','SELECTED')) AS shortlisted, "
                + "(SELECT COUNT(*) FROM offers) AS offers, "
                + "(SELECT COUNT(*) FROM offers WHERE status = 'ACCEPTED') AS accepted, "
                + "(SELECT COUNT(DISTINCT a.student_id) FROM offers o JOIN applications a ON a.application_id = o.application_id "
                + "  WHERE o.status = 'ACCEPTED') AS placed");
    }

    public Map<String, Number> studentStats(Connection c, String studentId) throws SQLException {
        return single(c, "SELECT (SELECT COUNT(*) FROM applications WHERE student_id = ?) AS applications, "
                + "(SELECT COUNT(*) FROM applications WHERE student_id = ? AND status IN ('SHORTLISTED','SELECTED')) AS shortlisted, "
                + "(SELECT COUNT(*) FROM offers o JOIN applications a ON a.application_id = o.application_id WHERE a.student_id = ?) AS offers, "
                + "(SELECT COUNT(*) FROM offers o JOIN applications a ON a.application_id = o.application_id "
                + "  WHERE a.student_id = ? AND o.status = 'PENDING') AS pending_offers, "
                + "(SELECT COUNT(*) FROM placement_drives WHERE status = 'OPEN' AND application_deadline >= CURDATE()) AS open_drives",
                studentId, studentId, studentId, studentId);
    }
}
