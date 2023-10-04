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
 * Entity identity layer. This is the outer shell of a entity layer chain.
 * <p>
 * FH
 */
public abstract class EntityLayerIdentity extends EntityLayer
{
	protected EntityLayerIdentity()
	{
		super(null);
	}
	
	@Override
	protected final Entity entityIdentity()
	{
		return this;
	}
	
	@Override
	protected boolean updateEntityData(final Entity newData)
	{
		// the passed data instance must be validated before it gets passed to any other layer logic.
		this.validateNewData(Entity.data(newData));
		return super.updateEntityData(newData);
	}
}
