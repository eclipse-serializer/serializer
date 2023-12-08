package org.eclipse.serializer.communication.binarydynamic;

import org.eclipse.serializer.communication.types.ComChannel;

public class ComHandlerReceiveMessageClientTypeMismatch implements ComHandlerReceive<ComMessageClientTypeMismatch>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final ComChannel channel;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHandlerReceiveMessageClientTypeMismatch(final ComChannel connection)
	{
		this.channel = connection;
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public Object processMessage(final ComMessageClientTypeMismatch message)
	{
		this.channel.close();
		return message;
	}

	@Override
	public Object processMessage(final Object received)
	{
		final ComMessageClientTypeMismatch message = (ComMessageClientTypeMismatch)received;
		return this.processMessage(message);
	}
}
