package org.eclipse.serializer.util.iterables;

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


import java.util.Iterator;


public class ArrayIterable<T> implements Iterable<T>
{
	private final T[] array;

	public ArrayIterable(final T[] array)
	{
		super();
		if(array == null)
		{
			throw new NullPointerException("array may not be null");
		}
		this.array = array;
	}

	/**
	 * @see Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator()
	{
		return new ArrayIterator<>(this.array);
	}
	
}
