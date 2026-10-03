package com.campusplacement.service;

import com.campusplacement.model.User;

/** Holds the signed-in user for the lifetime of the desktop session. */
public final class Session {
    private static User current;

    private Session() { }

    public static void start(User u) { current = u; }

    public static void end() { current = null; }

    public static User user() {
        if (current == null) {
            throw new ServiceException("Your session has ended. Please sign in again.");
        }
        return current;
    }

    public static boolean isOfficer() { return current != null && current.role() == User.Role.OFFICER; }

    public static void requireOfficer() {
        if (!isOfficer()) {
            throw new ServiceException("Only placement officers can perform this action.");
        }
    }

    /** Returns the signed-in student's ID, or fails if the user is not a student. */
    public static String studentId() {
        User u = user();
        if (u.role() != User.Role.STUDENT || u.studentId() == null) {
            throw new ServiceException("This action is available to students only.");
        }
        return u.studentId();
    }
}
