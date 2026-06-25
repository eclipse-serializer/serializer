package org.eclipse.serializer.util;

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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.eclipse.serializer.branching.ThrowBreak;
import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.collections.*;
import org.eclipse.serializer.collections.types.XList;
import org.eclipse.serializer.collections.types.XReference;
import org.eclipse.serializer.collections.types.XSet;
import org.eclipse.serializer.exceptions.ArrayCapacityException;
import org.eclipse.serializer.functional.BooleanTerm;
import org.eclipse.serializer.functional._intIndexedSupplier;
import org.eclipse.serializer.functional._intSum;
import org.eclipse.serializer.typing.KeyValue;
import org.eclipse.serializer.typing._longKeyValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.datafaker.Faker;

public class XTest
{

    private final Faker faker = new Faker();

    @Test
    void emptyTest()
    {
        Empty<Object> empty = X.empty();
        Assertions.assertTrue(empty.isEmpty());
    }

    @Test
    void emptyTableTest()
    {
        EmptyTable<Object, Object> emptyTable = X.emptyTable();
        Assertions.assertTrue(emptyTable.isEmpty());
    }

    @Test
    void checkArrayRange()
    {
        Assertions.assertThrows(ArrayCapacityException.class, () -> X.checkArrayRange(Long.MAX_VALUE));
        Assertions.assertEquals(10, X.checkArrayRange(10));
    }

    @Test
    void notNullTest()
    {
        String value = "some value";
        Assertions.assertEquals(value, X.notNull(value));
        Assertions.assertThrows(NullPointerException.class, () -> X.notNull(null));
    }

    @Test
    void mayNullTest()
    {
        Assertions.assertAll(
                () -> Assertions.assertNull(X.mayNull(null)),
                () -> Assertions.assertNotNull(X.mayNull("hello"))
        );
    }

    @Test
    void isFalseTest()
    {
        Assertions.assertAll(
                () -> Assertions.assertTrue(X.isFalse(false)),
                () -> Assertions.assertFalse(X.isFalse(true)),
                () -> Assertions.assertTrue(X.isFalse(false))
        );
    }

    @Test
    void isTrueTest()
    {
        Assertions.assertAll(
                () -> Assertions.assertTrue(X.isTrue(true)),
                () -> Assertions.assertFalse(X.isTrue(false))
        );
    }

    @Test
    void isNotFalse()
    {
        Assertions.assertAll(
                () -> Assertions.assertTrue(X.isNotFalse(true)),
                () -> Assertions.assertFalse(X.isNotFalse(false))
        );
    }

    @Test
    void isNotTrueTest()
    {
        Assertions.assertAll(
                () -> Assertions.assertTrue(X.isNotTrue(false)),
                () -> Assertions.assertFalse(X.isNotTrue(true)),
                () -> Assertions.assertTrue(X.isNotTrue(null))
        );
    }

    @Test
    void isNullTest()
    {
        Assertions.assertTrue(X.isNull(null));
        Assertions.assertFalse(X.isNull("hello"));
    }

    @Test
    void isNotNull()
    {
        Assertions.assertFalse(X.isNotNull(null));
        Assertions.assertTrue(X.isNotNull("some text"));
    }

    @Test
    void coalesceTwoParamsTest()
    {
        String value = "some value";
        Assertions.assertAll(
                () -> Assertions.assertEquals(value, X.coalesce(null, value)),
                () -> Assertions.assertEquals(value, X.coalesce(value, null)),
                () -> Assertions.assertNull(X.coalesce(null, null))
        );
    }

    @Test
    void coalesce()
    {
        Assertions.assertAll(
                () -> Assertions.assertEquals("hello", X.coalesce("hello", "ahoj", "other word")),
                () -> Assertions.assertNull(X.coalesce())
        );
    }

    @Test
    void equalTest()
    {
        Assertions.assertTrue(X.equal("ahoj", "ahoj"));
        Assertions.assertFalse(X.equal(null, "ahoj"));
        Assertions.assertFalse(X.equal("hello", null));
    }

    @Test
    void unboxByte()
    {
        Byte b = (byte) 0;
        Byte b1 = null;
        Byte b3 = (byte) 5;
        Assertions.assertAll(
                () -> Assertions.assertEquals(b, X.unbox(b1)),
                () -> Assertions.assertEquals(b3, X.unbox(b3))
        );
    }

