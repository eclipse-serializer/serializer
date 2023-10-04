
package org.eclipse.serializer.entity;

/*-
 * #%L
 * Eclipse Serializer Base
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

import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.types.XGettingTable;

public final class EntityLayerVersioning<K> extends EntityLayer
{
	EntityVersionContext<K> context ;
	EqHashTable<K, Entity>  versions;
	
	protected EntityLayerVersioning(
		final Entity                  inner  ,
		final EntityVersionContext<K> context
	)
	{
		super(inner);
		
		this.context  = notNull(context);
		this.versions = EqHashTable.New(context.equalator());
	}
	
	synchronized XGettingTable<K, Entity> versions()
	{
		return this.versions.immure();
	}
	
	@Override
	protected synchronized Entity entityData()
	{
		final K versionKey = this.context.currentVersion();
		if(versionKey == null)
		{
			return super.entityData();
		}
		
		final Entity versionedData = this.versions.get(versionKey);
		if(versionedData == null)
		{
			throw new EntityExceptionMissingDataForVersion(this.entityIdentity(), versionKey);
		}
		
		return versionedData;
	}
	
	@Override
	protected synchronized void entityCreated()
	{
		final K versionKey = this.context.versionForUpdate();
		if(versionKey != null)
		{
			this.versions.put(versionKey, super.entityData());
		}
		
		super.entityCreated();
	}
	
	@Override
	protected synchronized boolean updateEntityData(final Entity data)
	{
		final K versionKey = this.context.versionForUpdate();
		if(versionKey != null)
		{
			this.versions.put(versionKey, data);
			
			EntityVersionCleaner<K> cleaner;
			if((cleaner = this.context.cleaner()) != null)
			{
				cleaner.cleanVersions(this.versions);
			}
		}
		
		return super.updateEntityData(data);
	}
	
}
