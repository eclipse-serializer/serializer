package org.eclipse.serializer.persistence.binary.types;

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

import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.persistence.binary.internal.AbstractBinaryHandlerCustom;
import org.eclipse.serializer.persistence.types.PersistenceLegacyTypeHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;

public interface BinaryLegacyTypeHandler<T> extends PersistenceLegacyTypeHandler<Binary, T>, BinaryTypeHandler<T>
{
	@Override
	public default BinaryLegacyTypeHandler<T> initialize(final long typeId)
	{
		PersistenceLegacyTypeHandler.super.initialize(typeId);
		return this;
	}
		
	@Override
	public default void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		PersistenceLegacyTypeHandler.super.store(data, instance, objectId, handler);
	}
	
	
	
	public abstract class Abstract<T>
	extends PersistenceLegacyTypeHandler.Abstract<Binary, T>
	implements BinaryLegacyTypeHandler<T>
	{
		protected Abstract(final PersistenceTypeDefinition typeDefinition)
		{
			super(typeDefinition);
		}
		
	}
	
	public abstract class AbstractCustom<T>
	extends AbstractBinaryHandlerCustom<T>
	implements BinaryLegacyTypeHandler<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected AbstractCustom(
			final Class<T>                                                    type   ,
			final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
		)
		{
			super(type, members);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public synchronized BinaryLegacyTypeHandler.AbstractCustom<T> initialize(final long typeId)
		{
			super.initialize(typeId);
			return this;
		}
		
		@Override
		public void store(
			final Binary                          data    ,
			final T                               instance,
			final long                            objectId,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			BinaryLegacyTypeHandler.super.store(data, instance, objectId, handler);
		}
		
	}
	
}
