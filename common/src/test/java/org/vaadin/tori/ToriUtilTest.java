package org.vaadin.tori;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Collection;

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

    @Test(expected = IllegalArgumentException.class)
    public void checkForNullAndEmptyCollectionWhenGivenNull() {
        ToriUtil.checkForNullAndEmpty((Collection<?>) null, "null", "empty");
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkForNullAndEmptyStringWhenGivenNull() {
        ToriUtil.checkForNullAndEmpty((String) null, "null", "empty");
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkForNullAndEmptyCollectionWhenGivenEmpty() {
        ToriUtil.checkForNullAndEmpty(new ArrayList<Object>(),
                "null was given", "empty");
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkForNullAndEmptyStringWhenGivenEmpty() {
        ToriUtil.checkForNullAndEmpty("", "null", "empty");
    }

    @Test
    public void checkForNullAndEmptyCollectionWhenGivenValid() {
        final Collection<String> list = new ArrayList<String>();
        list.add("a");
        ToriUtil.checkForNullAndEmpty(list, "null", "empty");
        // succeeds
    }

    @Test
    public void checkForNullAndEmptyStringWhenGivenValid() {
        ToriUtil.checkForNullAndEmpty("a", "null", "empty");
        // succeeds
    }

    @Test(expected = IllegalArgumentException.class)
    public void tailNullShouldThrowException() {
        ToriUtil.tail(null);
    }

    @Test
    public void tailOneStringElementReturnsEmptyArray() {
        assertArrayEquals(new String[] {},
                ToriUtil.tail(new String[] { "foo" }));
    }

    @Test
    public void tailOneIntegerElementReturnsEmptyArray() {
        assertArrayEquals(new Integer[] {}, ToriUtil.tail(new Integer[] { 0 }));
    }

    @Test
    public void tailThreeElementsReturnsTwoElements() {
        assertArrayEquals(new Integer[] { 1, 2 },
                ToriUtil.tail(new Integer[] { 0, 1, 2 }));
    }
}
