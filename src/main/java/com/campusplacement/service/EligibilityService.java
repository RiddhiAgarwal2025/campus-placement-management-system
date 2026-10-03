package com.campusplacement.service;

import com.campusplacement.dao.DriveDao;
import com.campusplacement.dao.SkillDao;
import com.campusplacement.dao.StudentDao;
import com.campusplacement.db.Db;
import com.campusplacement.model.Department;
import com.campusplacement.model.EligibilityCriteria;
import com.campusplacement.model.EligibilityResult;
import com.campusplacement.model.Skill;
import com.campusplacement.model.Student;
import com.campusplacement.model.StudentSkill;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The single eligibility engine. Officer screens, student screens and application submission
 * all evaluate eligibility through {@link #evaluate}. The SQL view vw_drive_eligible_students
 * implements the same rules for reporting.
 */
public class EligibilityService {
    private final DriveDao drives = new DriveDao();
    private final StudentDao students = new StudentDao();
    private final SkillDao skills = new SkillDao();

    /** Pure rule evaluation: one reason per failed criterion. */
    public EligibilityResult evaluate(Student s, Set<Integer> studentSkillIds, EligibilityCriteria cr) {
        List<String> reasons = new ArrayList<>();
        if (s.cgpa().compareTo(cr.minCgpa()) < 0) {
            reasons.add("Minimum CGPA required: " + cr.minCgpa() + "  |  Student CGPA: " + s.cgpa());
        }
        if (s.backlogs() > cr.maxBacklogs()) {
            reasons.add("Maximum backlogs allowed: " + cr.maxBacklogs() + "  |  Student backlogs: " + s.backlogs());
        }
        if (cr.graduationYear() != null && s.graduationYear() != cr.graduationYear()) {
            reasons.add("Graduation year required: " + cr.graduationYear() + "  |  Student graduates: " + s.graduationYear());
        }
        if (!cr.departments().isEmpty() && cr.departments().stream().noneMatch(d -> d.deptId() == s.deptId())) {
            reasons.add("Open to departments: " + cr.departments().stream().map(Department::code)
                    .collect(Collectors.joining(", ")) + "  |  Student department: " + s.deptCode());
        }
        List<String> missing = cr.skills().stream().filter(k -> !studentSkillIds.contains(k.skillId()))
                .map(Skill::name).toList();
        if (!missing.isEmpty()) {
            reasons.add("Missing required skills: " + String.join(", ", missing));
        }
        return new EligibilityResult(s, reasons.isEmpty(), reasons);
    }

    public EligibilityCriteria criteria(Connection c, int driveId) throws SQLException {
        return drives.criteria(c, driveId).orElseThrow(() ->
                new ServiceException("Eligibility criteria have not been defined for drive #" + driveId + "."));
    }

    /** Evaluates one student for one drive using the given connection (used inside the application transaction). */
    public EligibilityResult check(Connection c, String studentId, int driveId) throws SQLException {
        Student s = students.findById(c, studentId)
                .orElseThrow(() -> new ServiceException("Student " + studentId + " was not found."));
        Set<Integer> ids = skills.studentSkills(c, studentId).stream().map(StudentSkill::skillId).collect(Collectors.toSet());
        return evaluate(s, ids, criteria(c, driveId));
    }

    public EligibilityResult check(String studentId, int driveId) {
        return Db.query(c -> check(c, studentId, driveId));
    }

    /** Evaluates every student for a drive; eligible students first, then by CGPA. */
    public List<EligibilityResult> checkAll(int driveId) {
        return Db.query(c -> {
            EligibilityCriteria cr = criteria(c, driveId);
            Map<String, Set<Integer>> skillMap = skills.allStudentSkillIds(c);
            return students.findAll(c, "", null, null).stream()
                    .map(s -> evaluate(s, skillMap.getOrDefault(s.studentId(), Set.of()), cr))
                    .sorted(Comparator.comparing((EligibilityResult r) -> !r.eligible())
                            .thenComparing(r -> r.student().cgpa(), Comparator.reverseOrder()))
                    .toList();
        });
    }

    public EligibilityCriteria criteria(int driveId) {
        return Db.query(c -> criteria(c, driveId));
    }
}
