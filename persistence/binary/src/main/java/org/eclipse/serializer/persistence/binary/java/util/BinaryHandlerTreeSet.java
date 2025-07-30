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

import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomCollection;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.util.X;


public final class BinaryHandlerTreeSet extends AbstractBinaryHandlerCustomCollection<TreeSet<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_COMPARATOR =                                                      0;
	static final long BINARY_OFFSET_ELEMENTS   = BINARY_OFFSET_COMPARATOR + Binary.objectIdByteLength();

	static final long FIELD_OFFSET_MAP  = XMemory.objectFieldOffset(XReflect.getAnyField(TreeSet.class, "m"));
	static final long FIELD_OFFSET_COMPARATOR  = XMemory.objectFieldOffset(XReflect.getAnyField(TreeMap.class, "comparator"));

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<TreeSet<?>> handledType()
	{
		return (Class)TreeSet.class; // no idea how to get ".class" to work otherwise
	}
	
	@SuppressWarnings("unchecked")
	private static <E> Comparator<? super E> getComparator(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		return (Comparator<? super E>)data.readReference(BINARY_OFFSET_COMPARATOR, handler);
	}

	static final int getElementCount(final Binary data)
	{
		return X.checkArrayRange(data.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}
	
	public static BinaryHandlerTreeSet New()
	{
		return new BinaryHandlerTreeSet();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerTreeSet()
	{
		super(
			handledType(),
			SimpleArrayFields(
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
		final TreeSet<?>                      instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// store elements simply as array binary form
		data.storeIterableAsList(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);
		
		data.store_long(
			BINARY_OFFSET_COMPARATOR,
			handler.apply(instance.comparator())
		);
	}
	
	@Override
	public final TreeSet<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new TreeSet<>();
	}

	@Override
	public final void updateState(final Binary data, final TreeSet<?> instance, final PersistenceLoadHandler handler)
	{
		Object map = XMemory.getObject(instance, FIELD_OFFSET_MAP);
		XMemory.setObject(map, FIELD_OFFSET_COMPARATOR, getComparator(data, handler));
		
		instance.clear();
		
		/*
		 * Tree collections don't use hashing, but their comparing logic still uses the elements' state,
		 * which might not yet be available when this method is called. Hence the detour to #complete.
		 */
		final Object[] elementsHelper = new Object[getElementCount(data)];
		data.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, handler, elementsHelper);
		data.registerHelper(instance, elementsHelper);
	}
	
	@Override
	public final void complete(final Binary data, final TreeSet<?> instance, final PersistenceLoadHandler handler)
	{
		AbstractBinaryHandlerCollection.populateCollectionFromHelperArray(instance, data.getHelper(instance));
	}

	@Override
	public final void iterateInstanceReferences(final TreeSet<?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.comparator());
		Persistence.iterateReferencesIterable(iterator, instance);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		iterator.acceptObjectId(data.readObjectId(BINARY_OFFSET_COMPARATOR));
		data.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}
