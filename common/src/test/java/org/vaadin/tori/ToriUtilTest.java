package org.vaadin.tori;

import org.junit.Test;

public class ToriUtilTest {
    @Test(expected = IllegalArgumentException.class)
    public void checkForNullWhenGivenNull() {
        ToriUtil.checkForNull(null, "null was given");
    }

    @Test
    public void checkForNullWhenGivenNonNull() {
        ToriUtil.checkForNull(true, "null was not given");
        // succeeds
    }
}
