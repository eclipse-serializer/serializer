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

import org.eclipse.serializer.reflect.ClassLoaderProvider;
import org.eclipse.serializer.reflect.XReflect;

public interface PersistenceTypeResolver
{
	public default String substituteClassIdentifierSeparator()
	{
		return Persistence.substituteClassIdentifierSeparator();
	}
	
	public default String deriveTypeName(final Class<?> type)
	{
		return Persistence.derivePersistentTypeName(type, this.substituteClassIdentifierSeparator());
	}
	
	public default ClassLoader getTypeResolvingClassLoader(final String typeName)
	{
		return XReflect.defaultTypeResolvingClassLoader();
	}
	
	public default Class<?> resolveType(final String typeName)
	{
		return Persistence.resolveType(
			typeName,
			this.getTypeResolvingClassLoader(typeName),
			this.substituteClassIdentifierSeparator()
		);
	}
	
	public default Class<?> tryResolveType(final String typeName)
	{
		return Persistence.tryResolveType(typeName, this.getTypeResolvingClassLoader(typeName));
	}
	
	
	
//	public static PersistenceTypeResolver New()
//	{
//		return New(
//			ClassLoaderProvider.New()
//		);
//	}
	
	public static PersistenceTypeResolver New(final ClassLoaderProvider classLoaderProvider)
	{
		return new PersistenceTypeResolver.Default(
			notNull(classLoaderProvider)
		);
	}
	
	public final class Default implements PersistenceTypeResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ClassLoaderProvider classLoaderProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final ClassLoaderProvider classLoaderProvider)
		{
			super();
			this.classLoaderProvider = classLoaderProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ClassLoader getTypeResolvingClassLoader(final String typeName)
		{
			return this.classLoaderProvider.provideClassLoader(typeName);
		}
		
	}
			
}
