package org.eclipse.serializer.communication.types;

import static org.eclipse.serializer.util.X.notNull;

import java.nio.ByteOrder;

import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.persistence.types.PersistenceIdStrategy;

/**
 * 
 * @param <C> the communication layer type
 */
@FunctionalInterface
public interface ComPersistenceAdaptorCreator<C>
{
	public ComPersistenceAdaptor<C> createPersistenceAdaptor(
		PersistenceIdStrategy  hostIdStrategyInitialization,
		XGettingEnum<Class<?>> entityTypes                 ,
		ByteOrder              hostByteOrder               ,
		PersistenceIdStrategy  hostIdStrategy
	);
	
	public default ComPersistenceAdaptor<C> createHostPersistenceAdaptor(
		final PersistenceIdStrategy  hostIdStrategyInitialization,
		final XGettingEnum<Class<?>> entityTypes                 ,
		final ByteOrder              hostByteOrder               ,
		final PersistenceIdStrategy  hostIdStrategy
	)
	{
		return this.createPersistenceAdaptor(
			notNull(hostIdStrategyInitialization),
			notNull(entityTypes)                 ,
			notNull(hostByteOrder),
			notNull(hostIdStrategy)
		);
	}
	
	public default ComPersistenceAdaptor<C> createClientPersistenceAdaptor()
	{
		return this.createPersistenceAdaptor(null, null, null, null);
	}
	
}
