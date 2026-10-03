package com.campusplacement.db;

import com.campusplacement.service.ServiceException;
import com.campusplacement.util.DbErrors;
import java.sql.Connection;
import java.sql.SQLException;

/** Runs units of work on a connection, translating SQL errors into user-friendly ServiceExceptions. */
public final class Db {
    private Db() { }

    @FunctionalInterface
    public interface Work<T> {
        T run(Connection c) throws SQLException;
    }

    @FunctionalInterface
    public interface VoidWork {
        void run(Connection c) throws SQLException;
    }

    /** Executes read or single-statement work in auto-commit mode. */
    public static <T> T query(Work<T> work) {
        try (Connection c = ConnectionManager.open()) {
            return work.run(c);
        } catch (SQLException e) {
            throw new ServiceException(DbErrors.translate(e), e);
        }
    }

    public static void exec(VoidWork work) {
        query(c -> { work.run(c); return null; });
    }

    /** Executes work inside a JDBC transaction: COMMIT on success, ROLLBACK on any failure. */
    public static <T> T tx(Work<T> work) {
        try (Connection c = ConnectionManager.open()) {
            c.setAutoCommit(false);
            c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            try {
                T result = work.run(c);
                c.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(c);
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException(DbErrors.translate(e), e);
        }
    }

    public static void txExec(VoidWork work) {
        tx(c -> { work.run(c); return null; });
    }

    private static void rollbackQuietly(Connection c) {
        try {
            c.rollback();
        } catch (SQLException ignored) {
            // connection is unusable; the original error is reported instead
        }
    }
}
