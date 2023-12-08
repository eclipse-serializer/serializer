package org.eclipse.serializer.communication.types;

import static org.eclipse.serializer.util.X.notNull;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.eclipse.serializer.com.XSockets;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

/**
 * 
 * @param <C> the communication layer type
 */
public interface ComConnectionListener<C>
{
	public ComConnection createConnection(SocketChannel channel);
	
	public C listenForConnection();
	
	public void close();
	
	public boolean isAlive();
	
	public static ComConnectionListener.Default Default(final ServerSocketChannel serverSocketChannel)
	{
		return new ComConnectionListener.Default(
			notNull(serverSocketChannel)
		);
	}
	
	public class Default implements ComConnectionListener<ComConnection>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private final static Logger logger = Logging.getLogger(ComConnectionListener.class);
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ServerSocketChannel serverSocketChannel;
		
						
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(final ServerSocketChannel serverSocketChannel)
		{
			super();
			this.serverSocketChannel = serverSocketChannel;
		}
				
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ComConnection createConnection(final SocketChannel channel)
		{
			final ComConnection connection =  new ComConnection.Default(channel);
			logger.debug("created new ComConnection {}", connection);
			return connection;
		}
		
		@Override
		public final ComConnection listenForConnection()
		{
			logger.debug("listening for incoming connections at {} ", this.serverSocketChannel);
			final SocketChannel channel = XSockets.acceptSocketChannel(this.serverSocketChannel);
			logger.debug("incoming connection {}", channel);
			return this.createConnection(channel);
		}

		@Override
		public final void close()
		{
			logger.debug("closing serverSocket Channel {}", this.serverSocketChannel);
			XSockets.closeChannel(this.serverSocketChannel);
		}

		@Override
		public boolean isAlive()
		{
			return this.serverSocketChannel.isOpen();
		}
		
	}
	
}
