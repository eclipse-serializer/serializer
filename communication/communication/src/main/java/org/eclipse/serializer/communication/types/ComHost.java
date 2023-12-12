package org.eclipse.serializer.communication.types;

/*-
 * #%L
 * Eclipse Serializer Communication Parent
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

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;

import java.net.InetSocketAddress;

import org.eclipse.serializer.com.ComException;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

/**
 * Host type to listen for new connections and relay them to logic for further processing,
 * potentially in another, maybe even dedicated thread.
 *
 * @param <C> the communication layer type
 */
public interface ComHost<C> extends Runnable
{
	public InetSocketAddress address();
	
	public ComProtocolProvider<C> protocolProvider();
	
	/**
	 * Listens for incoming connections and relays them for processing.
	 */
	public void acceptConnections();
	
	@Override
	public void run();
	
	public void stop();
	
	public boolean isListening();
	
	
	
	public static <C> ComHost<C> New(
		final InetSocketAddress        address           ,
		final ComConnectionHandler<C>  connectionHandler ,
		final ComConnectionAcceptor<C> connectionAcceptor
	)
	{
		return new ComHost.Default<>(
			mayNull(address)           ,
			notNull(connectionHandler) ,
			notNull(connectionAcceptor)
		);
	}
	
	public final class Default<C> implements ComHost<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final InetSocketAddress        address           ;
		private final ComConnectionHandler<C>  connectionHandler ;
		private final ComConnectionAcceptor<C> connectionAcceptor;
		
		private transient ComConnectionListener<C> liveConnectionListener;
		private volatile boolean stopped;
		
		private final static Logger logger = Logging.getLogger(Default.class);
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final InetSocketAddress        address           ,
			final ComConnectionHandler<C>  connectionHandler ,
			final ComConnectionAcceptor<C> connectionAcceptor
		)
		{
			super();
			this.address            = address           ;
			this.connectionHandler  = connectionHandler ;
			this.connectionAcceptor = connectionAcceptor;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final InetSocketAddress address()
		{
			return this.address;
		}

		@Override
		public final ComProtocolProvider<C> protocolProvider()
		{
			return this.connectionAcceptor.protocolProvider();
		}

		@Override
		public void run()
		{
			logger.info("Starting Eclipse Serializer Communication Server ...");
			// the whole method may not be synchronized, otherwise a running host could never be stopped
			synchronized(this)
			{
				if(this.isListening())
				{
					// if the host is already running, this method must abort here.
					return;
				}
				this.liveConnectionListener = this.connectionHandler.createConnectionListener(this.address);
			}
			if(!this.stopped)
			{
				logger.info("Eclipse Serializer Communication Server started!");
				this.acceptConnections();
			}
		}
		
		@Override
		public void stop()
		{
			logger.debug("Stopping ComHost...");
			
			this.stopped = true;
			
			if(this.liveConnectionListener == null)
			{
				return;
			}
			
			this.liveConnectionListener.close();
			this.liveConnectionListener = null;
			
			logger.info("ComHost has been stopped");
		}

		@Override
		public synchronized boolean isListening()
		{
			
			if(this.liveConnectionListener != null)
			{
				return this.liveConnectionListener.isAlive();
			}
			
			return false;
		}

		@Override
		public void acceptConnections()
		{
			// repeatedly accept new connections until stopped.
			while(!this.stopped)
			{
				synchronized(this)
				{
					if(!this.isListening())
					{
						break;
					}
					
					this.synchAcceptConnection();
				}
			}
		}
		
		private void synchAcceptConnection()
		{
			final C connection;
			try
			{
				connection = this.liveConnectionListener.listenForConnection();
			}
			catch(final ComException e)
			{
				//intentional, don't stop the host if a connection attempt failed
				logger.error("Failed connection attempt", e);
				return;
			}
			
			this.connectionAcceptor.acceptConnection(connection, this);
		}
	}
	
	
	
	public static <C> ComHostCreator<C> Creator()
	{
		return ComHostCreator.New();
	}
	
}
