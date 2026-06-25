package test.eclipse.serializer.fixtures;

/*-
 * #%L
 * EclipseStore Integration Tests
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


import test.eclipse.serializer.fixtures.types.ArrayDequeData;
import test.eclipse.serializer.fixtures.types.ArrayListData;
import test.eclipse.serializer.fixtures.types.AtomicPrimitiveData;
import test.eclipse.serializer.fixtures.types.BasicNonPrimitive;
import test.eclipse.serializer.fixtures.types.BasicNonPrimitiveArrayTypes;
import test.eclipse.serializer.fixtures.types.BigDecimalData;
import test.eclipse.serializer.fixtures.types.BigIntegerData;
import test.eclipse.serializer.fixtures.types.BinaryHandlerTestData;
import test.eclipse.serializer.fixtures.types.BitSetData;
import test.eclipse.serializer.fixtures.types.BulkListData;
import test.eclipse.serializer.fixtures.types.CalendarApiData;
import test.eclipse.serializer.fixtures.types.ClassTypeData;
import test.eclipse.serializer.fixtures.types.ConcurrentHashMapData;
import test.eclipse.serializer.fixtures.types.ConcurrentLinkedDequeData;
import test.eclipse.serializer.fixtures.types.ConcurrentLinkedQueueData;
import test.eclipse.serializer.fixtures.types.ConcurrentSkipListMapData;
import test.eclipse.serializer.fixtures.types.ConcurrentSkipListSetData;
import test.eclipse.serializer.fixtures.types.ConstHashEnumData;
import test.eclipse.serializer.fixtures.types.ConstHashTableData;
import test.eclipse.serializer.fixtures.types.ConstListData;
import test.eclipse.serializer.fixtures.types.CopyOnWriteArrayListData;
import test.eclipse.serializer.fixtures.types.CopyOnWriteArraySetData;
import test.eclipse.serializer.fixtures.types.CurrencyData;
import test.eclipse.serializer.fixtures.types.CustomEnumTrivialData;
import test.eclipse.serializer.fixtures.types.DateData;
import test.eclipse.serializer.fixtures.types.EqBulkListData;
import test.eclipse.serializer.fixtures.types.EqConstHashEnumData;
import test.eclipse.serializer.fixtures.types.EqConstHashTableData;
import test.eclipse.serializer.fixtures.types.EqHashEnumData;
import test.eclipse.serializer.fixtures.types.FileData;
import test.eclipse.serializer.fixtures.types.HashEnumData;
import test.eclipse.serializer.fixtures.types.HashMapData;
import test.eclipse.serializer.fixtures.types.HashSetData;
import test.eclipse.serializer.fixtures.types.HashTableMSData;
import test.eclipse.serializer.fixtures.types.HashtableData;
import test.eclipse.serializer.fixtures.types.IntetAddressData;
import test.eclipse.serializer.fixtures.types.JavaNetURLData;
import test.eclipse.serializer.fixtures.types.LimitListData;
import test.eclipse.serializer.fixtures.types.LinkedHashMapData;
import test.eclipse.serializer.fixtures.types.LinkedHashSetData;
import test.eclipse.serializer.fixtures.types.LinkedListData;
import test.eclipse.serializer.fixtures.types.OptionalDoubleData;
import test.eclipse.serializer.fixtures.types.OptionalIntData;
import test.eclipse.serializer.fixtures.types.OptionalLongData;
import test.eclipse.serializer.fixtures.types.PathData;
import test.eclipse.serializer.fixtures.types.PrimitiveArrayTypes;
import test.eclipse.serializer.fixtures.types.PrimitiveTypes;
import test.eclipse.serializer.fixtures.types.PriorityQueueData;
import test.eclipse.serializer.fixtures.types.PropertiesData;
import test.eclipse.serializer.fixtures.types.RegexPatternData;
import test.eclipse.serializer.fixtures.types.RootsData;
import test.eclipse.serializer.fixtures.types.SocketAddressData;
import test.eclipse.serializer.fixtures.types.SqlCalendarData;
import test.eclipse.serializer.fixtures.types.StackData;
import test.eclipse.serializer.fixtures.types.StringBufferData;
import test.eclipse.serializer.fixtures.types.StringBuilderData;
import test.eclipse.serializer.fixtures.types.SubstituterDefaultData;
import test.eclipse.serializer.fixtures.types.TreeMapData;
import test.eclipse.serializer.fixtures.types.TreeSetData;
import test.eclipse.serializer.fixtures.types.URIData;
import test.eclipse.serializer.fixtures.types.UUIDData;
import test.eclipse.serializer.fixtures.types.VectorData;
import test.eclipse.serializer.fixtures.types.WeakHashMapData;

import java.util.function.Supplier;

public enum TypeEnumCellular {
    PrimitiveTypes(new PrimitiveTypes().fillSampleData(), PrimitiveTypes::new),
    ArrayDeque(new ArrayDequeData().fillSampleData(), ArrayDequeData::new),
    ArrayList(new ArrayListData().fillSampleData(), ArrayListData::new),
    BasicNonPrimitiveArrayTypes(new BasicNonPrimitiveArrayTypes().fillSampleData(), BasicNonPrimitiveArrayTypes::new),
    BasicNonPrimitive(new BasicNonPrimitive().fillSampleData(), BasicNonPrimitive::new),
    BigDecimal(new BigDecimalData().fillSampleData(), BigDecimalData::new),
    BigInteger(new BigIntegerData().fillSampleData(), BigIntegerData::new),
    BulkList(new BulkListData().fillSampleData(), BulkListData::new),
    Class(new ClassTypeData().fillSampleData(), ClassTypeData::new),
    ConcurrentHashMap(new ConcurrentHashMapData().fillSampleData(), ConcurrentHashMapData::new),
    ConcurrentLinkedDeque(new ConcurrentLinkedDequeData().fillSampleData(), ConcurrentLinkedDequeData::new),
    ConcurrentLinkedQueue(new ConcurrentLinkedQueueData().fillSampleData(), ConcurrentLinkedQueueData::new),
    ConcurrentSkipListMap(new ConcurrentSkipListMapData().fillSampleData(), ConcurrentSkipListMapData::new),
    ConcurrentSkipListSet(new ConcurrentSkipListSetData().fillSampleData(), ConcurrentSkipListSetData::new),
    ConstHashEnum(new ConstHashEnumData().fillSampleData(), ConstHashEnumData::new),
    ConstHashTable(new ConstHashTableData().fillSampleData(), ConstHashTableData::new),
    ConstList(new ConstListData().fillSampleData(), ConstListData::new),
    CopyOnWriteArrayList(new CopyOnWriteArrayListData().fillSampleData(), CopyOnWriteArrayListData::new),
    CopyOnWriteArraySet(new CopyOnWriteArraySetData().fillSampleData(), CopyOnWriteArraySetData::new),
    Currency(new CurrencyData().fillSampleData(), CurrencyData::new),
    CustomEnum(new CustomEnumTrivialData().fillSampleData(), CustomEnumTrivialData::new),
    Date(new DateData().fillSampleData(), DateData::new),
    EqBulkList(new EqBulkListData().fillSampleData(), EqBulkListData::new),
    EqConstHashEnum(new EqConstHashEnumData().fillSampleData(), EqConstHashEnumData::new),
    EqConstHashTable(new EqConstHashTableData().fillSampleData(), EqConstHashTableData::new),
    EqHashEnum(new EqHashEnumData().fillSampleData(), EqHashEnumData::new),
    File(new FileData().fillSampleData(), FileData::new),
    HashEnum(new HashEnumData().fillSampleData(), HashEnumData::new),
    HashMap(new HashMapData().fillSampleData(), HashMapData::new),
    HashSet(new HashSetData().fillSampleData(), HashSetData::new),
    HashTable(new HashtableData().fillSampleData(), HashtableData::new),
    HashTableMicroStream(new HashTableMSData().fillSampleData(), HashTableMSData::new),
    //IdentityHashMap(new IdentityHashMapData().fillSampleData(), IdentityHashMapData::new),
    InetAddress(new IntetAddressData().fillSampleData(), IntetAddressData::new),
    LimitList(new LimitListData().fillSampleData(), LimitListData::new),
    LinkedHashMap(new LinkedHashMapData().fillSampleData(), LinkedHashMapData::new),
    LinkedHashSet(new LinkedHashSetData().fillSampleData(), LinkedHashSetData::new),
    LinkedList(new LinkedListData().fillSampleData(), LinkedListData::new),
    OptionalDouble(new OptionalDoubleData().fillSampleData(), OptionalDoubleData::new),
    OptionalInt(new OptionalIntData().fillSampleData(), OptionalIntData::new),
    OptionalLong(new OptionalLongData().fillSampleData(), OptionalLongData::new),
    Path(new PathData().fillSampleData(), PathData::new),
    PersistenceRootsDefault(new RootsData().fillSampleData(), RootsData::new),
    PriorityQueue(new PriorityQueueData().fillSampleData(), PriorityQueueData::new),
    Properties(new PropertiesData().fillSampleData(), PropertiesData::new),
    RegexPattern(new RegexPatternData().fillSampleData(), RegexPatternData::new),
    SocketAddress(new SocketAddressData().fillSampleData(), SocketAddressData::new),
    Stack(new StackData().fillSampleData(), StackData::new),
    StringBuffer(new StringBufferData().fillSampleData(), StringBufferData::new),
    StringBuilder(new StringBuilderData().fillSampleData(), StringBuilderData::new),
    Substituter(new SubstituterDefaultData().fillSampleData(), SubstituterDefaultData::new),
    TreeMap(new TreeMapData().fillSampleData(), TreeMapData::new),
    TreeSet(new TreeSetData().fillSampleData(), TreeSetData::new),
    Vector(new VectorData().fillSampleData(), VectorData::new),
    WeakHashMap(new WeakHashMapData().fillSampleData(), WeakHashMapData::new),
    URI(new URIData().fillSampleData(), URIData::new),
    URL(new JavaNetURLData().fillSampleData(), JavaNetURLData::new),
    UUID(new UUIDData().fillSampleData(), UUIDData::new),
    PrimitiveArrayTypes(new PrimitiveArrayTypes().fillSampleData(), PrimitiveArrayTypes::new),
    Java_util_Calendar(new CalendarApiData().fillSampleData(), CalendarApiData::new),
    Java_SQL_Dates(new SqlCalendarData().fillSampleData(), SqlCalendarData::new),
    Atomic_types(new AtomicPrimitiveData().fillSampleData(), AtomicPrimitiveData::new),
    BitSet(new BitSetData().fillSampleData(), BitSetData::new);

    final private BinaryHandlerTestData original;
    final private Supplier<?> instanceSupplier;

    TypeEnumCellular(BinaryHandlerTestData original, Supplier<?> copy) {
        this.original = original;
        this.instanceSupplier = copy;
    }

    public BinaryHandlerTestData getOriginal() {
        return original;
    }

    public Object createEmptyInstance() {
        return instanceSupplier.get();
    }

    @Deprecated
    public Supplier<?> getCopy() {
        return instanceSupplier;
    }
}
