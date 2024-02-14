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

import org.eclipse.serializer.collections.EqConstHashTable;
import org.eclipse.serializer.hashing.HashEqualator;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomCollection;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.util.X;


public final class BinaryHandlerEqConstHashTable
extends AbstractBinaryHandlerCustomCollection<EqConstHashTable<?, ?>>
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

	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field
		FIELD_EQUALATOR = getInstanceFieldOfType(EqConstHashTable.class, HashEqualator.class)          ,
		FIELD_KEYS      = getInstanceFieldOfType(EqConstHashTable.class, EqConstHashTable.Keys.class)  ,
		FIELD_VALUES    = getInstanceFieldOfType(EqConstHashTable.class, EqConstHashTable.Values.class)
	;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<EqConstHashTable<?, ?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)EqConstHashTable.class;
	}

	private static int getBuildItemElementCount(final Binary data)
	{
		return X.checkArrayRange(data.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS));
	}

	private static float getBuildItemHashDensity(final Binary data)
	{
		return data.read_float(BINARY_OFFSET_HASH_DENSITY);
	}
	
	public static BinaryHandlerEqConstHashTable New()
	{
		return new BinaryHandlerEqConstHashTable();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerEqConstHashTable()
	{
		// binary layout definition
		super(
			handledType(),
			keyValuesFields(
				CustomField(HashEqualator.class, "hashEqualator"),
				CustomField(EqConstHashTable.Keys.class, "keys"),
				CustomField(EqConstHashTable.Values.class, "values"),
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
		final EqConstHashTable<?, ?>          instance,
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
	public final EqConstHashTable<?, ?> create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		return EqConstHashTable.NewCustom(
			getBuildItemElementCount(data),
			getBuildItemHashDensity(data)
		);
	}

	@Override
	public final void updateState(
		final Binary                 data    ,
		final EqConstHashTable<?, ?> instance,
		final PersistenceLoadHandler handler
	)
	{
		// validate to the best of possibilities (or should an immutable instance be updatedable from outside?)
		if(instance.size() != 0)
		{
			throw new IllegalStateException("Instance already contains data: " + instance);
		}
		
		// set single instances (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_EQUALATOR),
			data.readReference(BINARY_OFFSET_EQUALATOR, handler)
		);
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_KEYS),
			data.readReference(BINARY_OFFSET_KEYS, handler)
		);
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_VALUES),
			data.readReference(BINARY_OFFSET_VALUES, handler)
		);
		XCollectionsInternals.setSize(instance, data.collectKeyValueReferences(
			BINARY_OFFSET_ELEMENTS,
			getBuildItemElementCount(data),
			handler,
			(k, v) -> XCollectionsInternals.internalAdd(instance, k, v)
		));
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	@Override
	public final void complete(
		final Binary                 data    ,
		final EqConstHashTable<?, ?> instance,
		final PersistenceLoadHandler handler
	)
	{
		// rehash all previously unhashed collected elements
		XCollectionsInternals.internalRehash(instance);
	}

	@Override
	public final void iterateInstanceReferences(
		final EqConstHashTable<?, ?> instance,
		final PersistenceFunction    iterator
	)
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
