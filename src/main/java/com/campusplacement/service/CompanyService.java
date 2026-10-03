package com.campusplacement.service;

import com.campusplacement.dao.CompanyDao;
import com.campusplacement.dao.JobProfileDao;
import com.campusplacement.db.Db;
import com.campusplacement.model.Company;
import com.campusplacement.model.JobProfile;
import com.campusplacement.model.Skill;
import com.campusplacement.util.Validators;
import java.math.BigDecimal;
import java.util.List;

public class CompanyService {
    private final CompanyDao companies = new CompanyDao();
    private final JobProfileDao jobs = new JobProfileDao();

    public List<Company> list(String search) {
        return Db.query(c -> companies.findAll(c, search));
    }

    private Company validate(int id, String name, String industry, String website, String person, String email, String phone) {
        String w = Validators.optional("Website", website, 150);
        if (w != null && !w.matches("(?i)^https?://\\S+$")) {
            throw new ServiceException("Website must start with http:// or https://");
        }
        return new Company(id, Validators.required("Company name", name, 100), Validators.required("Industry", industry, 60),
                w, Validators.optional("Contact person", person, 100), Validators.email(email, false),
                Validators.phone(phone), 0, 0);
    }

    public void create(String name, String industry, String website, String person, String email, String phone) {
        Session.requireOfficer();
        Company co = validate(0, name, industry, website, person, email, phone);
        Db.exec(c -> companies.insert(c, co));
    }

    public void update(int id, String name, String industry, String website, String person, String email, String phone) {
        Session.requireOfficer();
        Company co = validate(id, name, industry, website, person, email, phone);
        Db.exec(c -> companies.update(c, co));
    }

    public void delete(Company co) {
        Session.requireOfficer();
        if (co.jobCount() > 0) {
            throw new ServiceException(co.name() + " has " + co.jobCount()
                    + " job profile(s). Delete those job profiles before deleting the company.");
        }
        Db.exec(c -> companies.delete(c, co.companyId()));
    }

    // ---- job profiles ----
    public List<JobProfile> jobs(Integer companyId, String search) {
        return Db.query(c -> jobs.findAll(c, companyId, search));
    }

    private JobProfile validateJob(int id, Company co, String position, String description, String pkg, String location,
                                   List<Skill> skills) {
        if (co == null) {
            throw new ServiceException("Company is required.");
        }
        BigDecimal p = Validators.decimal("Package (LPA)", pkg, 0, 500, false);
        return new JobProfile(id, co.companyId(), co.name(), Validators.required("Position", position, 100),
                Validators.optional("Description", description, 1000), p, Validators.required("Location", location, 100),
                skills == null ? List.of() : skills);
    }

    public void createJob(Company co, String position, String description, String pkg, String location, List<Skill> skills) {
        Session.requireOfficer();
        JobProfile j = validateJob(0, co, position, description, pkg, location, skills);
        Db.txExec(c -> {
            int id = jobs.insert(c, j);
            jobs.replaceSkills(c, id, j.requiredSkills().stream().map(Skill::skillId).toList());
        });
    }

    public void updateJob(int jobId, Company co, String position, String description, String pkg, String location,
                          List<Skill> skills) {
        Session.requireOfficer();
        JobProfile j = validateJob(jobId, co, position, description, pkg, location, skills);
        Db.txExec(c -> {
            jobs.update(c, j);
            jobs.replaceSkills(c, jobId, j.requiredSkills().stream().map(Skill::skillId).toList());
        });
    }

    public void deleteJob(int jobId) {
        Session.requireOfficer();
        Db.exec(c -> jobs.delete(c, jobId));
    }
}
