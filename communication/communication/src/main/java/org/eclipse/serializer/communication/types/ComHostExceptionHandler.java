package org.eclipse.serializer.communication.types;

import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public interface ComHostExceptionHandler<C>
{
	public void handleException(Throwable exception, ComChannel channel);
	public void handleConnectException(Throwable exception, C connection);
	
	public static <C> ComHostExceptionHandler<C> New(final ComConnectionHandler<C> connectionHandler)
	{
		return new ComHostExceptionHandler.Default<>(connectionHandler);
	}
	
	public final class Default<C> implements ComHostExceptionHandler<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
			
		private final static Logger logger = Logging.getLogger(ComConnectionHandler.class);

		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ComConnectionHandler<C> connectionHandler;

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final ComConnectionHandler<C> connectionHandler)
		{
			super();
			this.connectionHandler = connectionHandler;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void handleException(final Throwable exception, final ComChannel channel)
		{
			logger.error("Closing connection because: ", exception);
			channel.close();
		}


		@Override
		public void handleConnectException(final Throwable exception, final C connection)
		{
			logger.error("Closing connection because of ", exception);
			try
			{
				this.connectionHandler.close(connection);
			}
			catch(final Exception e)
			{
				logger.error("failed to close connection! ", exception);
			}
		}
		
	}
}
