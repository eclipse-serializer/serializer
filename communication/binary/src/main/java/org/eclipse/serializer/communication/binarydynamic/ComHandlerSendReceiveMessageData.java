package org.eclipse.serializer.communication.binarydynamic;

public class ComHandlerSendReceiveMessageData implements ComHandlerSend<ComMessageData>, ComHandlerReceive<ComMessageData>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final ComChannelDynamic<?> comChannel;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHandlerSendReceiveMessageData(
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
	public Object processMessage(final ComMessageData message)
	{
		return message.getData();
	}

	@Override
	public Object processMessage(final Object received)
	{
		final ComMessageData message = (ComMessageData)received;
		return this.processMessage(message);
	}

	@Override
	public Object sendMessage(final ComMessageData message)
	{
		this.comChannel.send(message);
		return null;
	}

	@Override
	public Object sendMessage(final Object messageObject)
	{
		final ComMessageData message = (ComMessageData)messageObject;
		return this.sendMessage(message);
	}
}