    @Test
    void unboxByteWithSubstitute()
    {
        Byte b1 = null;
        Byte substitute = (byte) 1;
        Assertions.assertEquals(substitute, X.unbox(b1, substitute));
    }

    @Test
    void unboxByteWithSubstituteNotNull()
    {
        Byte b1 = (byte) 0;
        Byte substitute = (byte) 1;
        Assertions.assertEquals(b1, X.unbox(b1, substitute));
    }

    @Test
    void unboxBoolean()
    {

        Boolean b = null;
        Assertions.assertAll(
                () -> Assertions.assertTrue(X.unbox(true)),
                () -> Assertions.assertFalse(X.unbox(false)),
                () -> Assertions.assertFalse(X.unbox(b))
        );
    }

    @Test
    void unboxBooleanWithSubstitute()
    {
        Boolean b = null;
        Boolean substitute = true;
        Assertions.assertEquals(substitute, X.unbox(b, substitute));
    }

    @Test
    void unboxBooleanWithSubstituteNotNull()
    {
        Boolean b = false;
        Boolean substitute = true;
        Assertions.assertFalse(X.unbox(b, substitute));
    }

    @Test
    void unboxShort()
    {
        Short s = null;
        Assertions.assertEquals(0, X.unbox(s));
        s = 1;
        Assertions.assertEquals(1, X.unbox(s));
    }

    @Test
    void unboxShortSubstitute()
    {
        Short subtsitute = 100;
        Short nullShort = null;

        Assertions.assertEquals(100, X.unbox(nullShort, subtsitute));
        Short notNullShort = 20;
        Assertions.assertEquals(20, X.unbox(notNullShort, subtsitute));
    }

    @Test
    void unboxCharNull()
    {
        Character nullCharacter = null;
        Character character = 'c';
        Assertions.assertAll(
                () -> Assertions.assertEquals(0, X.unbox(nullCharacter)),
                () -> Assertions.assertEquals(character, X.unbox(character))
        );
    }

    @Test
    void unboxCharacterNullSubstitude()
    {
        Character nullSubstituteCharacter = 50;
        Character nullCharacter = null;
        Assertions.assertEquals(50, X.unbox(nullCharacter, nullSubstituteCharacter));
        Character valueCharacter = 45;
        Assertions.assertEquals(45, X.unbox(valueCharacter, nullSubstituteCharacter));
    }

    @Test
    void unboxIntegerSubstitute()
    {
        Integer nullInteger = null;
        Assertions.assertEquals(100, X.unbox(nullInteger, 100));
        Integer valueInteger = 40;
        Assertions.assertEquals(40, X.unbox(valueInteger, 100));
    }

    @Test
    void unboxFloatNull()
    {
        Float nullFloat = null;
        Assertions.assertEquals(0f, X.unbox(nullFloat));
        Float valueFloat = 50f;
        Assertions.assertEquals(50f, X.unbox(valueFloat));
    }

    @Test
    void unboxFloatWithNullSubstitute()
    {
        Float nullSubstituteFloat = 50f;
        Float nullFloat = null;
        Assertions.assertEquals(50f, X.unbox(nullFloat, nullSubstituteFloat));
        Float valueFloat = 35f;
        Assertions.assertEquals(35f, X.unbox(valueFloat, nullSubstituteFloat));
    }

    @Test
    void longUnbox()
    {
        Long l = null;
        Assertions.assertEquals(0L, X.unbox(l));
        Long valueLong = 50L;
        Assertions.assertEquals(50L, X.unbox(valueLong));
    }

    @Test
    void unboxLongWithNullSubstitute()
    {
        Long nullSubstitute = 50L;
        Long nullLong = null;
        Assertions.assertEquals(50l, X.unbox(nullLong, nullSubstitute));
        Long valueLong = 30L;
        Assertions.assertEquals(30L, X.unbox(valueLong, nullSubstitute));
    }

    @Test
    void unboxDouble()
    {
        Double nullDouble = null;
        Assertions.assertEquals(0.0D, X.unbox(nullDouble));
        Double valueDouble = 5.0D;
        Assertions.assertEquals(5.0D, X.unbox(valueDouble));
    }

    @Test
    void UnboxDoubleNullSubstitute()
    {
        Double nullDouble = null;
        Double nullSubstituteDouble = 100.0D;
        Assertions.assertEquals(nullSubstituteDouble, X.unbox(nullDouble, nullSubstituteDouble));
        Double valueDouble = 50.0D;
        Assertions.assertEquals(valueDouble, X.unbox(valueDouble, nullSubstituteDouble));
    }

