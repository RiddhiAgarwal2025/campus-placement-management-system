package com.campusplacement.model;

import java.util.Map;

/** Named counters read from the database for a dashboard. */
public record DashboardStats(Map<String, Number> values) {
    public Number get(String key) {
        Number n = values.get(key);
        return n != null ? n : 0;
    }

    public Number getOrDefault(String key, Number fallback) {
        Number n = values.get(key);
        return n != null ? n : fallback;
    }

    public int getInt(String key) {
        return get(key).intValue();
    }
}
