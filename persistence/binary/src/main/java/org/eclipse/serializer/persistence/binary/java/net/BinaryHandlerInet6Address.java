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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.serializer.persistence.exceptions.PersistenceException;

public class BinaryHandlerInet6Address extends AbstractBinaryHandlerInetAddress<Inet6Address>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerInet6Address New()
	{
		return new BinaryHandlerInet6Address();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerInet6Address()
	{
		super(Inet6Address.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
		
	@Override
	protected Inet6Address createInstance(final String hostNamePart, final String addressPart)
	{
		final byte[] address = parseIpV6Address(addressPart);
				
		// sadly, they did not provide a method that _just_ creates an unresolved instance.
		try
		{
			return (Inet6Address)InetAddress.getByAddress(hostNamePart, address);
		}
		catch(final UnknownHostException e)
		{
			throw new PersistenceException(e);
		}
	}
	
}