    @Test
    void unboxInteger()
    {
        Integer nullInteger = null;
        Assertions.assertEquals(0, X.unbox(nullInteger));
        Integer value = 10;
        Assertions.assertEquals(value, X.unbox(value));
    }

    @Test
    void boxBytes()
    {
        Byte[] bytes = {1, 2, 3};
        byte b1 = 1;
        byte b2 = 2;
        byte b3 = 3;
        Assertions.assertArrayEquals(bytes, X.box(b1, b2, b3));

        byte[] nullBytes = null;
        Assertions.assertNull(X.box(nullBytes));

    }

    @Test
    void boxBoolean()
    {
        Boolean[] booleans = {true, false, true};
        boolean b1 = true;
        boolean b2 = false;
        boolean b3 = true;
        Assertions.assertArrayEquals(booleans, X.box(b1, b2, b3));

        boolean[] nullBooleans = null;
        Assertions.assertNull(X.box(nullBooleans));
    }

    @Test
    void boxShorts()
    {
        Short[] shorts = {1, 3, 4};
        short s1 = 1;
        short s2 = 3;
        short s3 = 4;
        Assertions.assertArrayEquals(shorts, X.box(s1, s2, s3));

        short[] nullShorts = null;
        Assertions.assertNull(X.box(nullShorts));
    }

    @Test
    void boxInteger()
    {
        Integer[] integers = {1, 3, 5};
        int i1 = 1;
        int i2 = 3;
        int i3 = 5;
        Assertions.assertArrayEquals(integers, X.box(i1, i2, i3));

        int[] ints = null;
        Assertions.assertNull(X.box(ints));
    }

    @Test
    void boxCharacter()
    {
        Character[] characters = {1, 'c', '%'};
        char c1 = 1;
        char c2 = 'c';
        char c3 = '%';
        Assertions.assertArrayEquals(characters, X.box(c1, c2, c3));

        char[] chars = null;
        Assertions.assertNull(X.box(chars));
    }

    @Test
    void boxFloat()
    {
        Float[] floats = {1f, 3f, 4.3f};
        float f1 = 1f;
        float f2 = 3f;
        float f3 = 4.3f;
        Assertions.assertArrayEquals(floats, X.box(f1, f2, f3));

        float[] nullFloats = null;
        Assertions.assertNull(X.box(nullFloats));
    }

    @Test
    void longBox()
    {
        Long[] longs = {4L, 34L, 43L};
        long l1 = 4L;
        long l2 = 34L;
        long l3 = 43L;
        Assertions.assertArrayEquals(longs, X.box(l1, l2, l3));

        long[] nullLongs = null;
        Assertions.assertNull(X.box(nullLongs));
    }

    @Test
    void doubleBox()
    {
        Double[] doubles = {1.0D, 2.3423D, 432.43234D};
        double d1 = 1.0D;
        double d2 = 2.3423D;
        double d3 = 432.43234D;
        Assertions.assertArrayEquals(doubles, X.box(d1, d2, d3));

        double[] nullDoubles = null;
        Assertions.assertNull(X.box(nullDoubles));
    }

    @Test
    void unboxIntegerArray()
    {
        Integer[] integers = {1, 3, 4};
        int[] ints = {1, 3, 4};
        Assertions.assertArrayEquals(ints, X.unbox(integers));
    }

    @Test
    void unboxIntegerArrayNull()
    {
        Integer[] integers = null;
        int[] ints = {0};
        Assertions.assertNull(X.unbox(integers));
    }

    @Test
    void unboxIntegerArrayNullReplacement()
    {
        Integer[] integers = {null};
        int[] ints = {0};
        Assertions.assertArrayEquals(ints, X.unbox(integers, 0));
    }

    @Test
    void unboxLongArray()
    {
        Long[] longs = {1L, 3L, 4L};
        long[] longs1 = {1, 3, 4};
        Assertions.assertArrayEquals(longs1, X.unbox(longs));
    }

    @Test
    void unboxLongArrayNull()
    {
        Long[] longs = null;
        long[] longs1 = {0};
        Assertions.assertNull(X.unbox(longs));
    }

    @Test
    void unboxLongArrayNullReplacement()
    {
        Long[] longs = {null};
        long[] longs1 = {0};
        Assertions.assertArrayEquals(longs1, X.unbox(longs, 0));
    }

