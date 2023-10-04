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

public class DummyIterable<T> implements Iterable<T>
{
	T element;

	@Override
	public Iterator<T> iterator()
	{
		return new DummyIterator();
	}


	public void set(final T element)
	{
		this.element = element;
	}

	public T get()
	{
		return this.element;
	}



	private class DummyIterator implements Iterator<T>
	{
		private boolean hasNext = true;

		DummyIterator()
		{
			super();
		}

		@Override
		public boolean hasNext()
		{
			return this.hasNext;
		}

		@Override
		public T next()
		{
			this.hasNext = false;
			return DummyIterable.this.element;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}
}
