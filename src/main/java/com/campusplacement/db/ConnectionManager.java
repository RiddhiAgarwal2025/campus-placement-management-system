package com.campusplacement.db;

import com.campusplacement.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens JDBC connections using the single configuration source. */
public final class ConnectionManager {
    private ConnectionManager() { }

    public static Connection open() throws SQLException {
        DatabaseConfig cfg = DatabaseConfig.get();
        return DriverManager.getConnection(cfg.url(), cfg.username(), cfg.password());
    }
}
