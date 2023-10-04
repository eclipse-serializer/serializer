package org.eclipse.serializer.persistence.binary.java.util;

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

import java.util.List;

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.persistence.binary.internal.AbstractBinaryHandlerCustomIterableSimpleListElements;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;


public abstract class AbstractBinaryHandlerList<T extends List<?>>
extends AbstractBinaryHandlerCustomIterableSimpleListElements<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerList(final Class<T> type)
	{
		super(type);
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	protected long getElementCount(final T instance)
	{
		return instance.size();
	}

	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		// instance must be cleared in case an existing one is updated
		instance.clear();
		
		@SuppressWarnings("unchecked")
		final List<Object> castedInstance = (List<Object>)instance;
		
		data.collectObjectReferences(
			this.binaryOffsetElements(),
			X.checkArrayRange(getElementCount(data)),
			handler,
			e ->
				castedInstance.add(e)
		);
	}
	
}
