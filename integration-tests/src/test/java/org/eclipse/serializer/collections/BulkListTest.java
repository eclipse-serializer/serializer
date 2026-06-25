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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.serializer.collections.types.XIterable;
import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.exceptions.IndexBoundsException;
import org.eclipse.serializer.functional.Aggregator;
import org.eclipse.serializer.meta.NotImplementedYetError;
import org.eclipse.serializer.typing.KeyValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class BulkListTest
{
    @Test
    void immurerTest()
    {
        Function<BulkList<Integer>, ConstList<Integer>> immurer = BulkList.Immurer();
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6);
        ConstList<Integer> constList = immurer.apply(bulkList);

        Assertions.assertIterableEquals(bulkList, constList);
    }

    @Test
    void builderDefaultTest()
    {
        Integer i = 20;
        Aggregator<Integer, BulkList<Integer>> builder = BulkList.Builder();
        builder.accept(i);
        BulkList<Integer> yield = builder.yield();
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, yield.size()),
                () -> Assertions.assertTrue(yield.contains(i))
        );
    }

    @Test
    void newTest()
    {
        BulkList<Integer> bulkList = BulkList.New();
        Assertions.assertAll(
                () -> Assertions.assertNotNull(bulkList),
                () -> Assertions.assertEquals(0, bulkList.size()),
                () -> Assertions.assertTrue(bulkList.isEmpty())
        );
    }

    @Test
    void newXIterableTest()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> newBulkList = BulkList.New((XIterable<Integer>) bulkList);

        Assertions.assertTrue(bulkList.equals(newBulkList, Equalator.value()));
    }

    @Test
    void newIterableTest()
    {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> newBulkList = BulkList.New((Iterable<Integer>) list);

        Assertions.assertTrue(bulkList.equals(newBulkList, Equalator.value()));
    }

    @Test
    void newInitialCapacityTest()
    {
        BulkList<Integer> bulkList = BulkList.New(100);
        Assertions.assertTrue(bulkList.currentCapacity() > 99);
    }

    @Test
    void constructorWithExistingBulkList()
    {
        Integer item = 50;
        BulkList<Integer> bulkList = BulkList.New(100);
        bulkList.add(item);
        BulkList<Integer> bulkList1 = new BulkList<>(bulkList);
        Assertions.assertAll(
                () -> Assertions.assertEquals(bulkList.currentCapacity(), bulkList1.currentCapacity()),
                () -> Assertions.assertTrue(bulkList1.contains(item))
        );
    }

    @Test
    void constructorWithInitialCapacityArrayAndRange()
    {
        Integer[] integers = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        BulkList<Integer> bulkList = new BulkList<>(12, integers, 2, 4);
        Assertions.assertAll(
                () -> Assertions.assertTrue(bulkList.currentCapacity() > 10),
                () -> Assertions.assertEquals(4, bulkList.size())
        );
    }

    @Test
    void equalator()
    {
        BulkList<Integer> bulkList = new BulkList<>();
        Assertions.assertEquals(Equalator.identity(), bulkList.equality());
    }

    @Test
    void copyTest()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> copy = bulkList.copy();
        Assertions.assertIterableEquals(bulkList, copy);
    }

    @Test
    void toReversed()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> reversedToCompare = BulkList.New(8, 7, 6, 5, 4, 3, 2, 1);
        BulkList<Integer> bulkList1 = bulkList.toReversed();
        Assertions.assertIterableEquals(reversedToCompare, bulkList1);
    }

    @Test
    void toArray()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {1, 2, 3, 4, 5, 6, 7, 8};
        Integer[] integers1 = bulkList.toArray(Integer.class);
        Assertions.assertArrayEquals(integers, integers1);
    }

    @Test
    void iterate()
    {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Consumer<Integer> consumer = atomicInteger::addAndGet;
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Consumer<Integer> iterate = bulkList.iterate(consumer);
        Assertions.assertEquals(36, atomicInteger.get());
    }

    @Test
    void joinTest()
    {
        Map<Integer, Integer> map = new HashMap<>();
        BiConsumer<Integer, Integer> biConsumer = map::put;
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer join = bulkList.join(biConsumer, 8);
        Assertions.assertEquals(8, map.size());
    }

    @Test
    void countTest()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 1, 1);
        Assertions.assertEquals(3, bulkList.count(1));
    }

    @Test
    void countBy()
    {
        Predicate<Integer> predicate = i -> i < 5;
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(4, bulkList.countBy(predicate));
    }

    @Test
    void newFromSingle()
    {
        Integer i = 20;
        BulkList<Integer> bulkList = BulkList.NewFromSingle(i);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, bulkList.size()),
                () -> Assertions.assertTrue(bulkList.contains(i))
        );
    }

    @Test
    void newFromXIterable()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> aNew = BulkList.New(bulkList);
        Assertions.assertIterableEquals(bulkList, aNew);
    }

    @Test
    void indexOf()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(3, bulkList.indexOf(4));
    }

    @Test
    void indexBy()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Predicate<Integer> predicate = i -> i == 4;
        Assertions.assertEquals(3, bulkList.indexBy(predicate));
    }

    @Test
    void lastIndexOf()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 1, 2, 3, 1, 2, 3);
        Assertions.assertEquals(6, bulkList.lastIndexOf(1));
    }

    @Test
    void lastIndexBy()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 1, 2, 3, 1, 2, 3);
        Predicate<Integer> predicate = i -> i == 1;
        Assertions.assertEquals(6, bulkList.lastIndexBy(predicate));
    }

    @Test
    void maxIndex()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 1, 2, 3, 1, 2, 3);
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
        Assertions.assertEquals(7, bulkList.maxIndex(comparator2));
    }

    @Test
    void minIndex()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 1, 2, 3, 1, 2, 3);
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
        Assertions.assertNotEquals(0, bulkList.minIndex(comparator2));
    }

    @Test
    void scan()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Predicate<Integer> predicate = (integer -> integer.equals(2));
        Assertions.assertEquals(1, bulkList.scan(predicate));
    }

    @Test
    void get()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(1, bulkList.get());
    }

    @Test
    void getEmptyList()
    {
        BulkList<Integer> bulkList = BulkList.New();
        Assertions.assertThrows(NoSuchElementException.class, bulkList::get);
    }

    @Test
    void first()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(1, bulkList.first());
    }

    @Test
    void firstEmptyList()
    {
        BulkList<Integer> bulkList = BulkList.New();
        Assertions.assertThrows(IndexOutOfBoundsException.class, bulkList::first);
    }

    @Test
    void last()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(8, bulkList.last());
    }

    @Test
    void lastEmptyList()
    {
        BulkList<Integer> bulkList = BulkList.New();
        Assertions.assertThrows(IndexOutOfBoundsException.class, bulkList::last);
    }

    @Test
    void poll()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(1, bulkList.poll());
    }

    @Test
    void pollEmptyList()
    {
        BulkList<Integer> bulkList = BulkList.New();
        Assertions.assertNull(bulkList.poll());
    }

    @Test
    void peek()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(8, bulkList.peek());
    }

    @Test
    void peekEmptyList()
    {
        BulkList<Integer> bulkList = BulkList.New();
        Assertions.assertNull(bulkList.peek());
    }

    @Test
    void search()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Predicate<Integer> searchPredicate = i -> i.equals(3);
        Assertions.assertEquals(3, bulkList.search(searchPredicate));
    }

    @Test
    void searchEmpty()
    {
        BulkList<Integer> bulkList = BulkList.New();
        Predicate<Integer> searchPredicate = i -> i.equals(3);
        Assertions.assertNull(bulkList.search(searchPredicate));
    }

    @Test
    void searchNotFound()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Predicate<Integer> searchPredicate = i -> i.equals(11);
        Assertions.assertNull(bulkList.search(searchPredicate));
    }

    @Test
    void seek()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(3, bulkList.seek(3));
    }

    @Test
    void seekEmpty()
    {
        BulkList<Integer> bulkList = BulkList.New();
        Assertions.assertNull(bulkList.seek(10));
    }

    @Test
    void seekNotFound()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertNull(bulkList.seek(10));
    }

    @Test
    @Disabled("https://github.com/microstream-one/microstream-private/issues/668")
    void max()
    {
        Comparator<Integer> integerComparator = Integer::compare;
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(8, bulkList.max(integerComparator));
    }

    @Test
    @Disabled("https://github.com/microstream-one/microstream-private/issues/668")
    void min()
    {
        Comparator<Integer> integerComparator = Integer::compare;
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(1, bulkList.min(integerComparator));
    }

    @Test
    void hasVolatileElements()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertFalse(bulkList.hasVolatileElements());
    }

    @Test
    void nullAllowed()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.nullAllowed());
    }

    @Test
    void isSorted()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Comparator<Integer> integerComparator = Integer::compare;
        Assertions.assertTrue(bulkList.isSorted(integerComparator));
    }

    @Test
    void isSortedFalse()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 1, 2, 3, 1, 2, 3);
        Comparator<Integer> integerComparator = Integer::compare;
        Assertions.assertFalse(bulkList.isSorted(integerComparator));
    }

    @Test
    void containsSearched()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Predicate<Integer> searchPredicate = i -> i.equals(8);
        Assertions.assertTrue(bulkList.containsSearched(searchPredicate));
    }

    @Test
    void applies()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Predicate<Integer> searchPredicate = i -> i < 9;
        Assertions.assertTrue(bulkList.applies(searchPredicate));
    }

    @Test
    void appliesNotAll()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Predicate<Integer> searchPredicate = i -> i < 5;
        Assertions.assertFalse(bulkList.applies(searchPredicate));
    }

    @Test
    void appliesEmptyList()
    {
        BulkList<Integer> bulkList = BulkList.New();
        Predicate<Integer> searchPredicate = i -> i < 5;
        Assertions.assertFalse(bulkList.applies(searchPredicate));
    }

    @Test
    void nullContained()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertFalse(bulkList.nullContained());
        BulkList<Integer> bulkList1 = BulkList.New(1, 2, 3, 4, null, 6, 7, 8);
        Assertions.assertTrue(bulkList1.nullContained());
    }

    @Test
    void containsId()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertAll(
                () -> Assertions.assertTrue(bulkList.containsId(1)),
                () -> Assertions.assertFalse(bulkList.containsId(99))
        );
    }

    @Test
    void containsAll()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> contains = BulkList.New(1, 2, 5, 3, 4);
        BulkList<Integer> containsNot = BulkList.New(1, 2, 5, 3, 4, 10);
        Assertions.assertAll(
                () -> Assertions.assertTrue(bulkList.containsAll(contains)),
                () -> Assertions.assertFalse(bulkList.containsAll(containsNot))
        );
    }

    @Test
    void equalsWithAnotherCollection()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkListEqual = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkListNotEqual = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Assertions.assertAll(
                () -> Assertions.assertTrue(bulkList.equals(bulkListEqual, Equalator.value())),
                () -> Assertions.assertFalse(bulkList.equals(bulkListNotEqual, Equalator.value())),
                () -> Assertions.assertTrue(bulkList.equals(bulkList, Equalator.value()))
        );
    }

    @Test
    void equalsContent()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkListEqual = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkListNotEqual = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Assertions.assertAll(
                () -> Assertions.assertTrue(bulkList.equalsContent(bulkListEqual, Equalator.value())),
                () -> Assertions.assertFalse(bulkList.equalsContent(bulkListNotEqual, Equalator.value())),
                () -> Assertions.assertTrue(bulkList.equalsContent(bulkList, Equalator.value()))
        );
    }

    @Test
    void intersect()
    {
        BulkList<Integer> collection1 = BulkList.New(1, 2, 3);
        BulkList<Integer> collection2 = BulkList.New(2, 3, 4);
        BulkList<Integer> intersection = collection1.intersect(collection2, Equalator.identity(), BulkList.New());
        BulkList<Integer> collectionForCompare = BulkList.New(2, 3);
        Assertions.assertAll(
                () -> Assertions.assertIterableEquals(collectionForCompare, intersection),
                () -> Assertions.assertTrue(intersection.equals(collectionForCompare, Equalator.value()))
        );
    }

    @Test
    void except()
    {
        BulkList<Integer> collection1 = BulkList.New(1, 2, 3);
        BulkList<Integer> collection2 = BulkList.New(2, 3, 4);
        BulkList<Integer> exceptCollection = collection1.except(collection2, Equalator.identity(), BulkList.New());
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptCollection.size()),
                () -> Assertions.assertEquals(1, exceptCollection.get())
        );
    }

    @Test
    void union()
    {
        BulkList<Integer> collection1 = BulkList.New(1, 2, 3);
        BulkList<Integer> collection2 = BulkList.New(2, 3, 4);
        BulkList<Integer> union = collection1.union(collection2, Equalator.identity(), BulkList.New());
        BulkList<Integer> collectionForCompare = BulkList.New(1, 2, 3, 4);
        Assertions.assertAll(
                () -> Assertions.assertIterableEquals(collectionForCompare, union),
                () -> Assertions.assertTrue(union.equals(collectionForCompare, Equalator.value()))
        );
    }

    @Test
    void copyTo()
    {
        BulkList<Integer> collection1 = BulkList.New(1, 2, 3);
        BulkList<Integer> copiedCollection = collection1.copyTo(BulkList.New());
        Assertions.assertAll(
                () -> Assertions.assertIterableEquals(copiedCollection, collection1),
                () -> Assertions.assertTrue(copiedCollection.equals(collection1, Equalator.value()))
        );
    }

    @Test
    void filterTo()
    {
        BulkList<Integer> collection1 = BulkList.New(1, 2, 3);
        BulkList<Integer> filteredCollection = collection1.filterTo(BulkList.New(), e -> e % 2 == 0);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, filteredCollection.size()),
                () -> Assertions.assertEquals(2, filteredCollection.get())
        );
    }

    @Test
    void distinct()
    {
        BulkList<Integer> collection1 = BulkList.New(1, 2, 2, 3);
        BulkList<Integer> distinctCollection = collection1.distinct(BulkList.New());
        BulkList<Integer> compareResult = BulkList.New(1, 2, 3);
        Assertions.assertTrue(distinctCollection.equals(compareResult, Equalator.value()));
    }

    @Test
    void distinctEqualator()
    {
        BulkList<Integer> collection1 = BulkList.New(1, 2, 2, 3);
        BulkList<Integer> distinctCollection = collection1.distinct(BulkList.New(), Equalator.value());
        BulkList<Integer> compareResult = BulkList.New(1, 2, 3);
        Assertions.assertTrue(distinctCollection.equals(compareResult, Equalator.value()));
    }

    @Test
    void copySelection()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkList1 = bulkList.copySelection(BulkList.New(), 1, 2, 3);
        BulkList<Integer> compareResult = BulkList.New(2, 3, 4);
        Assertions.assertTrue(bulkList1.equals(compareResult, Equalator.value()));
    }

    @Test
    void view_ListView()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ListView<Integer> view = bulkList.view();
        Assertions.assertIterableEquals(bulkList, view);
    }

    @Test
    void view_SubListView()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        SubListView<Integer> view = bulkList.view(1, 3);
        Assertions.assertEquals(3, view.size());
    }

    @Test
    void shiftTo()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkList1 = bulkList.shiftTo(1, 3);

        BulkList<Integer> resultToCompare = BulkList.New(1, 3, 4, 2, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList1.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void shiftTo_validationTest()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertAll(
                () -> Assertions.assertThrows(IndexExceededException.class, () -> bulkList.shiftTo(20, 25)),
                () -> Assertions.assertThrows(IndexExceededException.class, () -> bulkList.shiftTo(2, 25)),
                () -> Assertions.assertThrows(IndexExceededException.class, () -> bulkList.shiftTo(-5, -5))
        );
    }

    @Test
    void shiftTo_sameIndex()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkList1 = bulkList.shiftTo(2, 2);
        Assertions.assertTrue(bulkList.equals(bulkList1, Equalator.value()));
    }

    @Test
    void shiftAnotherDirection()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkList1 = bulkList.shiftTo(3, 1);

        BulkList<Integer> resultToCompare = BulkList.New(1, 4, 2, 3, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList1.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void shiftToAmount()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkList1 = bulkList.shiftTo(1, 3, 2);

        BulkList<Integer> resultToCompare = BulkList.New(1, 4, 5, 2, 3, 6, 7, 8);
        Assertions.assertTrue(bulkList1.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void shiftToAmount_validationTest()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertAll(
                () -> Assertions.assertThrows(IndexExceededException.class, () -> bulkList.shiftTo(20, 25, 2)),
                () -> Assertions.assertThrows(IndexExceededException.class, () -> bulkList.shiftTo(2, 25, 2)),
                () -> Assertions.assertThrows(IndexExceededException.class, () -> bulkList.shiftTo(-5, -5, 2))
        );
    }

    @Test
    void shiftToAmount_sameIndex()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkList1 = bulkList.shiftTo(2, 2, 2);
        Assertions.assertTrue(bulkList.equals(bulkList1, Equalator.value()));
    }

    @Test
    void shiftToAmountAnotherDirection()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkList1 = bulkList.shiftTo(3, 1, 2);

        BulkList<Integer> resultToCompare = BulkList.New(1, 4, 5, 2, 3, 6, 7, 8);
        Assertions.assertTrue(bulkList1.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void shiftBy()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkList1 = bulkList.shiftBy(1, 3);

        BulkList<Integer> resultToCompare = BulkList.New(1, 3, 4, 5, 2, 6, 7, 8);
        Assertions.assertTrue(bulkList1.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void shiftByLength()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkList1 = bulkList.shiftBy(1, 3, 2);

        BulkList<Integer> resultToCompare = BulkList.New(1, 4, 5, 6, 2, 3, 7, 8);
        Assertions.assertTrue(bulkList1.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void swap()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> swap = bulkList.swap(1, 4, 2);

        BulkList<Integer> resultToCompare = BulkList.New(1, 5, 6, 4, 2, 3, 7, 8);
        Assertions.assertTrue(swap.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void swapElement()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> swap = bulkList.swap(1, 4);

        BulkList<Integer> resultToCompare = BulkList.New(1, 5, 3, 4, 2, 6, 7, 8);
        Assertions.assertTrue(swap.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void reverse()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> reverse = bulkList.reverse();

        BulkList<Integer> resultToCompare = BulkList.New(8, 7, 6, 5, 4, 3, 2, 1);
        Assertions.assertTrue(reverse.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void setFirst()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.setFirst(10);

        BulkList<Integer> resultToCompare = BulkList.New(10, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void setLast()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.setLast(10);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 10);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void setAll()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);

        bulkList.setAll(0, 10, 20, 30, 40, 50, 60, 70 /*, 80*/);

        BulkList<Integer> resultToCompare = BulkList.New(10, 20, 30, 40, 50, 60, 70, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void setAll_fullLength()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);

        bulkList.setAll(0, 10, 20, 30, 40, 50, 60, 70, 80);

        BulkList<Integer> resultToCompare = BulkList.New(10, 20, 30, 40, 50, 60, 70, 80);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void setAll_oneElement()
    {
        BulkList<Integer> bulkList = BulkList.NewFromSingle(1);

        bulkList.setAll(0, 80);

        BulkList<Integer> resultToCompare = BulkList.NewFromSingle(80);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void set()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30, 40, 50, 60, 70, 80};
        BulkList<Integer> set = bulkList.set(0, integers, 0, 6);

        BulkList<Integer> resultToCompare = BulkList.New(10, 20, 30, 40, 50, 60, 7, 8);
        Assertions.assertTrue(set.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void setWithCollection()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> src = BulkList.New(10, 20, 30, 40, 50, 60, 70, 80);
        Assertions.assertThrows(NotImplementedYetError.class, () -> bulkList.set(0, src, 0, 6));
    }

    @Test
    void fill()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> fill = bulkList.fill(0, 8, 10);

        BulkList<Integer> resultToCompare = BulkList.New(10, 10, 10, 10, 10, 10, 10, 10);
        Assertions.assertTrue(fill.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void sort()
    {
        BulkList<Integer> bulkList = BulkList.New(10, 2, 3, 4, 5, 6, 7, 8);
        Comparator<Integer> integerComparator = Integer::compare;
        BulkList<Integer> sort = bulkList.sort(integerComparator);

        BulkList<Integer> resultToCompare = BulkList.New(2, 3, 4, 5, 6, 7, 8, 10);
        Assertions.assertTrue(sort.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void replaceOne_element()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 40, 5, 6, 7, 8);
        Assertions.assertAll(
                () -> Assertions.assertTrue(bulkList.replaceOne(4, 40)),
                () -> Assertions.assertFalse(bulkList.replaceOne(50, 50)),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void replaceOne_predicate()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 1, 2, 3, 1, 2, 3);
        Predicate<Integer> predicate = i -> i.equals(2);
        Predicate<Integer> predicateNotExists = i -> i.equals(80);

        BulkList<Integer> resultToCompare = BulkList.New(1, 20, 3, 1, 2, 3, 1, 2, 3);
        Assertions.assertAll(
                () -> Assertions.assertTrue(bulkList.replaceOne(predicate, 20)),
                () -> Assertions.assertFalse(bulkList.replaceOne(predicateNotExists, 50)),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void replace_element()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 1, 2, 3, 1, 2, 3);
        long replace = bulkList.replace(2, 20);

        BulkList<Integer> resultToCompare = BulkList.New(1, 20, 3, 1, 20, 3, 1, 20, 3);
        Assertions.assertAll(
                () -> Assertions.assertEquals(3, replace),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void replace_predicate()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 1, 2, 3, 1, 2, 3);
        Predicate<Integer> predicate = i -> i.equals(2);
        Predicate<Integer> predicateNotExists = i -> i.equals(80);

        BulkList<Integer> resultToCompare = BulkList.New(1, 20, 3, 1, 20, 3, 1, 20, 3);
        Assertions.assertAll(
                () -> Assertions.assertEquals(3, bulkList.replace(predicate, 20)),
                () -> Assertions.assertEquals(0, bulkList.replace(predicateNotExists, 50)),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void replaceAll()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> listToReplace = BulkList.New(1, 2, 3);

        long l = bulkList.replaceAll(listToReplace, 20);
        BulkList<Integer> resultToCompare = BulkList.New(20, 20, 20, 4, 5, 6, 7, 8);
        Assertions.assertAll(
                () -> Assertions.assertEquals(3, l),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void substitute()
    {
        Function<Integer, Integer> substituteFunction = integer -> integer.equals(3) ? 30 : integer;
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 1, 2, 3, 1, 2, 3);

        long substitute = bulkList.substitute(substituteFunction);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 30, 1, 2, 30, 1, 2, 30);
        Assertions.assertAll(
                () -> Assertions.assertEquals(3, substitute),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void substitute_predicate_function()
    {
        Function<Integer, Integer> substituteFunction = integer -> 30;
        Predicate<Integer> predicate = integer -> integer.equals(3);

        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 1, 2, 3, 1, 2, 3);
        long substitute = bulkList.substitute(predicate, substituteFunction);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 30, 1, 2, 30, 1, 2, 30);
        Assertions.assertAll(
                () -> Assertions.assertEquals(3, substitute),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void currentCapacity()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(8, bulkList.currentCapacity());
    }

    @Test
    void maximumCapacity()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(Integer.MAX_VALUE, bulkList.maximumCapacity());
    }

    @Test
    void isFull()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertFalse(bulkList.isFull());
    }

    @Test
    void optimize()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.add(10);
        bulkList.remove(10);
        long optimize = bulkList.optimize();
        Assertions.assertEquals(8, optimize);
    }

    @Test
    void ensureFreeCapacity()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.ensureFreeCapacity(1000);
        long l = bulkList.currentCapacity();
        Assertions.assertEquals(1024, l);
    }

    @Test
    void ensureCapacity()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.ensureCapacity(1024);
        long l = bulkList.currentCapacity();
        Assertions.assertEquals(1024, l);
    }

    @Test
    void addAllElements()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {11, 22, 33, 44, 55, 66};
        BulkList<Integer> bulkList1 = bulkList.addAll(integers);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 11, 22, 33, 44, 55, 66);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void addAllElements_offsetLenth()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {11, 22, 33, 44, 55, 66};
        BulkList<Integer> bulkList1 = bulkList.addAll(integers, 2, 2);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 33, 44);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void addAllElements_offsetLenth_minus()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {11, 22, 33, 44, 55, 66};
        BulkList<Integer> bulkList1 = bulkList.addAll(integers, 4, -2);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 55, 44);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void addAllXGettingCollection()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        KeyValue<Integer, Integer> keyValue = KeyValue.New(10, 30);
        ConstHashTable<Integer, Integer> table = ConstHashTable.New(keyValue);
        bulkList.addAll(table.values());

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 30);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void nullAdd()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.nullAdd());

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, null);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void nullPut()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.nullPut());

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, null);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void put()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.put(9));

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void putAll()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30, 40, 50};
        bulkList.putAll(integers);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 10, 20, 30, 40, 50);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void putAllOffsetLength()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30, 40, 50};
        bulkList.putAll(integers, 2, 2);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 30, 40);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void putAllXCollection()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> listToPut = BulkList.New(10, 20);

        bulkList.putAll(listToPut);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 10, 20);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void prepend()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.prepend(10);

        BulkList<Integer> resultToCompare = BulkList.New(10, 1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void prepend_emptyCollection()
    {
        BulkList<Integer> bulkList = BulkList.New();
        bulkList.prepend(10);

        BulkList<Integer> resultToCompare = BulkList.NewFromSingle(10);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void prependAll()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30, 40, 50};
        bulkList.prependAll(integers);

        BulkList<Integer> resultToCompare = BulkList.New(10, 20, 30, 40, 50, 1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void prependAllOffsetLength()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30, 40, 50};
        bulkList.prependAll(integers, 2, 2);

        BulkList<Integer> resultToCompare = BulkList.New(30, 40, 1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void prependXGettingCollection()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> prependList = BulkList.New(10, 20, 30, 40, 50);

        bulkList.prependAll(prependList);
        BulkList<Integer> resultToCompare = BulkList.New(10, 20, 30, 40, 50, 1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void nullPrepend()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.nullPrepend();

        BulkList<Integer> resultToCompare = BulkList.New(null, 1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void nullPrependEmpty()
    {
        BulkList<Integer> bulkList = BulkList.New();
        bulkList.nullPrepend();

        BulkList<Integer> resultToCompare = BulkList.NewFromSingle(null);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void preput()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.preput(5));

        BulkList<Integer> resultToCompare = BulkList.New(5, 1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void preputEmpty()
    {
        BulkList<Integer> bulkList = BulkList.New();
        Assertions.assertTrue(bulkList.preput(5));

        BulkList<Integer> resultToCompare = BulkList.NewFromSingle(5);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void preputAll()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30};
        bulkList.preputAll(integers);

        BulkList<Integer> resultToCompare = BulkList.New(10, 20, 30, 1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void preputAll_offsetLength()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30, 40, 50};
        bulkList.preputAll(integers, 2, 2);

        BulkList<Integer> resultToCompare = BulkList.New(30, 40, 1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void preputAll_collection()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> preput = BulkList.New(10, 20, 30, 40, 50);
        bulkList.preputAll(preput);

        BulkList<Integer> resultToCompare = BulkList.New(10, 20, 30, 40, 50, 1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void nullPreput_empty()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.nullPreput();

        BulkList<Integer> resultToCompare = BulkList.New(null, 1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void nullPreput()
    {
        BulkList<Integer> bulkList = BulkList.New();
        bulkList.nullPreput();

        BulkList<Integer> resultToCompare = BulkList.NewFromSingle(null);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void insert()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.insert(5, 1));

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 1, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void insert_toEmpty()
    {
        BulkList<Integer> bulkList = BulkList.New();
        Assertions.assertTrue(bulkList.insert(0, 1));

        BulkList<Integer> resultToCompare = BulkList.NewFromSingle(1);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void testInsert_outsideListSize()
    {
        // Create a new BulkList with some values
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);

        // Try to insert a value at an index that is outside the size of the list
        // and assert that an IndexExceededException is thrown
        Assertions.assertThrows(
                IndexExceededException.class,
                () -> bulkList.insert(9, 9)
        );

        // Assert that the size of the list remains unchanged
        // and that the list still contains the original values
        Assertions.assertEquals(8, bulkList.size());
        Assertions.assertArrayEquals(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8}, bulkList.toArray());
    }


    @Test
    void insert_newOne()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.insert(8, 10);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 10);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void insertAllElements()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30, 40};

        bulkList.insertAll(2, integers);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 10, 20, 30, 40, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void insertAllElementsOnEnd()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30, 40};

        bulkList.insertAll(8, integers);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 10, 20, 30, 40);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void insertAllElementsOnEndMinusOne()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30, 40};

        bulkList.insertAll(7, integers);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 10, 20, 30, 40, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void insertAll()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30, 40, 50, 60, 70, 80};

        bulkList.insertAll(2, integers, 2, 3);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 30, 40, 50, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void insertAll_end()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30, 40, 50, 60, 70, 80};

        bulkList.insertAll(8, integers, 2, 1);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 30);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void insertAll_endNegativ()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30, 40, 50, 60, 70, 80};

        bulkList.insertAll(8, integers, 2, -2);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 30, 20);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void insertAllCollection()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> insert = BulkList.New(10, 20, 30, 40);

        bulkList.insertAll(2, insert);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 10, 20, 30, 40, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void insertAllCollection_end()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> insert = BulkList.New(10, 20, 30, 40);

        bulkList.insertAll(8, insert);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 10, 20, 30, 40);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void insertAllCollectionNullInside()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> insert = BulkList.New(10, null, 30, 40, null);

        bulkList.insertAll(2, insert);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 10, null, 30, 40, null, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void nullInsert()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.nullInsert(2);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, null, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void input()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.input(5, 50);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 50, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void input_end()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.input(8, 50);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 50);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
        ;

    }

    @Test
    void input_first()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.input(0, 50);

        BulkList<Integer> resultToCompare = BulkList.New(50, 1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
        ;
    }

    @Test
    void input_outside()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertThrows(IndexExceededException.class, () -> bulkList.input(10, 50));
    }

    @Test
    void inputAll()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30};

        bulkList.inputAll(2, integers);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 10, 20, 30, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void inputAll_end()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30};

        bulkList.inputAll(8, integers);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 10, 20, 30);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void inputAllOffsetIndex()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30, 40, 50, 60, 70, 80};

        bulkList.inputAll(2, integers, 2, 3);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 30, 40, 50, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void inputAllOffsetIndexSameSize()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        Integer[] integers = {10, 20, 30, 40, 50, 60, 70, 80};

        bulkList.inputAll(2, integers, 2, 3);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 30, 40, 50, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void inputAllOffsetIndexSameSize_end()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        Integer[] integers = {10, 20, 30, 40, 50, 60, 70, 80};

        bulkList.inputAll(11, integers, 2, 3);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 30, 40, 50);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void inputAllOffsetIndexMinus()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer[] integers = {10, 20, 30, 40, 50, 60, 70, 80};

        bulkList.inputAll(2, integers, 5, -3);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 60, 50, 40, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void inputAllOffsetIndexSameSizeMinus()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        Integer[] integers = {10, 20, 30, 40, 50, 60, 70, 80};

        bulkList.inputAll(2, integers, 5, -3);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 60, 50, 40, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void inputAllOffset_EndIndexSameSizeMinus()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        Integer[] integers = {10, 20, 30, 40, 50, 60, 70, 80};

        bulkList.inputAll(11, integers, 5, -3);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 60, 50, 40);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }


    @Test
    void inputAllCollection()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> insert = BulkList.New(10, 20, 30, 40);

        bulkList.inputAll(2, insert);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 10, 20, 30, 40, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void inputAllCollection_end()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> insert = BulkList.New(10, 20, 30, 40);

        bulkList.inputAll(8, insert);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 10, 20, 30, 40);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void inputAllCollectionWihoutSizeChange()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        BulkList<Integer> insert = BulkList.New(10, 20);

        bulkList.inputAll(2, insert);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 10, 20, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void nullInput()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.nullInput(3);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, null, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void truncate()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.truncate();
        Assertions.assertTrue(bulkList.isEmpty());
    }

    @Test
    void consolidate()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(0, bulkList.consolidate());

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void removeOne()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.removeOne(5));

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));

        Assertions.assertFalse(bulkList.removeOne(1000));
    }

    @Test
    void retrieve()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(5, bulkList.retrieve(5));

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));

        Assertions.assertNull(bulkList.retrieve(1000));
    }

    @Test
    void retrieveBy()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Predicate<Integer> by = integer -> integer.equals(3);

        Assertions.assertEquals(3, bulkList.retrieveBy(by));

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));

        Predicate<Integer> notFound = integer -> integer.equals(1000);
        Assertions.assertNull(bulkList.retrieveBy(notFound));
    }

    @Test
    void nullRemove()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, null, 7, null);
        Assertions.assertEquals(2, bulkList.nullRemove());

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 7);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void removeAt()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 5, 6, 7, 8);
        Assertions.assertAll(
                () -> Assertions.assertEquals(4, bulkList.removeAt(3)),
                () -> Assertions.assertThrows(IndexExceededException.class, () -> bulkList.removeAt(100)),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void removeByPredicate()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 1, 2, 3, 1, 2, 3);
        Predicate<Integer> removeBy = integer -> integer.equals(2);
        BulkList<Integer> resultToCompare = BulkList.New(1, 3, 1, 3, 1, 3);
        Assertions.assertAll(
                () -> Assertions.assertEquals(3, bulkList.removeBy(removeBy)),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value())),
                () -> Assertions.assertEquals(0, bulkList.removeBy(integer -> integer.equals(100)))
        );
    }

    @Test
    void retainAll()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 1, 2, 3, 1, 2, 3);
        BulkList<Integer> retain = BulkList.New(1, 2);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 1, 2, 1, 2);
        Assertions.assertAll(
                () -> Assertions.assertEquals(3, bulkList.retainAll(retain)),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void process()
    {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Consumer<Integer> consumer = atomicInteger::getAndAdd;
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.process(consumer);
        Assertions.assertEquals(36, atomicInteger.get());
    }

    @Test
    void moveTo()
    {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Consumer<Integer> consumer = atomicInteger::getAndAdd;
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.moveTo(consumer, integer -> integer < 3);
        Assertions.assertEquals(3, atomicInteger.get());
    }

    @Test
    void moveSelection()
    {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Consumer<Integer> consumer = atomicInteger::getAndAdd;
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.moveSelection(consumer, 1, 2, 3, 4);
        Assertions.assertEquals(14, atomicInteger.get());
    }

    @Test
    void removeAll()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> bulkListToRemove = BulkList.New(1, 2, 3);
        long l = bulkList.removeAll(bulkListToRemove);

        BulkList<Integer> resultToCompare = BulkList.New(4, 5, 6, 7, 8);
        Assertions.assertAll(
                () -> Assertions.assertEquals(3, l),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void removeAllRepeat()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 1, 2, 3, 4);
        BulkList<Integer> bulkListToRemove = BulkList.New(1, 2, 3);
        long l = bulkList.removeAll(bulkListToRemove);

        BulkList<Integer> resultToCompare = BulkList.New(4, 4);
        Assertions.assertAll(
                () -> Assertions.assertEquals(6, l),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void removeDuplicatesWithEqualator()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 1, 2, 3, 4);
        long l = bulkList.removeDuplicates(Equalator.value());

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4);
        Assertions.assertAll(
                () -> Assertions.assertEquals(4, l),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void removeDuplicates()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 1, 2, 3, 4);
        long l = bulkList.removeDuplicates();

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4);
        Assertions.assertAll(
                () -> Assertions.assertEquals(4, l),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void fetch()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer fetch = bulkList.fetch();

        BulkList<Integer> resultToCompare = BulkList.New(2, 3, 4, 5, 6, 7, 8);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, fetch),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void fetchEmpty()
    {
        BulkList<Integer> bulkList = BulkList.New();
        Assertions.assertThrows(IndexOutOfBoundsException.class, bulkList::fetch);
    }

    @Test
    void pop()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer pop = bulkList.pop();

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7);
        Assertions.assertAll(
                () -> Assertions.assertEquals(8, pop),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void popEmpty()
    {
        BulkList<Integer> bulkList = BulkList.New();
        Assertions.assertThrows(IndexOutOfBoundsException.class, bulkList::pop);
    }

    @Test
    void pinch()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer pinch = bulkList.pinch();

        BulkList<Integer> resultToCompare = BulkList.New(2, 3, 4, 5, 6, 7, 8);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, pinch),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value())),
                () -> Assertions.assertNull(BulkList.New()
                        .pinch())
        );
    }

    @Test
    void pick()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer pick = bulkList.pick();

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7);
        Assertions.assertAll(
                () -> Assertions.assertEquals(8, pick),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value())),
                () -> Assertions.assertNull(BulkList.New()
                        .pick())
        );
    }

    @Test
    void removeSelection()
    {
        long[] indicies = {1, 2, 3};
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        long l = bulkList.removeSelection(indicies);

        BulkList<Integer> resultToCompare = BulkList.New(1, 5, 6, 7, 8);
        Assertions.assertAll(
                () -> Assertions.assertEquals(3, l),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void removeRange()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.removeRange(1, 4);

        BulkList<Integer> resultToCompare = BulkList.New(1, 6, 7, 8);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void removeRangeOut()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> bulkList.removeRange(23, 4));
    }

    @Test
    void retainRange()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.retainRange(1, 4);

        BulkList<Integer> resultToCompare = BulkList.New(2, 3, 4, 5);
        Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void range()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        SubList<Integer> range = bulkList.range(1, 4);

        BulkList<Integer> result = BulkList.New();
        range.process(result::add);
        BulkList<Integer> resultToCompare = BulkList.New(2, 3, 4, 5);
        Assertions.assertTrue(result.equals(resultToCompare, Equalator.value()));
    }

    @Test
    void listIterator()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        ListIterator<Integer> integerListIterator = bulkList.listIterator();

        BulkList<Integer> result = BulkList.New();
        while (integerListIterator.hasNext()) {
            result.add(integerListIterator.next());
        }

        Assertions.assertTrue(bulkList.equals(result, Equalator.value()));
    }

    @Test
    void listIteratorIndex()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        BulkList<Integer> resultToCompare = BulkList.New(3, 4, 5, 6, 7, 8);

        ListIterator<Integer> integerListIterator = bulkList.listIterator(2);

        BulkList<Integer> result = BulkList.New();
        while (integerListIterator.hasNext()) {
            result.add(integerListIterator.next());
        }

        Assertions.assertTrue(resultToCompare.equals(result, Equalator.value()));
    }

    @Test
    void listIteratorIndexThrown()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertThrows(IndexBoundsException.class, () -> bulkList.listIterator(50));
    }

    @Test
    void atTest()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertEquals(8, bulkList.at(7));
    }

    @Test
    void setTest()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        boolean set = bulkList.set(5, 50);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 50, 7, 8);
        Assertions.assertAll(
                () -> Assertions.assertFalse(set),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void setIndexBoundsExceptionTest()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertThrows(IndexBoundsException.class, () -> bulkList.set(50, 50));

    }

    @Test
    void setGet()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Integer integer = bulkList.setGet(5, 50);

        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 50, 7, 8);
        Assertions.assertAll(
                () -> Assertions.assertEquals(6, integer),
                () -> Assertions.assertTrue(bulkList.equals(resultToCompare, Equalator.value()))
        );
    }

    @Test
    void setGetIndexBoundsException()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertThrows(IndexBoundsException.class, () -> bulkList.setGet(50, 50));
    }

    @Test
    void clear()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        bulkList.clear();
        Assertions.assertTrue(bulkList.isEmpty());
        Assertions.assertEquals(0, bulkList.size());
    }

    @Test
    @SuppressWarnings("deprecation")
    void equalsTest()
    {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Assertions.assertTrue(bulkList.equals(list));
    }

    @Test
    @SuppressWarnings("deprecation")
    void hashCodeTest()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Assertions.assertTrue(bulkList.hashCode() != 0);
    }

    @Test
    void supplierTest()
    {
        BulkList.Supplier<Integer, Integer> supplier = new BulkList.Supplier<>(50);
        Assertions.assertEquals(64, supplier.getInitialCapacity());
        BulkList<Integer> apply = supplier.apply(1);
        Assertions.assertEquals(64, apply.currentCapacity());
    }

    @Test
    void collectorTest()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8);
        Aggregator<Integer, BulkList<Integer>> collector = bulkList.collector();
        collector.accept(9);
        BulkList<Integer> yield = collector.yield();
        BulkList<Integer> resultToCompare = BulkList.New(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Assertions.assertTrue(yield.equals(resultToCompare, Equalator.value()));
    }
}
