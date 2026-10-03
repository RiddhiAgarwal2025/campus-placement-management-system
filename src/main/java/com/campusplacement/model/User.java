package com.campusplacement.model;

public record User(int userId, String username, Role role, String displayName, String studentId) {
    public enum Role { OFFICER, STUDENT }
}
