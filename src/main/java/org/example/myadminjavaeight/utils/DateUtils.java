package org.example.myadminjavaeight.utils;

import java.util.Date;

public class DateUtils {

    private DateUtils() {}

    /**
     * 将 java.util.Date 转换为 Unix 秒级时间戳（10 位）。
     * 入参为 null 时返回 null。
     */
    public static Long toEpochSeconds(Date date) {
        return date == null ? null : date.getTime() / 1000L;
    }

    /**
     * 当前时间的 Unix 秒级时间戳。
     */
    public static Long nowEpochSeconds() {
        return System.currentTimeMillis() / 1000L;
    }
}
