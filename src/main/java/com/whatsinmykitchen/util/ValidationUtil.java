package com.whatsinmykitchen.util;

public final class ValidationUtil {
    private ValidationUtil() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    public static int parsePositiveInt(String value, int defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public static Integer parseIntegerOrNull(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String sanitizeSqlLike(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("%", "\\%").replace("_", "\\_");
    }

    public static boolean isValidIngredientId(int id) {
        return id > 0;
    }
}
