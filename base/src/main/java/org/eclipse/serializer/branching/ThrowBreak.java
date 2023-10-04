package org.eclipse.serializer.branching;

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
 * Thrown to signals the outer context to break the current loop,
 * normally proceeding with the actions following the loop.
 */
public class ThrowBreak extends AbstractBranchingThrow
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ThrowBreak()
	{
		super();
	}

	public ThrowBreak(final Throwable cause)
	{
		super(cause);
	}

	public ThrowBreak(final Object hint)
	{
		super(hint);
	}

	public ThrowBreak(final Object hint, final Throwable cause)
	{
		super(hint, cause);
	}



}
