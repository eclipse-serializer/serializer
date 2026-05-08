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
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;


/**
 * Skeletal base for handlers of mutable types that hold no references &mdash; e.g. {@link java.util.Date}.
 * Pins {@link #hasPersistedReferences()} to {@code false} and supplies a no-op
 * {@link #iterateLoadableReferences} so subclasses only need to implement {@link #store},
 * {@link #create}, and {@link #updateState}.
 *
 * @param <T> the runtime type handled.
 *
 * @see AbstractBinaryHandlerCustomNonReferentialFixedLength
 */
public abstract class AbstractBinaryHandlerCustomNonReferential<T>
extends AbstractBinaryHandlerCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected AbstractBinaryHandlerCustomNonReferential(final Class<T> type)
	{
		super(type);
	}

	protected AbstractBinaryHandlerCustomNonReferential(
		final Class<T>                                                    type   ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		super(type, members);
	}
	
	protected AbstractBinaryHandlerCustomNonReferential(
		final Class<T>                                                    type    ,
		final String                                                      typeName,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		super(type, typeName, members);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////


	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		// no-op
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return false;
	}

}
