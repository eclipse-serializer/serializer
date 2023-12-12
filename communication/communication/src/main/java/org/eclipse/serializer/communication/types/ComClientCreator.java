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

import java.net.InetSocketAddress;

/**
 * 
 * @param <C> the communication layer type
 */
@FunctionalInterface
public interface ComClientCreator<C>
{
	public ComClient.Default<C> createClient(
		InetSocketAddress          hostAddress       ,
		ComConnectionHandler<C>    connectionHandler ,
		ComProtocolStringConverter protocolParser    ,
		ComPersistenceAdaptor<C>   persistenceAdaptor,
		int                        inactivityTimeOut
	);
	
	
	public static <C> ComClientCreator.Default<C> New()
	{
		return new ComClientCreator.Default<>();
	}
	
	public final class Default<C> implements ComClientCreator<C>
	{
		@Override
		public ComClient.Default<C> createClient(
			final InetSocketAddress          hostAddress       ,
			final ComConnectionHandler<C>    connectionHandler ,
			final ComProtocolStringConverter protocolParser    ,
			final ComPersistenceAdaptor<C>   persistenceAdaptor,
			final int                        inactivityTimeOut
		)
		{
			return ComClient.New(
				notNull(hostAddress)       ,
				notNull(connectionHandler) ,
				notNull(protocolParser)    ,
				notNull(persistenceAdaptor),
				inactivityTimeOut
			);
		}
		
	}
}
