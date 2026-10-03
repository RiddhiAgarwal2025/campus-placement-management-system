package com.campusplacement.model;

import java.math.BigDecimal;
import java.util.List;

public record EligibilityCriteria(int criteriaId, int driveId, BigDecimal minCgpa, int maxBacklogs,
                                  Integer graduationYear, List<Department> departments, List<Skill> skills) {
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("CGPA ≥ ").append(minCgpa).append(", backlogs ≤ ").append(maxBacklogs);
        if (graduationYear != null) {
            sb.append(", batch ").append(graduationYear);
        }
        sb.append(departments.isEmpty() ? ", all departments" : ", " + departments.stream().map(Department::code).toList()
                .toString().replaceAll("[\\[\\]]", ""));
        if (!skills.isEmpty()) {
            sb.append(", skills: ").append(skills.stream().map(Skill::name).toList().toString().replaceAll("[\\[\\]]", ""));
        }
        return sb.toString();
    }
}
