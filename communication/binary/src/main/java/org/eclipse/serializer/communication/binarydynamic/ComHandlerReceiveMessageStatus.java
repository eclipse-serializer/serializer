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
