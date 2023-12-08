package org.eclipse.serializer.communication.binarydynamic;

import org.eclipse.serializer.communication.types.ComChannel;
import org.eclipse.serializer.communication.types.ComProtocol;
import org.eclipse.serializer.persistence.types.PersistenceManager;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public abstract class ComChannelDynamic<C> implements ComChannel
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private final static Logger logger = Logging.getLogger(Default.class);

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	protected final PersistenceManager<?> persistenceManager;
	protected final C                     connection;
	protected final ComProtocol           protocol;
	protected final ComHandlerRegistry    handlers = new ComHandlerRegistry.Default();

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComChannelDynamic(
		final PersistenceManager<?> persistenceManager,
		final C                     connection,
		final ComProtocol           protocol
	)
	{
		this.connection         = connection;
		this.persistenceManager = persistenceManager;
		this.protocol           = protocol;
	}

	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	//Bypass Handlers to avoid recursion if called inside an handler ...
	public Object requestUnhandled(final Object object)
	{
		this.persistenceManager.store(object);
		return this.persistenceManager.get();
	}
	
	@Override
	public final void send(final Object graphRoot)
	{
		logger.trace("sending data");
		
		ComHandlerSend<?> handler = null;
		
		if(graphRoot != null)
		{
			handler = this.handlers.lookupSend(graphRoot.getClass());
		}
		
		if(handler != null )
		{
			logger.trace("sending data with handler {}", handler.getClass());
			handler.sendMessage(graphRoot);
		}
		else
		{
			this.persistenceManager.store(new ComMessageData(graphRoot));
		}
		
		logger.trace("sent data successfully");
	}

	@Override
	public final Object receive()
	{
		Object received = null;
		
		while(null == received)
		{
			logger.trace("waiting for data");
			received = this.persistenceManager.get();
			this.persistenceManager.objectRegistry().clear();
	
			final ComHandlerReceive<?> handler = this.handlers.lookupReceive(received.getClass());
			if(handler != null )
			{
				logger.trace("processing received data with handler {}", handler.getClass());
				received = handler.processMessage(received);
			
				if(!handler.continueReceiving())
				{
					break;
				}
			}
		}
		
		logger.trace("data received successfully");
		
		return received;
	}

	@Override
	public final void close()
	{
		logger.trace("closing ComChannel");
		this.persistenceManager.close();
	}
}
