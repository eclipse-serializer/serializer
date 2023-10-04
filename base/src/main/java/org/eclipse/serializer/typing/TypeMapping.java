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

import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.types.XTable;
import org.eclipse.serializer.hashing.HashEqualator;

public interface TypeMapping<V> extends TypeMappingLookup<V>
{
	public boolean add(TypePair typePair, V value);
	
	public boolean put(TypePair typePair, V value);
	
	public TypeMapping<V> register(TypePair typePair, V value);
	
	
	
	public default boolean add(final Class<?> type1, final Class<?> type2, final V value)
	{
		return this.add(TypePair.New(type1, type2), value);
	}
	
	public default boolean put(final Class<?> type1, final Class<?> type2, final V value)
	{
		return this.put(TypePair.New(type1, type2), value);
	}
	
	public default TypeMapping<V> register(final Class<?> type1, final Class<?> type2, final V value)
	{
		this.register(TypePair.New(type1, type2), value);
		return this;
	}
	

	@Override
	public XTable<TypePair, V> table();
	
	

	public static <T> TypeMapping<T> New()
	{
		return New(TypePair.HashEquality());
	}
	
	public static <T> TypeMapping<T> New(final HashEqualator<? super TypePair> hashEquality)
	{
		return new TypeMapping.Default<>(
                EqHashTable.New(hashEquality)
		);
	}
	
	public final class Default<V> implements TypeMapping<V>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final EqHashTable<TypePair, V> table;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final EqHashTable<TypePair, V> table)
		{
			super();
			this.table = table;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final boolean contains(final TypePair typePair)
		{
			synchronized(this.table)
			{
				return this.table.keys().contains(typePair);
			}
		}

		@Override
		public final V lookup(final TypePair typePair)
		{
			synchronized(this.table)
			{
				return this.table.get(typePair);
			}
		}

		@Override
		public final boolean add(final TypePair typePair, final V value)
		{
			synchronized(this.table)
			{
				return this.table.add(typePair, value);
			}
		}

		@Override
		public final boolean put(final TypePair typePair, final V value)
		{
			synchronized(this.table)
			{
				return this.table.put(typePair, value);
			}
		}

		@Override
		public final TypeMapping<V> register(final TypePair typePair, final V value)
		{
			synchronized(this.table)
			{
				// registering without feedback is a definite command that must be reliable, hence put.
				this.table.put(typePair, value);
			}
			
			return this;
		}

		@Override
		public final XTable<TypePair, V> table()
		{
			return this.table;
		}
		
	}
	
}
