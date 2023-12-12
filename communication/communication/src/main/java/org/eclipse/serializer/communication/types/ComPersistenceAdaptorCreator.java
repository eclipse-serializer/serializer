package org.eclipse.serializer.communication.types;

/*-
 * #%L
 * Eclipse Serializer Communication Parent
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

import java.nio.ByteOrder;

import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.persistence.types.PersistenceIdStrategy;

/**
 * 
 * @param <C> the communication layer type
 */
@FunctionalInterface
public interface ComPersistenceAdaptorCreator<C>
{
	public ComPersistenceAdaptor<C> createPersistenceAdaptor(
		PersistenceIdStrategy  hostIdStrategyInitialization,
		XGettingEnum<Class<?>> entityTypes                 ,
		ByteOrder              hostByteOrder               ,
		PersistenceIdStrategy  hostIdStrategy
	);
	
	public default ComPersistenceAdaptor<C> createHostPersistenceAdaptor(
		final PersistenceIdStrategy  hostIdStrategyInitialization,
		final XGettingEnum<Class<?>> entityTypes                 ,
		final ByteOrder              hostByteOrder               ,
		final PersistenceIdStrategy  hostIdStrategy
	)
	{
		return this.createPersistenceAdaptor(
			notNull(hostIdStrategyInitialization),
			notNull(entityTypes)                 ,
			notNull(hostByteOrder),
			notNull(hostIdStrategy)
		);
	}
	
	public default ComPersistenceAdaptor<C> createClientPersistenceAdaptor()
	{
		return this.createPersistenceAdaptor(null, null, null, null);
	}
	
}
