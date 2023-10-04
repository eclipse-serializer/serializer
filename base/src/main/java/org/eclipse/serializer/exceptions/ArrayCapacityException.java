package org.eclipse.serializer.exceptions;

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

/**
 * This implementation intentionally does NOT extend from {@link ArrayIndexOutOfBoundsException}.
 * See architectural explanation in {@link IndexBoundsException}.
 *
 */
// hopefully, this can be removed at some point in the future ...
public class ArrayCapacityException extends IndexBoundsException
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final String MESSAGE_BODY = "Java technical array capacity limit of max signed 32 bit integer value exceeded";
	


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ArrayCapacityException()
	{
		this(Integer.MAX_VALUE + 1L);
	}

	public ArrayCapacityException(final long exceedingCapacity)
	{
		this(exceedingCapacity, null);
	}

	public ArrayCapacityException(final long exceedingCapacity, final String s)
	{
		super(Integer.MAX_VALUE , exceedingCapacity, s);
	}

	@Override
	public String assembleDetailString()
	{
		return MESSAGE_BODY + ": " + this.index();
	}

	
	
	// hacky buggy security hole misconception serialization
	private static final long serialVersionUID = 3168758028720258369L;
}
