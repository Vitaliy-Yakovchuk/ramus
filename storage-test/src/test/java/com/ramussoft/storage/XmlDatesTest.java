package com.ramussoft.storage;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

import com.ramussoft.core.impl.XmlDates;

public class XmlDatesTest {

    @Test
    public void secondsSurviveRoundTrip() throws Exception {
        Calendar calendar = new GregorianCalendar(
                TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(2026, Calendar.SEPTEMBER, 6, 14, 22, 31);
        calendar.set(Calendar.MILLISECOND, 500);
        Date original = calendar.getTime();

        assertEquals(original, XmlDates.parse(XmlDates.format(original)));
    }

    @Test
    public void formatIsUtcAndIso() {
        Calendar calendar = new GregorianCalendar(
                TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(2026, Calendar.SEPTEMBER, 6, 14, 22, 31);

        assertEquals("2026-09-06T14:22:31.000Z",
                XmlDates.format(calendar.getTime()));
    }

    @Test
    public void legacyFormatWithoutCommaIsRead() throws Exception {
        Date parsed = XmlDates.parse("9/3/09 5:23 PM");

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(parsed);
        assertEquals(2009, calendar.get(Calendar.YEAR));
        assertEquals(Calendar.SEPTEMBER, calendar.get(Calendar.MONTH));
        assertEquals(3, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(17, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(23, calendar.get(Calendar.MINUTE));
    }

    @Test
    public void isoIsTimeZoneIndependent() throws Exception {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kyiv"));
            String written = XmlDates.format(new Date(1_788_688_263_000L));

            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
            assertEquals(new Date(1_788_688_263_000L),
                    XmlDates.parse(written));
        } finally {
            TimeZone.setDefault(original);
        }
    }
}
