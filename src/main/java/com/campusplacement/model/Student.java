package com.campusplacement.model;

import java.math.BigDecimal;

public record Student(String studentId, String fullName, String email, String phone, int deptId, String deptCode,
                      String deptName, int graduationYear, BigDecimal cgpa, int backlogs) {
    @Override
    public String toString() { return fullName + " (" + studentId + ")"; }
}
