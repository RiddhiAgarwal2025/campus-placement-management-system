package com.campusplacement.model;

public record Company(int companyId, String name, String industry, String website, String contactPerson,
                      String contactEmail, String contactPhone, int jobCount, int driveCount) {
    @Override
    public String toString() { return name; }
}
