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

import static org.eclipse.serializer.util.X.notNull;

import java.io.Closeable;

import org.eclipse.serializer.persistence.types.PersistenceManager;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public interface ComChannel extends Closeable
{
	public Object receive();
	
	public void send(Object graphRoot);
	
	public default Object request(final Object graphRoot)
	{
		synchronized(this)
		{
			this.send(graphRoot);
			return this.receive();
		}
	}
	
	@Override
	public void close();
	
	
	
	public static ComChannel New(final PersistenceManager<?> persistenceManager)
	{
		return new ComChannel.Default(
			notNull(persistenceManager)
		);
	}
		
	public class Default implements ComChannel
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private final static Logger logger = Logging.getLogger(Default.class);

		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceManager<?> persistenceManager;
		
			
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final PersistenceManager<?> persistenceManager)
		{
			super();
			this.persistenceManager = persistenceManager;
		}
				
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final void send(final Object graphRoot)
		{
			/*
			 * "store" is a little unfitting here.
			 * However, technically, it is correct. The graph is "stored" (written) to the network connection.
			 */
			
			logger.trace("sending data");
			this.persistenceManager.store(graphRoot);
			logger.trace("sended data successfully");
		}
		
		@Override
		public final Object receive()
		{
			/*
			 * in the context of a network connection, the generic get() means
			 * receive whatever the other side is sending.
			 */
			
			logger.trace("waiting for data");
			final Object received = this.persistenceManager.get();
			logger.trace("data received successfully");
			
			return received;
		}
		
		@Override
		public final void close()
		{
			logger.trace("closing ComChannel");
			this.persistenceManager.close();
		}
		
	}
	
}