    @Test
    void unboxXGettingCollection()
    {
        BulkList<Integer> list = BulkList.New();
        list.add(1);
        list.add(3);
        list.add(5);
        int[] ints = {1, 3, 5};
        Assertions.assertArrayEquals(ints, X.unbox(list));
    }

    @Test
    void unboxXGettingCollectionNull()
    {
        BulkList<Integer> list = null;
        Assertions.assertNull(X.unbox(list));
    }

    @Test
    void booleansArray()
    {
        boolean[] booleans = {true, false, true, false};
        Assertions.assertArrayEquals(booleans, X.booleans(true, false, true, false));
    }

    @Test
    void bytesTest()
    {
        byte[] bytes = {1, 3, 5};
        byte b1 = 1;
        byte b2 = 3;
        byte b3 = 5;
        Assertions.assertArrayEquals(bytes, X.bytes(b1, b2, b3));
    }

    @Test
    void toBytesTest()
    {
        byte[] result = {0, 0, 1, 2};
        Assertions.assertArrayEquals(result, X.toBytes(258));
    }

    @Test
    void toBytesLongTest()
    {
        byte[] result = {0, 0, 0, 0, 0, 0, 1, 2};
        Assertions.assertArrayEquals(result, X.toBytes(258L));
    }

    @Test
    void shortsTest()
    {
        short[] shorts = {0, 5, 3};
        short s1 = 0;
        short s2 = 5;
        short s3 = 3;
        Assertions.assertArrayEquals(shorts, X.shorts(s1, s2, s3));
    }

    @Test
    void intsTest()
    {
        int[] ints = {1, 3, 5};
        int i1 = 1;
        int i2 = 3;
        int i3 = 5;
        Assertions.assertArrayEquals(ints, X.ints(i1, i2, i3));
    }

    @Test
    void longsTests()
    {
        long[] longs = {1L, 3L, 4L};
        long l1 = 1L;
        long l2 = 3L;
        long l3 = 4L;
        Assertions.assertArrayEquals(longs, X.longs(l1, l2, l3));
    }

    @Test
    void floatsTest()
    {
        float[] floats = {1.0f, 3.0f, 5.0f};
        float f1 = 1.0f;
        float f2 = 3.0f;
        float f3 = 5.0f;
        Assertions.assertArrayEquals(floats, X.floats(f1, f2, f3));
    }

    @Test
    void doublesTest()
    {
        double[] doubles = {1.0D, 3.0D, 5.0D};
        double d1 = 1.0D;
        double d2 = 3.0D;
        double d3 = 5.0D;
        Assertions.assertArrayEquals(doubles, X.doubles(d1, d2, d3));
    }

    @Test
    void charsTest()
    {
        char[] chars = {'a', 'b', 'c'};
        Assertions.assertArrayEquals(chars, X.chars('a', 'b', 'c'));
    }

    @Test
    void arrayTest()
    {
        Assertions.assertArrayEquals(new Integer[]{1, 2, 3}, X.array(1, 2, 3));
    }

    @Test
    void objectsTests()
    {
        Object o1 = new Object();
        Object o2 = new Object();
        Object o3 = new Object();
        Assertions.assertArrayEquals(new Object[]{o1, o2, o3}, X.objects(o1, o2, o3));
    }

    @Test
    void stringsTest()
    {
        String s1 = "Hello";
        String s2 = "MicroStream";
        String s3 = "Super Fast";
        Assertions.assertArrayEquals(new String[]{s1, s2, s3}, X.strings(s1, s2, s3));
    }

    @Test
    void timesTest()
    {
        BulkList<Integer> integers = BulkList.New(1, 2, 3, 4, 5);
        Assertions.assertIterableEquals(integers, X.times(5));
    }

    @Test
    void rangeTest()
    {
        BulkList<Integer> integers = BulkList.New(3, 4, 5);
        Assertions.assertIterableEquals(integers, X.range(3, 5));
    }

    @Test
    void rangeRevertTest()
    {
        BulkList<Integer> integers = BulkList.New(5, 4, 3);
        Assertions.assertIterableEquals(integers, X.range(5, 3));
    }

    @Test
    void listTest()
    {
        BulkList<Integer> list = BulkList.New(1, 2, 3);
        XList<Integer> list1 = X.List(list);
        Assertions.assertIterableEquals(list, list1);
    }

    @Test
    void listTestNullParam()
    {
        Integer[] integers = {1, 2, 3};
        XList<Integer> list = X.List(integers);
        Assertions.assertEquals(3, list.size());
    }

