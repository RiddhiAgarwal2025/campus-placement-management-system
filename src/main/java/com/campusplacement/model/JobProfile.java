package com.campusplacement.model;

import java.math.BigDecimal;
import java.util.List;

public record JobProfile(int jobId, int companyId, String companyName, String position, String description,
                         BigDecimal packageLpa, String location, List<Skill> requiredSkills) {
    @Override
    public String toString() { return companyName + " — " + position; }
}
