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

import org.eclipse.serializer.util.Cloneable;

/**
 * Generator and writable holder of object ids. Extends {@link PersistenceObjectIdHolder} (which exposes and
 * overrides the current highest assigned object id) with the operations needed to advance it and to clone
 * the provider for context-dispatched setups (see {@link PersistenceContextDispatcher}).
 * <p>
 * A provider is the runtime counterpart of a {@link PersistenceObjectIdStrategy}: the strategy describes
 * <em>how</em> object ids are generated (and is itself persistable as part of the id-strategy string), while
 * the provider is the live, mutable instance created from it via
 * {@link PersistenceObjectIdStrategy#createObjectIdProvider()} and consumed by {@link PersistenceObjectManager}.
 * <p>
 * Two ready-made providers are bundled:
 * <ul>
 * <li>{@link Transient} &mdash; in-memory counter that does not persist its state across runs. Suitable for
 * one-shot serialization where every run is allowed to re-assign object ids from
 * {@link Persistence#defaultStartObjectId()}.</li>
 * <li>{@link Failing} &mdash; refuses to hand out new ids, used together with
 * {@link PersistenceObjectIdStrategy.None} for read-only setups where every needed object must already be
 * registered.</li>
 * </ul>
 *
 * @see PersistenceObjectIdHolder
 * @see PersistenceObjectIdStrategy
 * @see PersistenceObjectManager
 * @see PersistenceTypeIdProvider
 */
public interface PersistenceObjectIdProvider
extends PersistenceObjectIdHolder, Cloneable<PersistenceObjectIdProvider>
{
	/**
	 * Generates and returns the next object id, advancing the highest assigned object id so that subsequent
	 * calls to {@link #currentObjectId()} reflect the new value.
	 *
	 * @return the newly generated object id.
	 */
	public long provideNextObjectId();

	/**
	 * Initializes the provider's internal state, performing whatever lookup or restore step the implementation
	 * needs before {@link #provideNextObjectId()} is called for the first time. For purely in-memory providers
	 * this is a no-op.
	 *
	 * @return this provider, for fluent chaining.
	 */
	public PersistenceObjectIdProvider initializeObjectId();

	@Override
	public long currentObjectId();

	@Override
	public PersistenceObjectIdProvider updateCurrentObjectId(long currentObjectId);

	/**
	 * Useful for {@link PersistenceContextDispatcher}.
	 * @return A Clone of this instance as described in {@link Cloneable}.
	 */
	@Override
	public default PersistenceObjectIdProvider Clone()
	{
		return Cloneable.super.Clone();
	}


	/**
	 * Creates a new {@link Transient} provider starting at {@link Persistence#defaultStartObjectId()}, which
	 * places the object-id range above the type-id range so the two never overlap.
	 *
	 * @return the new transient provider.
	 */
	public static PersistenceObjectIdProvider Transient()
	{
		return new Transient(Persistence.defaultStartObjectId());
	}

	/**
	 * Creates a new {@link Transient} provider whose first call to {@link #provideNextObjectId()} returns
	 * {@code startingObjectId + 1}. The starting value is validated via
	 * {@link Persistence#validateObjectId(long)}.
	 *
	 * @param startingObjectId the starting value; the next handed-out id is one above this.
	 *
	 * @return the new transient provider.
	 */
	public static PersistenceObjectIdProvider Transient(final long startingObjectId)
	{
		return new Transient(Persistence.validateObjectId(startingObjectId));
	}

	/**
	 * In-memory {@link PersistenceObjectIdProvider}. Generates object ids by incrementing an internal counter
	 * and does not persist its state &mdash; on the next run, ids start over at the configured starting value.
	 * All mutating operations are synchronized on the instance, so a single provider may be shared by
	 * concurrent serialization threads.
	 */
	public final class Transient implements PersistenceObjectIdProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private long currentObjectId;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Transient(final long startingObjectId)
		{
			super();
			this.currentObjectId = startingObjectId;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////
		
		@Override
		public final synchronized PersistenceObjectIdProvider.Transient Clone()
		{
			return new PersistenceObjectIdProvider.Transient(this.currentObjectId);
		}

		@Override
		public final synchronized long provideNextObjectId()
		{
			return ++this.currentObjectId;
		}

		@Override
		public final synchronized long currentObjectId()
		{
			return this.currentObjectId;
		}

		@Override
		public final Transient initializeObjectId()
		{
			return this;
		}

		@Override
		public final synchronized PersistenceObjectIdProvider updateCurrentObjectId(final long currentObjectId)
		{
			this.currentObjectId = currentObjectId;
			return this;
		}

	}
	
	/**
	 * Creates a new {@link Failing} provider that refuses to generate new object ids.
	 *
	 * @return the new failing provider.
	 */
	public static PersistenceObjectIdProvider.Failing Failing()
	{
		return new PersistenceObjectIdProvider.Failing();
	}

	/**
	 * {@link PersistenceObjectIdProvider} that throws {@link UnsupportedOperationException} from
	 * {@link #provideNextObjectId()}, used together with {@link PersistenceObjectIdStrategy.None} for
	 * read-only setups where every needed object must already be registered. The current-id getter and setter
	 * still work so that the highest assigned object id can be restored from persistent state for inspection.
	 */
	public final class Failing implements PersistenceObjectIdProvider
	{

		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private long currentObjectId;
		
		
		
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
		public PersistenceObjectIdProvider.Failing Clone()
		{
			return new PersistenceObjectIdProvider.Failing();
		}

		@Override
		public PersistenceObjectIdProvider.Failing initializeObjectId()
		{
			// no-op, nothing to initialize
			return this;
		}

		@Override
		public long currentObjectId()
		{
			return this.currentObjectId;
		}

		@Override
		public PersistenceObjectIdProvider.Failing updateCurrentObjectId(final long currentObjectId)
		{
			this.currentObjectId = currentObjectId;
			return this;
		}

		@Override
		public long provideNextObjectId()
		{
			throw new UnsupportedOperationException();
		}
	}
	
}
