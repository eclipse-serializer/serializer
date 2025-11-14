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

import java.util.Comparator;

import org.eclipse.serializer.typing.KeyValue;



/**
 * @param <K> type of contained keys
 * @param <V> type of contained values
 * 
 *
 */
public interface XTable<K, V> extends XMap<K, V>, XGettingTable<K, V>, XEnum<KeyValue<K, V>>
{
    @Override
    public Keys<K, V> keys();

    @Override
    public Values<K, V> values();

    @Override
    public XTable<K, V> copy();

    @Override
    public XTable<K, V> sort(Comparator<? super KeyValue<K, V>> comparator);

    @SuppressWarnings("unchecked")
    @Override
    public XTable<K, V> putAll(KeyValue<K, V>... elements);

    @Override
    public XTable<K, V> putAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);

    @Override
    public XTable<K, V> putAll(XGettingCollection<? extends KeyValue<K, V>> elements);

    @SuppressWarnings("unchecked")
    @Override
    public XTable<K, V> addAll(KeyValue<K, V>... elements);

    @Override
    public XTable<K, V> addAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);

    @Override
    public XTable<K, V> addAll(XGettingCollection<? extends KeyValue<K, V>> elements);



    public interface Satellite<K, V> extends XGettingTable.Satellite<K, V>
    {
        @Override
        public XTable<K, V> parent();

    }

    public interface Keys<K, V> extends XGettingTable.Keys<K, V>, XMap.Keys<K, V>, XEnum<K>
    {
        @Override
        public XTable<K, V> parent();

        @SuppressWarnings("unchecked")
        @Override
        public Keys<K, V> putAll(K... elements);

        @Override
        public Keys<K, V> putAll(K[] elements, int srcStartIndex, int srcLength);

        @Override
        public Keys<K, V> putAll(XGettingCollection<? extends K> elements);

        @SuppressWarnings("unchecked")
        @Override
        public Keys<K, V> addAll(K... elements);

        @Override
        public Keys<K, V> addAll(K[] elements, int srcStartIndex, int srcLength);

        @Override
        public Keys<K, V> addAll(XGettingCollection<? extends K> elements);

        @Override
        public XEnum<K> copy();

    }

    public interface Values<K, V> extends XGettingTable.Values<K, V>, XMap.Values<K, V>, XList<V>
    {
        @Override
        public XTable<K, V> parent();

        @Override
        public XList<V> copy();

    }

    public interface Bridge<K, V> extends XGettingTable.Bridge<K, V>, XMap.Bridge<K, V>
    {
        @Override
        public XTable<K, V> parent();

    }

}
