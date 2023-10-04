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

import org.eclipse.serializer.collections.MiniMap;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerManager;
import org.eclipse.serializer.reference.Referencing;

@FunctionalInterface
public interface EntityTypeHandlerManager
{
	public <T> PersistenceTypeHandler<Binary, T> ensureInternalEntityTypeHandler(
		T instance
	);
	
	
	public static EntityTypeHandlerManager New(
		final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager
	)
	{
		return new Default(
			notNull(typeHandlerManager)
		);
	}
	
	
	public static class Default implements EntityTypeHandlerManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Referencing<PersistenceTypeHandlerManager<Binary>>   typeHandlerManager                 ;
		private final MiniMap<Class<?>, PersistenceTypeHandler<Binary, ?>> internalHandlers  = new MiniMap<>();
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
	
		Default(final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager)
		{
			super();
			this.typeHandlerManager = typeHandlerManager;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		@SuppressWarnings({"unchecked"}) // generics safety guaranteed by registration logic
		public <T> PersistenceTypeHandler<Binary, T> ensureInternalEntityTypeHandler(
			final T instance
		)
		{
			final Class<?> type = instance.getClass();
			
			PersistenceTypeHandler<Binary, ?> handler;
			synchronized(this.internalHandlers)
			{
				if((handler = this.internalHandlers.get(type)) == null)
				{
					handler = this.typeHandlerManager.get().ensureTypeHandler(type);
					if(handler instanceof BinaryHandlerEntityLoading)
					{
						handler = ((BinaryHandlerEntityLoading<?>)handler).createStoringEntityHandler();
						this.internalHandlers.put(type, handler);
					}
				}
			}
			
			return (PersistenceTypeHandler<Binary, T>)handler;
		}
	
	}
	
}
