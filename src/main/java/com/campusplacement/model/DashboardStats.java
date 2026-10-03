package com.campusplacement.model;

import java.util.Map;

/** Named counters read from the database for a dashboard. */
public record DashboardStats(Map<String, Number> values) {
    public Number get(String key) { return values.getOrDefault(key, 0); }
}
