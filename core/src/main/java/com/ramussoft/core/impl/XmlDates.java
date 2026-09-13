package com.ramussoft.core.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class XmlDates {

    private static final String ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final ThreadLocal<DateFormat> ISO = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat(ISO_PATTERN,
                    Locale.ROOT);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            format.setLenient(false);
            return format;
        }
    };

    private static final String[] LEGACY_PATTERNS = {
            "M/d/yy h:mm a",
            "M/d/yy, h:mm a",
            "dd.MM.yy HH:mm",
            "dd.MM.yy, HH:mm"};

    private static final ThreadLocal<DateFormat[]> LEGACY = new ThreadLocal<DateFormat[]>() {
        @Override
        protected DateFormat[] initialValue() {
            DateFormat[] formats = new DateFormat[LEGACY_PATTERNS.length];
            for (int i = 0; i < formats.length; i++) {
                SimpleDateFormat format = new SimpleDateFormat(
                        LEGACY_PATTERNS[i], Locale.ENGLISH);
                format.setLenient(true);
                formats[i] = format;
            }
            return formats;
        }
    };

    private XmlDates() {
    }

    public static String format(Object date) {
        return ISO.get().format(date);
    }

    public static Date parse(String value) throws ParseException {
        try {
            return ISO.get().parse(value);
        } catch (ParseException ignored) {
        }
        for (DateFormat legacy : LEGACY.get()) {
            try {
                return legacy.parse(value);
            } catch (ParseException ignored) {
            }
        }
        throw new ParseException("Unparsable date: " + value, 0);
    }
}
