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

public class CapacityExceededException extends IndexExceededException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public CapacityExceededException()
	{
		super();
	}

	public CapacityExceededException(final String message)
	{
		super(message);
	}

	public CapacityExceededException(final int bound, final int index, final String message)
	{
		super(bound, index, message);
	}

	public CapacityExceededException(final int bound, final int index)
	{
		super(bound, index);
	}



}
