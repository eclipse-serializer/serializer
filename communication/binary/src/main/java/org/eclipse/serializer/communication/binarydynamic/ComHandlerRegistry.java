package org.eclipse.serializer.communication.binarydynamic;

import org.eclipse.serializer.collections.MiniMap;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public interface ComHandlerRegistry
{
	public <T> boolean registerSendHandler    (final Class<T> type, final ComHandlerSend<?> handler);
	public <T> boolean registerReceiveHandler (final Class<T> type, final ComHandlerReceive<?> handler);
	
	public <T extends ComMessage> ComHandlerSend<ComMessage>    lookupSend(final Class<?> type);
	public <T extends ComMessage> ComHandlerReceive<ComMessage> lookupReceive(final Class<?> type);

	
	
	public final class Default implements ComHandlerRegistry
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private final static Logger logger = Logging.getLogger(Default.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final MiniMap<Class<?>, ComHandlerSend<?>>    sendHandlers    = new MiniMap<>();
		private final MiniMap<Class<?>, ComHandlerReceive<?>> receiveHandlers = new MiniMap<>();
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default()
		{
			super();
		}
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final <T> boolean registerSendHandler(final Class<T> type, final ComHandlerSend<?> handler)
		{
			logger.debug("registered sending handler {} for type {}", handler.getClass(), type);
			return this.sendHandlers.put(type, handler) != null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final <T extends ComMessage> ComHandlerSend<ComMessage> lookupSend(final Class<?> type)
		{
			return (ComHandlerSend<ComMessage>) this.sendHandlers.get(type);
		}
		
		@Override
		public final <T> boolean registerReceiveHandler(final Class<T> type, final ComHandlerReceive<?> handler)
		{
			logger.debug("registered receiving handler {} for type {}", handler.getClass(), type);
			return this.receiveHandlers.put(type, handler) != null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final <T extends ComMessage> ComHandlerReceive<ComMessage> lookupReceive(final Class<?> type)
		{
			return (ComHandlerReceive<ComMessage>) this.receiveHandlers.get(type);
		}
	}
	
	
}
