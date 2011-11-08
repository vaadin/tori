package org.vaadin.tori;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

public class ToriUtilTest {
    @Test
    public void relativeTimeStringAMomentAgo() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -1);
        cal.add(Calendar.SECOND, 10);
        assertEquals("a moment ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));
    }

    @Test
    public void relativeTimeStringSingleMinuteAgo() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -1);
        assertEquals("1 minute ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));
    }

    @Test
    public void relativeTimeStringMultipleMinutesAgo() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -2);
        assertEquals("2 minutes ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));

        cal.add(Calendar.MINUTE, -3);
        assertEquals("3 minutes ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));
    }

    @Test
    public void relativeTimeStringSingleHourAgo() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        assertEquals("1 hour ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));
    }

    @Test
    public void relativeTimeStringMultipleHoursAgo() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -2);
        assertEquals("2 hours ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));

        cal.add(Calendar.HOUR, -1);
        assertEquals("3 hours ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));
    }

    @Test
    public void relativeTimeStringSingleDayAgo() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        assertEquals("1 day ago", ToriUtil.getRelativeTimeString(cal.getTime()));
    }

    @Test
    public void relativeTimeStringMultipleDaysAgo() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -2);
        assertEquals("2 days ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));

        cal.add(Calendar.DAY_OF_MONTH, -1);
        assertEquals("3 days ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));
    }

    @Test
    public void relativeTimeStringSingleMongthAgo() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        assertEquals("1 month ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));
    }

    @Test
    public void relativeTimeStringMultipleMonthsAgo() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -2);
        assertEquals("2 months ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));

        cal.add(Calendar.DAY_OF_MONTH, -1);
        assertEquals("3 months ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));
    }

    @Test
    public void relativeTimeStringSingleYearAgo() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        assertEquals("1 year ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));
    }

    @Test
    public void relativeTimeStringMultipleYearsAgo() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -2);
        assertEquals("2 years ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));

        cal.add(Calendar.YEAR, -1);
        assertEquals("3 years ago",
                ToriUtil.getRelativeTimeString(cal.getTime()));
    }
}
