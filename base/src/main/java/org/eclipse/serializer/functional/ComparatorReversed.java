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

import java.util.Comparator;


public class ComparatorReversed<T> implements Comparator<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Comparator<? super T> comparator;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ComparatorReversed(final Comparator<? super T> comparator)
	{
		super();
		this.comparator = comparator;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public int compare(final T o1, final T o2)
	{
		return -this.comparator.compare(o1, o2);
	}

}
