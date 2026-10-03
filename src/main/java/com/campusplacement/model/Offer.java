package com.campusplacement.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Offer(int offerId, int applicationId, String studentId, String studentName, String deptCode,
                    String companyName, String position, String location, BigDecimal packageLpa, LocalDate offerDate,
                    LocalDate joiningDate, String status, LocalDateTime respondedAt) {
    public enum Status { PENDING, ACCEPTED, REJECTED }
}
