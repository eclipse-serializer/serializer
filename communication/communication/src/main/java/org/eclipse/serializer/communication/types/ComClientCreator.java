package org.eclipse.serializer.communication.types;

import static org.eclipse.serializer.util.X.notNull;

import java.net.InetSocketAddress;

/**
 * 
 * @param <C> the communication layer type
 */
@FunctionalInterface
public interface ComClientCreator<C>
{
	public ComClient.Default<C> createClient(
		InetSocketAddress          hostAddress       ,
		ComConnectionHandler<C>    connectionHandler ,
		ComProtocolStringConverter protocolParser    ,
		ComPersistenceAdaptor<C>   persistenceAdaptor,
		int                        inactivityTimeOut
	);
	
	
	public static <C> ComClientCreator.Default<C> New()
	{
		return new ComClientCreator.Default<>();
	}
	
	public final class Default<C> implements ComClientCreator<C>
	{
		@Override
		public ComClient.Default<C> createClient(
			final InetSocketAddress          hostAddress       ,
			final ComConnectionHandler<C>    connectionHandler ,
			final ComProtocolStringConverter protocolParser    ,
			final ComPersistenceAdaptor<C>   persistenceAdaptor,
			final int                        inactivityTimeOut
		)
		{
			return ComClient.New(
				notNull(hostAddress)       ,
				notNull(connectionHandler) ,
				notNull(protocolParser)    ,
				notNull(persistenceAdaptor),
				inactivityTimeOut
			);
		}
		
	}
}
