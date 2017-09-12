package com.ants.common.util;

import org.apache.commons.lang3.time.FastDateFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by thangpham on 07/09/2017.
 */
public class DateTimeUtil {

    //<Format, <TimeZone, FastDateFormat>>
    private static ConcurrentMap<String, ConcurrentMap<String, FastDateFormat>> dateFmtMap;

    public static final String DDMMYYYY_DASH = "dd-MM-yyyy";
    public static final String DDMMYYYYHH_DASH = "yyyy-MM-dd-HH";

    static {
        synchronized (DateTimeUtil.class) {
            dateFmtMap = new ConcurrentHashMap<>();
            dateFmtMap.put(DDMMYYYY_DASH, new ConcurrentHashMap<>());
            dateFmtMap.get(DDMMYYYY_DASH).put("System", FastDateFormat.getInstance(DDMMYYYY_DASH));
            dateFmtMap.put(DDMMYYYYHH_DASH, new ConcurrentHashMap<>());
            dateFmtMap.get(DDMMYYYYHH_DASH).put("System", FastDateFormat.getInstance(DDMMYYYYHH_DASH));
        }
    }

    public static Date addTime(Date date, int numb, int unit) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(numb, unit);
        return cal.getTime();
    }

    public static Date addTime(Calendar cal, int numb, int unit) {
        return addTime(cal.getTime(), numb, unit);
    }

    public static String formatDate(Date dateTime, String fmt) {
        return formatDate(dateTime.getTime(), fmt, null);
    }

    public static String formatDate(Calendar cal, String fmt) {
        return formatDate(cal.getTimeInMillis(), fmt, null);
    }

    public static String formatDate(long timeInMilis, String fmt) {
        return formatDate(timeInMilis, fmt, null);
    }

    public static String formatDate(long timeInMilis, String fmt, TimeZone tz) {
        ConcurrentMap<String, FastDateFormat> fmtMap = dateFmtMap.get("fmt");
        if (null == fmtMap) {
            fmtMap = new ConcurrentHashMap();
            dateFmtMap.put(fmt, fmtMap);
        }
        String tzId;
        if(null == tz) tzId = "System";
        else tzId = tz.getID();
        FastDateFormat fdf = fmtMap.get(tzId);
        if(null == fdf) {
            fdf = tzId.equals("System") ? FastDateFormat.getInstance(fmt) : FastDateFormat.getInstance(fmt, tz);
            fmtMap.put(tzId, fdf);
        }
        return fdf.format(timeInMilis);
    }

    public static Date truncateDateTime(Calendar cal, int at) {
        return truncateDateTime(cal.getTime(), at);
    }

    public static Date truncateDateTime(Date date, int at) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (Calendar.DATE == at) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        } else if (Calendar.HOUR == at || Calendar.HOUR_OF_DAY == at) {
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        return cal.getTime();
    }

    public static Date stretchDateTime(Calendar cal, int at) {
        return stretchDateTime(cal.getTime(), at);
    }

    public static Date stretchDateTime(Date date, int at) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (Calendar.DATE == at) {
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
        } else if (Calendar.HOUR == at || Calendar.HOUR_OF_DAY == at) {
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
        }
        return cal.getTime();
    }
}
