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

public final class _intSum implements _intProcedure
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private int result;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public _intSum()
	{
		super();
	}

	public _intSum(final int result)
	{
		super();
		this.result = result;
	}


	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public final int yield()
	{
		return this.result;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final int value)
	{
		this.result += value;
	}

}
