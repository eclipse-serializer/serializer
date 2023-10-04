package org.eclipse.serializer.persistence.binary.org.eclipse.serializer.entity;

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

import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public interface EntityPersister extends PersistenceFunction
{
	public static EntityPersister New(
		final EntityTypeHandlerManager        entityTypeHandlerManager,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		return new Default(
			notNull(entityTypeHandlerManager),
			notNull(handler)
		);
	}
	
	
	public static class Default implements EntityPersister
	{
		private final EntityTypeHandlerManager        entityTypeHandlerManager;
		private final PersistenceStoreHandler<Binary> handler                 ;
		
		Default(
			final EntityTypeHandlerManager        entityTypeHandlerManager,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			super();
			this.entityTypeHandlerManager = entityTypeHandlerManager;
			this.handler                  = handler                 ;
		}

		@Override
		public <T> long apply(final T instance)
		{
			return this.handler.applyEager(
				instance,
				this.entityTypeHandlerManager.ensureInternalEntityTypeHandler(instance)
			);
		}
		
	}
	
}
