package org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections;

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

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.collections.ConstList;
import org.eclipse.serializer.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerConstList
extends AbstractBinaryHandlerCustomCollection<ConstList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final long BINARY_OFFSET_LIST = 0; // binary form is 100% just a simple list, so offset 0



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<ConstList<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)ConstList.class;
	}
	
	public static BinaryHandlerConstList New()
	{
		return new BinaryHandlerConstList();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerConstList()
	{
		// binary layout definition
		super(
			handledType(),
			SimpleArrayFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final ConstList<?>                    instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeReferences(
			this.typeId()                          ,
			objectId                               ,
			0                                      ,
			handler                                ,
			XCollectionsInternals.getData(instance)
		);
	}

	@Override
	public final ConstList<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return ConstList.New(X.checkArrayRange(data.getListElementCountReferences(0)));
	}

	@Override
	public final void updateState(final Binary data, final ConstList<?> instance, final PersistenceLoadHandler handler)
	{
		final Object[] arrayInstance = XCollectionsInternals.getData(instance);

		// Length must be checked for consistency reasons. No clear required.
		data.validateArrayLength(arrayInstance, BINARY_OFFSET_LIST);
		data.collectElementsIntoArray(BINARY_OFFSET_LIST, handler, arrayInstance);
	}

	@Override
	public final void iterateInstanceReferences(final ConstList<?> instance, final PersistenceFunction iterator)
	{
		final Object[] arrayInstance = XCollectionsInternals.getData(instance);
		Persistence.iterateReferences(iterator, arrayInstance, 0, arrayInstance.length);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		data.iterateListElementReferences(BINARY_OFFSET_LIST, iterator);
	}

}
