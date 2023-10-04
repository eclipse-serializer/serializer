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

import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.persistence.binary.exceptions.BinaryPersistenceException;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;

public interface ValidatingBinaryHandler<T, S>
{
	public default void validateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		this.validateStates(
			instance,
			this.getValidationStateFromInstance(instance),
			this.getValidationStateFromBinary(data)
		);
	}
	
	public S getValidationStateFromInstance(T instance);
	
	public S getValidationStateFromBinary(Binary data);
	
	public default void validateStates(
		final T instance     ,
		final S instanceState,
		final S binaryState
	)
	{
		if(instanceState.equals(binaryState))
		{
			return;
		}
		
		this.throwInconsistentStateException(instance, instanceState, binaryState);
	}
	
	public default void throwInconsistentStateException(
		final T      instance                   ,
		final Object instanceStateRepresentation,
		final Object binaryStateRepresentation
	)
	{
		throw new BinaryPersistenceException(
			"Inconsistent state for instance " + XChars.systemString(instance) + ": \""
			+ instanceStateRepresentation + "\" not equal to \"" + binaryStateRepresentation + "\""
		);
	}
	
}
