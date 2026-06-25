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

import org.eclipse.serializer.equality.Equalator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractArrayStorageTest {

    @Test
    void rangedRemoveDuplicatesEqualatorTest() {
        Integer[] integers = {1, 2, 3, 4, 1, 2, 3, 4};
        Integer marker = (Integer) null;

        int i = AbstractArrayStorage.rangedRemoveDuplicates(
                integers,
                integers.length,
                0,
                integers.length,
                Equalator.value(),
                marker
        );

        Integer[] expected = {1, 2, 3, 4, null, null, null, null};
        Assertions.assertArrayEquals(expected, integers);
    }

    @Test
    void rangedRemoveDuplicatesEqualatorOffsetTest() {
        Integer[] integers = {1, 2, 3, 4, 1, 2, 3, 4};
        Integer marker = (Integer) null;

        int i = AbstractArrayStorage.rangedRemoveDuplicates(
                integers,
                integers.length,
                2,
                integers.length - 2,
                Equalator.value(),
                marker
        );

        Integer[] expected = {1, 2, 3, 4, 1, 2, null, null};
        Assertions.assertArrayEquals(expected, integers);
    }

    @Test
    void rangedRemoveDuplicatesEqualatorReversTest() {
        Integer[] integers = {1, 2, 3, 4, 1, 2, 3, 4};
        Integer marker = (Integer) null;

        int i = AbstractArrayStorage.rangedRemoveDuplicates(
                integers,
                integers.length,
                7,
                -integers.length,
                Equalator.value(),
                marker
        );

        Integer[] expected = {1, 2, 3, 4, null, null, null, null};
        Assertions.assertArrayEquals(expected, integers);
    }

    @Test
    void rangedRemoveDuplicatesTest() {
        Integer[] integers = {1, 2, 3, 4, 1, 2, 3, 4};
        Integer marker = (Integer) null;

        int i = AbstractArrayStorage.rangedRemoveDuplicates(
                integers,
                integers.length,
                0,
                integers.length,
                marker
        );

        Integer[] expected = {1, 2, 3, 4, null, null, null, null};
        Assertions.assertArrayEquals(expected, integers);
    }

    @Test
    void rangedRemoveDuplicatesOffsetTest() {
        Integer[] integers = {1, 2, 3, 4, 1, 2, 3, 4};
        Integer marker = (Integer) null;

        int i = AbstractArrayStorage.rangedRemoveDuplicates(
                integers,
                integers.length,
                2,
                integers.length - 2,
                marker
        );

        Integer[] expected = {1, 2, 3, 4, 1, 2, null, null};
        Assertions.assertArrayEquals(expected, integers);
    }

    @Test
    void rangedRemoveDuplicatesReversTest() {
        Integer[] integers = {1, 2, 3, 4, 1, 2, 3, 4};
        Integer marker = (Integer) null;

        int i = AbstractArrayStorage.rangedRemoveDuplicates(
                integers,
                integers.length,
                7,
                -integers.length,
                marker
        );

        Integer[] expected = {1, 2, 3, 4, null, null, null, null};
        Assertions.assertArrayEquals(expected, integers);
    }

    @Test
    void testRangedConditionalIndexOf() {
        // Create an array of integers to search
        Integer[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        // Test searching for an element that is present in the array within the
        // specified range and matches the given predicate
        Predicate<Integer> predicate1 = (i) -> i > 5;
        int index1 = AbstractArrayStorage.rangedConditionalIndexOf(data, 10, 3, 3, predicate1);
        Assertions.assertEquals(5, index1);

        // Test searching for an element that is present in the array within the
        // specified range but does not match the given predicate
        Predicate<Integer> predicate2 = (i) -> i > 8;
        int index2 = AbstractArrayStorage.rangedConditionalIndexOf(data, 10, 3, 3, predicate2);
        Assertions.assertEquals(-1, index2);

        // Test searching for an element that is not present in the array within the
        // specified range
        Predicate<Integer> predicate3 = (i) -> i > 10;
        int index3 = AbstractArrayStorage.rangedConditionalIndexOf(data, 10, 3, 3, predicate3);
        Assertions.assertEquals(-1, index3);

        // Test searching for an element using a range with a negative length
        Predicate<Integer> predicate4 = (i) -> i > 5;
        int index4 = AbstractArrayStorage.rangedConditionalIndexOf(data, 10, 7, -3, predicate4);
        Assertions.assertEquals(7, index4);
    }

}
