package com.campusplacement.model;

public record Skill(int skillId, String name, String category, int studentCount) {
    @Override
    public String toString() { return name; }
}
