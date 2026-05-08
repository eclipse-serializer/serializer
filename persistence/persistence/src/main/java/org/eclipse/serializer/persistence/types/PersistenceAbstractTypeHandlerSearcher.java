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

import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.HashEnum;
import org.eclipse.serializer.reflect.XReflect;

/**
 * Locates a registered "abstract" handler that is assignable from the passed runtime type, by walking
 * {@code type}'s super-class and super-interface graph in priority order: deeper class-hierarchy levels
 * first, with super-classes preferred over interfaces at each level. The first registered match wins.
 * <p>
 * Used during type-handler discovery as a fallback when no concrete handler is registered for an exact type:
 * if e.g. a custom handler has been registered for a common interface, instances of every class that
 * implements that interface can be funneled to it.
 *
 * @param <D> the persistence data type passed through to the {@link PersistenceTypeHandler}.
 *
 * @see PersistenceCustomTypeHandlerRegistry
 * @see PersistenceTypeHandler
 */
public interface PersistenceAbstractTypeHandlerSearcher<D>
{
	/**
	 * Default-method shortcut delegating to
	 * {@link #searchAbstractTypeHandler(PersistenceCustomTypeHandlerRegistry, Class)} so callers can
	 * invoke the search through a registered searcher instance.
	 *
	 * @param <T>                       the searched type.
	 * @param type                      the runtime type to look up.
	 * @param customTypeHandlerRegistry the registry to search.
	 *
	 * @return a registered handler whose type is a super-type of {@code type}, or {@code null} if none
	 *         exists.
	 */
	public default <T> PersistenceTypeHandler<D, ? super T> searchAbstractTypeHandler(
		final Class<T>                                type                     ,
		final PersistenceCustomTypeHandlerRegistry<D> customTypeHandlerRegistry
	)
	{
		return PersistenceAbstractTypeHandlerSearcher.searchAbstractTypeHandler(
			customTypeHandlerRegistry,
			type
		);
	}



	/**
	 * Walks {@code type}'s super-class and super-interface graph in priority order and returns the first
	 * handler registered in {@code customTypeHandlerRegistry} for any of the abstract super-types.
	 * <p>
	 * Priority: at each hierarchy level, the super-class is considered before that level's interfaces; only
	 * abstract classes (including interfaces) are considered as potential targets.
	 *
	 * @param <D>                       the persistence data type.
	 * @param <T>                       the searched type.
	 * @param customTypeHandlerRegistry the registry to search.
	 * @param type                      the runtime type to look up.
	 *
	 * @return a handler whose type is a super-type of {@code type}, or {@code null} if none is registered.
	 */
	public static <D, T> PersistenceTypeHandler<D, ? super T> searchAbstractTypeHandler(
		final PersistenceCustomTypeHandlerRegistry<D> customTypeHandlerRegistry,
		final Class<T>                                type
	)
	{
		final HashEnum<Class<?>> abstractSuperTypesInOrder       = HashEnum.New();
		final HashEnum<Class<?>> abstractTypesForNextLevel       = HashEnum.New();
		final BulkList<Class<?>> abstractTypesToAddFromLastLevel = BulkList.New();
		
		Class<?> currentClass = type;
		
		/*
		 * There are 2 conditions that keep the interface collecting loop going:
		 * - There are still super classes in the class hierarchy to be checked
		 * - There are still interface hierarchy interfaces (interface hierarchy is "deeper" than class hierarchy)
		 * 
		 * Note that just the count check would not be enough since a class can have a non-abstract superclass
		 * and no interfaces. So the count would be 0 for the lowest level and the loop would abort prematurely.
		 */
		while(currentClass != Object.class || !abstractTypesToAddFromLastLevel.isEmpty())
		{
			// "currentClass" is actually the previous class at the start of the cycle
			final Class<?>[] previousClassInterfaces = currentClass.getInterfaces();

			// get actual "current" class for this cycle from previous class.
			currentClass = XReflect.getSuperClassNonNull(currentClass);
			
			// add current class with higher priority than previous level's interfaces, but only if abstract
			Default.addAbstractClass(abstractTypesForNextLevel, currentClass);
			
			// add previous class's interfaces with secondary priority
			abstractTypesForNextLevel.addAll(previousClassInterfaces);
			Default.collectAllSuperInterfaces(abstractTypesForNextLevel, abstractTypesToAddFromLastLevel);
			
			// add last hierarchy level's interfaces with second highest priority for this cycle
			abstractSuperTypesInOrder.addAll(abstractTypesToAddFromLastLevel);
			abstractTypesToAddFromLastLevel.clear();
			
			// move all "next level" types to "toAdd" collection for next cycle.
			abstractTypesToAddFromLastLevel.addAll(abstractTypesForNextLevel);
			abstractTypesForNextLevel.clear();
		}

		PersistenceTypeHandler<D, ?> abstractTypeHandler = null;
		for(final Class<?> abstractSuperType : abstractSuperTypesInOrder)
		{
			abstractTypeHandler = customTypeHandlerRegistry.lookupTypeHandler(abstractSuperType);
			if(abstractTypeHandler != null)
			{
				break;
			}
		}
		
		@SuppressWarnings("unchecked")
		final PersistenceTypeHandler<D, ? super T> result =
			(PersistenceTypeHandler<D, ? super T>)abstractTypeHandler
		;
		
		return result;
	}
	

	
	
	/**
	 * Creates a new {@link Default} searcher.
	 *
	 * @param <D> the persistence data type.
	 *
	 * @return the newly created searcher.
	 */
	public static <D> PersistenceAbstractTypeHandlerSearcher<D> New()
	{
		return new PersistenceAbstractTypeHandlerSearcher.Default<>();
	}

	/**
	 * Default {@link PersistenceAbstractTypeHandlerSearcher} that just delegates to the static
	 * {@link #searchAbstractTypeHandler(PersistenceCustomTypeHandlerRegistry, Class)}. Stateless and
	 * freely shareable.
	 *
	 * @param <D> the persistence data type.
	 */
	public final class Default<D> implements PersistenceAbstractTypeHandlerSearcher<D>
	{
		// actually belongs in the interface, but JLS' visibility rules are too stupid to allow clean architecture.
		static final void addAbstractClass(
			final HashEnum<Class<?>> collection,
			final Class<?>           clazz
		)
		{
			if(!XReflect.isAbstract(clazz))
			{
				return;
			}
			
			collection.add(clazz);
		}
		
		static final void collectAllSuperInterfaces(
			final HashEnum<Class<?>> collection,
			final Iterable<Class<?>> classes
		)
		{
			for(final Class<?> c : classes)
			{
				collection.addAll(c.getInterfaces());
			}
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}

		// that's all, folks!
	}
	
}
