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
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;



public class PersistenceExceptionTypeHandlerConsistencyConflictedType extends PersistenceExceptionTypeHandlerConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Class<?>                    type             ;
	final PersistenceTypeHandler<?, ?> actualTypeHandler;
	final PersistenceTypeHandler<?, ?> passedTypeHandler;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeHandlerConsistencyConflictedType(
		final Class<?>                    type             ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler,
		final PersistenceTypeHandler<?, ?> passedTypeHandler
	)
	{
		this(type, actualTypeHandler, passedTypeHandler, null, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyConflictedType(
		final Class<?>                     type             ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler,
		final PersistenceTypeHandler<?, ?> passedTypeHandler,
		final String                       message
	)
	{
		this(type, actualTypeHandler, passedTypeHandler, message, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyConflictedType(
		final Class<?>                     type              ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler,
		final PersistenceTypeHandler<?, ?> passedTypeHandler,
		final Throwable                    cause
	)
	{
		this(type, actualTypeHandler, passedTypeHandler, null, cause);
	}

	public PersistenceExceptionTypeHandlerConsistencyConflictedType(
		final Class<?>                     type             ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler,
		final PersistenceTypeHandler<?, ?> passedTypeHandler,
		final String                       message          ,
		final Throwable                    cause
	)
	{
		this(type, actualTypeHandler, passedTypeHandler, message, cause, true, true);
	}

	public PersistenceExceptionTypeHandlerConsistencyConflictedType(
		final Class<?>                     type              ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler ,
		final PersistenceTypeHandler<?, ?> passedTypeHandler ,
		final String                       message           ,
		final Throwable                    cause             ,
		final boolean                      enableSuppression ,
		final boolean                      writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.type              = type             ;
		this.actualTypeHandler = actualTypeHandler;
		this.passedTypeHandler = passedTypeHandler;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getType()
	{
		return this.type;
	}

	public PersistenceTypeHandler<?, ?> getActualTypeHandler()
	{
		return this.actualTypeHandler;
	}

	public PersistenceTypeHandler<?, ?> getPassedTypeHandler()
	{
		return this.passedTypeHandler;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Type \"" + this.type + "\" is already associated to type handler "
			+ XChars.systemString(this.actualTypeHandler)
			+ ", cannot be associated to type handler \"" + XChars.systemString(this.passedTypeHandler) + "\" as well."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
