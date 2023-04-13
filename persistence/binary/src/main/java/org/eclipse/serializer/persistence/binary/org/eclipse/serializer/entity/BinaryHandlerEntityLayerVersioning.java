package org.eclipse.serializer.persistence.binary.org.eclipse.serializer.entity;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
 * %%
 * Copyright (C) 2023 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.old.KeyValueFlatCollector;
import org.eclipse.serializer.entity.EntityLayerVersioning;
import org.eclipse.serializer.entity.EntityVersionContext;
import org.eclipse.serializer.persistence.binary.internal.AbstractBinaryHandlerCustom;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryTypeHandler;
import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceInstantiator;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public class BinaryHandlerEntityLayerVersioning
	extends AbstractBinaryHandlerCustom<EntityLayerVersioning<?>>
	implements BinaryHandlerEntityLoading<EntityLayerVersioning<?>>
{
	public static BinaryHandlerEntityLayerVersioning New(
		final EntityTypeHandlerManager entityTypeHandlerManager
	)
	{
		return new BinaryHandlerEntityLayerVersioning(
			notNull(entityTypeHandlerManager)
		);
	}
	
	
	static final long BINARY_OFFSET_CONTEXT  =                                                   0;
	static final long BINARY_OFFSET_VERSIONS = BINARY_OFFSET_CONTEXT + Binary.objectIdByteLength();

	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<EntityLayerVersioning<?>> handledType()
	{
		return (Class)EntityLayerVersioning.class; // no idea how to get ".class" to work otherwise
	}
	
	
	final EntityTypeHandlerManager entityTypeHandlerManager;
	
	BinaryHandlerEntityLayerVersioning(
		final EntityTypeHandlerManager entityTypeHandlerManager
	)
	{
		super(
			handledType(),
			CustomFields(
				CustomField(EntityVersionContext.class, "context"),
				Complex("versions",
					CustomField(Object.class, "version"),
					CustomField(Object.class, "entity")
				)
			)
		);
		this.entityTypeHandlerManager = entityTypeHandlerManager;
	}
	
	@Override
	public final EntityLayerVersioning<?> create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		return PersistenceInstantiator.instantiateBlank(EntityLayerVersioning.class);
	}

	@Override
	public final void updateState(
		final Binary                   data    ,
		final EntityLayerVersioning<?> instance,
		final PersistenceLoadHandler   handler
	)
	{
		EntityInternals.setContext(
			instance,
			(EntityVersionContext<?>)handler.lookupObject(data.read_long(BINARY_OFFSET_CONTEXT))
		);
		
		final int elementCount = X.checkArrayRange(data.getListElementCountKeyValue(BINARY_OFFSET_VERSIONS));
		final KeyValueFlatCollector<Object, Object> collector = KeyValueFlatCollector.New(elementCount);
		data.collectKeyValueReferences(BINARY_OFFSET_VERSIONS, elementCount, handler, collector);
		data.registerHelper(instance, collector.yield());
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void complete(
		final Binary                   data    ,
		final EntityLayerVersioning    instance,
		final PersistenceLoadHandler   handler
	)
	{
		final Object[] elements = (Object[])data.getHelper(instance);
		final EqHashTable versions = EqHashTable.New(EntityInternals.getContext(instance).equalator());
		for(int i = 0; i < elements.length; )
		{
			versions.put(elements[i++], elements[i++]);
		}
		
		EntityInternals.setVersions(instance, versions);
	}
	
	@Override
	public void iterateInstanceReferences(
		final EntityLayerVersioning<?> instance,
		final PersistenceFunction      iterator
	)
	{
		iterator.apply(EntityInternals.getContext(instance));
		Persistence.iterateReferences(iterator, EntityInternals.getVersions(instance));
	}

	@Override
	public final void iterateLoadableReferences(
		final Binary                     data    ,
		final PersistenceReferenceLoader iterator
	)
	{
		iterator.acceptObjectId(data.read_long(BINARY_OFFSET_CONTEXT));
		data.iterateKeyValueEntriesReferences(BINARY_OFFSET_VERSIONS, iterator);
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}
	
	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return true;
	}
		
	@Override
	public void store(
		final Binary                          data    ,
		final EntityLayerVersioning<?>        instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		BinaryHandlerEntityLoading.super.store(data, instance, objectId, handler);
	}
	
	@Override
	public BinaryTypeHandler<EntityLayerVersioning<?>> createStoringEntityHandler()
	{
		final Storing storing = new Storing(this.entityTypeHandlerManager);
		storing.initialize(this.typeId());
		return storing;
	}
	
	
	static class Storing extends BinaryHandlerEntityLayerVersioning
	{
		Storing(
			final EntityTypeHandlerManager entityTypeHandlerManager
		)
		{
			super(entityTypeHandlerManager);
		}
	
		@Override
		public void store(
			final Binary                          data    ,
			final EntityLayerVersioning<?>        instance,
			final long                            objectId,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			final EqHashTable<?, ?> versions = EntityInternals.getVersions(instance);
			data.storeKeyValuesAsEntries(
				this.typeId()           ,
				objectId                ,
				BINARY_OFFSET_VERSIONS  ,
				versions                ,
				versions.size()         ,
				EntityPersister.New(this.entityTypeHandlerManager, handler)
			);
			
			data.store_long(
				BINARY_OFFSET_CONTEXT,
				handler.applyEager(EntityInternals.getContext(instance))
			);
		}
		
	}
	
}
