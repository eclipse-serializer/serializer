package org.eclipse.serializer.collections;

/*-
 * #%L
 * Eclipse Serializer Integration Tests
 * %%
 * Copyright (C) 2023 - 2026 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.util.Comparator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class XSortTest {

    @Test
    void compare_samevalues() {
        Assertions.assertAll(
                () -> Assertions.assertEquals(0, XSort.compare(true, true)),
                () -> Assertions.assertEquals(0, XSort.compare(Byte.valueOf("1"), Byte.valueOf("1"))),
                () -> Assertions.assertEquals(0, XSort.compare(Short.valueOf("1"), Short.valueOf("1"))),
                () -> Assertions.assertEquals(0, XSort.compare(1, 1)),
                () -> Assertions.assertEquals(0, XSort.compare(Float.valueOf("1"), Float.valueOf("1"))),
                () -> Assertions.assertEquals(0, XSort.compare(Long.valueOf("1"), Long.valueOf("1"))),
                () -> Assertions.assertEquals(0, XSort.compare(1.0, 1.0)),
                () -> Assertions.assertEquals(0, XSort.compare("a", "a")));
    }

    @Test
    void compare_differentvalues() {
        Assertions.assertAll(
                () -> Assertions.assertEquals(-1, XSort.compare(false, true)),
                () -> Assertions.assertEquals(-1, XSort.compare(Byte.valueOf("2"), Byte.valueOf("5"))),
                () -> Assertions.assertEquals(-1, XSort.compare(Short.valueOf("2"), Short.valueOf("3"))),
                () -> Assertions.assertEquals(-1, XSort.compare(1, 134)),
                () -> Assertions.assertEquals(-1, XSort.compare(Float.valueOf("1"), Float.valueOf("2"))),
                () -> Assertions.assertEquals(-1, XSort.compare(Long.valueOf("-1"), Long.valueOf("1"))),
                () -> Assertions.assertEquals(-1, XSort.compare(1.0, 1.1)),
                () -> Assertions.assertTrue(XSort.compare("A", "a") < 0));
    }

    @Test
    void compareLength() {
        Assertions.assertEquals(XSort.compareLength("String1", "String2"), 0);
    }

    @Test
    void compareIdentityHash() {
        Assertions.assertEquals(XSort.compareIdentityHash(1, 1), 0);
    }

    @Test
    void reverse() {
        Comparator<Integer> comparator = Integer::compareTo;

        Assertions.assertNotNull(XSort.reverse(comparator));
    }

    @Test
    void insertionsort_boolean() {
        boolean[] input = new boolean[]{false, true, false, true, false};
        XSort.insertionsort(input);

        boolean[] expected = new boolean[]{false, false, false, true, true};
        Assertions.assertArrayEquals(expected, input);
    }

    @Test
    void insertionsort_byte() {
        byte[] input = new byte[]{5, 2, 9, 1, 8};
        XSort.insertionsort(input);

        byte[] expected = new byte[]{1, 2, 5, 8, 9};
        Assertions.assertArrayEquals(expected, input);
    }

    @Test
    void insertionsort_short() {
        short[] input = new short[]{5, 2, 9, 1, 8};
        XSort.insertionsort(input);

        short[] expected = new short[]{1, 2, 5, 8, 9};
        Assertions.assertArrayEquals(expected, input);
    }

    @Test
    void insertionsort_int() {
        int[] input = new int[]{5, 2, 9, 1, 8};
        XSort.insertionsort(input);

        int[] expected = new int[]{1, 2, 5, 8, 9};
        Assertions.assertArrayEquals(expected, input);
    }

    @Test
    void insertionsort_int_bound()
    {
        int[] input = new int[]{5, 2, 9, 1, 8};

        XSort.insertionsort(input,2,5);

        int[] expected  = new int[]{5, 2, 1, 8, 9};
        Assertions.assertArrayEquals(expected, input);
    }

    @Test
    void insertionsort_int_comparator_bound()
    {
        Integer[] input = new Integer[]{5, 2, 9, 1, 8};

        XSort.insertionsort(input,Comparator.naturalOrder(), 2,5);

        Integer[] expected  = new Integer[]{5, 2, 1, 8, 9};
        Assertions.assertArrayEquals(expected, input);
    }

    @Test
    void insertionsort_long() {
        long[] input = new long[]{5, 2, 9, 1, 8};
        XSort.insertionsort(input);

        long[] expected = new long[]{1, 2, 5, 8, 9};
        Assertions.assertArrayEquals(expected, input);
    }

    @Test
    void insertionsort_float() {
        float[] input = new float[]{5, 2, 9, 1, 8};
        XSort.insertionsort(input);

        float[] expected = new float[]{1, 2, 5, 8, 9};
        Assertions.assertArrayEquals(expected, input);
    }

    @Test
    void insertionsort_double() {
        double[] input = new double[]{5, 2, 9, 1, 8};
        XSort.insertionsort(input);

        double[] expected = new double[]{1, 2, 5, 8, 9};
        Assertions.assertArrayEquals(expected, input);
    }

    @Test
    void insertionsort_char() {
        char[] input = new char[]{5, 2, 9, 1, 8};
        XSort.insertionsort(input);

        char[] expected = new char[]{1, 2, 5, 8, 9};
        Assertions.assertArrayEquals(expected, input);
    }

    @Test
    void insertionsort_values() {
        Integer[] values = new Integer[]{5, 1, 3, 6, 4, 2};

        XSort.insertionsort(values, Integer::compareTo);

        Assertions.assertArrayEquals(new Integer[]{1, 2, 3, 4, 5, 6}, values);
    }

    @Test
    void sort_completeRange() {
        Integer[] input = {5, 4, 3, 2, 1};

        XSort.sort(input, Comparator.naturalOrder());

        Integer[] expected = {1, 2, 3, 4, 5};
        Assertions.assertArrayEquals(expected, input);
    }

    @Test
    void sort_subRange() {
        Integer[] input = {4, 2, 3, 1, 5};

        XSort.sort(input, 1, 4, Comparator.naturalOrder());

        Integer[] expected = {4, 1, 2, 3, 5};
        Assertions.assertArrayEquals(expected, input);
    }


}
