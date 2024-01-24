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
