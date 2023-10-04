package org.eclipse.serializer.functional;

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

import org.eclipse.serializer.collections.BulkList;

public final class AggregateArrayBuilder<E> implements Aggregator<E, E[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final AggregateArrayBuilder<Object> New()
	{
		return New(Object.class);
	}

	public static final <E> AggregateArrayBuilder<E> New(final Class<E> elementType)
	{
		return New(elementType, 1);
	}

	public static final <E> AggregateArrayBuilder<E> New(final Class<E> elementType, final int initialCapacity)
	{
		return new AggregateArrayBuilder<>(elementType, new BulkList<>(initialCapacity));
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Class<E>    elementType;
	final BulkList<E> collector  ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	AggregateArrayBuilder(final Class<E> elementType, final BulkList<E> collector)
	{
		super();
		this.elementType = elementType;
		this.collector   = collector  ;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E element)
	{
		this.collector.add(element);
	}

	@Override
	public final E[] yield()
	{
		return this.collector.toArray(this.elementType);
	}

}
