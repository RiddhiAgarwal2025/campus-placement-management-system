package com.campusplacement.service;

import com.campusplacement.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthorizationSecurityTest {

    @AfterEach
    void tearDown() {
        Session.end();
    }

    @Test
    @DisplayName("Student cannot access another student's academic profile (BOLA defense)")
    void testStudentCannotAccessPeerProfile() {
        User studentA = new User(1, "studentA", User.Role.STUDENT, "Student A", "2022CSE001");
        Session.start(studentA);

        StudentService studentService = new StudentService();

        assertThatThrownBy(() -> studentService.get("2022CSE999"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Unauthorized: You cannot access records belonging to another student.");
    }

    @Test
    @DisplayName("Student cannot access another student's semester records (BOLA defense)")
    void testStudentCannotAccessPeerRecords() {
        User studentA = new User(1, "studentA", User.Role.STUDENT, "Student A", "2022CSE001");
        Session.start(studentA);

        StudentService studentService = new StudentService();

        assertThatThrownBy(() -> studentService.records("2022CSE999"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Unauthorized: You cannot view records belonging to another student.");
    }

    @Test
    @DisplayName("Student cannot inspect another student's job applications (BOLA defense)")
    void testStudentCannotAccessPeerApplications() {
        User studentA = new User(1, "studentA", User.Role.STUDENT, "Student A", "2022CSE001");
        Session.start(studentA);

        ApplicationService appService = new ApplicationService();

        assertThatThrownBy(() -> appService.forStudent("2022CSE999"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Unauthorized: You cannot view applications belonging to another student.");
    }

    @Test
    @DisplayName("Student cannot view another student's offers and compensation packages (BOLA defense)")
    void testStudentCannotAccessPeerOffers() {
        User studentA = new User(1, "studentA", User.Role.STUDENT, "Student A", "2022CSE001");
        Session.start(studentA);

        OfferService offerService = new OfferService();

        assertThatThrownBy(() -> offerService.forStudent("2022CSE999"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Unauthorized: You cannot view offers belonging to another student.");
    }

    @Test
    @DisplayName("Student cannot call StudentService.list() to enumerate all students")
    void testStudentCannotListAllStudents() {
        User studentA = new User(1, "studentA", User.Role.STUDENT, "Student A", "2022CSE001");
        Session.start(studentA);

        StudentService studentService = new StudentService();
        assertThatThrownBy(() -> studentService.list("", null, null))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Only placement officers can perform this action.");
    }

    @Test
    @DisplayName("Student cannot call EligibilityService.checkAll() to view all candidates' eligibility")
    void testStudentCannotCheckAllEligibility() {
        User studentA = new User(1, "studentA", User.Role.STUDENT, "Student A", "2022CSE001");
        Session.start(studentA);

        EligibilityService eligibilityService = new EligibilityService();
        assertThatThrownBy(() -> eligibilityService.checkAll(1))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Only placement officers can perform this action.");
    }

    @Test
    @DisplayName("Student cannot check eligibility for another student in EligibilityService")
    void testStudentCannotCheckPeerEligibility() {
        User studentA = new User(1, "studentA", User.Role.STUDENT, "Student A", "2022CSE001");
        Session.start(studentA);

        EligibilityService eligibilityService = new EligibilityService();
        assertThatThrownBy(() -> eligibilityService.check("2022CSE999", 1))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Unauthorized: You cannot check eligibility for another student.");

        assertThatThrownBy(() -> eligibilityService.checkStudentDrives("2022CSE999", java.util.List.of(1, 2)))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Unauthorized: You cannot check eligibility for another student.");
    }

    @Test
    @DisplayName("Student cannot call SelectionService.rounds() or candidates()")
    void testStudentCannotAccessSelectionRoundsOrCandidates() {
        User studentA = new User(1, "studentA", User.Role.STUDENT, "Student A", "2022CSE001");
        Session.start(studentA);

        SelectionService selectionService = new SelectionService();
        assertThatThrownBy(() -> selectionService.rounds(1))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Only placement officers can perform this action.");

        com.campusplacement.model.SelectionRound round = new com.campusplacement.model.SelectionRound(
                1, 1, "Technical Test", 1, null, "UPCOMING", 0, 0);
        assertThatThrownBy(() -> selectionService.candidates(round))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Only placement officers can perform this action.");
    }
}
