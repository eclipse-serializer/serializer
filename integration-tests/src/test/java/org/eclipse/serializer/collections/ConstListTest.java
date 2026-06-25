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

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.functional.Aggregator;
import org.eclipse.serializer.meta.NotImplementedYetError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConstListTest
{

    @Test
    void builder()
    {
        ConstList<Object> yield = ConstList.Builder()
                .yield();

        assertTrue(yield.isEmpty());
    }

    @Test
    void builderInitialCapacity()
    {
        Aggregator<Integer, ConstList<Integer>> builder = ConstList.Builder(8);
        builder.accept(10);

        ConstList<Integer> yield = builder.yield();
        assertEquals(1, yield.size());
    }

    @Test
    void NewEmpty()
    {
        ConstList<Integer> aNew = ConstList.New();
        assertTrue(aNew.isEmpty());
    }

    @Test
    void New_initialCapacity()
    {
        ConstList<Object> aNew = ConstList.New(10);
        assertEquals(10, aNew.maximumCapacity());
    }

    @Test
    void New_fromConstList()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ConstList<Integer> aNew = ConstList.New(integers);
        Assertions.assertIterableEquals(integers, aNew);
    }

    @Test
    void New_fromXGettingCollection()
    {
        BulkList<Integer> integers = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ConstList<Integer> aNew = ConstList.New(integers);
        Assertions.assertIterableEquals(integers, aNew);
    }

    @Test
    void New_arrayStartLength()
    {
        Integer[] integers = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        ConstList<Integer> aNew = ConstList.New(integers, 2, 3);

        ConstList<Integer> resultToCompare = ConstList.New(3, 4, 5);
        Assertions.assertIterableEquals(resultToCompare, aNew);
    }

    @Test
    void internalGetStorageArray()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] internalGetStorageArray = integers.internalGetStorageArray();

        Integer[] arrayToCompare = {1, 2, 3, 4, 5, 6, 7, 8};
        Assertions.assertArrayEquals(arrayToCompare, internalGetStorageArray);
    }

    @Test
    void internalSize()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        int i = integers.internalSize();
        Assertions.assertEquals(8, i);
    }

    @Test
    void internalGetSectionIndices()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        int[] ints = integers.internalGetSectionIndices();

        int[] result = {0, 8};
        Assertions.assertArrayEquals(result, ints);
    }

    @Test
    void equality()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Equalator<? super Integer> equality = integers.equality();
        Assertions.assertEquals(Equalator.identity(), equality);
    }

    @Test
    void internalCountingAddAll()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);

        Integer[] instToAdd = {7, 7, 8, 9};
        Class<UnsupportedOperationException> exc = UnsupportedOperationException.class;
        Assertions.assertAll(
                () -> Assertions.assertThrows(exc, () -> integers.internalCountingAddAll(instToAdd)),
                () -> Assertions.assertThrows(exc, () -> integers.internalCountingAddAll(instToAdd, 2, 3)),
                () -> Assertions.assertThrows(exc, () -> integers.internalCountingAddAll(integers))
        );
    }

    @Test
    void internalCountingPutAll()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);

        Integer[] instToAdd = {7, 7, 8, 9};
        Class<UnsupportedOperationException> exc = UnsupportedOperationException.class;
        Assertions.assertAll(
                () -> Assertions.assertThrows(exc, () -> integers.internalCountingPutAll(instToAdd)),
                () -> Assertions.assertThrows(exc, () -> integers.internalCountingPutAll(instToAdd, 2, 3)),
                () -> Assertions.assertThrows(exc, () -> integers.internalCountingPutAll(integers))
        );
    }

    @Test
    void copy()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ConstList<Integer> copy = integers.copy();
        Assertions.assertIterableEquals(integers, copy);
    }

    @Test
    void immure()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ConstList<Integer> immure = integers.immure();
        Assertions.assertIterableEquals(integers, immure);
    }

    @Test
    void toReversed()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ConstList<Integer> list = integers.toReversed();

        ConstList<Integer> toCompare = ConstList.New(8, 7, 6, 5, 4, 3, 2, 1);
        Assertions.assertIterableEquals(toCompare, list);
    }

    @Test
    void toArray_withType()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] intArray = integers.toArray(Integer.class);

        Integer[] toCompare = {1, 2, 3, 4, 5, 6, 7, 8};
        Assertions.assertArrayEquals(toCompare, intArray);
    }

    @Test
    void joinTest()
    {
        Map<Integer, Integer> map = new HashMap<>();
        BiConsumer<Integer, Integer> biConsumer = map::put;
        ConstList<Integer> constList = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer join = constList.join(biConsumer, 8);
        Assertions.assertEquals(8, map.size());
    }

    @Test
    void iterateIndexed()
    {
        AtomicInteger aInt = new AtomicInteger(0);
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        integers.iterateIndexed((e, index) -> aInt.addAndGet(e));
        Assertions.assertEquals(36, aInt.get());
    }

    @Test
    void count()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 1, 2);
        Assertions.assertAll(
                () -> assertEquals(2, integers.count(2)),
                () -> assertEquals(1, integers.count(3)),
                () -> assertEquals(0, integers.count(9))
        );
    }

    @Test
    void countBy()
    {
        Predicate<Integer> predicate = i -> i < 5;
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(4, list.countBy(predicate));
    }

    @Test
    void indexOf()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(4, integers.indexOf(5));
    }

    @Test
    void indexBy()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(4, integers.indexBy(integer -> integer.equals(5)));
    }

    @Test
    void lastIndexOf()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 1, 2);
        assertEquals(7, integers.lastIndexOf(2));
    }

    @Test
    void lastIndexBy()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 1, 2);
        assertEquals(7, integers.lastIndexBy(integer -> integer.equals(2)));
    }

    @Test
    void maxIndex()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 1, 2, 3, 1, 2, 3);
        Comparator<Integer> comparator2 = (Integer i1, Integer i2) -> {
            Integer valueToFind = 2;
            if (i1 == null || i1 < valueToFind) {
                return -1;
            } else if (i2 == valueToFind) {
                return 0;
            } else if (i2 > valueToFind) {
                return 1;
            } else {
                return -1;
            }
        };
        assertEquals(7, list.maxIndex(comparator2));
    }

    @Test
    void minIndex()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 1, 2, 3, 1, 2, 3);
        Comparator<Integer> comparator2 = (Integer i1, Integer i2) -> {
            Integer valueToFind = 2;
            if (i2 == null) {
                return -1;
            } else if (i1 < i2) {
                return -1;
            } else if (i1 < valueToFind) {
                return -1;
            } else if (i1.equals(valueToFind)) {
                return 0;
            } else {
                return 1;
            }
        };
        assertNotEquals(0, list.minIndex(comparator2));
    }

    @Test
    void scan()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Predicate<Integer> predicate = (integer -> integer.equals(2));
        assertEquals(1, list.scan(predicate));
    }

    @Test
    void get()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(1, integers.get());
    }

    @Test
    void first()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(1, integers.first());
    }

    @Test
    void last()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(8, integers.last());
    }

    @Test
    void poll()
    {
        ConstList<Integer> integers = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(1, integers.poll());
    }

    @Test
    void pollEmpty()
    {
        ConstList<Integer> list = ConstList.New();
        Assertions.assertNull(list.poll());
    }

    @Test
    void peek()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(8, list.peek());
    }

    @Test
    void peekEmpty()
    {
        ConstList<Integer> list = ConstList.New();
        Assertions.assertNull(list.peek());
    }

    @Test
    void seek()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(5, list.seek(5));
        Assertions.assertNull(list.seek(4000));
    }

    @Test
    void seekEmpty()
    {
        ConstList<Integer> list = ConstList.New();
        Assertions.assertNull(list.seek(5));
    }

    @Test
    void search()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Predicate<Integer> searchPredicate = i -> i.equals(3);
        assertEquals(3, list.search(searchPredicate));
    }

    @Test
    void searchEmpty()
    {
        ConstList<Integer> list = ConstList.New();
        Predicate<Integer> searchPredicate = i -> i.equals(3);
        Assertions.assertNull(list.search(searchPredicate));
    }

    @Test
    void hasVolatileElements()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        assertFalse(list.hasVolatileElements());
    }

    @Test
    void nulAllowed()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        assertTrue(list.nullAllowed());
    }

    @Test
    void isSorted()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Comparator<Integer> integerComparator = Integer::compare;
        assertTrue(list.isSorted(integerComparator));
    }

    @Test
    void isSortedFalse()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8, 1);
        Comparator<Integer> integerComparator = Integer::compare;
        assertFalse(list.isSorted(integerComparator));
    }

    @Test
    void constainsSearched()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertAll(
                () -> assertTrue(list.containsSearched(integer -> integer.equals(3))),
                () -> assertFalse(list.containsSearched(integer -> integer.equals(50)))
        );
    }

    @Test
    void applies()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Predicate<Integer> searchPredicate = i -> i < 9;
        assertTrue(list.applies(searchPredicate));
    }

    @Test
    void appliesNotAll()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Predicate<Integer> searchPredicate = i -> i < 5;
        assertFalse(list.applies(searchPredicate));
    }

    @Test
    void nullContained()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ConstList<Integer> nullList = ConstList.New(1, 2, 3, 4, 5, 6, 7, null, 8);
        Assertions.assertAll(
                () -> assertFalse(list.nullContained()),
                () -> assertTrue(nullList.nullContained())
        );
    }

    @Test
    void containsId()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertAll(
                () -> assertTrue(list.containsId(3)),
                () -> assertFalse(list.containsId(50))
        );
    }

    @Test
    void contains()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertAll(
                () -> assertTrue(list.contains(3)),
                () -> assertFalse(list.contains(50))
        );
    }

    @Test
    void containsAll()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ConstList<Integer> contains = ConstList.New(1, 2, 5, 3, 4);
        ConstList<Integer> containsNot = ConstList.New(1, 2, 5, 3, 4, 10);
        Assertions.assertAll(
                () -> assertTrue(list.containsAll(contains)),
                () -> assertFalse(list.containsAll(containsNot))
        );
    }

    @Test
    void equalsTestXGettingCollection()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ConstList<Integer> sameList = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ConstList<Integer> differentList = ConstList.New(1, 2, 3, 4, 5, 6, 7);

        Assertions.assertAll(
                () -> assertTrue(list.equals(sameList, Equalator.value())),
                () -> assertTrue(list.equals(list, Equalator.value())),
                () -> assertFalse(list.equals(differentList, Equalator.value()))
        );
    }

    @Test
    void equalsContent()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ConstList<Integer> sameList = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ConstList<Integer> differentList = ConstList.New(1, 2, 3, 4, 5, 6, 7);

        Assertions.assertAll(
                () -> assertTrue(list.equalsContent(sameList, Equalator.value())),
                () -> assertTrue(list.equalsContent(list, Equalator.value())),
                () -> assertFalse(list.equalsContent(differentList, Equalator.value()))
        );
    }

    @Test
    void intersect()
    {
        ConstList<Integer> collection1 = ConstList.New(1, 2, 3);
        ConstList<Integer> collection2 = ConstList.New(2, 3, 4);
        BulkList<Integer> intersection = collection1.intersect(collection2, Equalator.identity(), BulkList.New());
        BulkList<Integer> collectionForCompare = BulkList.New(2, 3);
        Assertions.assertAll(
                () -> Assertions.assertIterableEquals(collectionForCompare, intersection),
                () -> assertTrue(intersection.equals(collectionForCompare, Equalator.value()))
        );
    }

    @Test
    void except()
    {
        ConstList<Integer> collection1 = ConstList.New(1, 2, 3);
        ConstList<Integer> collection2 = ConstList.New(2, 3, 4);
        BulkList<Integer> exceptCollection = collection1.except(collection2, Equalator.identity(), BulkList.New());
        Assertions.assertAll(
                () -> assertEquals(1, exceptCollection.size()),
                () -> assertEquals(1, exceptCollection.get())
        );
    }

    @Test
    void union()
    {
        ConstList<Integer> collection1 = ConstList.New(1, 2, 3);
        ConstList<Integer> collection2 = ConstList.New(2, 3, 4);
        BulkList<Integer> union = collection1.union(collection2, Equalator.identity(), BulkList.New());
        BulkList<Integer> collectionForCompare = BulkList.New(1, 2, 3, 4);
        Assertions.assertAll(
                () -> Assertions.assertIterableEquals(collectionForCompare, union),
                () -> assertTrue(union.equals(collectionForCompare, Equalator.value()))
        );
    }

    @Test
    void copyTo()
    {
        ConstList<Integer> collection1 = ConstList.New(1, 2, 3);
        BulkList<Integer> copiedCollection = collection1.copyTo(BulkList.New());
        Assertions.assertAll(
                () -> Assertions.assertIterableEquals(copiedCollection, collection1),
                () -> Assertions.assertIterableEquals(copiedCollection, collection1)
        );
    }

    @Test
    void filterTo()
    {
        ConstList<Integer> collection1 = ConstList.New(1, 2, 3);
        BulkList<Integer> filteredCollection = collection1.filterTo(BulkList.New(), e -> e % 2 == 0);
        Assertions.assertAll(
                () -> assertEquals(1, filteredCollection.size()),
                () -> assertEquals(2, filteredCollection.get())
        );
    }

    @Test
    void distinct()
    {
        ConstList<Integer> collection1 = ConstList.New(1, 2, 2, 3);
        BulkList<Integer> distinctCollection = collection1.distinct(BulkList.New());
        BulkList<Integer> compareResult = BulkList.New(1, 2, 3);
        assertTrue(distinctCollection.equals(compareResult, Equalator.value()));
    }

    @Test
    void distinctEqualator()
    {
        ConstList<Integer> collection1 = ConstList.New(1, 2, 2, 3);
        BulkList<Integer> distinctCollection = collection1.distinct(BulkList.New(), Equalator.value());
        BulkList<Integer> compareResult = BulkList.New(1, 2, 3);
        assertTrue(distinctCollection.equals(compareResult, Equalator.value()));
    }

    @Test
    void copySelection()
    {
        ConstList<Integer> constList = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> ConstList1 = constList.copySelection(BulkList.New(), 1, 2, 3);
        BulkList<Integer> compareResult = BulkList.New(2, 3, 4);
        assertTrue(ConstList1.equals(compareResult, Equalator.value()));
    }

    @Test
    void listIterator()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertNotNull(list.listIterator());
    }

    @Test
    void listIteratorIndex()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ListIterator<Integer> integerListIterator = list.listIterator(3);
        Integer next = integerListIterator.next();
        Assertions.assertEquals(4, next);
    }

    @Test
    void isFull()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        assertTrue(list.isFull());
    }

    @Test
    void remainingCapacity()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(0, list.remainingCapacity());
    }

    @Test
    void view()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ConstList<Integer> view = list.view();
        Assertions.assertIterableEquals(list, view);
    }

    @Test
    void view_index()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertThrows(NotImplementedYetError.class, () -> list.view(1, 4));
    }

    @Test
    void range_index()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertThrows(NotImplementedYetError.class, () -> list.range(1, 4));
    }

    @Test
    void toStringTest()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        String toString = list.toString();
        Assertions.assertEquals("[1, 2, 3, 4, 5, 6, 7, 8]", toString);
    }

    @Test
    void toArray()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Object[] objects = list.toArray();
        Object[] compare = {1, 2, 3, 4, 5, 6, 7, 8};
        Assertions.assertArrayEquals(compare, objects);
    }

    @Test
    void atIndex()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertAll(
                () -> assertEquals(3, list.at(2)),
                () -> Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.at(Long.MAX_VALUE))
        );
    }

    @Test
    @SuppressWarnings("deprecation")
    void equalsTest()
    {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        ConstList<Integer> constList = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertTrue(constList.equals(list));
    }

    @Test
    @SuppressWarnings("deprecation")
    void hashCodeTest()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(list.hashCode() != 0);
    }

}
