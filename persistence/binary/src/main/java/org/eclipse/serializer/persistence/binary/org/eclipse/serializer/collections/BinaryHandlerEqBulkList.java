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

import static org.eclipse.serializer.util.X.notNull;

import java.lang.reflect.Field;

import org.eclipse.serializer.collections.EqBulkList;
import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.hashing.HashEqualator;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomIterableSizedArray;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceSizedArrayLengthController;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerEqBulkList
extends AbstractBinaryHandlerCustomIterableSizedArray<EqBulkList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_EQUALATOR   =                                                     0;
	static final long BINARY_OFFSET_SIZED_ARRAY = BINARY_OFFSET_EQUALATOR + Binary.objectIdByteLength();

	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field FIELD_EQULATOR = getInstanceFieldOfType(EqBulkList.class, Equalator.class);



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<EqBulkList<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)EqBulkList.class;
	}
	
	public static BinaryHandlerEqBulkList New(final PersistenceSizedArrayLengthController controller)
	{
		return new BinaryHandlerEqBulkList(
			notNull(controller)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerEqBulkList(final PersistenceSizedArrayLengthController controller)
	{
		// binary layout definition
		super(
			handledType(),
			SizedArrayFields(
				CustomField(HashEqualator.class, "hashEqualator")
			),
			controller
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final EqBulkList<?>                   instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// store elements as sized array, leave out space for equalator reference
		data.storeSizedArray(
			this.typeId()                          ,
			objectId                               ,
			BINARY_OFFSET_SIZED_ARRAY              ,
			XCollectionsInternals.getData(instance),
			instance.intSize()                     ,
			handler
		);

		// persist equalator and set the resulting oid at its binary place
		data.store_long(
			BINARY_OFFSET_EQUALATOR,
			handler.apply(instance.equality())
		);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public final EqBulkList<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		// this method only creates shallow instances, so hashEqualator gets set during update like other references.
		return new EqBulkList((Equalator)null);
	}

	@Override
	public final void updateState(
		final Binary                 data   ,
		final EqBulkList<?>          instance,
		final PersistenceLoadHandler handler
	)
	{
		// must clear to avoid memory leaks due to residual references beyond the new size in existing instances.
		instance.clear();
		
		// length must be checked for consistency reasons
		instance.ensureCapacity(this.determineArrayLength(data, BINARY_OFFSET_SIZED_ARRAY));

		XCollectionsInternals.setSize(instance, data.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY,
			handler,
			XCollectionsInternals.getData(instance)
		));

		// set equalator instance (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_EQULATOR),
			data.readReference(BINARY_OFFSET_EQUALATOR, handler)
		);
	}

	@Override
	public final void iterateInstanceReferences(final EqBulkList<?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.equality());
		Persistence.iterateReferences(iterator, XCollectionsInternals.getData(instance), 0, instance.intSize());
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		iterator.acceptObjectId(data.readObjectId(BINARY_OFFSET_EQUALATOR));
		data.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}

}
