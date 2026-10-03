package com.campusplacement.dao;

import com.campusplacement.model.TableData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDao {
    public TableData run(Connection c, String sql) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            int n = md.getColumnCount();
            List<String> cols = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                cols.add(md.getColumnLabel(i));
            }
            List<Object[]> rows = new ArrayList<>();
            while (rs.next()) {
                Object[] row = new Object[n];
                for (int i = 1; i <= n; i++) {
                    Object v = rs.getObject(i);
                    row[i - 1] = v instanceof java.sql.Date d ? d.toLocalDate()
                            : v instanceof java.sql.Timestamp t ? t.toLocalDateTime()
                            : v instanceof java.time.LocalDateTime ldt ? ldt : v;
                }
                rows.add(row);
            }
            return new TableData(cols, rows);
        }
    }
}
