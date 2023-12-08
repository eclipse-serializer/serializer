package org.eclipse.serializer.communication.binary.types;

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
