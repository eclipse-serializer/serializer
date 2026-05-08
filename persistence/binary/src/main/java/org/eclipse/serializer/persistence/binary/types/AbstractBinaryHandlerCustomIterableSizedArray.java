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
import org.eclipse.serializer.persistence.types.PersistenceSizedArrayLengthController;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;


/**
 * Skeletal base for handlers of iterable types backed by a sized array (capacity-bearing arrays whose
 * occupied portion can be smaller than the array length, e.g. {@code ArrayList}'s internal array). Holds
 * a {@link PersistenceSizedArrayLengthController} that, on load, decides the effective array length from
 * the persisted capacity and actual element count &mdash; allowing the runtime to clamp or grow the array
 * to a sensible size rather than blindly trusting the persisted capacity.
 *
 * @param <T> the iterable runtime type handled.
 *
 * @see PersistenceSizedArrayLengthController
 */
public abstract class AbstractBinaryHandlerCustomIterableSizedArray<T extends Iterable<?>>
extends AbstractBinaryHandlerCustomIterable<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceSizedArrayLengthController controller;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCustomIterableSizedArray(
		final Class<T>                                                                type        ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> customFields,
		final PersistenceSizedArrayLengthController                                   controller
	)
	{
		super(type, customFields);
		this.controller = controller;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected final int determineArrayLength(final Binary data, final long sizedArrayOffset)
	{
		final int specifiedLength      = data.getSizedArrayLength(sizedArrayOffset);
		final int actualElementCount   = data.getSizedArrayElementCount(sizedArrayOffset);
		final int effectiveArrayLength = this.controller.controlArrayLength(specifiedLength, actualElementCount);
		
		return effectiveArrayLength;
	}

}
