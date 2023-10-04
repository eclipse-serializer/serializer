package org.eclipse.serializer.collections;

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

import org.eclipse.serializer.collections.interfaces.ExtendedCollection;
import org.eclipse.serializer.collections.types.XAddingCollection;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.exceptions.ArrayCapacityException;
import org.eclipse.serializer.exceptions.IndexBoundsException;


/**
 * This class is an implementation-internal for optional performance optimisation.
 * <p>
 * It is the base class for every extended collection, even if the extending class does not implement
 * {@link XAddingCollection}. Subclasses of this class that do not implement {@link XAddingCollection} will throw an
 * {@link UnsupportedOperationException} in the adding methods defined in this class.<br>
 * All code using the optimisation methods in here has to ensure that it can only be legally called for implementations
 * of {@link XAddingCollection}, for example by using {@link XAddingCollection} as the concrete parameter type.
 * <p>
 * Note that this technique of using {@link UnsupportedOperationException} is explicitly not comparable to the
 * JDK's approach like in {@link java.util.Collections#unmodifiableCollection(java.util.Collection)} where a
 * general purpose type (java.util.Collection) is implemented broken to achieve a certain reduced
 * behavior,
 * while the technique described here is a cleanly encapsulated implementation detail used in combination with proper
 * typing.
 *
 * @param <E> type of contained elements
 */
public abstract class AbstractExtendedCollection<E> implements ExtendedCollection<E>
{
	public static void validateIndex(final long bound, final long index) throws IndexBoundsException
	{
		if(index < 0)
		{
			throw new IndexBoundsException(bound, index);
		}
		if(index >= bound)
		{
			throw new IndexBoundsException(bound, index);
		}
	}

	public static void ensureFreeArrayCapacity(final int size)
	{
		// actually just checks for "==", but ">=" proved to be faster in tests (probably due to simple sign checking)
		if(size >= Integer.MAX_VALUE)
		{
			throw new ArrayCapacityException(size);
		}
	}

	// (28.06.2011 TM)FIXME: implement counting add() util methods

	protected abstract int internalCountingAddAll(E[] elements) throws UnsupportedOperationException;

	protected abstract int internalCountingAddAll(E[] elements, int offset, int length)
		throws UnsupportedOperationException;

	protected abstract int internalCountingAddAll(XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException;

	protected abstract int internalCountingPutAll(E[] elements) throws UnsupportedOperationException;

	protected abstract int internalCountingPutAll(E[] elements, int offset, int length)
		throws UnsupportedOperationException;

	protected abstract int internalCountingPutAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException;

}
