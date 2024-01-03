package org.eclipse.serializer.communication.binary.types;

/*-
 * #%L
 * Eclipse Serializer Communication Binary
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
import org.eclipse.serializer.persistence.binary.types.BinaryPersistenceRootsProvider;
import org.eclipse.serializer.persistence.types.PersistenceCustomTypeHandlerRegistry;
import org.eclipse.serializer.persistence.types.PersistenceObjectRegistry;
import org.eclipse.serializer.persistence.types.PersistenceRoots;

public class ComBinaryPersistenceRootsProvider implements BinaryPersistenceRootsProvider
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ComBinaryPersistenceRootsProvider()
	{
		super();
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public PersistenceRoots provideRoots()
	{
		//no-op
		return null;
	}

	@Override
	public PersistenceRoots peekRoots()
	{
		//no-op
		return null;
	}

	@Override
	public void updateRuntimeRoots(final PersistenceRoots runtimeRoots)
	{
		//no-op
	}

	@Override
	public void registerRootsTypeHandlerCreator(
		final PersistenceCustomTypeHandlerRegistry<Binary> typeHandlerRegistry,
		final PersistenceObjectRegistry                    objectRegistry
	)
	{
		//no-op
	}

}
