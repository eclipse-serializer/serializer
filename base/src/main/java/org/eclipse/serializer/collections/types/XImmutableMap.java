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
public interface XImmutableMap<K, V> extends XGettingMap<K, V>, XImmutableSet<KeyValue<K, V>>
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
	public XImmutableMap<K, V> copy();

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

	public interface Satellite<K, V>
	{
		public XImmutableMap<K, V> parent();

	}

	public interface Values<K, V> extends XGettingMap.Values<K, V>, XImmutableBag<V>, Satellite<K, V>
	{
		// empty so far
	}

	public interface Keys<K, V> extends XGettingMap.Keys<K, V>, XImmutableSet<K>, Satellite<K, V>
	{
		// empty so far
	}

	public interface Bridge<K, V> extends XGettingMap.Bridge<K, V>
	{
		// empty so far
	}

}

