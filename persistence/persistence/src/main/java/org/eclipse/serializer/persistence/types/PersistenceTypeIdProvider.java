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

/**
 * Generator and writable holder of type ids. Extends {@link PersistenceTypeIdHolder} (which exposes the current
 * watermark) with the operations needed to advance, initialize and override that watermark when types are first
 * encountered or when the dictionary is restored from persistent state.
 * <p>
 * A provider is the runtime counterpart of a {@link PersistenceTypeIdStrategy}: the strategy describes <em>how</em>
 * type ids are generated (and is itself persistable as part of the id-strategy string), while the provider is the
 * live, mutable instance created from it via {@link PersistenceTypeIdStrategy#createTypeIdProvider()} and consumed by
 * {@link PersistenceTypeManager}.
 * <p>
 * Two ready-made providers are bundled:
 * <ul>
 * <li>{@link Transient} &mdash; in-memory counter that does not persist its state across runs. Suitable for one-shot
 * serialization where every run is allowed to re-assign type ids from {@link Persistence#defaultStartTypeId()}.</li>
 * <li>{@link Failing} &mdash; refuses to hand out new ids, used together with {@link PersistenceTypeIdStrategy.None}
 * for read-only setups where the dictionary must already contain every needed type.</li>
 * </ul>
 *
 * @see PersistenceTypeIdHolder
 * @see PersistenceTypeIdStrategy
 * @see PersistenceTypeManager
 * @see PersistenceObjectIdProvider
 */
public interface PersistenceTypeIdProvider extends PersistenceTypeIdHolder
{
	/**
	 * Generates and returns the next type id, advancing the internal watermark so that subsequent calls to
	 * {@link #currentTypeId()} reflect the new value.
	 *
	 * @return the newly generated type id.
	 */
	public long provideNextTypeId();

	/**
	 * Initializes the provider's internal state, performing whatever lookup or restore step the implementation needs
	 * before {@link #provideNextTypeId()} is called for the first time. For purely in-memory providers this is a
	 * no-op.
	 *
	 * @return this provider, for fluent chaining.
	 */
	public PersistenceTypeIdProvider initializeTypeId();

	/**
	 * Overrides the current type id watermark, typically when the dictionary is restored from persistent state and
	 * the provider must be advanced past every id already in use.
	 *
	 * @param currentTypeId the new highest type id.
	 *
	 * @return this provider, for fluent chaining.
	 */
	public PersistenceTypeIdProvider updateCurrentTypeId(long currentTypeId);



	/**
	 * Creates a new {@link Transient} provider starting at {@link Persistence#defaultStartTypeId()}, leaving the
	 * range below that value reserved for the JDK's native types.
	 *
	 * @return the new transient provider.
	 */
	public static PersistenceTypeIdProvider Transient()
	{
		return new Transient(Persistence.defaultStartTypeId());
	}

	/**
	 * Creates a new {@link Transient} provider whose first call to {@link #provideNextTypeId()} returns
	 * {@code startingTypeId + 1}. The starting value is validated via {@link Persistence#validateTypeId(long)}.
	 *
	 * @param startingTypeId the watermark to start from; the next handed-out id is one above this.
	 *
	 * @return the new transient provider.
	 */
	public static PersistenceTypeIdProvider Transient(final long startingTypeId)
	{
		return new Transient(Persistence.validateTypeId(startingTypeId));
	}

	/**
	 * In-memory {@link PersistenceTypeIdProvider}. Generates type ids by incrementing an internal counter and does
	 * not persist its state &mdash; on the next run, ids start over at the configured starting value. All mutating
	 * operations are synchronized on the instance, so a single provider may be shared by concurrent serialization
	 * threads.
	 */
	public final class Transient implements PersistenceTypeIdProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private long currentTypeId;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Transient(final long startingTypeId)
		{
			super();
			this.currentTypeId = startingTypeId;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final synchronized long provideNextTypeId()
		{
			return ++this.currentTypeId;
		}

		@Override
		public final synchronized long currentTypeId()
		{
			return this.currentTypeId;
		}

		@Override
		public final Transient initializeTypeId()
		{
			return this;
		}

		@Override
		public final synchronized PersistenceTypeIdProvider updateCurrentTypeId(final long currentTypeId)
		{
			this.currentTypeId = currentTypeId;
			return this;
		}

	}
	
	/**
	 * Creates a new {@link Failing} provider that refuses to generate new type ids.
	 *
	 * @return the new failing provider.
	 */
	public static PersistenceTypeIdProvider.Failing Failing()
	{
		return new PersistenceTypeIdProvider.Failing();
	}

	/**
	 * {@link PersistenceTypeIdProvider} that throws {@link UnsupportedOperationException} from
	 * {@link #provideNextTypeId()}, used together with {@link PersistenceTypeIdStrategy.None} for read-only setups
	 * where every type must already be present in the dictionary. The current-id getter and setter still work so
	 * that the watermark can be restored from persistent state for inspection.
	 */
	public final class Failing implements PersistenceTypeIdProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private long currentTypeId;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Failing()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceTypeIdProvider.Failing initializeTypeId()
		{
			// no-op, nothing to initialize
			return this;
		}

		@Override
		public long currentTypeId()
		{
			return this.currentTypeId;
		}

		@Override
		public PersistenceTypeIdProvider.Failing updateCurrentTypeId(final long currentTypeId)
		{
			this.currentTypeId = currentTypeId;
			return this;
		}

		@Override
		public long provideNextTypeId()
		{
			throw new UnsupportedOperationException();
		}
		
	}
	
}
