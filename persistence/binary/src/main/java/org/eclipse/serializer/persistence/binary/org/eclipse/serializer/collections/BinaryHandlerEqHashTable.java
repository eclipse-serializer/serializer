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

import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.hashing.HashEqualator;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomCollection;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.util.X;


public final class BinaryHandlerEqHashTable
extends AbstractBinaryHandlerCustomCollection<EqHashTable<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long
		BINARY_OFFSET_EQUALATOR    =                                                        0,
		BINARY_OFFSET_KEYS         = BINARY_OFFSET_EQUALATOR    + Binary.objectIdByteLength(),
		BINARY_OFFSET_VALUES       = BINARY_OFFSET_KEYS         + Binary.objectIdByteLength(),
		BINARY_OFFSET_HASH_DENSITY = BINARY_OFFSET_VALUES       + Binary.objectIdByteLength(),
		BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_HASH_DENSITY + Float.BYTES
	;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<EqHashTable<?, ?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)EqHashTable.class;
	}

	private static int getBuildItemElementCount(final Binary data)
	{
		return X.checkArrayRange(data.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS));
	}

	private static float getBuildItemHashDensity(final Binary data)
	{
		return data.read_float(BINARY_OFFSET_HASH_DENSITY);
	}
	
	public static BinaryHandlerEqHashTable New()
	{
		return new BinaryHandlerEqHashTable();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerEqHashTable()
	{
		// binary layout definition
		super(
			handledType(),
			keyValuesFields(
				CustomField(HashEqualator.class, "hashEqualator"),
				CustomField(EqHashTable.Keys.class, "keys"),
				CustomField(EqHashTable.Values.class, "values"),
				CustomField(float.class, "hashDensity")
			)

		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final EqHashTable<?, ?>               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// store elements simply as array binary form
		data.storeKeyValuesAsEntries(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);
		data.store_long(
			BINARY_OFFSET_EQUALATOR,
			handler.apply(instance.hashEquality())
		);
		data.store_long(
			BINARY_OFFSET_KEYS,
			handler.apply(instance.keys())
		);
		data.store_long(
			BINARY_OFFSET_VALUES,
			handler.apply(instance.values())
		);
		data.store_float(
			BINARY_OFFSET_HASH_DENSITY,
			instance.hashDensity()
		);
	}

	@Override
	public final EqHashTable<?, ?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return EqHashTable.NewCustom(
			getBuildItemElementCount(data),
			getBuildItemHashDensity(data)
		);
	}

	@Override
	public final void updateState(
		final Binary                 data    ,
		final EqHashTable<?, ?>      instance,
		final PersistenceLoadHandler handler
	)
	{
		// must clear to ensure consistency
		instance.clear();
		
		XCollectionsInternals.setHashEqualator(
			instance,
			(HashEqualator<?>)data.readReference(BINARY_OFFSET_EQUALATOR, handler)
		);
		XCollectionsInternals.setKeys(
			instance,
			(EqHashTable<?, ?>.Keys)data.readReference(BINARY_OFFSET_KEYS, handler)
		);
		XCollectionsInternals.setValues(
			instance,
			(EqHashTable<?, ?>.Values)data.readReference(BINARY_OFFSET_VALUES, handler)
		);
		XCollectionsInternals.setSize(instance, data.collectKeyValueReferences(
			BINARY_OFFSET_ELEMENTS,
			getBuildItemElementCount(data),
			handler,
			(k, v) -> XCollectionsInternals.internalCollectUnhashed(instance, k, v)
		));
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	@Override
	public final void complete(
		final Binary                 data    ,
		final EqHashTable<?, ?>      instance,
		final PersistenceLoadHandler handler
	)
	{
		// rehash all previously unhashed collected elements
		instance.rehash();
	}

	@Override
	public final void iterateInstanceReferences(final EqHashTable<?, ?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.equality());
		iterator.apply(instance.keys());
		iterator.apply(instance.values());
		Persistence.iterateReferences(iterator, instance);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		iterator.acceptObjectId(data.readObjectId(BINARY_OFFSET_EQUALATOR));
		iterator.acceptObjectId(data.readObjectId(BINARY_OFFSET_KEYS));
		iterator.acceptObjectId(data.readObjectId(BINARY_OFFSET_VALUES));
		data.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

}