    @Test
    void listEmptyTest()
    {
        BulkList<Integer> list = BulkList.New();
        Integer[] integers = null;

        XList<Integer> list1 = X.List(integers);
        Assertions.assertIterableEquals(list, list1);
    }

    @Test
    void constListTest()
    {
        Integer[] integers = new Integer[]{5, 8, 9, 6};
        ConstList<Integer> list = ConstList.New(5, 8, 9, 6);
        ConstList<Integer> integers1 = X.ConstList(integers);
        Assertions.assertIterableEquals(list, integers1);
    }

    @Test
    void arrayView()
    {
        ArrayView<Integer> integers = new ArrayView<>(1, 3, 5, 7, 9);
        Assertions.assertIterableEquals(integers, X.ArrayView(1, 3, 5, 7, 9));
    }

    @Test
    void arrayViewEmptyTest()
    {
        Integer[] nullIntegers = null;
        ArrayView<Integer> integers = X.ArrayView(nullIntegers);
        Assertions.assertNotNull(integers);
        Assertions.assertTrue(integers.isEmpty());
    }

    @Test
    void singletonTest()
    {
        Singleton<Integer> singleton = X.Singleton(5);
        Assertions.assertNotNull(singleton);
    }

    @Test
    void constantTest()
    {
        Assertions.assertNotNull(X.Constant(3));
    }

    @Test
    void enumTest()
    {
        HashEnum<Integer> anEnum = X.Enum(1, 2, 3, 4, 8);
        Assertions.assertEquals(5, anEnum.size());
    }

    @Test
    void enumEmptyTest()
    {
        Integer[] integers = null;
        HashEnum<Integer> anEnum = X.Enum(integers);
        Assertions.assertTrue(anEnum.isEmpty());
    }

    @Test
    void enumIterableTest()
    {
        List<Integer> list = List.of(1, 2, 3, 4, 8);
        HashEnum<Integer> anEnum = X.Enum(list);
        Assertions.assertEquals(5, anEnum.size());
    }

    @Test
    void constHashEnumTest()
    {
        List<Integer> list = List.of(1, 2, 3, 4, 8);
        ConstHashEnum<Integer> integers = X.ConstEnum(1, 2, 3, 4, 8);
        Assertions.assertIterableEquals(list, integers);
    }

    @Test
    void constHashEnumEmptyTest()
    {
        Integer[] ints = null;
        ConstHashEnum<Integer> integers = X.ConstEnum(ints);
        Assertions.assertTrue(integers.isEmpty());
    }

    @Test
    void tableKVTest()
    {
        HashTable<Integer, String> hello = X.Table(10, "hello");
        Assertions.assertEquals("hello", hello.get(10));
    }

    @Test
    void tableKeyValuesTest()
    {
        KeyValue<Integer, String> value1 = KeyValue.New(1, "first");
        KeyValue<Integer, String> value2 = KeyValue.New(2, "second");
        KeyValue<Integer, String> value3 = KeyValue.New(2, "third");
        HashTable<Integer, String> table = X.Table(value1, value2, value3);
        Assertions.assertEquals("first", table.get(1));
    }

    @Test
    void tableKeyValueEmptyVarArgs()
    {
        KeyValue<Integer, String>[] keyValues = null;
        HashTable<Integer, String> table = X.Table(keyValues);
        Assertions.assertTrue(table.isEmpty());
    }

    @Test
    void iterableTest()
    {
        Integer[] integers = {1, 2, 3, 4, 5, 6, 8};
        Iterable<?> iterable = X.Iterable(integers);
        int count = 0;
        for (Object o : iterable) {
            count++;
        }
        Assertions.assertEquals(7, count);
    }

    @Test
    void arrayForElement()
    {
        Integer[] array = X.Array(3);
        Assertions.assertArrayEquals(new Integer[]{3}, array);
    }

    @Test
    void arrayForElementTypeTest()
    {
        Integer i = 1;
        Integer[] array = X.ArrayForElementType(i, 50);
        Assertions.assertEquals(50, array.length);
    }

    @Test
    void arrayOfSameTypeLenghtTest()
    {
        Integer[] integers = {1, 2, 3, 4};
        Integer[] integers1 = X.ArrayOfSameType(integers, 50);
        Assertions.assertEquals(50, integers1.length);
    }

    @Test
    void arrayOfSameTypeTest()
    {
        Integer[] integers = {1, 2, 3, 4};
        Integer[] integers1 = X.ArrayOfSameType(integers);
        Assertions.assertEquals(integers.length, integers1.length);
    }

