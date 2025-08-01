package org.eclipse.serializer.persistence.binary.java.util.concurrent;

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

import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;

import org.eclipse.serializer.collections.KeyValueFlatCollector;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.java.util.AbstractBinaryHandlerMap;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomCollection;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.util.X;


public final class BinaryHandlerConcurrentSkipListMap
extends AbstractBinaryHandlerCustomCollection<ConcurrentSkipListMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_COMPARATOR =                                                      0;
	static final long BINARY_OFFSET_ELEMENTS   = BINARY_OFFSET_COMPARATOR + Binary.objectIdByteLength();
	
	static final long FIELD_OFFSET_COMPARATOR  = XMemory.objectFieldOffset(XReflect.getAnyField(ConcurrentSkipListMap.class, "comparator"));
	

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentSkipListMap<?, ?>> handledType()
	{
		return (Class)ConcurrentSkipListMap.class; // no idea how to get ".class" to work otherwise
	}

	static final int getElementCount(final Binary data)
	{
		return X.checkArrayRange(data.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS));
	}
		
	@SuppressWarnings("unchecked")
	private static <E> Comparator<? super E> getComparator(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		return (Comparator<? super E>)data.readReference(BINARY_OFFSET_COMPARATOR, handler);
	}
	
	public static BinaryHandlerConcurrentSkipListMap New()
	{
		return new BinaryHandlerConcurrentSkipListMap();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerConcurrentSkipListMap()
	{
		super(
			handledType(),
			keyValuesFields(
				CustomField(Comparator.class, "comparator")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final ConcurrentSkipListMap<?, ?>     instance,
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
		
		data.store_long(
			BINARY_OFFSET_COMPARATOR,
			handler.apply(instance.comparator())
		);
	}
	
	@Override
	public final ConcurrentSkipListMap<?, ?> create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		return new ConcurrentSkipListMap<>();
	}

	@Override
	public final void updateState(
		final Binary                      data    ,
		final ConcurrentSkipListMap<?, ?> instance,
		final PersistenceLoadHandler      handler
	)
	{
		XMemory.setObject(instance, FIELD_OFFSET_COMPARATOR, getComparator(data, handler));
		
		instance.clear();
						
		/*
		 * Tree collections don't use hashing, but their comparing logic still uses the elements' state,
		 * which might not yet be available when this method is called. Hence, the detour to #complete.
		 */
		final int elementCount = getElementCount(data);
		final KeyValueFlatCollector<Object, Object> collector = KeyValueFlatCollector.New(elementCount);
		data.collectKeyValueReferences(BINARY_OFFSET_ELEMENTS, elementCount, handler, collector);
		data.registerHelper(instance, collector.yield());
	}

	@Override
	public final void complete(
		final Binary                      data    ,
		final ConcurrentSkipListMap<?, ?> instance,
		final PersistenceLoadHandler      handler
	)
	{
		AbstractBinaryHandlerMap.populateMapFromHelperArray(instance, data.getHelper(instance));
	}

	@Override
	public final void iterateInstanceReferences(
		final ConcurrentSkipListMap<?, ?> instance,
		final PersistenceFunction         iterator
	)
	{
		iterator.apply(instance.comparator());
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		iterator.acceptObjectId(data.readObjectId(BINARY_OFFSET_COMPARATOR));
		data.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}
