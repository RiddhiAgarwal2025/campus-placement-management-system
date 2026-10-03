package com.campusplacement.service;

import com.campusplacement.dao.UserDao;
import com.campusplacement.db.Db;
import com.campusplacement.model.User;
import com.campusplacement.util.PasswordUtil;

public class AuthService {
    private final UserDao users = new UserDao();

    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ServiceException("Enter your username and password.");
        }
        User u = Db.query(c -> {
            UserDao.Credentials cr = users.findByUsername(c, username.trim()).orElse(null);
            if (cr == null || !PasswordUtil.verify(password, cr.passwordHash())) {
                throw new ServiceException("Invalid username or password.");
            }
            if (!cr.active()) {
                throw new ServiceException("This account has been disabled. Contact the placement office.");
            }
            users.touchLogin(c, cr.user().userId());
            return cr.user();
        });
        Session.start(u);
        return u;
    }

    public void logout() {
        Session.end();
    }

    public void changePassword(String current, String next, String confirm) {
        User u = Session.user();
        if (next == null || next.length() < 8) {
            throw new ServiceException("The new password must be at least 8 characters long.");
        }
        if (!next.equals(confirm)) {
            throw new ServiceException("The new passwords do not match.");
        }
        Db.txExec(c -> {
            if (!PasswordUtil.verify(current, users.passwordHash(c, u.userId()))) {
                throw new ServiceException("The current password is incorrect.");
            }
            users.updatePassword(c, u.userId(), PasswordUtil.hash(next));
        });
    }
}
