package com.petcloud.common.core.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期时间工具类
 * 提供统一的日期时间格式化方法
 *
 * @author luohao
 */
public final class DateUtils {

    private DateUtils() {
        // 工具类禁止实例化
    }

    /**
     * 默认日期时间格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认日期格式：yyyy-MM-dd
     */
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 默认时间格式：HH:mm:ss
     */
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    /**
     * 简短时间格式：HH:mm
     */
    public static final String SHORT_TIME_PATTERN = "HH:mm";

    /**
     * 月日格式：MM月dd日
     */
    public static final String MONTH_DAY_PATTERN = "MM月dd日";

    /**
     * 日期时间格式化器（线程安全）
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN);

    /**
     * 日期格式化器（线程安全）
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);

    /**
     * 时间格式化器（线程安全）
     */
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN);

    /**
     * 简短时间格式化器（线程安全）
     */
    public static final DateTimeFormatter SHORT_TIME_FORMATTER = DateTimeFormatter.ofPattern(SHORT_TIME_PATTERN);

    /**
     * 月日格式化器（线程安全）
     */
    public static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern(MONTH_DAY_PATTERN);

    /**
     * 将 Date 格式化为默认日期时间字符串 (yyyy-MM-dd HH:mm:ss)
     *
     * @param date 日期对象
     * @return 格式化后的字符串，如果 date 为 null 则返回 null
     */
    public static String format(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).format(DATE_TIME_FORMATTER);
    }

    /**
     * 将 Date 格式化为指定格式的字符串
     *
     * @param date    日期对象
     * @param pattern 格式模式
     * @return 格式化后的字符串，如果 date 为 null 则返回 null
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).format(formatter);
    }

    /**
     * 将 Date 格式化为指定格式的字符串
     *
     * @param date      日期对象
     * @param formatter 格式化器
     * @return 格式化后的字符串，如果 date 为 null 则返回 null
     */
    public static String format(Date date, DateTimeFormatter formatter) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).format(formatter);
    }

    /**
     * 将 LocalDateTime 格式化为默认日期时间字符串 (yyyy-MM-dd HH:mm:ss)
     *
     * @param dateTime LocalDateTime 对象
     * @return 格式化后的字符串，如果 dateTime 为 null 则返回 null
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * 将 LocalDateTime 格式化为指定格式的字符串
     *
     * @param dateTime LocalDateTime 对象
     * @param pattern  格式模式
     * @return 格式化后的字符串，如果 dateTime 为 null 则返回 null
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 将 LocalDateTime 格式化为指定格式的字符串
     *
     * @param dateTime  LocalDateTime 对象
     * @param formatter 格式化器
     * @return 格式化后的字符串，如果 dateTime 为 null 则返回 null
     */
    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(formatter);
    }

    /**
     * 获取当前时间的默认格式化字符串
     *
     * @return 当前时间的字符串表示 (yyyy-MM-dd HH:mm:ss)
     */
    public static String now() {
        return format(LocalDateTime.now());
    }

    /**
     * 获取当前 Date 对象
     *
     * @return 当前 Date 对象
     */
    public static Date currentDate() {
        return new Date();
    }

    /**
     * 获取当前 LocalDateTime 对象
     *
     * @return 当前 LocalDateTime 对象
     */
    public static LocalDateTime currentLocalDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 将 Date 转换为 LocalDateTime
     *
     * @param date Date 对象
     * @return LocalDateTime 对象，如果 date 为 null 则返回 null
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 将 LocalDateTime 转换为 Date
     *
     * @param dateTime LocalDateTime 对象
     * @return Date 对象，如果 dateTime 为 null 则返回 null
     */
    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    // ========================= 相对时间格式化 =========================

    /**
     * 一分钟的毫秒数
     */
    private static final long MILLIS_PER_MINUTE = 60000L;

    /**
     * 一小时的毫秒数
     */
    private static final long MILLIS_PER_HOUR = 3600000L;

    /**
     * 一天的毫秒数
     */
    private static final long MILLIS_PER_DAY = 86400000L;

    /**
     * 将 Date 格式化为相对时间字符串（刚刚、X分钟前、今天 HH:mm、昨天 HH:mm、MM月dd日）
     *
     * @param date 日期对象
     * @return 相对时间字符串，如果 date 为 null 则返回空字符串
     */
    public static String formatRelative(Date date) {
        if (date == null) {
            return "";
        }

        long now = System.currentTimeMillis();
        long time = date.getTime();
        long diff = now - time;

        if (diff < MILLIS_PER_MINUTE) {
            return "刚刚";
        } else if (diff < MILLIS_PER_HOUR) {
            return (diff / MILLIS_PER_MINUTE) + "分钟前";
        } else if (diff < MILLIS_PER_DAY) {
            LocalDateTime dateTime = toLocalDateTime(date);
            return "今天 " + dateTime.format(SHORT_TIME_FORMATTER);
        } else if (diff < MILLIS_PER_DAY * 2) {
            LocalDateTime dateTime = toLocalDateTime(date);
            return "昨天 " + dateTime.format(SHORT_TIME_FORMATTER);
        } else {
            LocalDateTime dateTime = toLocalDateTime(date);
            return dateTime.format(MONTH_DAY_FORMATTER);
        }
    }

    /**
     * 将 Date 格式化为相对时间字符串，支持自定义空值返回
     *
     * @param date        日期对象
     * @param nullDefault 当 date 为 null 时的默认返回值
     * @return 相对时间字符串
     */
    public static String formatRelative(Date date, String nullDefault) {
        if (date == null) {
            return nullDefault;
        }
        return formatRelative(date);
    }
}
