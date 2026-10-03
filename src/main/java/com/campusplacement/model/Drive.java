package com.campusplacement.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A placement drive joined with its company, job profile, criteria summary and application count. */
public record Drive(int driveId, int jobId, int companyId, String companyName, String position, BigDecimal packageLpa,
                    String location, LocalDate driveDate, LocalDate deadline, String venue, String status,
                    BigDecimal minCgpa, Integer maxBacklogs, Integer graduationYear, int applicationCount) {

    public enum Status { UPCOMING, OPEN, CLOSED, COMPLETED }

    public boolean acceptingApplications() {
        return "OPEN".equals(status) && !deadline.isBefore(LocalDate.now());
    }

    public String title() { return companyName + " — " + position; }

    @Override
    public String toString() { return "#" + driveId + "  " + title(); }
}
