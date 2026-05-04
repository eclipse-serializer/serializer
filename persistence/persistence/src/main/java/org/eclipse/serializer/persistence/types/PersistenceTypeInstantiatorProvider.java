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

import org.eclipse.serializer.collections.ConstHashTable;
import org.eclipse.serializer.collections.types.XGettingMap;

/**
 * Supplies a {@link PersistenceTypeInstantiator} for any requested {@link Class}. Type handlers consult their
 * provider once at construction time to obtain the per-type instantiator they will use during loading.
 * <p>
 * Two implementations are bundled:
 * <ul>
 * <li>{@link Default} &mdash; falls back to a single universal {@link PersistenceInstantiator} for every
 * type.</li>
 * <li>{@link Mapped} &mdash; consults a class-keyed map first and only falls back to the universal
 * instantiator when no entry exists. Use this to register custom construction strategies for specific types.</li>
 * </ul>
 *
 * @param <D> the persistence data type passed through to the {@link PersistenceTypeInstantiator}.
 *
 * @see PersistenceTypeInstantiator
 * @see PersistenceInstantiator
 */
public interface PersistenceTypeInstantiatorProvider<D>
{
	/**
	 * Returns the instantiator that should be used to create instances of the passed type.
	 *
	 * @param <T>  the requested type.
	 * @param type the runtime type.
	 *
	 * @return the instantiator for {@code type}.
	 */
	public <T> PersistenceTypeInstantiator<D, T> provideTypeInstantiator(Class<T> type);

	/**
	 * Returns the default provider, which delegates to a fresh {@link PersistenceInstantiator.Default} for
	 * every request.
	 *
	 * @param <D> the persistence data type.
	 *
	 * @return the default provider.
	 */
	public static <D> PersistenceTypeInstantiatorProvider<D> Provider()
	{
		return new PersistenceInstantiator.Default<>();
	}

	/**
	 * Creates a new {@link Default} provider that uses the passed universal instantiator for every type.
	 *
	 * @param <D>          the persistence data type.
	 * @param instantiator the universal instantiator; must not be {@code null}.
	 *
	 * @return the newly created provider.
	 */
	public static <D> PersistenceTypeInstantiatorProvider<D> New(
		final PersistenceInstantiator<D> instantiator
	)
	{
		return new PersistenceTypeInstantiatorProvider.Default<>(
			notNull(instantiator)
		);
	}

	/**
	 * Creates a new provider that consults {@code instantiatorMapping} first and falls back to
	 * {@code instantiator} for unmapped types. Returns a plain {@link Default} when the mapping is empty.
	 *
	 * @param <D>                 the persistence data type.
	 * @param instantiatorMapping the per-type override mapping.
	 * @param instantiator        the universal fallback instantiator; must not be {@code null}.
	 *
	 * @return the newly created provider.
	 */
	public static <D> PersistenceTypeInstantiatorProvider<D> New(
		final XGettingMap<Class<?>, PersistenceTypeInstantiator<D, ?>> instantiatorMapping,
		final PersistenceInstantiator<D>                               instantiator
	)
	{
		// there must always be a universal instantiator. Even it's just a dummy throwing an exception.
		return instantiatorMapping.isEmpty()
			? New(instantiator)
			: new PersistenceTypeInstantiatorProvider.Mapped<>(
				ConstHashTable.New(instantiatorMapping),
				notNull(instantiator)
			)
		;
	}


	/**
	 * Default {@link PersistenceTypeInstantiatorProvider}: returns a fresh
	 * {@link PersistenceTypeInstantiator} backed by the configured universal
	 * {@link PersistenceInstantiator} for every requested type.
	 *
	 * @param <D> the persistence data type.
	 */
	public class Default<D> implements PersistenceTypeInstantiatorProvider<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceInstantiator<D> instantiator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final PersistenceInstantiator<D> instantiator)
		{
			super();
			this.instantiator = instantiator;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <T> PersistenceTypeInstantiator<D, T> provideTypeInstantiator(final Class<T> type)
		{
			return PersistenceTypeInstantiator.New(type, this.instantiator);
		}
		
	}
	
	/**
	 * Mapping-aware {@link PersistenceTypeInstantiatorProvider}: consults an immutable class-keyed map of
	 * per-type overrides and falls back to {@link Default}'s universal instantiator when no entry matches.
	 *
	 * @param <D> the persistence data type.
	 */
	public final class Mapped<D> extends Default<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ConstHashTable<Class<?>, PersistenceTypeInstantiator<D, ?>> instantiatorMapping;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Mapped(
			final ConstHashTable<Class<?>, PersistenceTypeInstantiator<D, ?>> instantiatorMapping  ,
			final PersistenceInstantiator<D>                                  universalInstantiator
		)
		{
			super(universalInstantiator);
			this.instantiatorMapping = instantiatorMapping;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final <T> PersistenceTypeInstantiator<D, T> provideTypeInstantiator(final Class<T> type)
		{
			final PersistenceTypeInstantiator<D, ?> mappedInstatiator = this.instantiatorMapping.get(type);
			
			@SuppressWarnings("unchecked") // cast safety ensured by mapping logic
			final PersistenceTypeInstantiator<D, T> casted = mappedInstatiator != null
				? (PersistenceTypeInstantiator<D, T>)mappedInstatiator
				: super.provideTypeInstantiator(type)
			;
				
			return casted;
		}
		
	}
	
}
