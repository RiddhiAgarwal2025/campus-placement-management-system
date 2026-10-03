package com.campusplacement.service;

import com.campusplacement.dao.ReportDao;
import com.campusplacement.db.Db;
import com.campusplacement.model.ReportDefinition;
import com.campusplacement.model.TableData;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Loads the report queries from reports.sql (the same file as database/07_reports.sql) and runs them. */
public class ReportService {
    private static List<ReportDefinition> cache;
    private final ReportDao dao = new ReportDao();

    public synchronized List<ReportDefinition> reports() {
        if (cache == null) {
            cache = parse(load());
        }
        return cache;
    }

    public TableData run(ReportDefinition r) {
        Session.requireOfficer();
        return Db.query(c -> dao.run(c, r.sql()));
    }

    private String load() {
        try (InputStream in = ReportService.class.getResourceAsStream("/reports.sql")) {
            if (in == null) {
                throw new ServiceException("Report definitions (reports.sql) are missing from the application.");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ServiceException("Report definitions could not be read.", e);
        }
    }

    static List<ReportDefinition> parse(String text) {
        List<ReportDefinition> list = new ArrayList<>();
        String title = null;
        String desc = null;
        StringBuilder sql = new StringBuilder();
        for (String line : text.split("\\R")) {
            if (line.startsWith("-- @report")) {
                if (title != null) {
                    list.add(new ReportDefinition(list.size() + 1, title, desc, clean(sql)));
                }
                String[] parts = line.substring("-- @report".length()).split("\\|", 2);
                title = parts[0].trim();
                desc = parts.length > 1 ? parts[1].trim() : "";
                sql.setLength(0);
            } else if (title != null && !line.trim().startsWith("--")) {
                sql.append(line).append('\n');
            }
        }
        if (title != null) {
            list.add(new ReportDefinition(list.size() + 1, title, desc, clean(sql)));
        }
        return list;
    }

    private static String clean(StringBuilder sb) {
        String s = sb.toString().trim();
        return s.endsWith(";") ? s.substring(0, s.length() - 1) : s;
    }
}
