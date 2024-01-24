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

import org.eclipse.serializer.persistence.types.PersistenceManager;

/**
 * 
 * @param <C> the communication layer type
 */
public interface ComHostChannel<C> extends ComChannel
{
	public C connection();
	
	public ComProtocol protocol();
	
	public ComHost<C> parent();

	
	
	public static <C> ComHostChannel<C> New(
		final PersistenceManager<?> persistenceManager,
		final C                     connection        ,
		final ComProtocol           protocol          ,
		final ComHost<C>            parent
	)
	{
		return new ComHostChannel.Default<>(
			notNull(persistenceManager),
			notNull(connection)        ,
			notNull(protocol)          ,
			notNull(parent)
		);
	}
	
	public final class Default<C>
	extends ComChannel.Default
	implements ComHostChannel<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final C           connection;
		private final ComProtocol protocol  ;
		private final ComHost<C>  parent    ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceManager<?> persistenceManager,
			final C                     connection        ,
			final ComProtocol           protocol          ,
			final ComHost<C>            parent
		)
		{
			super(persistenceManager);
			this.connection = connection;
			this.protocol   = protocol  ;
			this.parent     = parent    ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final C connection()
		{
			return this.connection;
		}
		
		@Override
		public final ComProtocol protocol()
		{
			return this.protocol;
		}
		
		@Override
		public final ComHost<C> parent()
		{
			return this.parent;
		}
		
	}
	
}
