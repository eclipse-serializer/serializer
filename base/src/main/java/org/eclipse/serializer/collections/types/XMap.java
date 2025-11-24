package org.eclipse.serializer.collections.types;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.serializer.typing.KeyValue;


/**
 * @param <K> type of contained keys
 * @param <V> type of contained values
 * 
 *
 */
public interface XMap<K, V> extends XGettingMap<K, V>, XSet<KeyValue<K, V>>
{

    public KeyValue<K, V> addGet(K key, V value);

    public KeyValue<K, V> substitute(K key, V value);

    /**
     * Ensures that this map instance contains a non-null value for the passed key and returns that value.
     * <p>
     * If a non-null value can be found for the passed key, it is returned. Otherwise, the value provided
     * by the passed supplier will be associated with the passed key and is returned.
     *
     * @param key the search key.
     * @param valueProvider the value supplier used to provide a value for the passed key in case non could be found.
     * @return the value associated with the passed key, either already existing or newly assiciated by
     *         the call of this method.
     */
    public V ensure(K key, Function<? super K, V> valueProvider);

    /*
     * (06.07.2016 TM)NOTE: javac reported an ambiguity with XProcessingCollection here for the name "remove".
     * Hence it got changed to "removeFor".
     */
    public V removeFor(final K key);

    public default boolean shiftAdd(final K key, final V value)
    {
        return this.add(key, value);
    }


    @Override
    public Keys<K, V> keys();

    @Override
    public Values<K, V> values();

    @Override
    public XMap<K, V> copy();

    @Override
    public boolean nullKeyAllowed();

    @Override
    public boolean nullValuesAllowed();


    /**
     * Adds the passed key and value as an entry if key is not yet contained. Return value indicates new entry.
     * @param key to add
     * @param value to add
     * @return {@code true} if element was added; {@code false} if not
     */
    public boolean add(K key, V value);

    /**
     * Ensures the passed key and value to be contained as an entry in the map.
     * @param key to add
     * @param value to add
     * @return {@code true} if element was added; {@code false} if not
     */
    public boolean put(K key, V value);

    /**
     * Sets the passed key and value to an appropriate entry if one can be found.
     * @param key to find element to change
     * @param value to set
     * @return {@code true} if element was changed; {@code false} if not
     */
    public boolean set(K key, V value);

    /**
     * Ensures the passed key and value to be contained as an entry in the map.
     * @param key to add
     * @param value to add
     * @return the old value or {@code null}
     */
    public KeyValue<K, V> putGet(K key, V value);

    public KeyValue<K, V> replace(K key, V value);


    /**
     * Sets the passed key and value to an appropriate entry if one can be found.
     * @param key to find element to change
     * @param value to set
     * @return the old value
     */
    public KeyValue<K, V> setGet(K key, V value);

    /**
     * Ensures the passed value to be either set to an existing entry equal to sampleKey or inserted as a new one.
     * @param sampleKey to find an existing element
     * @param value to insert
     * @return {@code true} if element was changed; {@code false} if not
     */
    public boolean valuePut(K sampleKey, V value);

    /**
     * Sets only the passed value to an existing entry appropriate to the passed sampleKey.
     * @param sampleKey to find an existing element
     * @param value to set
     * @return {@code true} if element was changed; {@code false} if not
     */
    public boolean valueSet(K sampleKey, V value);

    /**
     * Ensures the passed value to be either set to an existing entry appropriate to sampleKey or inserted as a new one.
     * @param sampleKey to find an existing element
     * @param value to add
     * @return the old value
     */
    public V valuePutGet(K sampleKey, V value);

    /**
     * Sets only the passed value to an existing entry appropriate to the passed sampleKey.
     * @param sampleKey to find an existing element
     * @param value to add
     * @return the old value
     */
    public V valueSetGet(K sampleKey, V value);

    @Override
    public V get(K key);

    @Override
    public V searchValue(Predicate<? super K> keyPredicate);

    @Override
    public XImmutableMap<K, V> immure();


    /**
     * Ensures the passed key-value-pairs to be contained as entries in the map.
     * A return value indicates a new entry.
     *
     * @return this
     */
    @SuppressWarnings("unchecked")
    @Override
    public XMap<K, V> putAll(KeyValue<K, V>... elements);

    /**
     * Ensures the passed key-value-pairs to be contained as entries in the map.
     * Only the elements with indizes from the srcStartIndex to the srcStartIndex+srcLength are put in the collection. <br>
     * A return value indicates a new entry.
     *
     * @return this
     */
    @Override
    public XMap<K, V> putAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);

    @SuppressWarnings("unchecked")
    @Override
    public XMap<K, V> addAll(KeyValue<K, V>... elements);

    @Override
    public XMap<K, V> addAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);

    @Override
    public XMap<K, V> addAll(XGettingCollection<? extends KeyValue<K, V>> elements);

    public default boolean shiftPut(final K key, final V value)
    {
        return this.put(key, value);
    }



    public interface Satellite<K, V> extends XGettingMap.Satellite<K, V>
    {
        @Override
        public XMap<K, V> parent();

    }

    public interface Bridge<K, V> extends XGettingMap.Bridge<K, V>, Satellite<K, V>
    {
        @Override
        public XMap<K, V> parent();

    }

    public interface Values<K, V> extends XGettingMap.Values<K, V>, Satellite<K, V>, XBag<V>
    {
        @Override
        public XBag<V> copy(); // values in an unordered map is a practical example for a bag

    }

    public interface Keys<K, V> extends XGettingMap.Keys<K, V>, Satellite<K, V>, XSet<K>
    {
        // empty so far
    }

}

