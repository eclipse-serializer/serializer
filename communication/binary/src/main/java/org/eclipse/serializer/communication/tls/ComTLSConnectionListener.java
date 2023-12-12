package org.eclipse.serializer.communication.tls;

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

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;

import org.eclipse.serializer.communication.types.ComConnection;
import org.eclipse.serializer.communication.types.ComConnectionListener;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public class ComTLSConnectionListener extends ComConnectionListener.Default
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private final static Logger logger = Logging.getLogger(ComConnectionListener.class);


	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
		
	private final SSLContext sslContext;
	private final TLSParametersProvider sslParameters;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComTLSConnectionListener(
		final ServerSocketChannel   serverSocketChannel ,
		final SSLContext            context             ,
		final TLSParametersProvider tlsParameterProvider
	)
	{
		super(serverSocketChannel);
		this.sslContext = context;
		this.sslParameters = tlsParameterProvider;
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public ComConnection createConnection(final SocketChannel channel)
	{
		final ComConnection connection = new ComTLSConnection(channel, this.sslContext, this.sslParameters, false);
		logger.debug("created new ComConnection {}", connection);
		return connection;
	}
}
