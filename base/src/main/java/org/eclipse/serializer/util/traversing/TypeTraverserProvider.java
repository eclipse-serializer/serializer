package org.eclipse.serializer.util.traversing;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 MicroStream Software
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

import org.eclipse.serializer.collections.HashTable;
import org.eclipse.serializer.collections.types.XGettingMap;
import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.typing.KeyValue;

public interface TypeTraverserProvider
{
	public default boolean isUnhandled(final Object instance)
	{
		return this.provide(instance) == null;
	}
	
	public <T> TypeTraverser<T> provide(T instance);
	
	
	
	public static TypeTraverserProvider.Default New(
		final TypeTraverser.Creator                     traverserCreator          ,
		final XGettingMap<Object, TypeTraverser<?>>     traversersPerInstance     ,
		final XGettingMap<Class<?>, TypeTraverser<?>>   traversersPerConcreteType ,
		final XGettingTable<Class<?>, TypeTraverser<?>> traversersPerPolymorphType
	)
	{
		return new TypeTraverserProvider.Default(
			notNull(traverserCreator)          ,
			        traversersPerInstance      ,
			notNull(traversersPerConcreteType) ,
			notNull(traversersPerPolymorphType)
		);
	}
	
	public final class Default implements TypeTraverserProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XGettingMap<Object, TypeTraverser<?>>     traversersPerInstance     ;
		private final HashTable<Class<?>, TypeTraverser<?>>     traversersPerConcreteType ;
		private final XGettingTable<Class<?>, TypeTraverser<?>> traversersPerPolymorphType;
		private final TypeTraverser.Creator                     traverserCreator          ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final TypeTraverser.Creator                       traverserCreator          ,
			final XGettingMap<Object, TypeTraverser<?>>       traversersPerInstance     ,
			final XGettingMap<Class<?>, TypeTraverser<?>>     traversersPerConcreteType ,
			final XGettingTable<Class<?>, TypeTraverser<?>>   traversersPerPolymorphType
		)
		{
			super();
			this.traverserCreator           = traverserCreator ;
			this.traversersPerInstance      = traversersPerInstance     ;
			this.traversersPerPolymorphType = traversersPerPolymorphType;
			this.traversersPerConcreteType  = initializeHandlersPerConcreteType(traversersPerConcreteType);
		}
		
		private static HashTable<Class<?>, TypeTraverser<?>> initializeHandlersPerConcreteType(
			final XGettingMap<Class<?>, TypeTraverser<?>> traversersPerConcreteType
		)
		{
			final HashTable<Class<?>, TypeTraverser<?>> localMap = HashTable.New(traversersPerConcreteType);
			return localMap;
		}
		
		private TypeTraverser<?> handleNewType(final Class<?> type)
		{
			for(final KeyValue<Class<?>, TypeTraverser<?>> entry : this.traversersPerPolymorphType)
			{
				if(entry.key().isAssignableFrom(type))
				{
					this.traversersPerConcreteType.add(type, entry.value());
					return entry.value();
				}
			}
						
			final TypeTraverser<?> created = this.traverserCreator.createTraverser(type);
			if(created != null)
			{
				this.traversersPerConcreteType.add(type, created);
				return created;
			}

			throw new IllegalArgumentException("Untraversable type: " + type.getName());
		}
				
		protected final TypeTraverser<?> internalProvideTraversalHandler(final Object instance)
		{
			if(this.traversersPerInstance != null)
			{
				final TypeTraverser<?> perInstanceHandler;
				if((perInstanceHandler = this.traversersPerInstance.get(instance)) != null)
				{
					return perInstanceHandler;
				}
			}
						
			final TypeTraverser<?> perTypeHandler;
			if((perTypeHandler = this.traversersPerConcreteType.get(instance.getClass())) != null)
			{
				return perTypeHandler;
			}
			
			return this.handleNewType(instance.getClass());
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeTraverser<T> provide(final T instance)
		{
			return (TypeTraverser<T>)this.internalProvideTraversalHandler(instance);
		}
				
	}
	
}
