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
 * Callback hook into {@link PersistenceObjectManager#ensureObjectId} that is notified whenever an instance
 * receives or is about to receive an object id. Storers use it to chain their own per-instance bookkeeping
 * (e.g. enqueuing the instance for the next storage iteration) onto the id-assignment step without having to
 * inspect the registry afterwards.
 * <p>
 * The callbacks model the storage strategies the caller may want to express. {@code registerGuaranteed}
 * always fires &mdash; the storer must process the instance unconditionally. {@code registerLazyOptional} and
 * {@code registerEagerOptional} are dispatched only by the lazy and eager storer implementations
 * respectively, so a storer can declare its intent by which method it overrides and use the {@link NoOp}
 * default for the others. {@code registerSkippedOptional} fires when an instance is already globally known
 * and therefore skipped &mdash; only its object id gets referenced, no serialization happens.
 * <p>
 * For visitors that only need the bare object id (without instance or handler), implement
 * {@link PersistenceObjectIdAcceptor} instead.
 *
 * @param <D> the persistence data type passed through to the {@link PersistenceTypeHandler}.
 *
 * @see PersistenceObjectIdAcceptor
 * @see PersistenceObjectManager
 */
public interface PersistenceObjectIdRequestor<D>
{
	/**
	 * Mandatory hook fired whenever a guaranteed registration for the passed instance happens. Storers must
	 * react to this so that the instance is reliably scheduled for serialization.
	 *
	 * @param <T>             the instance type.
	 * @param objectId        the object id assigned to the instance.
	 * @param instance        the registered instance.
	 * @param optionalHandler the type handler responsible for {@code instance}, or {@code null} if not yet known.
	 */
	public <T> void registerGuaranteed(
		long                         objectId       ,
		T                            instance       ,
		PersistenceTypeHandler<D, T> optionalHandler
	);

	/**
	 * Optional hook fired only for lazy storer implementations &mdash; eager storers and {@link NoOp} treat it
	 * as a no-op. Lazy storers can use this to register the instance for deferred storage without forcing the
	 * unconditional behavior of {@link #registerGuaranteed}.
	 *
	 * @param <T>             the instance type.
	 * @param objectId        the object id assigned to the instance.
	 * @param instance        the registered instance.
	 * @param optionalHandler the type handler responsible for {@code instance}, or {@code null} if not yet known.
	 */
	public <T> void registerLazyOptional(
		long                         objectId       ,
		T                            instance       ,
		PersistenceTypeHandler<D, T> optionalHandler
	);

	/**
	 * Optional hook fired only for eager storer implementations &mdash; lazy storers and {@link NoOp} treat it
	 * as a no-op. Eager storers can use this to schedule the instance for immediate storage without forcing
	 * the unconditional behavior of {@link #registerGuaranteed}.
	 *
	 * @param <T>             the instance type.
	 * @param objectId        the object id assigned to the instance.
	 * @param instance        the registered instance.
	 * @param optionalHandler the type handler responsible for {@code instance}, or {@code null} if not yet known.
	 */
	public <T> void registerEagerOptional(
		long                         objectId       ,
		T                            instance       ,
		PersistenceTypeHandler<D, T> optionalHandler
	);

	/**
	 * Optional hook fired when the passed instance is already globally known (registered in the object
	 * registry) and is therefore skipped by lazy storing logic: only its object id gets referenced, the
	 * instance itself is not serialized. Default is a no-op.
	 * <p>
	 * Storers can use this to retain a strong reference to the skipped instance until commit. Without it,
	 * the application may drop its last strong reference to the instance between store and commit; the
	 * instance's object registry entry gets reaped and the storage garbage collector can delete the
	 * referenced entity while the chunk referencing it is not yet committed, so the commit would persist
	 * a dangling reference (missing entity on later loads).
	 * <p>
	 * Storers may additionally record the object id to have its existence validated by the persistence
	 * target before the data referencing it is committed (see the trusted-id validation).
	 *
	 * @param <T>             the instance type.
	 * @param objectId        the object id the instance is already registered with.
	 * @param instance        the skipped instance.
	 * @param optionalHandler the type handler responsible for {@code instance}, or {@code null} if not known.
	 */
	public default <T> void registerSkippedOptional(
		final long                         objectId       ,
		final T                            instance       ,
		final PersistenceTypeHandler<D, T> optionalHandler
	)
	{
		// no-op by default
	}



	/**
	 * Returns a {@link PersistenceObjectIdRequestor} whose callbacks are all no-ops. Used by callers that need
	 * a non-{@code null} requestor but have nothing to do per registration.
	 *
	 * @param <D> the persistence data type.
	 *
	 * @return the no-op requestor.
	 */
	public static <D> PersistenceObjectIdRequestor<D> NoOp()
	{
		return new PersistenceObjectIdRequestor.NoOp<>();
	}

	/**
	 * No-op {@link PersistenceObjectIdRequestor} implementation: every callback returns immediately without
	 * side effects. Stateless and freely shareable.
	 *
	 * @param <D> the persistence data type.
	 */
	public final class NoOp<D> implements PersistenceObjectIdRequestor<D>
	{

		@Override
		public <T> void registerGuaranteed(
			final long                         objectId       ,
			final T                            instance       ,
			final PersistenceTypeHandler<D, T> optionalHandler
		)
		{
			// no-op
		}

		@Override
		public <T> void registerLazyOptional(
			final long                         objectId       ,
			final T                            instance       ,
			final PersistenceTypeHandler<D, T> optionalHandler
		)
		{
			// no-op
		}

		@Override
		public <T> void registerEagerOptional(
			final long                         objectId       ,
			final T                            instance       ,
			final PersistenceTypeHandler<D, T> optionalHandler
		)
		{
			// no-op
		}
		
	}
	
}
