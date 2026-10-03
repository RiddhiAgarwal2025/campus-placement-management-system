package com.campusplacement.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Loads database settings from ./db.properties (if present) or the bundled classpath db.properties. */
public final class DatabaseConfig {
    private static DatabaseConfig instance;
    private final String url;
    private final String username;
    private final String password;
    private final String source;

    private DatabaseConfig(String url, String username, String password, String source) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.source = source;
    }

    public static synchronized DatabaseConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static DatabaseConfig load() {
        Properties p = new Properties();
        String source;
        Path external = Path.of("db.properties");
        try {
            if (Files.isRegularFile(external)) {
                try (InputStream in = Files.newInputStream(external)) {
                    p.load(in);
                }
                source = external.toAbsolutePath().toString();
            } else {
                try (InputStream in = DatabaseConfig.class.getResourceAsStream("/db.properties")) {
                    if (in == null) {
                        throw new IllegalStateException("db.properties was not found on the classpath.");
                    }
                    p.load(in);
                }
                source = "bundled db.properties";
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read db.properties: " + e.getMessage(), e);
        }
        return new DatabaseConfig(
                p.getProperty("db.url", "jdbc:mysql://localhost:3306/campus_placement").trim(),
                p.getProperty("db.username", "root").trim(),
                p.getProperty("db.password", ""),
                source);
    }

    public String url() { return url; }
    public String username() { return username; }
    public String password() { return password; }
    public String source() { return source; }
}
