package com.campusplacement.dao;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Small JDBC helpers shared by the DAOs. */
final class Jdbc {
    private Jdbc() { }

    static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            int idx = i + 1;
            if (p == null) {
                ps.setNull(idx, Types.NULL);
            } else if (p instanceof LocalDate d) {
                ps.setDate(idx, Date.valueOf(d));
            } else if (p instanceof LocalDateTime t) {
                ps.setTimestamp(idx, Timestamp.valueOf(t));
            } else if (p instanceof BigDecimal b) {
                ps.setBigDecimal(idx, b);
            } else if (p instanceof Integer n) {
                ps.setInt(idx, n);
            } else if (p instanceof Boolean b) {
                ps.setBoolean(idx, b);
            } else {
                ps.setString(idx, p.toString());
            }
        }
    }

    static LocalDate date(ResultSet rs, String col) throws SQLException {
        Date d = rs.getDate(col);
        return d == null ? null : d.toLocalDate();
    }

    static LocalDateTime time(ResultSet rs, String col) throws SQLException {
        Timestamp t = rs.getTimestamp(col);
        return t == null ? null : t.toLocalDateTime();
    }

    static Integer intOrNull(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    static String like(String s) {
        return "%" + (s == null ? "" : s.trim()) + "%";
    }
}