    @Test
    void arrayComponentTypeWithElementTest()
    {
        String[] orig = {"hi"};
        String s = "hi";
        String[] array = X.Array(String.class, s);
        Assertions.assertArrayEquals(orig, array);
    }

    @Test
    void arrayClassAndMicrostreamCollectionTest()
    {
        String[] arrayStrings = {"hi", "this", "is", "a", "MicroStream"};
        BulkList<String> strings = BulkList.New(arrayStrings);
        String[] array = X.Array(String.class, strings);
        Assertions.assertArrayEquals(arrayStrings, array);
    }

    @Test
    void arrayTypeLengthSupplierTest()
    {
        Supplier<Integer> s = () -> faker.number()
                .randomDigitNotZero();
        Integer[] array = X.Array(Integer.class, 50, s);
        Assertions.assertEquals(50, array.length);
    }

    @Test
    void arrayTypeLength_intIndexedSupplierTest()
    {
        _intIndexedSupplier<Integer> supplier = (i) -> i;
        Integer[] array = X.Array(Integer.class, 50, supplier);
        Assertions.assertEquals(50, array.length);
    }

    @Test
    void weakReferenceTest()
    {
        Integer i = 40;
        WeakReference<Integer> integerWeakReference = X.WeakReference(i);
        Assertions.assertNotNull(integerWeakReference);
    }

