package org.eclipse.serializer.communication.binarydynamic;

import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public class ComHandlerSendMessageNewType implements ComHandlerSend<ComMessageNewType>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private final static Logger logger = Logging.getLogger(ComHandlerSendMessageNewType.class);
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final ComChannelDynamic<?> comChannel;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHandlerSendMessageNewType(
		final ComChannelDynamic<?> channel
	)
	{
		super();
		this.comChannel = channel;
	}
	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Void sendMessage(final ComMessageNewType message)
	{
		logger.debug("sending new type message for type {}", message.typeEntry());
		this.comChannel.persistenceManager.store(message);
		return null;
	}

	@Override
	public Object sendMessage(final Object messageObject)
	{
		final ComMessageNewType message = (ComMessageNewType)messageObject;
		return this.sendMessage(message);
	}
	
}
