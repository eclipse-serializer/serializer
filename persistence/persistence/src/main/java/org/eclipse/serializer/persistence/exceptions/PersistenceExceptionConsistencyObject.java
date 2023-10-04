package org.eclipse.serializer.persistence.exceptions;

/*-
 * #%L
 * Eclipse Serializer Persistence
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

import org.eclipse.serializer.chars.XChars;

public class PersistenceExceptionConsistencyObject extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Object actualRef;
	final Object passedRef;
	final long   oid      ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyObject(
		final long   oid,
		final Object actualRef,
		final Object passedRef
	)
	{
		super();
		this.oid       = oid      ;
		this.actualRef = actualRef;
		this.passedRef = passedRef;
	}

	@Override
	public String getMessage()
	{
		return "oid = " + this.oid
			+ " actualRef = " + XChars.systemString(this.actualRef)
			+ " passedRef = " + XChars.systemString(this.passedRef)
		;
	}

}
