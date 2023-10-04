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
import java.util.function.Predicate;


public class IsSmaller<E> implements Predicate<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Comparator<? super E> comparator;
	private       E                     currentMin;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public IsSmaller(final Comparator<? super E> comparator)
	{
		super();
		this.comparator = comparator;
		this.currentMin = null;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public boolean test(final E element)
	{
		if(this.comparator.compare(element, this.currentMin) < 0)
		{
			this.currentMin = element;
			return false;
		}
		return true;
	}

}
