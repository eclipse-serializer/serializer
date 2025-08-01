package org.eclipse.serializer.persistence.binary.java.util;

/*-
 * #%L
 * Eclipse Serializer Persistence JDK17
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

import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustom;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.reference.Swizzling;

/**
 * Generic abstract class for specialized handler for
 * java.util.ImmutableCollections.Set12 and java.util.ImmutableCollections.List12
 * in JDK 15 and later
 * <br><br>
 * The handler takes the internal constant java.util.ImmutableCollections.EMPTY
 * into account which must not be persisted.
 *
 * @param <T> the handled type.
 */
public abstract class AbstractBinaryHandlerGenericImmutableCollections12<T> extends AbstractBinaryHandlerCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// abstract methods //
	/////////////////////

	/**
	 * Create a new instance of the handled type.
	 *
	 * @return the new instance.
	 */
	protected abstract T createInstance();


	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final long BINARY_OFFSET_E0 = 0;
	private static final long BINARY_OFFSET_E1 = BINARY_OFFSET_E0 + Binary.referenceBinaryLength(1);
	private static final long BINARY_LENGTH    = BINARY_OFFSET_E1 + Binary.referenceBinaryLength(1);


	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final int memoryOffset_e0;
	final int memoryOffset_e1;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Constructor
	 *
	 * @param type the handled type.
	 */
	protected AbstractBinaryHandlerGenericImmutableCollections12(final Class<T> type)
	{
		super(type,
			CustomFields(
				CustomField(Object.class, "e0"),
				CustomField(Object.class, "e1")
				));

		this.memoryOffset_e0 = XMemory.byteSizeObjectHeader(type);
		this.memoryOffset_e1 = this.memoryOffset_e0 + XMemory.byteSizeReference();
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public T create(final Binary data, final PersistenceLoadHandler handler)
	{
		return this.createInstance();
	}

	@Override
	public void store(final Binary data, final T instance, final long objectId, final PersistenceStoreHandler<Binary> handler)
	{
		final Collection<?> items = (Collection<?>)instance;
		final Object[] arr = items.toArray();

		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		data.store_long(BINARY_OFFSET_E0, handler.apply(arr[0]));

		if(arr.length == 1)
		{
			data.store_long(BINARY_OFFSET_E1, handler.apply(null));
		}
		else if(arr.length == 2)
		{
			data.store_long(BINARY_OFFSET_E1, handler.apply(arr[1]));
		}
	}

	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		final Object e0 = handler.lookupObject(data.read_long(BINARY_OFFSET_E0));
		XMemory.setObject(instance, this.memoryOffset_e0, e0);

		final long objectE1Id = data.read_long(BINARY_OFFSET_E1);
		if(Swizzling.isNotProperId(objectE1Id))
		{
			return;
		}

		final Object e1 = handler.lookupObject(objectE1Id);
		XMemory.setObject(instance, this.memoryOffset_e1, e1);
	}

	@Override
	public boolean hasPersistedReferences()
	{
		return true;
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		iterator.acceptObjectId(data.read_long(BINARY_OFFSET_E0));
		iterator.acceptObjectId(data.read_long(BINARY_OFFSET_E1));
	}

}
