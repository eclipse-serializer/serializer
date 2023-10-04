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

import org.eclipse.serializer.typing.KeyValue;

/**
 * 
 * @param <K> the key type
 * @param <V> the value type
 */
public interface XPutGetMap<K, V> extends XPuttingMap<K, V>, XAddGetMap<K, V>
{
	/**
	 * 
	 * @param <K> the key type
	 * @param <V> the value type
	 */
	public interface Creator<K, V> extends XPuttingMap.Creator<K, V>, XAddGetMap.Creator<K, V>
	{
		@Override
		public XPutGetMap<K, V> newInstance();
	}
	
	
	
	/**
	 * Ensures the passed key and value to be contained as an entry in the map. Returns the old value or {@code null}.
	 * 
	 * @param key the key
	 * @param value the value
	 * @return the old value or {@code null}.
	 */
	public KeyValue<K, V> putGet(K key, V value);
	
	public KeyValue<K, V> replace(K key, V value);
		
}
