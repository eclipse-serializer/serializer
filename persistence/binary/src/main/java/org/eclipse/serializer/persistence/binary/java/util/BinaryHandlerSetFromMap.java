package org.eclipse.serializer.persistence.binary.java.util;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustom;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.reflect.XReflect;

/**
 * Binary Handler for private class "java.util.Collections$SetFromMap"
 */
public class BinaryHandlerSetFromMap<T> extends AbstractBinaryHandlerCustom<T>
{
	private static Field map;
	private static Field set;
	
	public static BinaryHandlerSetFromMap<?> New()
	{
		Class<?> clazz = XReflect.getDeclaredNestedClass(Collections.class, "java.util.Collections$SetFromMap");
				
		map = XReflect.getAnyField(clazz, "m");
		set = XReflect.getAnyField(clazz, "s");
				
		XReflect.setAccessible(map);
		XReflect.setAccessible(set);
	
		return new BinaryHandlerSetFromMap<>(clazz);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected BinaryHandlerSetFromMap(final Class<T> type)
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
	public void updateState(final Binary data, T instance, final PersistenceLoadHandler handler)
	{
		final Map<?,?> hashmap = (Map<?, ?>) handler.lookupObject(data.read_long(0));
		
		XReflect.setFieldValue(map, instance, hashmap);
		XReflect.setFieldValue(set, instance, hashmap.keySet());
	}

	@Override
	public void store(final Binary data, final T instance, final long objectId, final PersistenceStoreHandler<Binary> handler)
	{
		data.storeEntityHeader(Binary.referenceBinaryLength(1), this.typeId(), objectId);
		
		Object mapInstance = XReflect.getFieldValue(map, instance);
		data.storeReference(0, handler, mapInstance);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public T create(final Binary data, final PersistenceLoadHandler handler)
	{
		return (T) Collections.newSetFromMap(new HashMap<Object,Boolean>());
	}

}
