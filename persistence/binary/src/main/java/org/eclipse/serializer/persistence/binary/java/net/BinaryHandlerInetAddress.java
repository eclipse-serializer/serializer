package org.eclipse.serializer.persistence.binary.java.net;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.serializer.persistence.exceptions.PersistenceException;

public class BinaryHandlerInetAddress extends AbstractBinaryHandlerInetAddress<InetAddress>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerInetAddress New()
	{
		return new BinaryHandlerInetAddress();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerInetAddress()
	{
		super(InetAddress.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
		
	@Override
	protected InetAddress createInstance(final String hostNamePart, final String addressPart)
	{
		final byte[] address = parseIpAddress(addressPart);
				
		// sadly, they did not provide a method that _just_ creates an unresolved instance.
		try
		{
			return InetAddress.getByAddress(hostNamePart, address);
		}
		catch(final UnknownHostException e)
		{
			throw new PersistenceException(e);
		}
	}
	
}
