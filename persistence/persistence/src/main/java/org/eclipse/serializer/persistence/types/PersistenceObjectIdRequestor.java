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
 * The three callbacks model the storage strategies the caller may want to express. {@code registerGuaranteed}
 * always fires &mdash; the storer must process the instance unconditionally. {@code registerLazyOptional} and
 * {@code registerEagerOptional} are dispatched only by the lazy and eager storer implementations
 * respectively, so a storer can declare its intent by which method it overrides and use the {@link NoOp}
 * default for the others.
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
