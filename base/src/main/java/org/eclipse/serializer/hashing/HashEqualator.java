package org.eclipse.serializer.hashing;

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

import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.equality.IdentityEqualator;
import org.eclipse.serializer.equality.ValueTypeEqualator;


public interface HashEqualator<T> extends Equalator<T>, Hasher<T>
{
	@Override
	public int hash(T object);

	@Override
	public boolean equal(T object1, T object2);



	public interface Provider<T> extends Equalator.Provider<T>
	{
		@Override
		public HashEqualator<T> provideEqualator();
	}



	public interface ImmutableHashEqualator<E>
	extends HashEqualator<E>, Hasher.ImmutableHashCode<E>
	{
		// type interface only
	}

	public interface IdentityHashEqualator<E>
	extends IdentityEqualator<E>, Hasher.IdentityHashCode<E>, ImmutableHashEqualator<E>
	{
		// type interface only
	}

	public interface ValueTypeHashEqualator<E>
	extends HashEqualator<E>, Hasher.ValueHashCode<E>, ValueTypeEqualator<E>
	{
		// type interface only
	}

	public interface ImmutableValueTypeHashEqualator<E>
	extends ImmutableHashEqualator<E>, ValueTypeHashEqualator<E>
	{
		// type interface only
	}

}
