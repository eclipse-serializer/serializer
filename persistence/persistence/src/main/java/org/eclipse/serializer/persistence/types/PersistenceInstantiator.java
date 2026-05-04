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

import org.eclipse.serializer.exceptions.InstantiationRuntimeException;
import org.eclipse.serializer.memory.XMemory;


/**
 * Universal instance factory used during loading: bypasses constructors and produces a "blank" instance of
 * any class, into which the loader subsequently writes the persisted state. The bundled {@link Default}
 * uses {@link XMemory#instantiateBlank(Class)}, which leans on platform-specific memory APIs.
 * <p>
 * Type-bound counterpart is {@link PersistenceTypeInstantiator}; the latter is what type handlers actually
 * call, while {@link PersistenceInstantiator} is the underlying primitive. {@link Default} implements
 * {@link PersistenceTypeInstantiatorProvider} directly so the same instance can be used as both.
 *
 * @param <D> the persistence data type passed to {@link #instantiate(Class, Object)}.
 *
 * @see PersistenceTypeInstantiator
 * @see PersistenceTypeInstantiatorProvider
 */
public interface PersistenceInstantiator<D>
{
	/**
	 * Creates a new instance of {@code type}. Implementations may inspect {@code data} to dispatch to a
	 * type-specific construction strategy.
	 *
	 * @param <T>  the instantiated type.
	 * @param type the type to instantiate.
	 * @param data the persistence data, supplied for context-sensitive instantiation.
	 *
	 * @return the newly created instance.
	 *
	 * @throws InstantiationRuntimeException if instantiation fails.
	 */
	public <T> T instantiate(Class<T> type, D data) throws InstantiationRuntimeException;



	/**
	 * Constructs a new instance of {@code type} without invoking any constructor (i.e. all fields keep
	 * their JVM defaults). Used by {@link Default}.
	 *
	 * @param <T>  the instantiated type.
	 * @param type the type to instantiate.
	 *
	 * @return the newly created blank instance.
	 */
	public static <T> T instantiateBlank(final Class<T> type)
	{
		return XMemory.instantiateBlank(type);
	}



	/**
	 * Creates a new {@link Default} instantiator.
	 *
	 * @param <D> the persistence data type.
	 *
	 * @return the newly created instantiator.
	 */
	public static <D> PersistenceInstantiator<D> New()
	{
		return new PersistenceInstantiator.Default<>();
	}

	/**
	 * Default {@link PersistenceInstantiator}: produces blank instances via
	 * {@link #instantiateBlank(Class)} and ignores the {@code data} argument. Also implements
	 * {@link PersistenceTypeInstantiatorProvider} so the same instance can be used as a provider.
	 *
	 * @param <D> the persistence data type.
	 */
	public final class Default<D> implements PersistenceInstantiator<D>, PersistenceTypeInstantiatorProvider<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <T> T instantiate(final Class<T> type, final D data)
			throws InstantiationRuntimeException
		{
			return PersistenceInstantiator.instantiateBlank(type);
		}
		
		@Override
		public <T> PersistenceTypeInstantiator<D, T> provideTypeInstantiator(final Class<T> type)
		{
			return PersistenceTypeInstantiator.New(type, this);
		}
		
	}
	
}
