package org.vaadin.tori;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

public class ToriUtilTest {
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

}
