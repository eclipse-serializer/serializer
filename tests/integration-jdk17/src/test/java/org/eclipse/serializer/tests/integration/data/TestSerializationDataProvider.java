package org.eclipse.serializer.tests.integration.data;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/*-
 * #%L
 * Eclipse Serializer Test on JDK 17
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import org.assertj.core.api.Assertions;
import org.eclipse.serializer.tests.model.Address;
import org.eclipse.serializer.tests.model.Employee;
import org.eclipse.serializer.tests.model.Item;
import org.eclipse.serializer.tests.model.Person;
import org.eclipse.serializer.tests.model.Season;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class TestSerializationDataProvider implements ArgumentsProvider
{
    private static final long MOMENT_IN_TIME = 1673619718205L; // For date and time testing we need a fix moment

    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) throws Exception
    {
        final CompareInstances defaultCompare = (i1, i2) -> Assertions.assertThat(i1)
                .isEqualTo(i2);
        final CompareInstances toStringCompare = (i1, i2) -> Assertions.assertThat(i1.toString())
                .isEqualTo(i2.toString());
        final CompareInstances arrayDequeCompare = (i1, i2) -> Assertions.assertThat(new ArrayList<>((Collection) i1))
                .isEqualTo(new ArrayList<>((Collection) i2));
        final CompareInstances identityHashMapCompare = (i1, i2) -> Assertions.assertThat(new HashMap<>((Map) i1))
                .isEqualTo(new HashMap<>((Map) i2));
        return Stream.of(
                Arguments.of(new TestSerializationData(123, "Integer.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(-876, "IntegerNegative.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData("MicroStream", "String.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData("ܐܬܘܪܝܐ მარგალური", "StringUnicode.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(456789L, "Long.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(-4321987L, "LongNegative.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(12345.678, "Double.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(-12345.678, "DoubleNegative.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(Boolean.TRUE, "Boolean.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData('X', "Character.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(Byte.parseByte("12"), "Byte.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(Byte.parseByte("-78"), "ByteNegative.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(Short.parseShort("12"), "Short.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(Short.parseShort("-78"), "ShortNegative.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(new String[]{"item1", "item2"}, "StringArray.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(new Object[]{"item1", 123, 7654.321}, "MixedArray.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(12345.678F, "Float.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(-12345.678F, "FloatNegative.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(this.newStringBuilder(), "StringBuilder.test.txt", toStringCompare))
                , Arguments.of(new TestSerializationData(this.newStringBuffer(), "StringBuffer.test.txt", toStringCompare))
                , Arguments.of(new TestSerializationData(BigInteger.valueOf(Integer.MAX_VALUE)
                                                                 .add(BigInteger.valueOf(12345)), "BigInteger.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(BigDecimal.valueOf(15)
                                                                 .pow(24)
                                                                 .divide(BigDecimal.valueOf(3)), "BigDecimal.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, 0, 102}), "InetAddress4.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(InetAddress.getByName("1080:0:0:0:8:800:200C:417A"), "InetAddress6.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(new URL("https://www.eclipse.org/"), "URL.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(URI.create("https://www.eclipse.org/"), "URI.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(new File("."), "File.test.txt", defaultCompare))
                //, Arguments.of(new TestSerializationData(Path.of(".").toAbsolutePath(), "Path.test.txt", defaultCompare))  // Can't do this as it is location dependent
                , Arguments.of(new TestSerializationData(new Date(MOMENT_IN_TIME), "Date.test.txt", defaultCompare))
                //, Arguments.of(new TestSerializationData(defineLocalDateTime(), "LocalDateTime.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(new java.sql.Date(MOMENT_IN_TIME), "SqlDate.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(new java.sql.Time(MOMENT_IN_TIME), "SqlTime.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(new java.sql.Timestamp(MOMENT_IN_TIME), "SqlTimeStamp.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(ZoneOffset.of("+01:00"), "ZoneOffset.test.txt", defaultCompare))
//                , Arguments.of(new TestSerializationData(defineZonedDateTime(), "ZonedDateTime.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(Currency.getInstance("EUR"), "currency.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(Locale.GERMANY, "Locale.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(Optional.empty(), "OptionalEmpty.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(Optional.of(123), "OptionalInteger.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(Optional.of(7654L), "OptionalLong.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(Optional.of(1234.5678), "OptionalDouble.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(Optional.of("Real value"), "OptionalString.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(this.testArrayList(), "ArrayList.test.txt", defaultCompare))
                //, Arguments.of(new TestSerializationData(testHashMap(), "HashMap.test.txt", defaultCompare))
                //, Arguments.of(new TestSerializationData(testHashSet(), "HashSet.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(this.testArrayDeque(), "ArrayDeque.test.txt", arrayDequeCompare))
                , Arguments.of(new TestSerializationData(this.testCopyOnWriteArrayList(), "CopyOnWriteArrayList.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(this.testCopyOnWriteArraySet(), "CopyOnWriteArraySet.test.txt", defaultCompare))
                //, Arguments.of(new TestSerializationData(testHashtable(), "Hashtable.test.txt", defaultCompare))
                //, Arguments.of(new TestSerializationData(testIdentityHashMap(), "IdentityHashMap.test.txt", identityHashMapCompare))
                //, Arguments.of(new TestSerializationData(testLinkedHashMap(), "LinkedHashMap.test.txt", defaultCompare))
                //, Arguments.of(new TestSerializationData(testLinkedHashSet(), "LinkedHashSet.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(this.testLinkedList(), "LinkedList.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(this.testPriorityQueue(), "PriorityQueue.test.txt", arrayDequeCompare))
                , Arguments.of(new TestSerializationData(this.testStack(), "Stack.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(this.testTreeMap(), "TreeMap.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(this.testTreeSet(), "TreeSet.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(this.testVector(), "Vector.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(Pattern.compile("^.?$|^(..+?)\\1+$"), "Pattern.test.txt", toStringCompare))
                //, Arguments.of(new TestSerializationData(testConcurrentHashMap(), "ConcurrentHashMap.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(this.testConcurrentLinkedDeque(), "ConcurrentLinkedDequeue.test.txt", toStringCompare))
                , Arguments.of(new TestSerializationData(this.testConcurrentLinkedQueue(), "ConcurrentLinkedQueue.test.txt", toStringCompare))
                , Arguments.of(new TestSerializationData(this.testConcurrentSkipListMap(), "ConcurrentSkipListMap.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(this.testConcurrentSkipListSet(), "ConcurrentSkipListSet.test.txt", defaultCompare))
                //, Arguments.of(new TestSerializationData(testProperties(), "Properties.test.txt", defaultCompare))
//                , Arguments.of(new TestSerializationData(testItem(), "Enum.test.txt", defaultCompare))
//                , Arguments.of(new TestSerializationData(createCircular(), "circular.test.txt", defaultCompare))
//                , Arguments.of(new TestSerializationData(createObjectGraph(), "objectGraph.test.txt", defaultCompare))
//                , Arguments.of(new TestSerializationData(testAddress(), "pojo.test.txt", defaultCompare))
        );
    }

    private Item testItem()
    {
        final Item result = new Item();
        result.setName("Coat");
        result.setSeason(Season.WINTER);
        return result;

    }

    private Properties testProperties()
    {
        final Properties result = new Properties();
        result.setProperty("key1", "value1");
        result.setProperty("key2", "value3");
        result.setProperty("key3", "value3");
        return result;
    }

    private Person createObjectGraph()
    {
        return new Person(123L, "John Doe", 42, this.testAddress());
    }

    private ConcurrentSkipListSet<String> testConcurrentSkipListSet()
    {
        final ConcurrentSkipListSet<String> result = new ConcurrentSkipListSet<>();
        result.add("Item1");
        result.add("Item2");
        result.add("Item3");
        return result;

    }


    private ConcurrentSkipListMap<String, String> testConcurrentSkipListMap()
    {
        final ConcurrentSkipListMap<String, String> result = new ConcurrentSkipListMap<>();
        result.put("key1", "value1");
        result.put("key2", "value2");
        result.put("key3", "value3");

        return result;
    }

    private ConcurrentLinkedQueue<String> testConcurrentLinkedQueue()
    {
        final ConcurrentLinkedQueue<String> result = new ConcurrentLinkedQueue<>();
        result.add("Item1");
        result.add("Item2");
        result.add("Item3");
        return result;
    }

    private ConcurrentLinkedDeque<String> testConcurrentLinkedDeque()
    {
        final ConcurrentLinkedDeque<String> result = new ConcurrentLinkedDeque<>();
        result.add("Item1");
        result.add("Item2");
        result.add("Item3");

        return result;
    }

    private ConcurrentHashMap<String, String> testConcurrentHashMap()
    {
        final ConcurrentHashMap<String, String> result = new ConcurrentHashMap();
        result.put("key1", "value1");
        result.put("key2", "value2");
        result.put("key3", "value3");

        return result;
    }

    private Vector<String> testVector()
    {
        final Vector<String> result = new Vector<>();
        result.add("Item1");
        result.add("Item2");
        result.add("Item3");

        return result;

    }

    private TreeSet<String> testTreeSet()
    {
        return new TreeSet<>(Set.of("Item1", "Item2", "Item3"));
    }

    private TreeMap<String, String> testTreeMap()
    {
        final TreeMap<String, String> result = new TreeMap<>();
        result.put("key1", "value1");
        result.put("key2", "value2");
        result.put("key3", "value3");
        return result;

    }

    private Stack<String> testStack()
    {
        final Stack<String> result = new Stack<>();
        result.add("Item1");
        result.add("Item2");
        result.add("Item3");

        return result;
    }

    private PriorityQueue<String> testPriorityQueue()
    {
        final PriorityQueue<String> result = new PriorityQueue();
        result.add("Item1");
        result.add("Item2");
        result.add("Item3");
        return result;
    }


    private LinkedList<String> testLinkedList()
    {
        return new LinkedList<>(List.of("Item1", "Item2", "Item3"));
    }

    private LinkedHashSet<String> testLinkedHashSet()
    {
        final LinkedHashSet<String> result = new LinkedHashSet<>();
        // Not using the constructor with collection as that not seems to guarantee the same order.
        result.add("Item1");
        result.add("Item2");
        result.add("Item3");
        return result;
    }

    private LinkedHashMap<String, String> testLinkedHashMap()
    {
        final LinkedHashMap<String, String> result = new LinkedHashMap<>();
        result.put("key1", "value1");
        result.put("key2", "value2");
        result.put("key3", "value3");
        return result;

    }

    private IdentityHashMap<String, String> testIdentityHashMap()
    {
        final IdentityHashMap<String, String> result = new IdentityHashMap<>();
        result.put("key1", "value1");
        result.put("key2", "value2");
        result.put("key3", "value3");
        return result;

    }

    private Hashtable<String, String> testHashtable()
    {
        final Hashtable<String, String> result = new Hashtable<>();
        result.put("key1", "value1");
        result.put("key2", "value2");
        result.put("key3", "value3");
        return result;
    }

    private CopyOnWriteArraySet<String> testCopyOnWriteArraySet()
    {
        final CopyOnWriteArraySet<String> result = new CopyOnWriteArraySet<>();
        // Not using the constructor with collection as that not seems to guarantee the same order.
        result.add("Item1");
        result.add("Item2");
        result.add("Item3");
        return result;
    }

    private CopyOnWriteArrayList<String> testCopyOnWriteArrayList()
    {
        return new CopyOnWriteArrayList<>(List.of("Item1", "Item2", "Item3"));
    }

    private ArrayDeque<String> testArrayDeque()
    {
        final ArrayDeque<String> result = new ArrayDeque<>();
        result.add("Item1");
        result.add("Item2");
        result.add("Item3");
        return result;
    }

    private Set<String> testHashSet()
    {
        return new HashSet<>(Set.of("Item1", "Item2", "Item3"));

    }

    private Map<String, String> testHashMap()
    {
        final Map<String, String> result = new HashMap<>();
        result.put("key1", "value1");
        result.put("key2", "value2");
        result.put("key3", "value3");
        return result;
    }

    private List<String> testArrayList()
    {
        return new ArrayList<>(List.of("Item1", "Item2", "Item3"));
    }

    private LocalDateTime defineLocalDateTime()
    {
        return Instant.ofEpochMilli(MOMENT_IN_TIME)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private ZonedDateTime defineZonedDateTime()
    {
        return Instant.ofEpochMilli(MOMENT_IN_TIME)
                .atZone(ZoneOffset.of("+01:00")
                                .normalized());
    }

    private Object newStringBuilder()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("Builder content");
        return builder;
    }

    private Object newStringBuffer()
    {
        final StringBuffer builder = new StringBuffer();
        builder.append("StringBuffer content");
        return builder;
    }

    private Address testAddress()
    {
        return new Address(123, "to nowhere", "somewhere", "666");
    }

    private Employee createCircular()
    {
        final Employee theBoss = new Employee(1L, "The boss");

        final Employee employee1 = new Employee(2L, "Person X");
        final Employee employee2 = new Employee(3L, "Person Y");
        final Employee employee3 = new Employee(4L, "Person Z");

        employee3.setManager(employee2);

        employee1.setManager(theBoss);
        employee2.setManager(theBoss);

        return theBoss;
    }
}
