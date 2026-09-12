package com.ramussoft.core.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Формат дат у XML-таблицях файлу проєкту.
 * <p>
 * Історично використовувався {@code DateFormat.SHORT}, який не має секунд і не
 * фіксує часовий пояс: значення обрізалось при кожному збереженні, а той самий
 * файл у різних поясах читався по-різному. Тепер записуємо ISO-8601 в UTC,
 * а читаємо обидва формати.
 * <p>
 * {@link DateFormat} не потокобезпечний, тому екземпляри тримаються в
 * {@link ThreadLocal}.
 */
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

    /**
     * Формати, якими файли писались до версії 2.0.3.
     * <p>
     * Шаблони задані явно, а не через
     * {@code DateFormat.getDateTimeInstance(SHORT, SHORT, ENGLISH)}, бо той
     * залежить від набору локальних даних JDK: до переходу на CLDR англійський
     * SHORT був {@code M/d/yy h:mm a}, а після — {@code M/d/yy, h:mm a}.
     * Через це старі файли на новій JDK переставали читатися, а виняток
     * ковтався мовчки, і дати губилися.
     */
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

    /**
     * Читає дату в новому форматі, а якщо не вийшло — у застарілому.
     *
     * @throws ParseException якщо значення не відповідає жодному з форматів
     */
    public static Date parse(String value) throws ParseException {
        try {
            return ISO.get().parse(value);
        } catch (ParseException ignored) {
            // Нижче пробуємо всі відомі застарілі шаблони.
        }
        for (DateFormat legacy : LEGACY.get()) {
            try {
                return legacy.parse(value);
            } catch (ParseException ignored) {
                // наступний шаблон
            }
        }
        throw new ParseException("Не розпізнано дату: " + value, 0);
    }
}
