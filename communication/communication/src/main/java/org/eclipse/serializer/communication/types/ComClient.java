package org.eclipse.serializer.communication.types;

import java.net.InetSocketAddress;
import java.time.Duration;

import org.eclipse.serializer.com.ComException;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public interface ComClient<C>
{
	public ComClientChannel<C> connect() throws ComException;
	
	ComClientChannel<C> connect(int retries, Duration retryDelay) throws ComException;

	public InetSocketAddress hostAddress();
	
	
	
	public static <C> ComClientCreator<C> Creator()
	{
		return ComClientCreator.New();
	}
	
	public static <C> ComClient.Default<C> New(
		final InetSocketAddress          hostAddress       ,
		final ComConnectionHandler<C>    connectionHandler ,
		final ComProtocolStringConverter protocolParser    ,
		final ComPersistenceAdaptor<C>   persistenceAdaptor,
		final int                        inactivityTimeOut
	)
	{
		return new ComClient.Default<>(
			hostAddress       ,
			connectionHandler ,
			protocolParser    ,
			persistenceAdaptor,
			inactivityTimeOut
		);
	}
	
	public final class Default<C> implements ComClient<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private final static Logger logger = Logging.getLogger(Default.class);
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final InetSocketAddress          hostAddress       ;
		private final ComConnectionHandler<C>    connectionHandler ;
		private final ComProtocolStringConverter protocolParser    ;
		private final ComPersistenceAdaptor<C>   persistenceAdaptor;
		private final ComPeerIdentifier          peerIdentifier = ComPeerIdentifier.New();
		private final int                        inactivityTimeOut;
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final InetSocketAddress          hostAddress       ,
			final ComConnectionHandler<C>    connectionHandler ,
			final ComProtocolStringConverter protocolParser    ,
			final ComPersistenceAdaptor<C>   persistenceAdaptor,
			final int                        inactivityTimeOut
		)
		{
			super();
			this.hostAddress        = hostAddress       ;
			this.connectionHandler  = connectionHandler ;
			this.protocolParser     = protocolParser    ;
			this.persistenceAdaptor = persistenceAdaptor;
			this.inactivityTimeOut  = inactivityTimeOut ;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final InetSocketAddress hostAddress()
		{
			return this.hostAddress;
		}
		
		@Override
		public ComClientChannel<C> connect() throws ComException
		{
			return this.connect(0, Duration.ZERO);
		}
		
		@Override
		public ComClientChannel<C> connect(final int retries, final Duration retryDelay) throws ComException
		{
			logger.info("Connecting to remote address {} ", this.hostAddress);
			final C                   conn     = this.connectionHandler.openConnection(this.hostAddress, retries, retryDelay);
						
			this.connectionHandler.sendClientIdentifer(conn, this.peerIdentifier);
						
			this.connectionHandler.enableSecurity(conn);
			
			final ComProtocol         protocol = this.connectionHandler.receiveProtocol(conn, this.protocolParser);
			this.connectionHandler.setInactivityTimeout(conn, this.inactivityTimeOut);
			
			final ComClientChannel<C> channel  = this.persistenceAdaptor.createClientChannel(conn, protocol, this);
			
			logger.info("Successfully connected to {} ", this.hostAddress);
			
			return channel;
		}
		
	}
	
}
