
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

public interface EntityLogger extends EntityLayerProviderProvider
{
	public default void entityCreated(final Entity identity, final Entity data)
	{
		// empty by default
	}
	
	public default void afterRead(final Entity identity, final Entity data)
	{
		// empty by default
	}
	
	public default void beforeUpdate(final Entity identity, final Entity data)
	{
		// empty by default
	}
	
	public default void afterUpdate(final Entity identity, final Entity data, final boolean successful)
	{
		// empty by default
	}
	
	@Override
	public default EntityLayerProvider provideEntityLayerProvider()
	{
		return e -> new EntityLayerLogging(e, this);
	}
}
