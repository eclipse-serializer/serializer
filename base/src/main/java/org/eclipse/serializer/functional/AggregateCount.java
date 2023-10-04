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

public final class AggregateCount<E> implements Aggregator<E, Long>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private long count;



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E element)
	{
		this.count++;
	}

	@Override
	public final Long yield()
	{
		return this.count;
	}

}
