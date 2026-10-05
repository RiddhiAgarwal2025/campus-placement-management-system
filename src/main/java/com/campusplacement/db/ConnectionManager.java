package com.campusplacement.db;

import com.campusplacement.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/** Manages a high-performance JDBC connection pool using HikariCP. */
public final class ConnectionManager {
    private static volatile HikariDataSource dataSource;

    private ConnectionManager() { }

    private static HikariDataSource getDataSource() {
        if (dataSource == null) {
            synchronized (ConnectionManager.class) {
                if (dataSource == null) {
                    DatabaseConfig cfg = DatabaseConfig.get();
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl(cfg.url());
                    config.setUsername(cfg.username());
                    config.setPassword(cfg.password());
                    config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                    config.setMaximumPoolSize(10);
                    config.setMinimumIdle(2);
                    config.setIdleTimeout(60000);
                    config.setConnectionTimeout(10000);
                    config.setPoolName("CampusPlacementPool");
                    // Recommended MySQL performance properties
                    config.addDataSourceProperty("cachePrepStmts", "true");
                    config.addDataSourceProperty("prepStmtCacheSize", "250");
                    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                    config.addDataSourceProperty("useServerPrepStmts", "true");
                    dataSource = new HikariDataSource(config);
                }
            }
        }
        return dataSource;
    }

    public static Connection open() throws SQLException {
        return getDataSource().getConnection();
    }

    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }
}
