package org.eclipse.serializer.persistence.binary.java.util;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
 * %%
 * Copyright (C) 2023 - 2024 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustom;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.reflect.XReflect;

/**
 * Binary Handler for private class "java.util.Collections$SetFromMap"
 */
public class BinaryHandlerSetFromMap<T> extends AbstractBinaryHandlerCustom<Set<T>>
{
	private static long offsetMap;
	private static long offsetSet;
			
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static BinaryHandlerSetFromMap New()
	{
		Class<?> clazz = XReflect.getDeclaredNestedClass(Collections.class, "java.util.Collections$SetFromMap");
		
		offsetMap = XMemory.objectFieldOffset(XReflect.getAnyField(clazz, "m"));
		offsetSet = XMemory.objectFieldOffset(XReflect.getAnyField(clazz, "s"));
		
		return new BinaryHandlerSetFromMap(clazz);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected BinaryHandlerSetFromMap(Class<Set<T>> type)
	{
		super(type,
			CustomFields(
				CustomField(Map.class, "map"))
		);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		iterator.acceptObjectId(data.read_long(0));
	}

	@Override
	public void updateState(final Binary data, Set<T> instance, final PersistenceLoadHandler handler)
	{
		final Map<?,?> hashmap = (Map<?, ?>) handler.lookupObject(data.read_long(0));
		
		XMemory.setObject(instance, offsetMap, hashmap);
		XMemory.setObject(instance, offsetSet, hashmap.keySet());
	}

	@Override
	public void store(final Binary data, final Set<T> instance, final long objectId, final PersistenceStoreHandler<Binary> handler)
	{
		data.storeEntityHeader(Binary.referenceBinaryLength(1), this.typeId(), objectId);
		
		Object mapInstance = XMemory.getObject(instance, offsetMap);
		data.storeReference(0, handler, mapInstance);
	}

	@Override
	public Set<T> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return Collections.newSetFromMap(new HashMap<T,Boolean>());
	}

}
