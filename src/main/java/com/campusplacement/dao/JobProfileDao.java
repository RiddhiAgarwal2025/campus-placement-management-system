package com.campusplacement.dao;

import com.campusplacement.model.JobProfile;
import com.campusplacement.model.Skill;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobProfileDao {
    public List<JobProfile> findAll(Connection c, Integer companyId, String search) throws SQLException {
        Map<Integer, List<Skill>> skills = new HashMap<>();
        String skSql = "SELECT jps.job_id, k.skill_id, k.skill_name, k.category FROM job_profile_skills jps "
                + "JOIN skills k ON k.skill_id = jps.skill_id ORDER BY k.skill_name";
        try (PreparedStatement ps = c.prepareStatement(skSql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                skills.computeIfAbsent(rs.getInt(1), k -> new ArrayList<>())
                        .add(new Skill(rs.getInt(2), rs.getString(3), rs.getString(4), 0));
            }
        }
        String sql = "SELECT j.job_id, j.company_id, c.company_name, j.position, j.description, j.package_lpa, j.location "
                + "FROM job_profiles j JOIN companies c ON c.company_id = j.company_id "
                + "WHERE (? IS NULL OR j.company_id = ?) AND (c.company_name LIKE ? OR j.position LIKE ? OR j.location LIKE ?) "
                + "ORDER BY c.company_name, j.position";
        List<JobProfile> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            String q = Jdbc.like(search);
            Jdbc.bind(ps, companyId, companyId, q, q, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    list.add(new JobProfile(id, rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5),
                            rs.getBigDecimal(6), rs.getString(7), skills.getOrDefault(id, List.of())));
                }
            }
        }
        return list;
    }

    public int insert(Connection c, JobProfile j) throws SQLException {
        String sql = "INSERT INTO job_profiles (company_id, position, description, package_lpa, location) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Jdbc.bind(ps, j.companyId(), j.position(), j.description(), j.packageLpa(), j.location());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                return k.getInt(1);
            }
        }
    }

    public void update(Connection c, JobProfile j) throws SQLException {
        String sql = "UPDATE job_profiles SET company_id = ?, position = ?, description = ?, package_lpa = ?, location = ? "
                + "WHERE job_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, j.companyId(), j.position(), j.description(), j.packageLpa(), j.location(), j.jobId());
            ps.executeUpdate();
        }
    }

    public void replaceSkills(Connection c, int jobId, List<Integer> skillIds) throws SQLException {
        try (PreparedStatement del = c.prepareStatement("DELETE FROM job_profile_skills WHERE job_id = ?")) {
            del.setInt(1, jobId);
            del.executeUpdate();
        }
        try (PreparedStatement ins = c.prepareStatement("INSERT INTO job_profile_skills (job_id, skill_id) VALUES (?, ?)")) {
            for (Integer id : skillIds) {
                Jdbc.bind(ins, jobId, id);
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    public void delete(Connection c, int jobId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM job_profiles WHERE job_id = ?")) {
            ps.setInt(1, jobId);
            ps.executeUpdate();
        }
    }
}
