package org.eclipse.serializer.persistence.binary.org.eclipse.serializer.persistence.types;

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

import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceObjectRegistry;
import org.eclipse.serializer.persistence.types.PersistenceRootReference;
import org.eclipse.serializer.persistence.types.PersistenceRootReferenceProvider;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;

public interface BinaryRootReferenceProvider<R extends PersistenceRootReference>
extends PersistenceRootReferenceProvider<Binary>
{
	public static BinaryRootReferenceProvider<PersistenceRootReference.Default> New()
	{
		return new BinaryRootReferenceProvider.Default(
			new PersistenceRootReference.Default(null)
		);
	}
	
	public final class Default implements BinaryRootReferenceProvider<PersistenceRootReference.Default>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceRootReference.Default rootReference;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final PersistenceRootReference.Default rootReference)
		{
			super();
			this.rootReference = rootReference;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceRootReference provideRootReference()
		{
			return this.rootReference;
		}
		
		@Override
		public PersistenceTypeHandler<Binary, ? extends PersistenceRootReference> provideTypeHandler(
			final PersistenceObjectRegistry globalRegistry
		)
		{
			return new BinaryHandlerRootReferenceDefault(this.rootReference, globalRegistry);
		}
		
	}
	
}
