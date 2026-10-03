package com.campusplacement.model;

public record Department(int deptId, String code, String name, int studentCount) {
    @Override
    public String toString() { return code + " — " + name; }
}
