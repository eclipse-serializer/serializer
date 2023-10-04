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
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceSizedArrayLengthController;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;


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
