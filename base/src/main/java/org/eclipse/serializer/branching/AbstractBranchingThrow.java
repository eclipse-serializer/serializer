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
 * 
 *
 */
public abstract class AbstractBranchingThrow extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Object hint;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractBranchingThrow()
	{
		super();
		this.hint = null;
	}

	protected AbstractBranchingThrow(final Throwable cause)
	{
		super(cause);
		this.hint = null;
	}

	protected AbstractBranchingThrow(final Object hint)
	{
		super();
		this.hint = hint;
	}

	protected AbstractBranchingThrow(final Object hint, final Throwable cause)
	{
		super(cause);
		this.hint = hint;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public Object getHint()
	{
		return this.hint;
	}
	
	@Override
	public synchronized AbstractBranchingThrow fillInStackTrace()
	{
		return this;
	}

}
