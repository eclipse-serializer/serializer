package org.eclipse.serializer.communication.binarydynamic;

import java.nio.ByteOrder;

import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.communication.binary.types.ComPersistenceAdaptorBinary;
import org.eclipse.serializer.communication.types.ComConnection;
import org.eclipse.serializer.communication.types.ComPersistenceAdaptor;
import org.eclipse.serializer.persistence.binary.types.BinaryPersistenceFoundation;
import org.eclipse.serializer.persistence.types.PersistenceIdStrategy;
import org.eclipse.serializer.util.BufferSizeProvider;

public final class ComPersistenceAdaptorBinaryDynamicCreator extends ComPersistenceAdaptorBinary.Creator.Abstract<ComConnection>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected ComPersistenceAdaptorBinaryDynamicCreator(
		final BinaryPersistenceFoundation<?> foundation        ,
		final BufferSizeProvider             bufferSizeProvider
	)
	{
		super(foundation, bufferSizeProvider);
	}
		
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public ComPersistenceAdaptor<ComConnection> createPersistenceAdaptor(
		final PersistenceIdStrategy  hostIdStrategyInitialization,
		final XGettingEnum<Class<?>> entityTypes                 ,
		final ByteOrder              hostByteOrder               ,
		final PersistenceIdStrategy  hostIdStrategy
	)
	{
		return ComPersistenceAdaptorBinaryDynamic.New(
			this.foundation()           ,
			this.bufferSizeProvider()   ,
			hostIdStrategyInitialization,
			entityTypes                 ,
			hostByteOrder               ,
			hostIdStrategy
		);
	}
}
