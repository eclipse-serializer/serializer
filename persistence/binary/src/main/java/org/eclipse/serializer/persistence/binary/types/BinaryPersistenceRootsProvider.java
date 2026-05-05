package org.eclipse.serializer.persistence.binary.types;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.persistence.types.BinaryHandlerPersistenceRootsDefault;
import org.eclipse.serializer.persistence.types.PersistenceCustomTypeHandlerRegistry;
import org.eclipse.serializer.persistence.types.PersistenceObjectRegistry;
import org.eclipse.serializer.persistence.types.PersistenceRootReference;
import org.eclipse.serializer.persistence.types.PersistenceRootReferenceProvider;
import org.eclipse.serializer.persistence.types.PersistenceRootResolver;
import org.eclipse.serializer.persistence.types.PersistenceRootResolverProvider;
import org.eclipse.serializer.persistence.types.PersistenceRoots;
import org.eclipse.serializer.persistence.types.PersistenceRootsProvider;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;


/**
 * Binary-specific specialization of {@link PersistenceRootsProvider}. Holds the
 * {@link PersistenceRootResolverProvider} and {@link PersistenceRootReferenceProvider} needed to assemble
 * the persistent root set on demand and to register the matching root-related type handlers
 * ({@code BinaryHandlerPersistenceRootsDefault} and the root-reference handler) with the type-handler
 * registry.
 *
 * @see PersistenceRootsProvider
 */
public interface BinaryPersistenceRootsProvider extends PersistenceRootsProvider<Binary>
{
	/**
	 * Creates a new default {@link BinaryPersistenceRootsProvider}.
	 *
	 * @param rootResolverProvider  supplies the root resolver.
	 * @param rootReferenceProvider supplies the root-reference type handler.
	 *
	 * @return the newly created provider.
	 */
	public static BinaryPersistenceRootsProvider New(
		final PersistenceRootResolverProvider          rootResolverProvider ,
		final PersistenceRootReferenceProvider<Binary> rootReferenceProvider
	)
	{
		return new BinaryPersistenceRootsProvider.Default(
			notNull(rootResolverProvider) ,
			notNull(rootReferenceProvider)
		);
	}
	
	/**
	 * Default {@link BinaryPersistenceRootsProvider} implementation. Lazily resolves the root resolver and
	 * the {@link PersistenceRoots} instance and caches both for the provider's lifetime; supports
	 * runtime-replacement of the cached roots via {@link #updateRuntimeRoots(PersistenceRoots)}.
	 */
	public final class Default implements BinaryPersistenceRootsProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceRootResolverProvider          rootResolverProvider ;
		final PersistenceRootReferenceProvider<Binary> rootReferenceProvider;
		
		transient PersistenceRootResolver rootResolver;
		transient PersistenceRoots        roots       ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceRootResolverProvider          rootResolverProvider ,
			final PersistenceRootReferenceProvider<Binary> rootReferenceProvider
		)
		{
			super();
			this.rootResolverProvider  = rootResolverProvider ;
			this.rootReferenceProvider = rootReferenceProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private PersistenceRootResolver ensureRootResolver()
		{
			if(this.rootResolver == null)
			{
				this.rootResolver = this.rootResolverProvider.provideRootResolver();
			}
			
			return this.rootResolver;
		}

		@Override
		public final PersistenceRoots provideRoots()
		{
			if(this.roots == null)
			{
				this.roots = PersistenceRoots.New(this.ensureRootResolver());
			}
			
			return this.roots;
		}
		
		@Override
		public final PersistenceRoots peekRoots()
		{
			return this.roots;
		}
		
		@Override
		public final void updateRuntimeRoots(final PersistenceRoots runtimeRoots)
		{
			this.roots = runtimeRoots;
		}
		
		@Override
		public final void registerRootsTypeHandlerCreator(
			final PersistenceCustomTypeHandlerRegistry<Binary> typeHandlerRegistry,
			final PersistenceObjectRegistry                    objectRegistry
		)
		{
			final BinaryHandlerPersistenceRootsDefault rootsHandler = BinaryHandlerPersistenceRootsDefault.New(
				this.rootResolverProvider,
				objectRegistry
			);
			
			final PersistenceTypeHandler<Binary, ? extends PersistenceRootReference> rootReferenceHandler =
				this.rootReferenceProvider.provideTypeHandler(objectRegistry)
			;
			
			typeHandlerRegistry.registerTypeHandler(rootsHandler);
			typeHandlerRegistry.registerTypeHandler(rootReferenceHandler);
		}

	}

}
