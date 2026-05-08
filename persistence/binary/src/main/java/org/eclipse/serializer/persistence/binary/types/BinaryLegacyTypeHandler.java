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
import org.eclipse.serializer.persistence.types.PersistenceLegacyTypeHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;

/**
 * Binary-specific specialization of {@link PersistenceLegacyTypeHandler} that handles loading of entities
 * persisted under an outdated type definition. Combines the generic legacy type handler contract with the
 * {@link BinaryTypeHandler} contract so legacy handlers can be plugged into the binary persistence layer
 * wherever a regular binary type handler is expected.
 * <p>
 * Legacy type handlers are read-only by design: they reconstruct instances from the persisted (legacy)
 * binary layout but never store new data &mdash; storing is always delegated to the current type handler.
 * The default {@link #store} implementation therefore inherits the parent's no-op-or-fail behavior.
 *
 * @param <T> the runtime type produced by this handler.
 *
 * @see PersistenceLegacyTypeHandler
 * @see BinaryTypeHandler
 */
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



	/**
	 * Skeletal base for {@link BinaryLegacyTypeHandler} implementations that work directly off a
	 * {@link PersistenceTypeDefinition} and do not need the full {@link AbstractBinaryHandlerCustom}
	 * machinery. Used by the translating handler hierarchy (rerouting, generic type, generic enum).
	 *
	 * @param <T> the runtime type produced by this handler.
	 */
	public abstract class Abstract<T>
	extends PersistenceLegacyTypeHandler.Abstract<Binary, T>
	implements BinaryLegacyTypeHandler<T>
	{
		/**
		 * @param typeDefinition the legacy type definition this handler reads from.
		 */
		protected Abstract(final PersistenceTypeDefinition typeDefinition)
		{
			super(typeDefinition);
		}

	}

	/**
	 * Skeletal base for hand-written legacy {@link BinaryLegacyTypeHandler} implementations that reuse the
	 * {@link AbstractBinaryHandlerCustom} infrastructure (member declarations, helper methods, etc.). Used
	 * when a custom built-in handler needs a tailored legacy variant rather than relying on generic
	 * translation.
	 *
	 * @param <T> the runtime type produced by this handler.
	 */
	public abstract class AbstractCustom<T>
	extends AbstractBinaryHandlerCustom<T>
	implements BinaryLegacyTypeHandler<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		 * @param type    the runtime type produced by this handler.
		 * @param members the legacy member descriptions in their persisted order.
		 */
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
