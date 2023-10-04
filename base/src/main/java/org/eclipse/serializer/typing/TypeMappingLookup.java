package org.eclipse.serializer.typing;

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

import org.eclipse.serializer.collections.types.XGettingTable;

public interface TypeMappingLookup<V>
{
	public boolean contains(TypePair typePair);
	
	public V lookup(TypePair typePair);
		
		
	public default boolean contains(final Class<?> type1, final Class<?> type2)
	{
		return this.contains(TypePair.New(type1, type2));
	}
	
	public default V lookup(final Class<?> type1, final Class<?> type2)
	{
		return this.lookup(TypePair.New(type1, type2));
	}
	
	
	public XGettingTable<TypePair, V> table();
		
}
