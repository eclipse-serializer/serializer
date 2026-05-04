package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
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

/**
 * Combined object-id and type-id strategy: pairs a {@link PersistenceObjectIdStrategy} with a
 * {@link PersistenceTypeIdStrategy} into the single artifact stored in a persisted id-strategy string.
 * Defers every interface method to the corresponding sub-strategy, so callers can hand a
 * {@link PersistenceIdStrategy} wherever either single-strategy interface is expected.
 *
 * @see PersistenceObjectIdStrategy
 * @see PersistenceTypeIdStrategy
 * @see PersistenceIdStrategyStringConverter
 */
public interface PersistenceIdStrategy extends PersistenceObjectIdStrategy, PersistenceTypeIdStrategy
{
	/**
	 * The underlying object-id strategy.
	 *
	 * @return the object-id strategy.
	 */
	public PersistenceObjectIdStrategy objectIdStragegy();

	/**
	 * The underlying type-id strategy.
	 *
	 * @return the type-id strategy.
	 */
	public PersistenceTypeIdStrategy typeIdStragegy();
	
	@Override
	public default String strategyTypeNameObjectId()
	{
		return this.objectIdStragegy().strategyTypeNameObjectId();
	}
	
	@Override
	default String strategyTypeNameTypeId()
	{
		return this.typeIdStragegy().strategyTypeNameTypeId();
	}
	
	@Override
	public default PersistenceObjectIdProvider createObjectIdProvider()
	{
		return this.objectIdStragegy().createObjectIdProvider();
	}
	
	@Override
	public default PersistenceTypeIdProvider createTypeIdProvider()
	{
		return this.typeIdStragegy().createTypeIdProvider();
	}
				
	/**
	 * Creates a new {@link Default} combined strategy from the passed sub-strategies. Both must be
	 * non-{@code null}.
	 *
	 * @param objectIdStrategy the object-id strategy.
	 * @param typeIdStrategy   the type-id strategy.
	 *
	 * @return the newly created combined strategy.
	 */
	public static PersistenceIdStrategy New(
		final PersistenceObjectIdStrategy objectIdStrategy,
		final PersistenceTypeIdStrategy   typeIdStrategy
	)
	{
		return new PersistenceIdStrategy.Default(
			notNull(objectIdStrategy),
			notNull(typeIdStrategy)
		);
	}

	/**
	 * Default {@link PersistenceIdStrategy}: stores both sub-strategies as final fields and exposes them
	 * via the corresponding accessors.
	 */
	public class Default implements PersistenceIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceObjectIdStrategy objectIdStrategy;
		private final PersistenceTypeIdStrategy   typeIdStrategy  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceObjectIdStrategy objectIdStrategy,
			final PersistenceTypeIdStrategy   typeIdStrategy
		)
		{
			super();
			this.objectIdStrategy = objectIdStrategy;
			this.typeIdStrategy   = typeIdStrategy  ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceObjectIdStrategy objectIdStragegy()
		{
			return this.objectIdStrategy;
		}
		
		@Override
		public PersistenceTypeIdStrategy typeIdStragegy()
		{
			return this.typeIdStrategy;
		}
		
	}
	
}
