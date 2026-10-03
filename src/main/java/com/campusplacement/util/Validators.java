package com.campusplacement.util;

import com.campusplacement.service.ServiceException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/** Input validation performed before any database operation. Messages are shown to users. */
public final class Validators {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+\\- ]{7,15}$");
    private static final Pattern CODE = Pattern.compile("^[A-Za-z0-9_-]+$");

    private Validators() { }

    public static String required(String label, String value, int maxLen) {
        String v = value == null ? "" : value.trim();
        if (v.isEmpty()) {
            throw new ServiceException(label + " is required.");
        }
        if (v.length() > maxLen) {
            throw new ServiceException(label + " must be at most " + maxLen + " characters.");
        }
        return v;
    }

    public static String optional(String label, String value, int maxLen) {
        String v = value == null ? "" : value.trim();
        if (v.length() > maxLen) {
            throw new ServiceException(label + " must be at most " + maxLen + " characters.");
        }
        return v.isEmpty() ? null : v;
    }

    public static String code(String label, String value, int maxLen) {
        String v = required(label, value, maxLen);
        if (!CODE.matcher(v).matches()) {
            throw new ServiceException(label + " may contain only letters, digits, '-' and '_'.");
        }
        return v.toUpperCase();
    }

    public static String email(String value, boolean required) {
        String v = required ? required("Email", value, 120) : optional("Email", value, 120);
        if (v != null && !EMAIL.matcher(v).matches()) {
            throw new ServiceException("Enter a valid email address (for example name@university.edu).");
        }
        return v == null ? null : v.toLowerCase();
    }

    public static String phone(String value) {
        String v = optional("Phone", value, 15);
        if (v != null && !PHONE.matcher(v).matches()) {
            throw new ServiceException("Phone must contain 7–15 digits.");
        }
        return v;
    }

    public static BigDecimal decimal(String label, String value, double min, double max, boolean inclusiveMin) {
        String v = required(label, value, 12);
        BigDecimal d;
        try {
            d = new BigDecimal(v);
        } catch (NumberFormatException e) {
            throw new ServiceException(label + " must be a number.");
        }
        boolean belowMin = inclusiveMin ? d.doubleValue() < min : d.doubleValue() <= min;
        if (belowMin || d.doubleValue() > max) {
            throw new ServiceException(label + " must be " + (inclusiveMin ? "between " + fmt(min) + " and " + fmt(max)
                    : "greater than " + fmt(min) + " and at most " + fmt(max)) + ".");
        }
        return d.setScale(2, RoundingMode.HALF_UP);
    }

    public static int integer(String label, String value, int min, int max) {
        String v = required(label, value, 9);
        try {
            int n = Integer.parseInt(v);
            if (n < min || n > max) {
                throw new ServiceException(label + " must be between " + min + " and " + max + ".");
            }
            return n;
        } catch (NumberFormatException e) {
            throw new ServiceException(label + " must be a whole number.");
        }
    }

    public static LocalDate date(String label, String value) {
        String v = required(label, value, 10);
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            throw new ServiceException(label + " must be a date in the form YYYY-MM-DD.");
        }
    }

    public static LocalDate optionalDate(String label, String value) {
        return value == null || value.isBlank() ? null : date(label, value);
    }

    private static String fmt(double d) {
        return d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
    }
}
