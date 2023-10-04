package org.eclipse.serializer.reference;

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


import org.eclipse.serializer.collections.Constant;

import java.util.Iterator;


public interface LinkingReferencing<T> extends Referencing<T>, Iterable<T>
{
	boolean hasNext();
	
	boolean isNext(LinkingReferencing<T> linkedReference);
	
	LinkingReferencing<T> next();

	Object[] toArray();

	@Override
	String toString();

	String toChainString();

	

	class Default<T> extends Constant<T> implements LinkingReferencing<T>
	{

		private final LinkingReferencing<T> next = null;

		public Default(final T ref)
		{
			super(ref);
		}
		
		@Override
		public LinkingReferencing<T> next()
		{
			return this.next;
		}

		@Override
		public boolean hasNext()
		{
			return this.next != null;
		}

		@Override
		public boolean isNext(final LinkingReferencing<T> linkedReference)
		{
			return this.next == linkedReference;
		}

		@Override
		public Object[] toArray()
		{
			LinkingReferencing<T> loopNext = this;
			int i = 1;
			while((loopNext = loopNext.next()) != null)
			{
				i++; //this is presumable faster than using an ArrayList or LinkedList for collection
			}

			final Object[] array = new Object[i];
			loopNext = this;
			i = 0;
			do
			{
				array[i++] = loopNext.get();
			}
			while((loopNext = loopNext.next()) != null);

			return array;
		}

		@Override
		public Iterator<T> iterator()
		{
			return new ChainIterator<>(this);
		}

		@Override
		public String toString()
		{
			final String e = String.valueOf(this.get());
			
			return '(' + e + ')' + (this.hasNext() ? '-' : 'x');
		}

		@Override
		public String toChainString()
		{
			final StringBuilder sb = new StringBuilder(1024);
			sb.append('(').append(this.get()).append(')');
			for(LinkingReferencing<T> r = this.next; r != null; r = r.next())
			{
				sb.append('-').append('(').append(r.get()).append(')');
			}
			
			return sb.toString();
		}

	}


	final class ChainIterator<T> implements Iterator<T>
	{
		private LinkingReferencing<T> current;



		ChainIterator(final LinkingReferencing<T> current)
		{
			super();
			this.current = current;
		}

		@Override
		public boolean hasNext()
		{
			return this.current.next() != null;
		}

		@Override
		public T next()
		{
			final LinkingReferencing<T> currentCurrent = this.current;
			this.current = currentCurrent.next();
			
			return currentCurrent.get();
		}

		@Override
		public void remove() throws UnsupportedOperationException
		{
			throw new UnsupportedOperationException(
				"Can't remove current element in a one directional chain"
			);
		}
		
	}
}
