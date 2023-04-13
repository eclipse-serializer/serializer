package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
 * %%
 * Copyright (C) 2023 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.collections.ConstHashTable;
import org.eclipse.serializer.collections.types.XGettingMap;

public interface PersistenceTypeInstantiatorProvider<D>
{
	public <T> PersistenceTypeInstantiator<D, T> provideTypeInstantiator(Class<T> type);
	
	public static <D> PersistenceTypeInstantiatorProvider<D> Provider()
	{
		return new PersistenceInstantiator.Default<>();
	}
	
	public static <D> PersistenceTypeInstantiatorProvider<D> New(
		final PersistenceInstantiator<D> instantiator
	)
	{
		return new PersistenceTypeInstantiatorProvider.Default<>(
			notNull(instantiator)
		);
	}
	
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
