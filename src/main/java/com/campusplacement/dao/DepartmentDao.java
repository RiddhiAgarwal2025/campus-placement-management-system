package com.campusplacement.dao;

import com.campusplacement.model.Department;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDao {
    public List<Department> findAll(Connection c, String search) throws SQLException {
        String sql = "SELECT d.dept_id, d.dept_code, d.dept_name, COUNT(s.student_id) AS n FROM departments d "
                + "LEFT JOIN students s ON s.dept_id = d.dept_id WHERE d.dept_code LIKE ? OR d.dept_name LIKE ? "
                + "GROUP BY d.dept_id, d.dept_code, d.dept_name ORDER BY d.dept_code";
        List<Department> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, Jdbc.like(search), Jdbc.like(search));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Department(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4)));
                }
            }
        }
        return list;
    }

    public void insert(Connection c, String code, String name) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO departments (dept_code, dept_name) VALUES (?, ?)")) {
            Jdbc.bind(ps, code, name);
            ps.executeUpdate();
        }
    }

    public void update(Connection c, int id, String code, String name) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE departments SET dept_code = ?, dept_name = ? WHERE dept_id = ?")) {
            Jdbc.bind(ps, code, name, id);
            ps.executeUpdate();
        }
    }

    public void delete(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM departments WHERE dept_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
