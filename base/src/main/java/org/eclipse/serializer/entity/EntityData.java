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

/**
 * Immutable entity data layer.
 * <p>
 * FH
 */
public abstract class EntityData extends Entity.AbstractAccessible
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Entity entity;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected EntityData(final Entity entity)
	{
		super();
		this.entity = Entity.identity(entity);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	protected final Entity entityIdentity()
	{
		return this.entity;
	}
	
	@Override
	protected final Entity entityData()
	{
		return this;
	}
	
	@Override
	protected final void entityCreated()
	{
		// nothing to do here
	}
		
	@Override
	protected final boolean updateEntityData(final Entity newData)
	{
		// updating an entity's data means to replace the data instance, not mutate it. Data itself is immutable.
		return false;
	}
}
