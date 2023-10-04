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

import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.hashing.HashEqualator;

public interface TypePair
{
	public Class<?> type1();
	
	public Class<?> type2();
	
	
	
	
	public static int hash(final TypePair tp)
	{
		return tp.type1().hashCode() | tp.type2().hashCode();
	}
	
	public static boolean equal(final TypePair tp1, final TypePair tp2)
	{
		return tp1 == tp2
			|| tp1 != null && tp2 != null && equalNonTrivial(tp1, tp2)
		;
	}
	
	public static boolean equalNonTrivial(final TypePair tp1, final TypePair tp2)
	{
		return tp1.type1() == tp2.type1()
			&& tp1.type2() == tp2.type2()
		;
	}
	
	public static HashEquality HashEquality()
	{
		return new HashEquality();
	}
	
	// HashEqualator is not implementation-dependant like the implemented equals is.
	public final class HashEquality implements HashEqualator<TypePair>
	{
		@Override
		public final int hash(final TypePair tp)
		{
			return TypePair.hash(tp);
		}

		@Override
		public final boolean equal(final TypePair tp1, final TypePair tp2)
		{
			return TypePair.equal(tp1, tp2);
		}
		
	}
	
	
		
	public static TypePair New(final Class<?> type1, final Class<?> type2)
	{
		return new TypePair.Default(
			notNull(type1),
			notNull(type2)
		);
	}
	
	public final class Default implements TypePair
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<?> type1;
		private final Class<?> type2;
		private final int      hash ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final Class<?> type1, final Class<?> type2)
		{
			super();
			this.type1 = type1;
			this.type2 = type2;
			this.hash  = TypePair.hash(this);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final Class<?> type1()
		{
			return this.type1;
		}
		
		@Override
		public final Class<?> type2()
		{
			return this.type2;
		}
		
		@Override
		public final int hashCode()
		{
			return this.hash;
		}
		
		@Override
		public final boolean equals(final Object other)
		{
			return other == this
				|| other instanceof TypePair.Default
				&& this.type1 == ((TypePair.Default)other).type1
				&& this.type2 == ((TypePair.Default)other).type2
			;
		}
		
	}
	
}
