package com.campusplacement.service;

import com.campusplacement.dao.UserDao;
import com.campusplacement.db.Db;
import com.campusplacement.model.User;
import com.campusplacement.util.PasswordUtil;

public class AuthService {
    private final UserDao users = new UserDao();

    private static final String DUMMY_HASH =
            "pbkdf2_sha256$65536$AAAAAAAAAAAAAAAAAAAAAA==$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000L; // 15 minutes

    private static class AttemptRecord {
        int failures;
        long lockedUntil;
    }

    private static final java.util.Map<String, AttemptRecord> FAILED_ATTEMPTS =
            new java.util.concurrent.ConcurrentHashMap<>();

    private void checkLockout(String username) {
        AttemptRecord rec = FAILED_ATTEMPTS.get(username.toLowerCase());
        if (rec != null && rec.lockedUntil > System.currentTimeMillis()) {
            long remainingMinutes = Math.max(1, (rec.lockedUntil - System.currentTimeMillis()) / 60000);
            throw new ServiceException("Account is temporarily locked due to too many failed login attempts. Try again in "
                    + remainingMinutes + " minute(s).");
        }
    }

    private void recordFailure(String username) {
        FAILED_ATTEMPTS.compute(username.toLowerCase(), (k, v) -> {
            if (v == null) {
                v = new AttemptRecord();
            }
            v.failures++;
            if (v.failures >= MAX_FAILED_ATTEMPTS) {
                v.lockedUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
            }
            return v;
        });
    }

    private void recordSuccess(String username) {
        FAILED_ATTEMPTS.remove(username.toLowerCase());
    }

    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ServiceException("Enter your username and password.");
        }
        String cleanUser = username.trim();
        checkLockout(cleanUser);

        User u = Db.query(c -> {
            UserDao.Credentials cr = users.findByUsername(c, cleanUser).orElse(null);
            if (cr == null) {
                PasswordUtil.verify(password, DUMMY_HASH);
                recordFailure(cleanUser);
                throw new ServiceException("Invalid username or password.");
            }
            if (!PasswordUtil.verify(password, cr.passwordHash())) {
                recordFailure(cleanUser);
                throw new ServiceException("Invalid username or password.");
            }
            if (!cr.active()) {
                throw new ServiceException("This account has been disabled. Contact the placement office.");
            }
            recordSuccess(cleanUser);
            users.touchLogin(c, cr.user().userId());
            return cr.user();
        });
        Session.start(u);
        return u;
    }

    public boolean isDefaultPassword(int userId) {
        return Db.query(c -> {
            String hash = users.passwordHash(c, userId);
            return hash != null && (PasswordUtil.verify("Student@123", hash) || PasswordUtil.verify("Officer@123", hash));
        });
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
