package com.campusplacement.service;

import com.campusplacement.model.Department;
import com.campusplacement.model.EligibilityCriteria;
import com.campusplacement.model.EligibilityResult;
import com.campusplacement.model.Skill;
import com.campusplacement.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityServiceTest {

    private EligibilityService service;
    private Department cse;
    private Department ece;
    private Skill javaSkill;
    private Skill sqlSkill;

    @BeforeEach
    void setUp() {
        service = new EligibilityService();
        cse = new Department(1, "CSE", "Computer Science", 100);
        ece = new Department(2, "ECE", "Electronics", 80);
        javaSkill = new Skill(10, "Java", "Programming", 50);
        sqlSkill = new Skill(20, "SQL", "Database", 40);
    }

    @Test
    @DisplayName("Student meeting all criteria is marked ELIGIBLE")
    void testStudentEligible() {
        Student student = new Student("2022CSE001", "Mohak Gupta", "mohak@univ.edu", "9876543210",
                cse.deptId(), cse.code(), cse.name(), 2026, new BigDecimal("8.50"), 0);

        EligibilityCriteria criteria = new EligibilityCriteria(1, 101, new BigDecimal("7.00"), 1,
                2026, List.of(cse), List.of(javaSkill));

        EligibilityResult result = service.evaluate(student, Set.of(javaSkill.skillId(), sqlSkill.skillId()), criteria);

        assertThat(result.eligible()).isTrue();
        assertThat(result.reasons()).isEmpty();
    }

    @Test
    @DisplayName("Student with lower CGPA is marked NOT ELIGIBLE with reason")
    void testStudentLowerCgpa() {
        Student student = new Student("2022CSE002", "Jane Doe", "jane@univ.edu", "9876543211",
                cse.deptId(), cse.code(), cse.name(), 2026, new BigDecimal("6.50"), 0);

        EligibilityCriteria criteria = new EligibilityCriteria(1, 101, new BigDecimal("7.50"), 0,
                2026, List.of(cse), List.of());

        EligibilityResult result = service.evaluate(student, Set.of(), criteria);

        assertThat(result.eligible()).isFalse();
        assertThat(result.reasons()).hasSize(1);
        assertThat(result.reasons().get(0)).contains("Minimum CGPA required: 7.50");
    }

    @Test
    @DisplayName("Student exceeding backlogs is marked NOT ELIGIBLE")
    void testStudentExceedsBacklogs() {
        Student student = new Student("2022CSE003", "Bob Smith", "bob@univ.edu", "9876543212",
                cse.deptId(), cse.code(), cse.name(), 2026, new BigDecimal("8.00"), 2);

        EligibilityCriteria criteria = new EligibilityCriteria(1, 101, new BigDecimal("7.00"), 0,
                2026, List.of(cse), List.of());

        EligibilityResult result = service.evaluate(student, Set.of(), criteria);

        assertThat(result.eligible()).isFalse();
        assertThat(result.reasons()).hasSize(1);
        assertThat(result.reasons().get(0)).contains("Maximum backlogs allowed: 0");
    }

    @Test
    @DisplayName("Student with wrong graduation year is marked NOT ELIGIBLE")
    void testGraduationYearMismatch() {
        Student student = new Student("2021CSE004", "Alice Senior", "alice@univ.edu", "9876543213",
                cse.deptId(), cse.code(), cse.name(), 2025, new BigDecimal("9.00"), 0);

        EligibilityCriteria criteria = new EligibilityCriteria(1, 101, new BigDecimal("7.00"), 1,
                2026, List.of(cse), List.of());

        EligibilityResult result = service.evaluate(student, Set.of(), criteria);

        assertThat(result.eligible()).isFalse();
        assertThat(result.reasons()).hasSize(1);
        assertThat(result.reasons().get(0)).contains("Graduation year required: 2026");
    }

    @Test
    @DisplayName("Student from non-eligible department is marked NOT ELIGIBLE")
    void testDepartmentMismatch() {
        Student student = new Student("2022ECE005", "Charlie ECE", "charlie@univ.edu", "9876543214",
                ece.deptId(), ece.code(), ece.name(), 2026, new BigDecimal("8.50"), 0);

        EligibilityCriteria criteria = new EligibilityCriteria(1, 101, new BigDecimal("7.00"), 1,
                2026, List.of(cse), List.of());

        EligibilityResult result = service.evaluate(student, Set.of(), criteria);

        assertThat(result.eligible()).isFalse();
        assertThat(result.reasons()).hasSize(1);
        assertThat(result.reasons().get(0)).contains("Open to departments: CSE");
    }

    @Test
    @DisplayName("Student missing required skills is marked NOT ELIGIBLE")
    void testMissingRequiredSkills() {
        Student student = new Student("2022CSE006", "Dave Code", "dave@univ.edu", "9876543215",
                cse.deptId(), cse.code(), cse.name(), 2026, new BigDecimal("8.00"), 0);

        EligibilityCriteria criteria = new EligibilityCriteria(1, 101, new BigDecimal("7.00"), 1,
                2026, List.of(cse), List.of(javaSkill, sqlSkill));

        // Student only has Java, missing SQL
        EligibilityResult result = service.evaluate(student, Set.of(javaSkill.skillId()), criteria);

        assertThat(result.eligible()).isFalse();
        assertThat(result.reasons()).hasSize(1);
        assertThat(result.reasons().get(0)).contains("Missing required skills: SQL");
    }

    @Test
    @DisplayName("Multiple failed criteria report all reasons together")
    void testMultipleFailures() {
        Student student = new Student("2022ECE007", "Multi Fail", "fail@univ.edu", "9876543216",
                ece.deptId(), ece.code(), ece.name(), 2025, new BigDecimal("6.00"), 3);

        EligibilityCriteria criteria = new EligibilityCriteria(1, 101, new BigDecimal("7.50"), 0,
                2026, List.of(cse), List.of(javaSkill));

        EligibilityResult result = service.evaluate(student, Set.of(), criteria);

        assertThat(result.eligible()).isFalse();
        assertThat(result.reasons()).hasSize(5); // CGPA, backlogs, grad year, department, missing skill
    }
}
