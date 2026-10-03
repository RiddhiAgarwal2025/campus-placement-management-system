package com.campusplacement.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class Formats {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private Formats() { }

    public static String date(LocalDate d) { return d == null ? "—" : DATE.format(d); }

    public static String dateTime(LocalDateTime t) { return t == null ? "—" : DATE_TIME.format(t); }

    public static String lpa(BigDecimal v) { return v == null ? "—" : "₹ " + v.setScale(2, java.math.RoundingMode.HALF_UP) + " LPA"; }

    public static String iso(LocalDate d) { return d == null ? "" : d.toString(); }

    /** "Today", "in 3 days", "2 days ago". */
    public static String relative(LocalDate d) {
        if (d == null) {
            return "";
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), d);
        if (days == 0) {
            return "today";
        }
        if (days == 1) {
            return "tomorrow";
        }
        if (days == -1) {
            return "yesterday";
        }
        return days > 0 ? "in " + days + " days" : (-days) + " days ago";
    }

    public static String title(String enumName) {
        if (enumName == null || enumName.isEmpty()) {
            return "—";
        }
        String s = enumName.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
