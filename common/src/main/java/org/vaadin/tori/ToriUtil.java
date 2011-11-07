package org.vaadin.tori;

import java.util.Date;

public final class ToriUtil {
    private ToriUtil() {
        // not instantiable
    }

    public static String getRelativeTimeString(final Date time) {
        // TODO Auto-generated method stub
        return String.format("%1$tF %1$tR", time);
    }
}
