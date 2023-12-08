package org.eclipse.serializer.communication.binarydynamic;

public class ComHandlerReceiveMessageStatus implements ComHandlerReceive<ComMessageStatus>, ComHandlerSend<ComMessageStatus>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final ComChannelDynamic<?> comChannel;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHandlerReceiveMessageStatus(
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
	public Object processMessage(final ComMessageStatus message)
	{
		return message;
	}

	@Override
	public Object processMessage(final Object received)
	{
		final ComMessageStatus message = (ComMessageStatus)received;
		return this.processMessage(message);
	}

	@Override
	public Object sendMessage(final ComMessageStatus message)
	{
		this.comChannel.persistenceManager.store(message);
		return null;
	}

	@Override
	public Object sendMessage(final Object messageObject)
	{
		final ComMessageStatus message = (ComMessageStatus)messageObject;
		return this.sendMessage(message);
	}
}
