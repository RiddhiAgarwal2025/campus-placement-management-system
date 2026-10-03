package com.campusplacement.service;

import com.campusplacement.dao.DepartmentDao;
import com.campusplacement.db.Db;
import com.campusplacement.model.Department;
import com.campusplacement.util.Validators;
import java.util.List;

public class DepartmentService {
    private final DepartmentDao dao = new DepartmentDao();

    public List<Department> list(String search) {
        return Db.query(c -> dao.findAll(c, search));
    }

    public void create(String code, String name) {
        Session.requireOfficer();
        String cd = Validators.code("Department code", code, 10);
        String nm = Validators.required("Department name", name, 100);
        Db.exec(c -> dao.insert(c, cd, nm));
    }

    public void update(int id, String code, String name) {
        Session.requireOfficer();
        String cd = Validators.code("Department code", code, 10);
        String nm = Validators.required("Department name", name, 100);
        Db.exec(c -> dao.update(c, id, cd, nm));
    }

    public void delete(Department d) {
        Session.requireOfficer();
        if (d.studentCount() > 0) {
            throw new ServiceException(d.code() + " still has " + d.studentCount()
                    + " student(s). Move or delete those students before deleting the department.");
        }
        Db.exec(c -> dao.delete(c, d.deptId()));
    }
}
