package com.campusplacement.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Application(int applicationId, String studentId, String studentName, String deptCode, BigDecimal cgpa,
                          int driveId, int companyId, String companyName, String position, BigDecimal packageLpa,
                          LocalDate driveDate, LocalDate deadline, String driveStatus, String status,
                          LocalDateTime appliedAt) {
    public enum Status { APPLIED, SHORTLISTED, SELECTED, REJECTED }
}
