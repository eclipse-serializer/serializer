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

import java.lang.reflect.Field;

import org.eclipse.serializer.collections.EqConstList;
import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomCollection;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.util.X;


public final class BinaryHandlerEqConstList
extends AbstractBinaryHandlerCustomCollection<EqConstList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	// one oid for equalator reference as leading header value
	static final long BINARY_OFFSET_EQUALATOR =                                                     0;
	// the simple element list follows the equalator header
	static final long BINARY_OFFSET_LIST      = BINARY_OFFSET_EQUALATOR + Binary.objectIdByteLength();

	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field FIELD_EQUALATOR = getInstanceFieldOfType(EqConstList.class, Equalator.class);



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<EqConstList<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)EqConstList.class;
	}

	private static int getBuildItemElementCount(final Binary data)
	{
		return X.checkArrayRange(data.getListElementCountReferences(BINARY_OFFSET_LIST));
	}

	public static BinaryHandlerEqConstList New()
	{
		return new BinaryHandlerEqConstList();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerEqConstList()
	{
		// binary layout definition
		super(
			handledType(),
			SimpleArrayFields(
				CustomField(Equalator.class, "equalator")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final EqConstList<?>                  instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// store elements simply as array binary form, leave out space for equalator reference
		data.storeReferences(
			this.typeId()                          ,
			objectId                               ,
			BINARY_OFFSET_LIST                     ,
			handler                                ,
			XCollectionsInternals.getData(instance)
		);

		// persist equalator and set the resulting oid at its binary place (leading header value)
		data.store_long(
			BINARY_OFFSET_EQUALATOR,
			handler.apply(instance.equality())
		);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public final EqConstList<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		// this method only creates a shallow instance, so the equalator gets set during update like other references.
		return new EqConstList((Equalator)null, getBuildItemElementCount(data));
	}

	@Override
	public final void updateState(final Binary data, final EqConstList<?> instance, final PersistenceLoadHandler handler)
	{
		// set equalator instance (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_EQUALATOR),
			data.readReference(BINARY_OFFSET_EQUALATOR, handler)
		);

		final Object[] arrayInstance = XCollectionsInternals.getData(instance);

		// Length must be checked for consistency reasons. No clear required.
		data.validateArrayLength(arrayInstance, BINARY_OFFSET_LIST);
		data.collectElementsIntoArray(BINARY_OFFSET_LIST, handler, arrayInstance);
	}

	@Override
	public final void iterateInstanceReferences(final EqConstList<?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.equality());

		final Object[] arrayInstance = XCollectionsInternals.getData(instance);
		Persistence.iterateReferences(iterator, arrayInstance, 0, arrayInstance.length);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		iterator.acceptObjectId(data.readObjectId(BINARY_OFFSET_EQUALATOR));
		data.iterateListElementReferences(BINARY_OFFSET_LIST, iterator);
	}

}
