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
 * Type-bound instance factory used by type handlers during loading: produces a fresh, empty {@code T} into
 * which the loader will write the persisted state. Each handler gets its own instantiator pinned to its
 * concrete type, hiding the generic {@link PersistenceInstantiator} dispatch behind a single-argument call.
 *
 * @param <D> the persistence data type passed to {@link #instantiate(Object)}.
 * @param <T> the instantiated type.
 *
 * @see PersistenceInstantiator
 * @see PersistenceTypeInstantiatorProvider
 */
@FunctionalInterface
public interface PersistenceTypeInstantiator<D, T>
{
	/**
	 * Creates a new instance of the bound type. Some implementations consult {@code data} to pick a
	 * subtype-specific construction strategy; the bundled {@link Default} ignores it and produces a blank
	 * instance.
	 *
	 * @param data the persistence data, supplied for context-sensitive instantiation.
	 *
	 * @return the newly created instance.
	 */
	public T instantiate(D data);



	/**
	 * Creates a new {@link Default} instantiator pinned to {@code type}, backed by a fresh
	 * {@link PersistenceInstantiator}.
	 *
	 * @param <T>  the instantiated type.
	 * @param <D>  the persistence data type.
	 * @param type the type to pin to.
	 *
	 * @return the newly created instantiator.
	 */
	public static <T, D> PersistenceTypeInstantiator<D, T> New(final Class<T> type)
	{
		return New(type, PersistenceInstantiator.New());
	}

	/**
	 * Creates a new {@link Default} instantiator pinned to {@code type}, backed by the passed universal
	 * instantiator.
	 *
	 * @param <T>                   the instantiated type.
	 * @param <D>                   the persistence data type.
	 * @param type                  the type to pin to; must not be {@code null}.
	 * @param universalInstantiator the universal instantiator that performs the actual construction; must
	 *                              not be {@code null}.
	 *
	 * @return the newly created instantiator.
	 */
	public static <T, D> PersistenceTypeInstantiator<D, T> New(
		final Class<T>                   type                 ,
		final PersistenceInstantiator<D> universalInstantiator
	)
	{
		return new PersistenceTypeInstantiator.Default<>(
			notNull(type),
			notNull(universalInstantiator)
		);
	}

	/**
	 * Default {@link PersistenceTypeInstantiator}: stores the pinned {@link Class} and delegates each
	 * call to the universal {@link PersistenceInstantiator}.
	 *
	 * @param <D> the persistence data type.
	 * @param <T> the instantiated type.
	 */
	public final class Default<D, T> implements PersistenceTypeInstantiator<D, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<T>                   type                 ;
		private final PersistenceInstantiator<D> universalInstantiator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Class<T>                   type                 ,
			final PersistenceInstantiator<D> universalInstantiator
		)
		{
			super();
			this.type                  = type                 ;
			this.universalInstantiator = universalInstantiator;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public T instantiate(final D data)
		{
			return this.universalInstantiator.instantiate(this.type, data);
		}
		
	}
	
}
