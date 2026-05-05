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
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;


/**
 * Specialization of {@link AbstractBinaryHandlerCustomValue} for value types whose persisted form has a
 * fixed length (e.g. {@link Integer}, {@link Long}, {@link Boolean}). Pins
 * {@link #hasPersistedVariableLength()} and {@link #hasVaryingPersistedLengthInstances()} to {@code false}.
 *
 * @param <T> the runtime value type handled.
 * @param <S> the validation-state representation.
 */
public abstract class AbstractBinaryHandlerCustomValueFixedLength<T, S>
extends AbstractBinaryHandlerCustomValue<T, S>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCustomValueFixedLength(
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
	public final boolean hasPersistedVariableLength()
	{
		return false;
	}
	
	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}
	
}
