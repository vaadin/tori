package org.vaadin.tori.component;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class LazyLayoutTest {

    @Test
    public void testGroupToRangesTypical() {
        // 1-5, 7-9
        final List<Integer> list = Lists.newArrayList(1, 2, 3, 4, 5, 7, 8, 9);

        final List<int[]> groupToRanges = LazyLayout.groupToRanges(list);
        assertEquals("should have two groups", 2, groupToRanges.size());
        assertEquals("first group first number should be 1", 1,
                groupToRanges.get(0)[0]);
        assertEquals("first group last number should be 5", 5,
                groupToRanges.get(0)[1]);
        assertEquals("second group first number should be 7", 7,
                groupToRanges.get(1)[0]);
        assertEquals("second group last number should be 9", 9,
                groupToRanges.get(1)[1]);
    }

    @Test
    public void testGroupToRangesEmpty() {
        final List<Integer> list = Lists.newArrayList();

        final List<int[]> groupToRanges = LazyLayout.groupToRanges(list);
        assertEquals("should have no groups", 0, groupToRanges.size());
    }

    @Test
    public void testGroupToRangesOneNumber() {
        // 55-55
        final List<Integer> list = Lists.newArrayList(55);

        final List<int[]> groupToRanges = LazyLayout.groupToRanges(list);
        assertEquals("should have 1 group", 1, groupToRanges.size());
        assertEquals("wrong start number", 55, groupToRanges.get(0)[0]);
        assertEquals("wrong end number", 55, groupToRanges.get(0)[1]);
    }

    @Test
    public void testGroupToRangesAloneFirst() {
        // 1-1, 3-4
        final List<Integer> list = Lists.newArrayList(1, 3, 4);

        final List<int[]> a = LazyLayout.groupToRanges(list);
        assertEquals("groups", 2, a.size());
        assertEquals("first start", 1, a.get(0)[0]);
        assertEquals("first end", 1, a.get(0)[1]);
        assertEquals("second start", 3, a.get(1)[0]);
        assertEquals("second end", 4, a.get(1)[1]);
    }

    @Test
    public void testGroupToRangesAloneLast() {
        // 1-2, 4-4
        final List<Integer> list = Lists.newArrayList(1, 2, 4);

        final List<int[]> a = LazyLayout.groupToRanges(list);
        assertEquals("groups", 2, a.size());
        assertEquals("first start", 1, a.get(0)[0]);
        assertEquals("first end", 2, a.get(0)[1]);
        assertEquals("second start", 4, a.get(1)[0]);
        assertEquals("second end", 4, a.get(1)[1]);
    }

}
