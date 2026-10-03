package com.campusplacement.util;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Converts JDBC/MySQL errors into plain-language messages. Raw SQL text is never shown. */
public final class DbErrors {
    private DbErrors() { }

    private static final Map<String, String> DUPLICATES = new LinkedHashMap<>();
    private static final Map<String, String> CHECKS = new LinkedHashMap<>();
    private static final Map<String, String> TABLES = new LinkedHashMap<>();

    static {
        DUPLICATES.put("students.PRIMARY", "A student with this student ID already exists.");
        DUPLICATES.put("uq_students_email", "A student with this email address already exists.");
        DUPLICATES.put("uq_users_username", "A user account with this username already exists.");
        DUPLICATES.put("uq_departments_code", "A department with this code already exists.");
        DUPLICATES.put("uq_departments_name", "A department with this name already exists.");
        DUPLICATES.put("uq_skills_name", "A skill with this name already exists.");
        DUPLICATES.put("student_skills.PRIMARY", "This skill is already assigned to the student.");
        DUPLICATES.put("job_profile_skills.PRIMARY", "This skill is already required by the job profile.");
        DUPLICATES.put("uq_companies_name", "A company with this name already exists.");
        DUPLICATES.put("uq_job_company_position", "This company already has a job profile with that position.");
        DUPLICATES.put("uq_application_student_drive", "The student has already applied to this drive.");
        DUPLICATES.put("uq_round_drive_sequence", "This drive already has a round with that sequence number.");
        DUPLICATES.put("uq_result_round_application", "A result for this candidate in this round already exists.");
        DUPLICATES.put("uq_offer_application", "An offer has already been issued for this application.");
        DUPLICATES.put("uq_academic_student_sem", "An academic record for this semester already exists.");
        DUPLICATES.put("uq_criteria_drive", "Eligibility criteria already exist for this drive.");

        CHECKS.put("chk_students_cgpa", "CGPA must be between 0.00 and 10.00.");
        CHECKS.put("chk_students_backlogs", "Backlogs must be zero or a positive number.");
        CHECKS.put("chk_students_gradyear", "Graduation year must be between 2000 and 2100.");
        CHECKS.put("chk_students_email", "Enter a valid email address.");
        CHECKS.put("chk_academic_semester", "Semester must be between 1 and 10.");
        CHECKS.put("chk_academic_sgpa", "SGPA must be between 0.00 and 10.00.");
        CHECKS.put("chk_academic_marks", "Marks must be between 0 and 100.");
        CHECKS.put("chk_job_package", "Package must be greater than 0 LPA.");
        CHECKS.put("chk_drive_dates", "The application deadline must be on or before the drive date.");
        CHECKS.put("chk_criteria_cgpa", "Minimum CGPA must be between 0.00 and 10.00.");
        CHECKS.put("chk_criteria_gradyear", "Graduation year must be between 2000 and 2100.");
        CHECKS.put("chk_round_sequence", "Round sequence must be between 1 and 20.");
        CHECKS.put("chk_offer_package", "Offer package must be greater than 0 LPA.");
        CHECKS.put("chk_offer_dates", "The joining date must be on or after the offer date.");
        CHECKS.put("chk_offer_response", "Offer status and response date are inconsistent.");

        TABLES.put("students", "students");
        TABLES.put("job_profiles", "job profiles");
        TABLES.put("placement_drives", "placement drives");
        TABLES.put("applications", "applications");
        TABLES.put("student_skills", "student skills");
        TABLES.put("job_profile_skills", "job profile skills");
        TABLES.put("eligibility_departments", "drive eligibility criteria");
        TABLES.put("eligibility_skills", "drive eligibility criteria");
        TABLES.put("selection_rounds", "selection rounds");
        TABLES.put("round_results", "round results");
        TABLES.put("offers", "offers");
    }

    private static final Pattern FK_CHILD = Pattern.compile("fails \\(`[^`]+`\\.`([^`]+)`");

    public static String translate(SQLException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage().replaceFirst("^\\(conn=\\d+\\)\\s*", "");
        int code = e.getErrorCode();
        String state = e.getSQLState() == null ? "" : e.getSQLState();

        if (state.startsWith("08") || code == 0 && msg.toLowerCase().contains("communications link")
                || msg.toLowerCase().contains("connection refused") || msg.toLowerCase().contains("could not connect")) {
            return "Cannot connect to the MySQL server. Make sure MySQL is running and the settings in db.properties are correct.";
        }
        switch (code) {
            case 1644:
                return msg;
            case 1045:
                return "MySQL rejected the username or password in db.properties.";
            case 1049:
                return "The campus_placement database does not exist. Run database/complete_database.sql first.";
            case 1062:
                for (Map.Entry<String, String> en : DUPLICATES.entrySet()) {
                    if (msg.contains(en.getKey())) {
                        return en.getValue();
                    }
                }
                return "This record already exists.";
            case 1451: {
                Matcher m = FK_CHILD.matcher(msg);
                String child = m.find() ? TABLES.getOrDefault(m.group(1), m.group(1).replace('_', ' ')) : "other records";
                return "This record is still used by " + child + " and cannot be deleted. Remove or reassign those first.";
            }
            case 1452:
                return "The selected related record (department, company, drive, skill or student) no longer exists.";
            case 3819:
                for (Map.Entry<String, String> en : CHECKS.entrySet()) {
                    if (msg.contains(en.getKey())) {
                        return en.getValue();
                    }
                }
                return "A value is outside the allowed range.";
            case 1048:
                return "A required field is missing.";
            case 1406:
                return "One of the values is too long.";
            case 1146:
                return "A required table is missing. Re-run database/complete_database.sql.";
            default:
                if (code == 0 && msg.toLowerCase().contains("no suitable driver")) {
                    return "The MySQL JDBC driver is missing. Build the project with 'mvn clean package' and run the generated JAR.";
                }
                return "The database could not complete the request (error " + code + ").";
        }
    }
}
