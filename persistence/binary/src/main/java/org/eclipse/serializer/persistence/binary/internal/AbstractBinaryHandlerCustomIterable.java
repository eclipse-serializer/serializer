package org.eclipse.serializer.persistence.binary.internal;

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
import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;


public abstract class AbstractBinaryHandlerCustomIterable<T extends Iterable<?>>
extends AbstractBinaryHandlerCustomCollection<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCustomIterable(
		final Class<T>                                                                type        ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> customFields
	)
	{
		super(type, customFields);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferencesIterable(iterator, instance);
	}
	
}
