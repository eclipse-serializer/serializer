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

import java.util.Map;

import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.collections.KeyValueFlatCollector;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomCollection;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.util.X;


public abstract class AbstractBinaryHandlerMap<T extends Map<?, ?>>
extends AbstractBinaryHandlerCustomCollection<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_ELEMENTS = 0;

	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final long getElementCount(final Binary data)
	{
		return data.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS);
	}
	
	public static final void populateMapFromHelperArray(final Map<?, ?> instance, final Object elementsHelper)
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
		final Map<Object, Object> castedInstance = (Map<Object, Object>)instance;
		populateMap(castedInstance, (Object[])elementsHelper);
	}
	
	public static final void populateMap(final Map<Object, Object> instance, final Object[] elements)
	{
		for(int i = 0; i < elements.length; i += 2)
		{
			if(instance.putIfAbsent(elements[i], elements[i + 1]) != null)
			{
				// (22.04.2016 TM)EXCP: proper exception
				throw new RuntimeException(
					"Element hashing inconsistency in " + XChars.systemString(instance)
				);
			}
		}
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public AbstractBinaryHandlerMap(final Class<T> type)
	{
		super(
			type,
			keyValuesFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// store elements simply as array binary form
		data.storeMapEntrySet(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance.entrySet()   ,
			handler
		);
	}

	@Override
	public void updateState(
		final Binary                 data    ,
		final T                      instance,
		final PersistenceLoadHandler handler
	)
	{
		instance.clear();
		final int elementCount = X.checkArrayRange(getElementCount(data));
		final KeyValueFlatCollector<Object, Object> collector = KeyValueFlatCollector.New(elementCount);
		data.collectKeyValueReferences(BINARY_OFFSET_ELEMENTS, elementCount, handler, collector);
		data.registerHelper(instance, collector.yield());
	}

	@Override
	public void complete(
		final Binary                 data    ,
		final T                      instance,
		final PersistenceLoadHandler handler
	)
	{
		populateMapFromHelperArray(instance, data.getHelper(instance));
	}
	
	@Override
	public void iterateInstanceReferences(
		final T                   instance,
		final PersistenceFunction iterator
	)
	{
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public void iterateLoadableReferences(
		final Binary                     data  ,
		final PersistenceReferenceLoader loader
	)
	{
		data.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, loader);
	}
		
}
