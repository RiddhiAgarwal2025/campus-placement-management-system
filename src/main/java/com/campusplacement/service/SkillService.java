package com.campusplacement.service;

import com.campusplacement.dao.SkillDao;
import com.campusplacement.db.Db;
import com.campusplacement.model.Skill;
import com.campusplacement.model.StudentSkill;
import com.campusplacement.util.Validators;
import java.util.List;
import java.util.Set;

public class SkillService {
    public static final List<String> PROFICIENCIES = List.of("BEGINNER", "INTERMEDIATE", "ADVANCED");
    private final SkillDao dao = new SkillDao();

    public List<Skill> list(String search) {
        return Db.query(c -> dao.findAll(c, search));
    }

    public void create(String name, String category) {
        Session.requireOfficer();
        String n = Validators.required("Skill name", name, 60);
        String cat = Validators.required("Category", category, 40);
        Db.exec(c -> dao.insert(c, n, cat));
    }

    public void update(int id, String name, String category) {
        Session.requireOfficer();
        String n = Validators.required("Skill name", name, 60);
        String cat = Validators.required("Category", category, 40);
        Db.exec(c -> dao.update(c, id, n, cat));
    }

    public void delete(Skill s) {
        Session.requireOfficer();
        Db.exec(c -> dao.delete(c, s.skillId()));
    }

    public List<StudentSkill> studentSkills(String studentId) {
        return Db.query(c -> dao.studentSkills(c, studentId));
    }

    public void assign(String studentId, Skill skill, String proficiency) {
        Session.requireOfficer();
        if (skill == null) {
            throw new ServiceException("Choose a skill to assign.");
        }
        String p = checkProficiency(proficiency);
        Db.exec(c -> dao.assign(c, studentId, skill.skillId(), p));
    }

    public void changeProficiency(String studentId, int skillId, String proficiency) {
        Session.requireOfficer();
        String p = checkProficiency(proficiency);
        Db.exec(c -> dao.updateProficiency(c, studentId, skillId, p));
    }

    public void unassign(String studentId, int skillId) {
        Session.requireOfficer();
        Db.exec(c -> dao.unassign(c, studentId, skillId));
    }

    private String checkProficiency(String p) {
        if (p == null || !PROFICIENCIES.contains(p)) {
            throw new ServiceException("Proficiency must be one of " + Set.copyOf(PROFICIENCIES) + ".");
        }
        return p;
    }
}
