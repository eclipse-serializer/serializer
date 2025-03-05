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

import java.util.Collection;

import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomIterableSimpleListElements;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.util.X;


public abstract class AbstractBinaryHandlerCollection<T extends Collection<?>>
extends AbstractBinaryHandlerCustomIterableSimpleListElements<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final void populateCollectionFromHelperArray(
		final Collection<?> instance      ,
		final Object        elementsHelper
	)
	{
		if(elementsHelper == null)
		{
			// (22.04.2016 TM)EXCP: proper exception
			throw new RuntimeException(
				"Missing collection elements helper instance for " + XChars.systemString(instance)
			);
		}
		
		if(!(elementsHelper instanceof Object[]))
		{
			// (22.04.2016 TM)EXCP: proper exception
			throw new RuntimeException(
				"Invalid collection elements helper instance for " + XChars.systemString(instance)
			);
		}
		
		@SuppressWarnings("unchecked")
		final Collection<Object> castedInstance = (Collection<Object>)instance;
		populateCollection(castedInstance, (Object[])elementsHelper);
	}
	
	public static final void populateCollection(final Collection<Object> instance, final Object[] elements)
	{
		for(int i = 0; i < elements.length; i++)
		{
			if(!instance.add(elements[i]))
			{
				// (22.04.2016 TM)EXCP: proper exception
				throw new RuntimeException(
					"Error in adding logic (e.g. element hashing inconsistency) in " + XChars.systemString(instance)
				);
			}
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCollection(final Class<T> type)
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
		// generic-generic collection handler logic uses the set workaround logic to be safe in any case
		instance.clear();
		final Object[] elementsHelper = new Object[X.checkArrayRange(getElementCount(data))];
		data.collectElementsIntoArray(this.binaryOffsetElements(), handler, elementsHelper);
		data.registerHelper(instance, elementsHelper);
	}

	@Override
	public void complete(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		// generic-generic collection handler logic uses the set workaround logic to be safe in any case
		populateCollectionFromHelperArray(instance, data.getHelper(instance));
	}
	
}
