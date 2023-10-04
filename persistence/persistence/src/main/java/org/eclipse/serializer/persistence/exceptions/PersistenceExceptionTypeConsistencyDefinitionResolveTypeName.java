
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


public class PersistenceExceptionTypeConsistencyDefinitionResolveTypeName
extends PersistenceExceptionTypeConsistencyDictionary
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final String messageBody()
	{
		return "Unresolvable dictionary type";
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance typeNames //
	///////////////////////

	private final String typeName;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(final String typeName)
	{
		this(typeName, null, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(final String typeName, final String message)
	{
		this(typeName, message, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(final String typeName, final Throwable cause)
	{
		this(typeName, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(
		final String    typeName,
		final String    message ,
		final Throwable cause
	)
	{
		this(typeName, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(
		final String    typeName          ,
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.typeName = typeName;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public String typeName()
	{
		return this.typeName;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	@Override
	public String assembleDetailString()
	{
		return messageBody() + ": \"" + this.typeName() + "\"";
	}

}
