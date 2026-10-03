package com.campusplacement.model;

public record ReportDefinition(int number, String title, String description, String sql) {
    @Override
    public String toString() { return number + ". " + title; }
}
