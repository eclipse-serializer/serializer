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

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.eclipse.serializer.com.ComException;
import org.eclipse.serializer.com.XSockets;
import org.eclipse.serializer.communication.types.ComConnection;
import org.eclipse.serializer.communication.types.ComConnectionHandler;
import org.eclipse.serializer.communication.types.ComConnectionListener;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public class ComTLSConnectionHandler extends ComConnectionHandler.Default
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private final static boolean TLS_CLIENT_MODE = true;
	
	private final static Logger logger = Logging.getLogger(ComConnectionHandler.class);
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final SSLContext context;
	
	private final TLSKeyManagerProvider   keyManagerProvider;
	private final TLSTrustManagerProvider trustManagerProvider;
	private final TLSParametersProvider   tlsParameterProvider;
	private final SecureRandomProvider    randomProvider;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private ComTLSConnectionHandler(
		final TLSKeyManagerProvider   keyManagerProvider  ,
		final TLSTrustManagerProvider trustManagerProvider,
		final TLSParametersProvider   tlsParameterProvider,
		final SecureRandomProvider    randomProvider
	)
	{
		super();
		
		this.tlsParameterProvider = tlsParameterProvider;
		this.keyManagerProvider   = keyManagerProvider  ;
		this.trustManagerProvider = trustManagerProvider;
		this.randomProvider       = randomProvider      ;
				
		try
		{
			this.context = SSLContext.getInstance(tlsParameterProvider.getSSLProtocol());
		}
		catch (final NoSuchAlgorithmException e)
		{
			throw new ComException("Failed get SSLContextInstance for " + tlsParameterProvider.getSSLProtocol(), e);
		}
		
		try
		{
			this.context.init(
				this.keyManagerProvider.get(),
				this.trustManagerProvider.get(),
				this.randomProvider.get()
			);
		}
		catch (final KeyManagementException e)
		{
			throw new ComException("Failed to init SSLContext", e);
		}
	}
	
	public static ComConnectionHandler<ComConnection> New(
		final TLSKeyManagerProvider   keyManagerProvider  ,
		final TLSTrustManagerProvider trustManagerProvider,
		final TLSParametersProvider   tlsParameterProvider,
		final SecureRandomProvider    randomProvider
	)
	{
		return new ComTLSConnectionHandler(keyManagerProvider, trustManagerProvider, tlsParameterProvider, randomProvider);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public ComConnectionListener<ComConnection> createConnectionListener(final InetSocketAddress address)
	{
		final ServerSocketChannel serverSocketChannel = XSockets.openServerSocketChannel(address);
		final ComConnectionListener<ComConnection> connectionListener =  new ComTLSConnectionListener(serverSocketChannel, this.context, this.tlsParameterProvider);
		logger.debug("created new ComConnectionListener {}", connectionListener);
		return connectionListener;
	}

	@Override
	public ComTLSConnection openConnection(final InetSocketAddress address)
	{
		final SocketChannel clientChannel = XSockets.openChannel(address);
		final ComTLSConnection connection =  new ComTLSConnection(clientChannel, this.context, this.tlsParameterProvider, TLS_CLIENT_MODE);
		logger.debug("created new ComConnection {}", connection);
		return connection;
	}

	@Override
	public void enableSecurity(final ComConnection connection)
	{
		connection.enableSecurity();
	}
}
