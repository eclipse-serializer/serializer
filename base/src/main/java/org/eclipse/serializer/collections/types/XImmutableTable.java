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

import java.util.function.Predicate;

import org.eclipse.serializer.typing.KeyValue;


/**
 * 
 *
 */
public interface XImmutableTable<K, V> extends XImmutableMap<K, V>, XGettingTable<K, V>, XImmutableEnum<KeyValue<K, V>>
{
	// key to value querying
	@Override
	public V get(K key);

	@Override
	public V searchValue(Predicate<? super K> keyPredicate);

	// satellite instances

	@Override
	public Keys<K, V> keys();

	@Override
	public Values<K, V> values();

	@Override
	public XImmutableTable<K, V> copy();

	/**
	 * Provides an instance of an immutable collection type with equal behavior and data as this instance.
	 * <p>
	 * If this instance already is of an immutable collection type, it returns itself.
	 *
	 * @return an immutable copy of this collection instance.
	 */
	@Override
	public XImmutableTable<K, V> immure();

	// null handling characteristics information

	@Override
	public boolean nullKeyAllowed();

	@Override
	public boolean nullValuesAllowed();


	///////////////////////////////////////////////////////////////////////////
	// satellite types //
	////////////////////

	public interface Satellite<K, V> extends XImmutableMap.Satellite<K, V>, XGettingTable.Satellite<K, V>
	{
		@Override
		public XImmutableTable<K, V> parent();

	}

	public interface Keys<K, V> extends XImmutableMap.Keys<K, V>, XGettingTable.Keys<K, V>, XImmutableEnum<K>
	{
		@Override
		public XImmutableTable<K, V> parent();

		@Override
		public XImmutableEnum<K> copy();
	}

	public interface Values<K, V>
	extends XImmutableMap.Values<K, V>, XGettingTable.Values<K, V>, XImmutableList<V>, Satellite<K, V>
	{
		@Override
		public XImmutableTable<K, V> parent();

		@Override
		public XImmutableList<V> copy();
	}

	public interface Bridge<K, V> extends XImmutableMap.Bridge<K, V>, XGettingTable.Bridge<K, V>, Satellite<K, V>
	{
		@Override
		public XImmutableTable<K, V> parent();

	}

}
