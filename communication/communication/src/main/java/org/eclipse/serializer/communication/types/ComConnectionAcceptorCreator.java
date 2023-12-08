package org.eclipse.serializer.communication.types;

/**
 * 
 *
 * @param <C> the connection's communication layer type
 */
@FunctionalInterface
public interface ComConnectionAcceptorCreator<C>
{
	public ComConnectionAcceptor<C> createConnectionAcceptor(
		ComProtocolProvider<C>     protocolProvider       ,
		ComProtocolStringConverter protocolStringConverter,
		ComConnectionHandler<C>    connectionHandler      ,
		ComPersistenceAdaptor<C>   persistenceAdaptor     ,
		ComHostChannelAcceptor<C>  channelAcceptor        ,
		ComHostExceptionHandler<C> comHostExceptionHandler,
		ComPeerIdentifier          peerIdentifier
	);
	
	
	public static <C> ComConnectionAcceptorCreator<C> New()
	{
		return new ComConnectionAcceptorCreator.Default<>();
	}
	
	public final class Default<C> implements ComConnectionAcceptorCreator<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ComConnectionAcceptor<C> createConnectionAcceptor(
			final ComProtocolProvider<C>     protocolProvider       ,
			final ComProtocolStringConverter protocolStringConverter,
			final ComConnectionHandler<C>    connectionHandler      ,
			final ComPersistenceAdaptor<C>   persistenceAdaptor     ,
			final ComHostChannelAcceptor<C>  channelAcceptor        ,
			final ComHostExceptionHandler<C> exceptionHandler       ,
			final ComPeerIdentifier          peerIdentifier
		)
		{
			return ComConnectionAcceptor.New(
				protocolProvider       ,
				protocolStringConverter,
				connectionHandler      ,
				persistenceAdaptor     ,
				channelAcceptor        ,
				exceptionHandler       ,
				peerIdentifier
			);
		}
		
	}
	
}
