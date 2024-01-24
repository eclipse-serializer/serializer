package org.eclipse.serializer.communication.binarydynamic;

/*-
 * #%L
 * Eclipse Serializer Communication Binary
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

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
