package com.campusplacement.dao;

import com.campusplacement.model.Company;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CompanyDao {
    public List<Company> findAll(Connection c, String search) throws SQLException {
        String sql = "SELECT c.*, (SELECT COUNT(*) FROM job_profiles j WHERE j.company_id = c.company_id) AS jobs, "
                + "(SELECT COUNT(*) FROM placement_drives d JOIN job_profiles j ON j.job_id = d.job_id "
                + " WHERE j.company_id = c.company_id) AS drives FROM companies c "
                + "WHERE c.company_name LIKE ? OR c.industry LIKE ? ORDER BY c.company_name";
        List<Company> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, Jdbc.like(search), Jdbc.like(search));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Company(rs.getInt("company_id"), rs.getString("company_name"), rs.getString("industry"),
                            rs.getString("website"), rs.getString("contact_person"), rs.getString("contact_email"),
                            rs.getString("contact_phone"), rs.getInt("jobs"), rs.getInt("drives")));
                }
            }
        }
        return list;
    }

    public void insert(Connection c, Company co) throws SQLException {
        String sql = "INSERT INTO companies (company_name, industry, website, contact_person, contact_email, contact_phone) "
                + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, co.name(), co.industry(), co.website(), co.contactPerson(), co.contactEmail(), co.contactPhone());
            ps.executeUpdate();
        }
    }

    public void update(Connection c, Company co) throws SQLException {
        String sql = "UPDATE companies SET company_name = ?, industry = ?, website = ?, contact_person = ?, contact_email = ?, "
                + "contact_phone = ? WHERE company_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Jdbc.bind(ps, co.name(), co.industry(), co.website(), co.contactPerson(), co.contactEmail(), co.contactPhone(),
                    co.companyId());
            ps.executeUpdate();
        }
    }

    public void delete(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM companies WHERE company_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
