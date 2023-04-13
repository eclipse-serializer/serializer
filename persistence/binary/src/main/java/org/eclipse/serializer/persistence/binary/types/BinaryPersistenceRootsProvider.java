package org.eclipse.serializer.persistence.binary.types;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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


public interface BinaryPersistenceRootsProvider extends PersistenceRootsProvider<Binary>
{
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
