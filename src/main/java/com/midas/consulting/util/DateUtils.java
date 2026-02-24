package com.midas.consulting.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive date utility class for handling date operations
 * Supports both legacy java.util.Date and modern java.time APIs
 *
 * Created by Dheeraj Singh.
 */
public class DateUtils {

    // ============================================================================
    // DATE FORMAT CONSTANTS
    // ============================================================================

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String US_DATE_FORMAT = "MM/dd/yyyy";
    public static final String EUROPEAN_DATE_FORMAT = "dd/MM/yyyy";
    public static final String COMPACT_DATE_FORMAT = "yyyyMMdd";
    public static final String DISPLAY_DATE_FORMAT = "MMM dd, yyyy";
    public static final String FULL_DATE_FORMAT = "EEEE, MMMM dd, yyyy";

    // Thread-safe SimpleDateFormat instances
    private static final SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
    private static final SimpleDateFormat sdfDateTime = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
    private static final SimpleDateFormat sdfTimestamp = new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT);
    private static final SimpleDateFormat sdfIso = new SimpleDateFormat(ISO_DATE_FORMAT);

    // Modern DateTimeFormatter instances
    private static final DateTimeFormatter DTF_DATE = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    private static final DateTimeFormatter DTF_DATETIME = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);
    private static final DateTimeFormatter DTF_TIMESTAMP = DateTimeFormatter.ofPattern(DEFAULT_TIMESTAMP_FORMAT);
    private static final DateTimeFormatter DTF_ISO = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT);

    static {
        // Set timezone for legacy formatters
        TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
        sdf.setTimeZone(utcTimeZone);
        sdfDateTime.setTimeZone(utcTimeZone);
        sdfTimestamp.setTimeZone(utcTimeZone);
        sdfIso.setTimeZone(utcTimeZone);
    }

    // ============================================================================
    // EXISTING METHODS (PRESERVED)
    // ============================================================================

    /**
     * Returns today's date as java.util.Date object
     *
     * @return today's date as java.util.Date object
     */
    public static Date today() {
        return new Date();
    }

    /**
     * Returns today's date as yyyy-MM-dd format
     *
     * @return today's date as yyyy-MM-dd format
     */
    public static String todayStr() {
        return sdf.format(today());
    }

    /**
     * Returns the formatted String date for the passed java.util.Date object
     *
     * @param date
     * @return
     */
    public static String formattedDate(Date date) {
        return date != null ? sdf.format(date) : todayStr();
    }

    // ============================================================================
    // ENHANCED DATE CREATION METHODS
    // ============================================================================

    /**
     * Returns current date and time as java.util.Date
     */
    public static Date now() {
        return new Date();
    }

    /**
     * Returns current LocalDateTime
     */
    public static LocalDateTime nowLocal() {
        return LocalDateTime.now();
    }

    /**
     * Returns current UTC date time
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Returns current date time as ISO string
     */
    public static String nowIsoString() {
        return sdfIso.format(now());
    }

    /**
     * Returns current timestamp as formatted string
     */
    public static String nowTimestamp() {
        return sdfTimestamp.format(now());
    }

    /**
     * Create date from year, month, day
     */
    public static Date createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Create date with time from components
     */
    public static Date createDateTime(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    // ============================================================================
    // DATE FORMATTING METHODS
    // ============================================================================

    /**
     * Format date with custom pattern
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    /**
     * Format LocalDateTime with custom pattern
     */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    /**
     * Format date as ISO string
     */
    public static String formatDateIso(Date date) {
        return date != null ? sdfIso.format(date) : null;
    }

    /**
     * Format date for display (MMM dd, yyyy)
     */
    public static String formatDateDisplay(Date date) {
        if (date == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat(DISPLAY_DATE_FORMAT);
        return formatter.format(date);
    }

    /**
     * Format date as full date string
     */
    public static String formatDateFull(Date date) {
        if (date == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat(FULL_DATE_FORMAT);
        return formatter.format(date);
    }

    /**
     * Format date as compact string (yyyyMMdd)
     */
    public static String formatDateCompact(Date date) {
        if (date == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat(COMPACT_DATE_FORMAT);
        return formatter.format(date);
    }

    // ============================================================================
    // DATE PARSING METHODS
    // ============================================================================

    /**
     * Parse date from string with default format
     */
    public static Date parseDate(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return sdf.parse(dateStr);
    }

    /**
     * Parse date from string with custom format
     */
    public static Date parseDate(String dateStr, String pattern) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.parse(dateStr);
    }

    /**
     * Parse date with exception handling - returns null on error
     */
    public static Date parseDateSafe(String dateStr) {
        try {
            return parseDate(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Parse date with custom format - returns null on error
     */
    public static Date parseDateSafe(String dateStr, String pattern) {
        try {
            return parseDate(dateStr, pattern);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Parse LocalDateTime from string
     */
    public static LocalDateTime parseLocalDateTime(String dateTimeStr) throws DateTimeParseException {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
        return LocalDateTime.parse(dateTimeStr, DTF_DATETIME);
    }

    /**
     * Parse LocalDateTime with custom format
     */
    public static LocalDateTime parseLocalDateTime(String dateTimeStr, String pattern) throws DateTimeParseException {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    // ============================================================================
    // DATE MANIPULATION METHODS
    // ============================================================================

    /**
     * Add days to date
     */
    public static Date addDays(Date date, int days) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    /**
     * Add months to date
     */
    public static Date addMonths(Date date, int months) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    /**
     * Add years to date
     */
    public static Date addYears(Date date, int years) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, years);
        return calendar.getTime();
    }

    /**
     * Add hours to date
     */
    public static Date addHours(Date date, int hours) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }

    /**
     * Add minutes to date
     */
    public static Date addMinutes(Date date, int minutes) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    /**
     * Subtract days from date
     */
    public static Date subtractDays(Date date, int days) {
        return addDays(date, -days);
    }

    /**
     * Subtract months from date
     */
    public static Date subtractMonths(Date date, int months) {
        return addMonths(date, -months);
    }

    /**
     * Subtract years from date
     */
    public static Date subtractYears(Date date, int years) {
        return addYears(date, -years);
    }

    // ============================================================================
    // DATE COMPARISON AND CALCULATION METHODS
    // ============================================================================

    /**
     * Check if two dates are on the same day
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(Date date) {
        return isSameDay(date, today());
    }

    /**
     * Check if date is yesterday
     */
    public static boolean isYesterday(Date date) {
        return isSameDay(date, subtractDays(today(), 1));
    }

    /**
     * Check if date is tomorrow
     */
    public static boolean isTomorrow(Date date) {
        return isSameDay(date, addDays(today(), 1));
    }

    /**
     * Calculate days between two dates
     */
    public static long daysBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return 0;
        long diff = endDate.getTime() - startDate.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    /**
     * Calculate hours between two dates
     */
    public static long hoursBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return 0;
        long diff = endDate.getTime() - startDate.getTime();
        return TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
    }

    /**
     * Calculate minutes between two dates
     */
    public static long minutesBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return 0;
        long diff = endDate.getTime() - startDate.getTime();
        return TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS);
    }

    /**
     * Calculate age in years from birth date
     */
    public static int calculateAge(Date birthDate) {
        if (birthDate == null) return 0;
        Calendar birth = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        birth.setTime(birthDate);

        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    // ============================================================================
    // DATE RANGE AND PERIOD METHODS
    // ============================================================================

    /**
     * Get start of day (00:00:00.000)
     */
    public static Date startOfDay(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Get end of day (23:59:59.999)
     */
    public static Date endOfDay(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * Get start of month
     */
    public static Date startOfMonth(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return startOfDay(calendar.getTime());
    }

    /**
     * Get end of month
     */
    public static Date endOfMonth(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return endOfDay(calendar.getTime());
    }

    /**
     * Get start of year
     */
    public static Date startOfYear(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return startOfDay(calendar.getTime());
    }

    /**
     * Get end of year
     */
    public static Date endOfYear(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        return endOfDay(calendar.getTime());
    }

    /**
     * Get list of dates in range
     */
    public static List<Date> getDateRange(Date startDate, Date endDate) {
        List<Date> dates = new ArrayList<>();
        if (startDate == null || endDate == null || startDate.after(endDate)) {
            return dates;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        while (!calendar.getTime().after(endDate)) {
            dates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dates;
    }

    // ============================================================================
    // BUSINESS DATE METHODS
    // ============================================================================

    /**
     * Check if date is weekend (Saturday or Sunday)
     */
    public static boolean isWeekend(Date date) {
        if (date == null) return false;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

    /**
     * Check if date is weekday (Monday to Friday)
     */
    public static boolean isWeekday(Date date) {
        return !isWeekend(date);
    }

    /**
     * Get next business day (skipping weekends)
     */
    public static Date getNextBusinessDay(Date date) {
        if (date == null) return null;
        Date nextDay = addDays(date, 1);
        while (isWeekend(nextDay)) {
            nextDay = addDays(nextDay, 1);
        }
        return nextDay;
    }

    /**
     * Get previous business day (skipping weekends)
     */
    public static Date getPreviousBusinessDay(Date date) {
        if (date == null) return null;
        Date prevDay = subtractDays(date, 1);
        while (isWeekend(prevDay)) {
            prevDay = subtractDays(prevDay, 1);
        }
        return prevDay;
    }

    /**
     * Add business days (excluding weekends)
     */
    public static Date addBusinessDays(Date date, int businessDays) {
        if (date == null) return null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int addedDays = 0;
        while (addedDays < businessDays) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            if (!isWeekend(calendar.getTime())) {
                addedDays++;
            }
        }

        return calendar.getTime();
    }

    // ============================================================================
    // UTILITY AND VALIDATION METHODS
    // ============================================================================

    /**
     * Check if year is leap year
     */
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0);
    }

    /**
     * Get days in month
     */
    public static int getDaysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * Get current quarter (1-4)
     */
    public static int getCurrentQuarter() {
        return getQuarter(today());
    }

    /**
     * Get quarter for date (1-4)
     */
    public static int getQuarter(Date date) {
        if (date == null) return 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
        return (month - 1) / 3 + 1;
    }

    /**
     * Get week of year
     */
    public static int getWeekOfYear(Date date) {
        if (date == null) return 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Get relative time description (e.g., "2 hours ago", "in 3 days")
     */
    public static String getRelativeTime(Date date) {
        if (date == null) return "Unknown";

        long diff = System.currentTimeMillis() - date.getTime();
        long absDiff = Math.abs(diff);

        boolean past = diff > 0;
        String suffix = past ? " ago" : "";
        String prefix = past ? "" : "in ";

        if (absDiff < 60 * 1000) {
            return "just now";
        } else if (absDiff < 60 * 60 * 1000) {
            long minutes = absDiff / (60 * 1000);
            return prefix + minutes + " minute" + (minutes != 1 ? "s" : "") + suffix;
        } else if (absDiff < 24 * 60 * 60 * 1000) {
            long hours = absDiff / (60 * 60 * 1000);
            return prefix + hours + " hour" + (hours != 1 ? "s" : "") + suffix;
        } else if (absDiff < 7 * 24 * 60 * 60 * 1000) {
            long days = absDiff / (24 * 60 * 60 * 1000);
            return prefix + days + " day" + (days != 1 ? "s" : "") + suffix;
        } else {
            return formatDateDisplay(date);
        }
    }

    /**
     * Convert Date to LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Convert LocalDateTime to Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Get min date from array
     */
    public static Date getMinDate(Date... dates) {
        if (dates == null || dates.length == 0) return null;

        Date min = null;
        for (Date date : dates) {
            if (date != null && (min == null || date.before(min))) {
                min = date;
            }
        }
        return min;
    }

    /**
     * Get max date from array
     */
    public static Date getMaxDate(Date... dates) {
        if (dates == null || dates.length == 0) return null;

        Date max = null;
        for (Date date : dates) {
            if (date != null && (max == null || date.after(max))) {
                max = date;
            }
        }
        return max;
    }

    /**
     * Check if date is in range (inclusive)
     */
    public static boolean isInRange(Date date, Date startDate, Date endDate) {
        if (date == null || startDate == null || endDate == null) return false;
        return !date.before(startDate) && !date.after(endDate);
    }

    /**
     * Get time zone offset in hours
     */
    public static int getTimeZoneOffset() {
        return TimeZone.getDefault().getRawOffset() / (1000 * 60 * 60);
    }

    /**
     * Format duration between two dates in human readable format
     */
    public static String formatDuration(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return "Unknown duration";

        long diff = Math.abs(endDate.getTime() - startDate.getTime());

        long days = diff / (24 * 60 * 60 * 1000);
        diff %= (24 * 60 * 60 * 1000);

        long hours = diff / (60 * 60 * 1000);
        diff %= (60 * 60 * 1000);

        long minutes = diff / (60 * 1000);

        StringBuilder result = new StringBuilder();
        if (days > 0) {
            result.append(days).append(" day").append(days != 1 ? "s" : "");
        }
        if (hours > 0) {
            if (result.length() > 0) result.append(", ");
            result.append(hours).append(" hour").append(hours != 1 ? "s" : "");
        }
        if (minutes > 0 && days == 0) {
            if (result.length() > 0) result.append(", ");
            result.append(minutes).append(" minute").append(minutes != 1 ? "s" : "");
        }

        return result.length() > 0 ? result.toString() : "0 minutes";
    }
}//package com.midas.consulting.util;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.*;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
//import java.time.temporal.ChronoUnit;
//import java.time.temporal.TemporalAdjusters;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
///**
// * Comprehensive date utility class for handling date operations
// * Supports both legacy java.util.Date and modern java.time APIs
// *
// * Created by Dheeraj Singh.
// */
//public class DateUtils {
//
//    // ============================================================================
//    // DATE FORMAT CONSTANTS
//    // ============================================================================
//
//    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
//    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
//    public static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
//    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
//    public static final String US_DATE_FORMAT = "MM/dd/yyyy";
//    public static final String EUROPEAN_DATE_FORMAT = "dd/MM/yyyy";
//    public static final String COMPACT_DATE_FORMAT = "yyyyMMdd";
//    public static final String DISPLAY_DATE_FORMAT = "MMM dd, yyyy";
//    public static final String FULL_DATE_FORMAT = "EEEE, MMMM dd, yyyy";
//
//    // Thread-safe SimpleDateFormat instances
//    private static final SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
//    private static final SimpleDateFormat sdfDateTime = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
//    private static final SimpleDateFormat sdfTimestamp = new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT);
//    private static final SimpleDateFormat sdfIso = new SimpleDateFormat(ISO_DATE_FORMAT);
//
//    // Modern DateTimeFormatter instances
//    private static final DateTimeFormatter DTF_DATE = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
//    private static final DateTimeFormatter DTF_DATETIME = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);
//    private static final DateTimeFormatter DTF_TIMESTAMP = DateTimeFormatter.ofPattern(DEFAULT_TIMESTAMP_FORMAT);
//    private static final DateTimeFormatter DTF_ISO = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT);
//
//    static {
//        // Set timezone for legacy formatters
//        TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
//        sdf.setTimeZone(utcTimeZone);
//        sdfDateTime.setTimeZone(utcTimeZone);
//        sdfTimestamp.setTimeZone(utcTimeZone);
//        sdfIso.setTimeZone(utcTimeZone);
//    }
//
//    // ============================================================================
//    // EXISTING METHODS (PRESERVED)
//    // ============================================================================
//
//    /**
//     * Returns today's date as java.util.Date object
//     *
//     * @return today's date as java.util.Date object
//     */
//    public static Date today() {
//        return new Date();
//    }
//
//    /**
//     * Returns today's date as yyyy-MM-dd format
//     *
//     * @return today's date as yyyy-MM-dd format
//     */
//    public static String todayStr() {
//        return sdf.format(today());
//    }
//
//    /**
//     * Returns the formatted String date for the passed java.util.Date object
//     *
//     * @param date
//     * @return
//     */
//    public static String formattedDate(Date date) {
//        return date != null ? sdf.format(date) : todayStr();
//    }
//
//    // ============================================================================
//    // ENHANCED DATE CREATION METHODS
//    // ============================================================================
//
//    /**
//     * Returns current date and time as java.util.Date
//     */
//    public static Date now() {
//        return new Date();
//    }
//
//    /**
//     * Returns current LocalDateTime
//     */
//    public static LocalDateTime nowLocal() {
//        return LocalDateTime.now();
//    }
//
//    /**
//     * Returns current UTC date time
//     */
//    public static LocalDateTime nowUtc() {
//        return LocalDateTime.now(ZoneOffset.UTC);
//    }
//
//    /**
//     * Returns current date time as ISO string
//     */
//    public static String nowIsoString() {
//        return sdfIso.format(now());
//    }
//
//    /**
//     * Returns current timestamp as formatted string
//     */
//    public static String nowTimestamp() {
//        return sdfTimestamp.format(now());
//    }
//
//    /**
//     * Create date from year, month, day
//     */
//    public static Date createDate(int year, int month, int day) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(year, month - 1, day, 0, 0, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return calendar.getTime();
//    }
//
//    /**
//     * Create date with time from components
//     */
//    public static Date createDateTime(int year, int month, int day, int hour, int minute, int second) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(year, month - 1, day, hour, minute, second);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return calendar.getTime();
//    }
//
//    // ============================================================================
//    // DATE FORMATTING METHODS
//    // ============================================================================
//
//    /**
//     * Format date with custom pattern
//     */
//    public static String formatDate(Date date, String pattern) {
//        if (date == null) return null;
//        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
//        return formatter.format(date);
//    }
//
//    /**
//     * Format LocalDateTime with custom pattern
//     */
//    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
//        if (dateTime == null) return null;
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
//        return dateTime.format(formatter);
//    }
//
//    /**
//     * Format date as ISO string
//     */
//    public static String formatDateIso(Date date) {
//        return date != null ? sdfIso.format(date) : null;
//    }
//
//    /**
//     * Format date for display (MMM dd, yyyy)
//     */
//    public static String formatDateDisplay(Date date) {
//        if (date == null) return null;
//        SimpleDateFormat formatter = new SimpleDateFormat(DISPLAY_DATE_FORMAT);
//        return formatter.format(date);
//    }
//
//    /**
//     * Format date as full date string
//     */
//    public static String formatDateFull(Date date) {
//        if (date == null) return null;
//        SimpleDateFormat formatter = new SimpleDateFormat(FULL_DATE_FORMAT);
//        return formatter.format(date);
//    }
//
//    /**
//     * Format date as compact string (yyyyMMdd)
//     */
//    public static String formatDateCompact(Date date) {
//        if (date == null) return null;
//        SimpleDateFormat formatter = new SimpleDateFormat(COMPACT_DATE_FORMAT);
//        return formatter.format(date);
//    }
//
//    // ============================================================================
//    // DATE PARSING METHODS
//    // ============================================================================
//
//    /**
//     * Parse date from string with default format
//     */
//    public static Date parseDate(String dateStr) throws ParseException {
//        if (dateStr == null || dateStr.trim().isEmpty()) return null;
//        return sdf.parse(dateStr);
//    }
//
//    /**
//     * Parse date from string with custom format
//     */
//    public static Date parseDate(String dateStr, String pattern) throws ParseException {
//        if (dateStr == null || dateStr.trim().isEmpty()) return null;
//        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
//        return formatter.parse(dateStr);
//    }
//
//    /**
//     * Parse date with exception handling - returns null on error
//     */
//    public static Date parseDateSafe(String dateStr) {
//        try {
//            return parseDate(dateStr);
//        } catch (ParseException e) {
//            return null;
//        }
//    }
//
//    /**
//     * Parse date with custom format - returns null on error
//     */
//    public static Date parseDateSafe(String dateStr, String pattern) {
//        try {
//            return parseDate(dateStr, pattern);
//        } catch (ParseException e) {
//            return null;
//        }
//    }
//
//    /**
//     * Parse LocalDateTime from string
//     */
//    public static LocalDateTime parseLocalDateTime(String dateTimeStr) throws DateTimeParseException {
//        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
//        return LocalDateTime.parse(dateTimeStr, DTF_DATETIME);
//    }
//
//    /**
//     * Parse LocalDateTime with custom format
//     */
//    public static LocalDateTime parseLocalDateTime(String dateTimeStr, String pattern) throws DateTimeParseException {
//        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
//        return LocalDateTime.parse(dateTimeStr, formatter);
//    }
//
//    // ============================================================================
//    // DATE MANIPULATION METHODS
//    // ============================================================================
//
//    /**
//     * Add days to date
//     */
//    public static Date addDays(Date date, int days) {
//        if (date == null) return null;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.add(Calendar.DAY_OF_MONTH, days);
//        return calendar.getTime();
//    }
//
//    /**
//     * Add months to date
//     */
//    public static Date addMonths(Date date, int months) {
//        if (date == null) return null;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.add(Calendar.MONTH, months);
//        return calendar.getTime();
//    }
//
//    /**
//     * Add years to date
//     */
//    public static Date addYears(Date date, int years) {
//        if (date == null) return null;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.add(Calendar.YEAR, years);
//        return calendar.getTime();
//    }
//
//    /**
//     * Add hours to date
//     */
//    public static Date addHours(Date date, int hours) {
//        if (date == null) return null;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.add(Calendar.HOUR_OF_DAY, hours);
//        return calendar.getTime();
//    }
//
//    /**
//     * Add minutes to date
//     */
//    public static Date addMinutes(Date date, int minutes) {
//        if (date == null) return null;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.add(Calendar.MINUTE, minutes);
//        return calendar.getTime();
//    }
//
//    /**
//     * Subtract days from date
//     */
//    public static Date subtractDays(Date date, int days) {
//        return addDays(date, -days);
//    }
//
//    /**
//     * Subtract months from date
//     */
//    public static Date subtractMonths(Date date, int months) {
//        return addMonths(date, -months);
//    }
//
//    /**
//     * Subtract years from date
//     */
//    public static Date subtractYears(Date date, int years) {
//        return addYears(date, -years);
//    }
//
//    // ============================================================================
//    // DATE COMPARISON AND CALCULATION METHODS
//    // ============================================================================
//
//    /**
//     * Check if two dates are on the same day
//     */
//    public static boolean isSameDay(Date date1, Date date2) {
//        if (date1 == null || date2 == null) return false;
//        Calendar cal1 = Calendar.getInstance();
//        Calendar cal2 = Calendar.getInstance();
//        cal1.setTime(date1);
//        cal2.setTime(date2);
//
//        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
//                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
//    }
//
//    /**
//     * Check if date is today
//     */
//    public static boolean isToday(Date date) {
//        return isSameDay(date, today());
//    }
//
//    /**
//     * Check if date is yesterday
//     */
//    public static boolean isYesterday(Date date) {
//        return isSameDay(date, subtractDays(today(), 1));
//    }
//
//    /**
//     * Check if date is tomorrow
//     */
//    public static boolean isTomorrow(Date date) {
//        return isSameDay(date, addDays(today(), 1));
//    }
//
//    /**
//     * Calculate days between two dates
//     */
//    public static long daysBetween(Date startDate, Date endDate) {
//        if (startDate == null || endDate == null) return 0;
//        long diff = endDate.getTime() - startDate.getTime();
//        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
//    }
//
//    /**
//     * Calculate hours between two dates
//     */
//    public static long hoursBetween(Date startDate, Date endDate) {
//        if (startDate == null || endDate == null) return 0;
//        long diff = endDate.getTime() - startDate.getTime();
//        return TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
//    }
//
//    /**
//     * Calculate minutes between two dates
//     */
//    public static long minutesBetween(Date startDate, Date endDate) {
//        if (startDate == null || endDate == null) return 0;
//        long diff = endDate.getTime() - startDate.getTime();
//        return TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS);
//    }
//
//    /**
//     * Calculate age in years from birth date
//     */
//    public static int calculateAge(Date birthDate) {
//        if (birthDate == null) return 0;
//        Calendar birth = Calendar.getInstance();
//        Calendar now = Calendar.getInstance();
//        birth.setTime(birthDate);
//
//        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
//
//        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
//            age--;
//        }
//
//        return age;
//    }
//
//    // ============================================================================
//    // DATE RANGE AND PERIOD METHODS
//    // ============================================================================
//
//    /**
//     * Get start of day (00:00:00.000)
//     */
//    public static Date startOfDay(Date date) {
//        if (date == null) return null;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return calendar.getTime();
//    }
//
//    /**
//     * Get end of day (23:59:59.999)
//     */
//    public static Date endOfDay(Date date) {
//        if (date == null) return null;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.set(Calendar.HOUR_OF_DAY, 23);
//        calendar.set(Calendar.MINUTE, 59);
//        calendar.set(Calendar.SECOND, 59);
//        calendar.set(Calendar.MILLISECOND, 999);
//        return calendar.getTime();
//    }
//
//    /**
//     * Get start of month
//     */
//    public static Date startOfMonth(Date date) {
//        if (date == null) return null;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.set(Calendar.DAY_OF_MONTH, 1);
//        return startOfDay(calendar.getTime());
//    }
//
//    /**
//     * Get end of month
//     */
//    public static Date endOfMonth(Date date) {
//        if (date == null) return null;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
//        return endOfDay(calendar.getTime());
//    }
//
//    /**
//     * Get start of year
//     */
//    public static Date startOfYear(Date date) {
//        if (date == null) return null;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.set(Calendar.MONTH, Calendar.JANUARY);
//        calendar.set(Calendar.DAY_OF_MONTH, 1);
//        return startOfDay(calendar.getTime());
//    }
//
//    /**
//     * Get end of year
//     */
//    public static Date endOfYear(Date date) {
//        if (date == null) return null;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
//        calendar.set(Calendar.DAY_OF_MONTH, 31);
//        return endOfDay(calendar.getTime());
//    }
//
//    /**
//     * Get list of dates in range
//     */
//    public static List<Date> getDateRange(Date startDate, Date endDate) {
//        List<Date> dates = new ArrayList<>();
//        if (startDate == null || endDate == null || startDate.after(endDate)) {
//            return dates;
//        }
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(startDate);
//
//        while (!calendar.getTime().after(endDate)) {
//            dates.add(calendar.getTime());
//            calendar.add(Calendar.DAY_OF_MONTH, 1);
//        }
//
//        return dates;
//    }
//
//    // ============================================================================
//    // BUSINESS DATE METHODS
//    // ============================================================================
//
//    /**
//     * Check if date is weekend (Saturday or Sunday)
//     */
//    public static boolean isWeekend(Date date) {
//        if (date == null) return false;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
//        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
//    }
//
//    /**
//     * Check if date is weekday (Monday to Friday)
//     */
//    public static boolean isWeekday(Date date) {
//        return !isWeekend(date);
//    }
//
//    /**
//     * Get next business day (skipping weekends)
//     */
//    public static Date getNextBusinessDay(Date date) {
//        if (date == null) return null;
//        Date nextDay = addDays(date, 1);
//        while (isWeekend(nextDay)) {
//            nextDay = addDays(nextDay, 1);
//        }
//        return nextDay;
//    }
//
//    /**
//     * Get previous business day (skipping weekends)
//     */
//    public static Date getPreviousBusinessDay(Date date) {
//        if (date == null) return null;
//        Date prevDay = subtractDays(date, 1);
//        while (isWeekend(prevDay)) {
//            prevDay = subtractDays(prevDay, 1);
//        }
//        return prevDay;
//    }
//
//    /**
//     * Add business days (excluding weekends)
//     */
//    public static Date addBusinessDays(Date date, int businessDays) {
//        if (date == null) return null;
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//
//        int addedDays = 0;
//        while (addedDays < businessDays) {
//            calendar.add(Calendar.DAY_OF_MONTH, 1);
//            if (!isWeekend(calendar.getTime())) {
//                addedDays++;
//            }
//        }
//
//        return calendar.getTime();
//    }
//
//    // ============================================================================
//    // UTILITY AND VALIDATION METHODS
//    // ============================================================================
//
//    /**
//     * Check if year is leap year
//     */
//    public static boolean isLeapYear(int year) {
//        return (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0);
//    }
//
//    /**
//     * Get days in month
//     */
//    public static int getDaysInMonth(int year, int month) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(year, month - 1, 1);
//        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
//    }
//
//    /**
//     * Get current quarter (1-4)
//     */
//    public static int getCurrentQuarter() {
//        return getQuarter(today());
//    }
//
//    /**
//     * Get quarter for date (1-4)
//     */
//    public static int getQuarter(Date date) {
//        if (date == null) return 0;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
//        return (month - 1) / 3 + 1;
//    }
//
//    /**
//     * Get week of year
//     */
//    public static int getWeekOfYear(Date date) {
//        if (date == null) return 0;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        return calendar.get(Calendar.WEEK_OF_YEAR);
//    }
//
//    /**
//     * Get relative time description (e.g., "2 hours ago", "in 3 days")
//     */
//    public static String getRelativeTime(Date date) {
//        if (date == null) return "Unknown";
//
//        long diff = System.currentTimeMillis() - date.getTime();
//        long absDiff = Math.abs(diff);
//
//        boolean past = diff > 0;
//        String suffix = past ? " ago" : "";
//        String prefix = past ? "" : "in ";
//
//        if (absDiff < 60 * 1000) {
//            return "just now";
//        } else if (absDiff < 60 * 60 * 1000) {
//            long minutes = absDiff / (60 * 1000);
//            return prefix + minutes + " minute" + (minutes != 1 ? "s" : "") + suffix;
//        } else if (absDiff < 24 * 60 * 60 * 1000) {
//            long hours = absDiff / (60 * 60 * 1000);
//            return prefix + hours + " hour" + (hours != 1 ? "s" : "") + suffix;
//        } else if (absDiff < 7 * 24 * 60 * 60 * 1000) {
//            long days = absDiff / (24 * 60 * 60 * 1000);
//            return prefix + days + " day" + (days != 1 ? "s" : "") + suffix;
//        } else {
//            return formatDateDisplay(date);
//        }
//    }
//
//    /**
//     * Convert Date to LocalDateTime
//     */
//    public static LocalDateTime toLocalDateTime(Date date) {
//        if (date == null) return null;
//        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
//    }
//
//    /**
//     * Convert LocalDateTime to Date
//     */
//    public static Date toDate(LocalDateTime localDateTime) {
//        if (localDateTime == null) return null;
//        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
//    }
//
//    /**
//     * Get min date from array
//     */
//    public static Date getMinDate(Date... dates) {
//        if (dates == null || dates.length == 0) return null;
//
//        Date min = null;
//        for (Date date : dates) {
//            if (date != null && (min == null || date.before(min))) {
//                min = date;
//            }
//        }
//        return min;
//    }
//
//    /**
//     * Get max date from array
//     */
//    public static Date getMaxDate(Date... dates) {
//        if (dates == null || dates.length == 0) return null;
//
//        Date max = null;
//        for (Date date : dates) {
//            if (date != null && (max == null || date.after(max))) {
//                max = date;
//            }
//        }
//        return max;
//    }
//
//    /**
//     * Check if date is in range (inclusive)
//     */
//    public static boolean isInRange(Date date, Date startDate, Date endDate) {
//        if (date == null || startDate == null || endDate == null) return false;
//        return !date.before(startDate) && !date.after(endDate);
//    }
//
//    /**
//     * Get time zone offset in hours
//     */
//    public static int getTimeZoneOffset() {
//        return TimeZone.getDefault().getRawOffset() / (1000 * 60 * 60);
//    }
//
//    /**
//     * Format duration between two dates in human readable format
//     */
//    public static String formatDuration(Date startDate, Date endDate) {
//        if (startDate == null || endDate == null) return "Unknown duration";
//
//        long diff = Math.abs(endDate.getTime() - startDate.getTime());
//
//        long days = diff / (24 * 60 * 60 * 1000);
//        diff %= (24 * 60 * 60 * 1000);
//
//        long hours = diff / (60 * 60 * 1000);
//        diff %= (60 * 60 * 1000);
//
//        long minutes = diff / (60 * 1000);
//
//        StringBuilder result = new StringBuilder();
//        if (days > 0) {
//            result.append(days).append(" day").append(days != 1 ? "s" : "");
//        }
//        if (hours > 0) {
//            if (result.length() > 0) result.append(", ");
//            result.append(hours).append(" hour").append(hours != 1 ? "s" : "");
//        }
//        if (minutes > 0 && days == 0) {
//            if (result.length() > 0) result.append(", ");
//            result.append(minutes).append(" minute").append(minutes != 1 ? "s" : "");
//        }
//
//        return result.length() > 0 ? result.toString() : "0 minutes";
//    }
//}
//
//
////package com.midas.consulting.util;
////
////import java.text.SimpleDateFormat;
////import java.util.Date;
////
/////**
//// * Created by Dheeraj Singh.
//// */
////public class DateUtils {
////
////    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
////
////    /**
////     * Returns today's date as java.util.Date object
////     *
////     * @return today's date as java.util.Date object
////     */
////    public static Date today() {
////        return new Date();
////    }
////
////    /**
////     * Returns today's date as yyyy-MM-dd format
////     *
////     * @return today's date as yyyy-MM-dd format
////     */
////    public static String todayStr() {
////        return sdf.format(today());
////    }
////
////    /**
////     * Returns the formatted String date for the passed java.util.Date object
////     *
////     * @param date
////     * @return
////     */
////    public static String formattedDate(Date date) {
////        return date != null ? sdf.format(date) : todayStr();
////    }
////
////}
