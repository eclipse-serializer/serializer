package org.eclipse.serializer.persistence.binary.types;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;


public abstract class AbstractBinaryHandlerCustomValue<T, S>
extends AbstractBinaryHandlerCustom<T>
implements ValidatingBinaryHandler<T, S>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCustomValue(
		final Class<T>                                                    type  ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> fields
	)
	{
		super(type, fields);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return false;
	}

	@Override
	public final void iterateLoadableReferences(final Binary offset, final PersistenceReferenceLoader iterator)
	{
		// no references
	}
	
	@Override
	public void initializeState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		/*
		 * No-op update logic by default. This is useful for all immutable value types (String, Integer, etc.)
		 * which normally get initialized directly at instance creation time..
		 */
	}
		
	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		this.validateState(data, instance, handler);
	}

}