    @Test
    void weakReferencesTest()
    {
        WeakReference<Object>[] weakReferences = X.WeakReferences(50);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(weakReferences),
                () -> Assertions.assertEquals(50, weakReferences.length)
        );
    }

    @Test
    void weakReferencesFromReferentsTest()
    {
        Integer[] integers = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        WeakReference<Integer>[] weakReferences = X.WeakReferences(integers);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(weakReferences),
                () -> Assertions.assertEquals(9, weakReferences.length)
        );
    }

    @Test
    void weakReferencesFromNulllReferentsTest()
    {
        Integer[] integers = null;
        WeakReference<Integer>[] weakReferences = X.WeakReferences(integers);
        Assertions.assertNull(weakReferences);
    }

    @Test
    void consolidateWeakReferencesTest()
    {
        Integer[] integers = {1, 2, 3, 4, 5, 6, 7, null, 8, 9, null, null};
        WeakReference[] references = X.WeakReferences(integers);
        references[10] = null;
        WeakReference[] weakReferences = X.consolidateWeakReferences(references);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(weakReferences),
                () -> Assertions.assertEquals(9, weakReferences.length)
        );
    }

    @Test
    void consolidateWeakReferencesNoOpTest()
    {
        Integer[] integers = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        WeakReference[] references = X.WeakReferences(integers);
        WeakReference[] weakReferences = X.consolidateWeakReferences(references);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(weakReferences),
                () -> Assertions.assertEquals(9, weakReferences.length)
        );
    }

    @Test
    void hashNoContent()
    {
        Integer[] integers = {1, 2, 3, 4};
        ConstList<Integer> list = ConstList.New(integers);
        ConstList<Integer> emptyList = ConstList.New();
        Assertions.assertAll(
                () -> Assertions.assertFalse(X.hasNoContent(list)),
                () -> Assertions.assertTrue(X.hasNoContent(emptyList))
        );
    }

    @Test
    void notEmpty()
    {
        Integer[] integers = {1, 2, 3, 4};
        ConstList<Integer> list = ConstList.New(integers);
        ConstList<Integer> list1 = X.notEmpty(list);
        Assertions.assertIterableEquals(list, list1);
    }

    @Test
    void notEmptyThrow()
    {
        ConstList<Integer> list = ConstList.New();
        Assertions.assertThrows(IllegalArgumentException.class, () -> X.notEmpty(list));
    }

    @Test
    void notEmpty_EmptyArray()
    {
        Integer[] integers = {};
        Assertions.assertThrows(IllegalArgumentException.class, () -> X.notEmpty(integers));
    }

    @Test
    void notEmptyArray()
    {
        Integer[] integers = {1, 2, 3, 4};
        Integer[] notEmpty = X.notEmpty(integers);
        Assertions.assertArrayEquals(integers, notEmpty);
    }

    @Test
    void toKeyValue()
    {
        Integer i = 20;
        Function<Integer, KeyValue<Integer, String>> mapper = new Function<Integer, KeyValue<Integer, String>>()
        {
            @Override
            public KeyValue<Integer, String> apply(Integer integer)
            {
                return new KeyValue<Integer, String>()
                {
                    @Override
                    public Integer key()
                    {
                        return integer;
                    }

                    @Override
                    public String value()
                    {
                        return String.valueOf(integer + 10);
                    }
                };
            }

            ;
        };

        KeyValue<Integer, String> integerStringKeyValue = X.toKeyValue(i, mapper);
        Assertions.assertAll(
                () -> Assertions.assertEquals("30", integerStringKeyValue.value()),
                () -> Assertions.assertEquals(20, integerStringKeyValue.key())
        );
    }

    @Test
    void _longKeyValueTest()
    {
        _longKeyValue longKeyValue = X._longKeyValue(10L, 40L);
        Assertions.assertAll(
                () -> Assertions.assertEquals(10L, longKeyValue.key()),
                () -> Assertions.assertEquals(40L, longKeyValue.value())
        );
    }

    @Test
    void toStringCollection()
    {
        ConstList<Integer> list = ConstList.New(1, 2, 3, 4, 5);
        String expectedOutput = "[1, 2, 3, 4, 5]";
        Assertions.assertEquals(expectedOutput, X.toString(list));
    }

    @Test
    void assembleStringEmptyCollection()
    {
        VarString varString = VarString.New("X");
        VarString expectedString = VarString.New("X[]");
        Assertions.assertEquals(expectedString.toString(), (X.assembleString(varString, ConstList.New())).toString());
    }

    @Test
    void synchronize()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5);
        SynchList<Integer> synchronizeList = (SynchList<Integer>) X.synchronize(bulkList);
        Assertions.assertIterableEquals(bulkList, synchronizeList);
    }

    @Test
    void synchronizeAlreadySynchronized()
    {
        BulkList<Integer> bulkList = BulkList.New(1, 2, 3, 4, 5);
        SynchList<Integer> synchronizeList = (SynchList<Integer>) X.synchronize(bulkList);
        XList<Integer> synchList2 = X.synchronize(synchronizeList);
        Assertions.assertIterableEquals(synchronizeList, synchList2);
    }

    @Test
    void synchronizeSet()
    {
        String s = "some value";
        HashTable<Integer, String> table = HashTable.New();
        table.put(100, s);
        XSet<KeyValue<Integer, String>> synchronize = X.synchronize(table);
        XSet<KeyValue<Integer, String>> synchronize1 = X.synchronize(synchronize);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(synchronize),
                () -> Assertions.assertIterableEquals(synchronize1, synchronize)
        );
    }

    @Test
    void toArray_XGetttingCollection()
    {
        BulkList<Integer> integers = BulkList.New(1, 2, 3, 4, 5, 6);
        Integer[] expectedIntegers = {1, 2, 3, 4, 5, 6};
        Assertions.assertArrayEquals(expectedIntegers, X.toArray(integers, Integer.class));
    }

    @Test
    void toArray_ArrayList()
    {
        List<Integer> integers = List.of(1, 2, 3, 4, 5, 6);
        Integer[] expectedIntegers = {1, 2, 3, 4, 5, 6};
        Assertions.assertArrayEquals(expectedIntegers, X.toArray(integers, Integer.class));
    }

    @Test
    void toArray_iterable()
    {
        Iterable<Integer> integers = range(1, 7);
        Integer[] expectedIntegers = {1, 2, 3, 4, 5, 6};
        Assertions.assertArrayEquals(expectedIntegers, X.toArray(integers, Integer.class));


    }

    static Iterable<Integer> range(final int from, final int to)
    {
        return new Iterable<Integer>()
        {
            public Iterator<Integer> iterator()
            {
                return new Iterator<Integer>()
                {
                    int current = from;

                    public boolean hasNext()
                    {
                        return current < to;
                    }

                    public Integer next()
                    {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        return current++;
                    }

                    public void remove()
                    { /*Optional; not implemented.*/ }
                };
            }
        };
    }

    @Test
    void addSuppressed()
    {
        IllegalStateException illegalStateException = new IllegalStateException();
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException();
        Exception ex = X.addSuppressed(illegalStateException, illegalArgumentException);
        Assertions.assertTrue(ex instanceof IllegalStateException);
        Throwable throwable = ex.getSuppressed()[0];
        Assertions.assertEquals(IllegalArgumentException.class.getName(), throwable.toString());
    }

    @Test
    void addSuppressedMore()
    {
        IllegalStateException illegalStateException = new IllegalStateException();
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException();
        Exception ex = X.addSuppressed(illegalStateException, illegalArgumentException, new NullPointerException());
        Assertions.assertTrue(ex instanceof IllegalStateException);
        Throwable throwable = ex.getSuppressed()[0];
        Assertions.assertEquals(IllegalArgumentException.class.getName(), throwable.toString());
        Throwable throwable2 = ex.getSuppressed()[1];
        Assertions.assertEquals(NullPointerException.class.getName(), throwable2.toString());
    }

    @Test
    void asUnchecked()
    {
        Exception runtimeException = X.asUnchecked(new IOException());
        Exception runtimeException2 = X.asUnchecked(new ArrayIndexOutOfBoundsException());
        Assertions.assertAll(
                () -> Assertions.assertTrue(runtimeException instanceof RuntimeException),
                () -> Assertions.assertTrue(runtimeException2 instanceof RuntimeException)
        );
    }

    @Test
    void onTest()
    {
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        Integer integer = 1;
        Consumer<Integer> consumer = (Integer i) -> atomicInteger.addAndGet(i);
        Integer on = X.on(integer, consumer);
        Assertions.assertEquals(1, atomicInteger.get());
    }

    @Test
    void checkSimpleTest()
    {
        BooleanTerm booleanTerm = () -> true;
        X.check(booleanTerm);
        BooleanTerm booleanTermFalse = () -> false;
        Assertions.assertThrows(Error.class, () -> X.check(booleanTermFalse));
    }

    @Test
    void checkMessageTest()
    {
        BooleanTerm booleanTerm = () -> true;
        X.check(booleanTerm, "everything ok");
        BooleanTerm booleanTermFalse = () -> false;
        Assertions.assertThrows(Error.class, () -> X.check(booleanTermFalse, "everything bad"), "Check failed: everything bad");
    }

    @Test
    void checkWithStatckLevel()
    {
        BooleanTerm booleanTerm = () -> true;
        X.check(booleanTerm, "everything ok", 2);
        BooleanTerm booleanTermFalse = () -> false;
        Assertions.assertThrows(Error.class, () -> X.check(booleanTermFalse, "everything bad", 2), "Check failed: everything bad");
    }

    @Test
    void validateTest()
    {
        Predicate<Integer> predicate = (i) -> (i < 5);
        Assertions.assertAll(
                () -> Assertions.assertEquals(4, X.validate(4, predicate)),
                () -> Assertions.assertThrows(IllegalArgumentException.class, () -> X.validate(10, predicate))
        );
    }

    @Test
    void repeat_intProcedureTest()
    {
        _intSum intSum = new _intSum(20);
        _intSum repeat = X.repeat(40, intSum);
        Assertions.assertEquals(800, repeat.yield());
    }

    @Test
    void repeatAmountTest()
    {
        List<Long> longs = new ArrayList<>();

        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                longs.add(System.currentTimeMillis());
            }
        };

        X.repeat(100, runnable);

        Assertions.assertEquals(100, longs.size());
    }

    @Test
    void validateIndexTest()
    {
        final long availableLength = 5;
        Assertions.assertAll(
                () -> Assertions.assertEquals(3, X.validateIndex(availableLength, 3)),
                () -> Assertions.assertThrows(IndexOutOfBoundsException.class, () -> X.validateIndex(availableLength, 15555555L)),
                () -> Assertions.assertThrows(IndexOutOfBoundsException.class, () -> X.validateIndex(availableLength, -5L))
        );
    }

    @Test
    void validateRangeTest()
    {
        Assertions.assertAll(
                () -> Assertions.assertEquals(7, X.validateRange(10, 2, 5)),
                () -> Assertions.assertThrows(IndexOutOfBoundsException.class, () -> X.validateRange(10, 2, 9)),
                () -> Assertions.assertThrows(IndexOutOfBoundsException.class, () -> X.validateRange(10, -2, 9)),
                () -> Assertions.assertThrows(IndexOutOfBoundsException.class, () -> X.validateRange(10, 15, 9)),
                () -> Assertions.assertThrows(IndexOutOfBoundsException.class, () -> X.validateRange(10, 5, -5))
        );
    }

    @Test
    void breakTest()
    {
        ThrowBreak aBreak = X.BREAK();
        Assertions.assertNotNull(aBreak);
    }

    @Test
    void referenceTest()
    {
        Integer i = 20;
        XReference<Integer> reference = X.Reference(i);
        Assertions.assertNotNull(reference);
    }
}
