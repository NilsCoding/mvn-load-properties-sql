package io.github.nilscoding.maven.loadprops.sql.utils;

import java.util.Collection;
import java.util.Map;

/**
 * String utility functions.
 * @author NilsCoding
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Checks if the given string is logically empty.
     * @param str string to check
     * @return true if empty, false if not
     */
    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }
        return (str.trim().isEmpty() == true);
    }

    /**
     * Repeats the given character.
     * @param ch    char to repeat
     * @param count number of repeats
     * @return string
     */
    public static String repeat(char ch, int count) {
        if (count < 1) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < count; i++) {
            buffer.append(ch);
        }
        return buffer.toString();
    }

    /**
     * Masks a password string.
     * @param str string to mask
     * @return masked string
     */
    public static String maskPassword(String str) {
        if ((str == null) || (str.isEmpty())) {
            return "";
        }
        return maskPassword(str, '*');
    }

    /**
     * Masks a password string with given character.
     * @param str      string to mask
     * @param maskChar mask character
     * @return masked string
     */
    public static String maskPassword(String str, char maskChar) {
        if ((str == null) || (str.isEmpty())) {
            return "";
        }
        return repeat(maskChar, str.length());
    }

    /**
     * Checks if all strings are non-empty.
     * @param strs strings to check
     * @return true if all strings are not logically empty, false if at least one string is empty
     * (or no strings are given)
     */
    public static boolean allNotEmpty(String... strs) {
        if ((strs == null) || (strs.length == 0)) {
            return false;
        }
        for (String oneStr : strs) {
            if (isEmpty(oneStr)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns either the singular or plural string, depending on the given object.
     * @param obj         object to check
     * @param singularStr singular string
     * @param pluralStr   plural string
     * @return singular or plural string
     */
    public static String singularPlural(Object obj, String singularStr, String pluralStr) {
        if (obj == null) {
            return singularStr;
        }
        boolean isSingular = true;
        if (obj instanceof Number) {
            isSingular = ((Number) obj).longValue() == 1L;
        } else if (obj instanceof Collection) {
            isSingular = ((Collection<?>) obj).size() == 1;
        } else if (obj instanceof Map) {
            isSingular = ((Map) obj).size() == 1;
        }
        return (isSingular ? singularStr : pluralStr);
    }

}
