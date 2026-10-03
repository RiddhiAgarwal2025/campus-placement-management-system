package com.campusplacement.dao;

import com.campusplacement.model.Skill;
import com.campusplacement.model.StudentSkill;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SkillDao {
    public List<Skill> findAll(Connection c, String search) throws SQLException {
        String sql = "SELECT k.skill_id, k.skill_name, k.category, COUNT(ss.student_id) FROM skills k "
                + "LEFT JOIN student_skills ss ON ss.skill_id = k.skill_id WHERE k.skill_name LIKE ? OR k.category LIKE ? "
                + "GROUP BY k.skill_id, k.skill_name, k.category ORDER BY k.skill_name";
        List<Skill> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, Jdbc.like(search), Jdbc.like(search));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Skill(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4)));
                }
            }
        }
        return list;
    }

    public void insert(Connection c, String name, String category) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO skills (skill_name, category) VALUES (?, ?)")) {
            Jdbc.bind(ps, name, category);
            ps.executeUpdate();
        }
    }

    public void update(Connection c, int id, String name, String category) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE skills SET skill_name = ?, category = ? WHERE skill_id = ?")) {
            Jdbc.bind(ps, name, category, id);
            ps.executeUpdate();
        }
    }

    public void delete(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM skills WHERE skill_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<StudentSkill> studentSkills(Connection c, String studentId) throws SQLException {
        String sql = "SELECT ss.student_id, k.skill_id, k.skill_name, k.category, ss.proficiency FROM student_skills ss "
                + "JOIN skills k ON k.skill_id = ss.skill_id WHERE ss.student_id = ? ORDER BY k.skill_name";
        List<StudentSkill> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudentSkill(rs.getString(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5)));
                }
            }
        }
        return list;
    }

    /** Student ID → set of skill IDs, for bulk eligibility evaluation. */
    public Map<String, Set<Integer>> allStudentSkillIds(Connection c) throws SQLException {
        Map<String, Set<Integer>> map = new HashMap<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT student_id, skill_id FROM student_skills");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.computeIfAbsent(rs.getString(1), k -> new HashSet<>()).add(rs.getInt(2));
            }
        }
        return map;
    }

    public void assign(Connection c, String studentId, int skillId, String proficiency) throws SQLException {
        String sql = "INSERT INTO student_skills (student_id, skill_id, proficiency) VALUES (?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, studentId, skillId, proficiency);
            ps.executeUpdate();
        }
    }

    public void updateProficiency(Connection c, String studentId, int skillId, String proficiency) throws SQLException {
        String sql = "UPDATE student_skills SET proficiency = ? WHERE student_id = ? AND skill_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, proficiency, studentId, skillId);
            ps.executeUpdate();
        }
    }

    public void unassign(Connection c, String studentId, int skillId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM student_skills WHERE student_id = ? AND skill_id = ?")) {
            Jdbc.bind(ps, studentId, skillId);
            ps.executeUpdate();
        }
    }
}
