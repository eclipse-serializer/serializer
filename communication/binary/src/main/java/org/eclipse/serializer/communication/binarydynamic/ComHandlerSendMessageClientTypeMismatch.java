package org.eclipse.serializer.communication.binarydynamic;

public class ComHandlerSendMessageClientTypeMismatch implements ComHandlerSend<ComMessageClientTypeMismatch>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final ComChannelDynamic<?> comChannel;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHandlerSendMessageClientTypeMismatch(
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
	public Void sendMessage(final ComMessageClientTypeMismatch message)
	{
		this.comChannel.persistenceManager.store(message);
		return null;
	}
	
	@Override
	public Object sendMessage(final Object messageObject)
	{
		final ComMessageClientTypeMismatch message = (ComMessageClientTypeMismatch)messageObject;
		return this.sendMessage(message);
	}
	
}
