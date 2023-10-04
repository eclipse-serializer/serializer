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

import org.eclipse.serializer.collections.interfaces.CapacityExtendable;
import org.eclipse.serializer.collections.interfaces.ExtendedMap;

/**
 * 
 * @param <K> the key type
 * @param <V> the value type
 */
public interface XAddingMap<K, V> extends CapacityExtendable, ExtendedMap<K, V>
{
	/**
	 * 
	 * @param <K> the key type
	 * @param <V> the value type
	 */
	public interface Creator<K, V>
	{
		public XAddingMap<K, V> newInstance();
	}


	public boolean nullKeyAllowed();
	public boolean nullValuesAllowed();

	/**
	 * Adds the passed key and value as an entry if key is not yet contained. Return value indicates new entry.
	 * @param key the key
	 * @param value the value
	 * @return <code>true</code> if a new entry was created
	 */
	public boolean add(K key, V value);

	/**
	 * Sets the passed key and value to an appropriate entry if one can be found. Return value indicates entry change.
	 * @param key the key
	 * @param value the value
	 * @return <code>true</code> if an entry was changed
	 */
	public boolean set(K key, V value);


	/**
	 * Sets only the passed value to an existing entry appropriate to the passed sampleKey.
	 * Returns value indicates change.
	 * @param sampleKey the key
	 * @param value the value
	 * @return <code>true</code> if a value was changed
	 */
	public boolean valueSet(K sampleKey, V value);


}
