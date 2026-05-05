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
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;


/**
 * Specialization of {@link AbstractBinaryHandlerCustomValue} for value types whose persisted form has a
 * variable length (e.g. {@link String}, {@link java.math.BigInteger}, byte arrays). Pins
 * {@link #hasPersistedVariableLength()} and {@link #hasVaryingPersistedLengthInstances()} to {@code true}.
 *
 * @param <T> the runtime value type handled.
 * @param <S> the validation-state representation.
 */
public abstract class AbstractBinaryHandlerCustomValueVariableLength<T, S>
extends AbstractBinaryHandlerCustomValue<T, S>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCustomValueVariableLength(
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
	public final boolean hasPersistedVariableLength()
	{
		return true;
	}
	
	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return true;
	}
	
}
