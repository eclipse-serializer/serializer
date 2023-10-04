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

public interface XAddingTable<K, V> extends XAddingMap<K, V>, XAddingSequence<KeyValue<K, V>>
{
	public interface Creator<K, V>
	{
		public XAddingTable<K, V> newInstance();
	}


	@SuppressWarnings("unchecked")
	@Override
	public XAddingTable<K, V> addAll(KeyValue<K, V>... elements);

	@Override
	public XAddingTable<K, V> addAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddingTable<K, V> addAll(XGettingCollection<? extends KeyValue<K, V>> elements);


}
