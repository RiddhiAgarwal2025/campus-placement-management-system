package com.campusplacement.service;

import com.campusplacement.dao.StudentDao;
import com.campusplacement.dao.UserDao;
import com.campusplacement.db.Db;
import com.campusplacement.model.AcademicRecord;
import com.campusplacement.model.Department;
import com.campusplacement.model.Student;
import com.campusplacement.model.User;
import com.campusplacement.util.PasswordUtil;
import com.campusplacement.util.Validators;
import java.math.BigDecimal;
import java.util.List;

public class StudentService {
    /** Initial password given to student accounts created by the placement office. */
    public static final String DEFAULT_PASSWORD = "Student@123";

    private final StudentDao dao = new StudentDao();
    private final UserDao users = new UserDao();

    /** Raw form values for a student; validated by {@link #validate}. */
    public record StudentForm(String studentId, String fullName, String email, String phone, Department department,
                              String graduationYear, String cgpa, String backlogs) { }

    public List<Student> list(String search, Integer deptId, Integer gradYear) {
        Session.requireOfficer();
        return Db.query(c -> dao.findAll(c, search, deptId, gradYear));
    }

    public List<Integer> graduationYears() {
        Session.requireOfficer();
        return Db.query(dao::graduationYears);
    }

    public Student get(String studentId) {
        if (!Session.isOfficer() && !Session.studentId().equals(studentId)) {
            throw new ServiceException("Unauthorized: You cannot access records belonging to another student.");
        }
        return Db.query(c -> dao.findById(c, studentId))
                .orElseThrow(() -> new ServiceException("Student " + studentId + " was not found."));
    }

    public Student validate(StudentForm f) {
        String id = Validators.code("Student ID", f.studentId(), 20);
        String name = Validators.required("Full name", f.fullName(), 100);
        String email = Validators.email(f.email(), true);
        String phone = Validators.phone(f.phone());
        if (f.department() == null) {
            throw new ServiceException("Department is required.");
        }
        int year = Validators.integer("Graduation year", f.graduationYear(), 2000, 2100);
        BigDecimal cgpa = Validators.decimal("CGPA", f.cgpa(), 0, 10, true);
        int backlogs = Validators.integer("Backlogs", f.backlogs(), 0, 60);
        return new Student(id, name, email, phone, f.department().deptId(), f.department().code(), f.department().name(),
                year, cgpa, backlogs);
    }

    /** Creates the student and the matching login account in one transaction. */
    public Student create(StudentForm form) {
        Session.requireOfficer();
        Student s = validate(form);
        Db.txExec(c -> {
            if (dao.findById(c, s.studentId()).isPresent()) {
                throw new ServiceException("A student with ID " + s.studentId() + " already exists.");
            }
            int userId = users.insert(c, s.studentId(), PasswordUtil.hash(DEFAULT_PASSWORD), User.Role.STUDENT, s.fullName());
            dao.insert(c, s, userId);
        });
        return s;
    }

    public void update(StudentForm form) {
        Session.requireOfficer();
        Student s = validate(form);
        Db.txExec(c -> {
            dao.update(c, s);
            Integer userId = dao.userIdOf(c, s.studentId());
            if (userId != null) {
                users.updateIdentity(c, userId, s.studentId(), s.fullName());
            }
        });
    }

    public void delete(String studentId) {
        Session.requireOfficer();
        Db.txExec(c -> {
            Integer userId = dao.userIdOf(c, studentId);
            dao.delete(c, studentId);
            if (userId != null) {
                users.delete(c, userId);
            }
        });
    }

    /** Lets a signed-in student update their own contact details. */
    public void updateOwnContact(String email, String phone) {
        String id = Session.studentId();
        String e = Validators.email(email, true);
        String p = Validators.phone(phone);
        Db.exec(c -> dao.updateContact(c, id, e, p));
    }

    // ---- academic records ----
    public List<AcademicRecord> records(String studentId) {
        if (!Session.isOfficer() && !Session.studentId().equals(studentId)) {
            throw new ServiceException("Unauthorized: You cannot view records belonging to another student.");
        }
        return Db.query(c -> dao.records(c, studentId));
    }

    public AcademicRecord validateRecord(int recordId, String studentId, String semester, String sgpa, String marks) {
        int sem = Validators.integer("Semester", semester, 1, 10);
        BigDecimal g = Validators.decimal("SGPA", sgpa, 0, 10, true);
        BigDecimal m = Validators.decimal("Marks (%)", marks, 0, 100, true);
        return new AcademicRecord(recordId, studentId, sem, g, m);
    }

    public void addRecord(String studentId, String semester, String sgpa, String marks) {
        Session.requireOfficer();
        AcademicRecord r = validateRecord(0, studentId, semester, sgpa, marks);
        Db.exec(c -> dao.insertRecord(c, r));
    }

    public void updateRecord(int recordId, String studentId, String semester, String sgpa, String marks) {
        Session.requireOfficer();
        AcademicRecord r = validateRecord(recordId, studentId, semester, sgpa, marks);
        Db.exec(c -> dao.updateRecord(c, r));
    }

    public void deleteRecord(int recordId) {
        Session.requireOfficer();
        Db.exec(c -> dao.deleteRecord(c, recordId));
    }
}
