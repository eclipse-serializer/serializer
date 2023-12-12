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

import java.nio.ByteOrder;

import org.eclipse.serializer.persistence.types.PersistenceIdStrategy;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryView;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryViewProvider;
import org.eclipse.serializer.typing.Immutable;

/**
 * 
 * @param <C> the communication layer type
 */
public interface ComProtocolProvider<C> extends ComProtocolData
{
	public ComProtocol provideProtocol(C connection);
	
	
	
	public static <C> ComProtocolProviderCreator<C> Creator()
	{
		return ComProtocolProviderCreator.New();
	}
	
	public static <C> ComProtocolProvider<C> New(
		final String                                name                  ,
		final String                                version               ,
		final ByteOrder                             byteOrder             ,
		final int                                   inactivityTimeout     ,
		final PersistenceIdStrategy                 idStrategy            ,
		final PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
		final ComProtocolCreator                    protocolCreator
	)
	{
		return new ComProtocolProvider.Default<>(
			notNull(name)                  ,
			notNull(version)               ,
			notNull(byteOrder)             ,
			inactivityTimeout              ,
			notNull(idStrategy)            ,
			notNull(typeDictionaryProvider),
			notNull(protocolCreator)
		);
	}
	
	public final class Default<C> implements ComProtocolProvider<C>, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String                                name                  ;
		private final String                                version               ;
		private final ByteOrder                             byteOrder             ;
		private final int                                   inactivityTimeout     ;
		private final PersistenceIdStrategy                 idStrategy            ;
		private final PersistenceTypeDictionaryViewProvider typeDictionaryProvider;
		private final ComProtocolCreator                    protocolCreator       ;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final String                                name                  ,
			final String                                version               ,
			final ByteOrder                             byteOrder             ,
			final int                                   inactivityTimeout     ,
			final PersistenceIdStrategy                 idStrategy            ,
			final PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
			final ComProtocolCreator                    protocolCreator
		)
		{
			
			super();
			this.name                   = name                  ;
			this.version                = version               ;
			this.byteOrder              = byteOrder             ;
			this.idStrategy             = idStrategy            ;
			this.typeDictionaryProvider = typeDictionaryProvider;
			this.protocolCreator        = protocolCreator       ;
			this.inactivityTimeout      = inactivityTimeout     ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String name()
		{
			return this.name;
		}

		@Override
		public final String version()
		{
			return this.version;
		}

		@Override
		public final ByteOrder byteOrder()
		{
			return this.byteOrder;
		}

		@Override
		public final PersistenceIdStrategy idStrategy()
		{
			return this.idStrategy;
		}
		
		@Override
		public final PersistenceTypeDictionaryView typeDictionary()
		{
			return this.typeDictionaryProvider.provideTypeDictionary();
		}
		
		@Override
		public int inactivityTimeout()
		{
			return this.inactivityTimeout;
		}
		
		@Override
		public ComProtocol provideProtocol(final C connection)
		{
			// the default implementation assigns the same id range to every client, hence no reference to connection
			return this.protocolCreator.creatProtocol(
				this.name()             ,
				this.version()          ,
				this.byteOrder()        ,
				this.inactivityTimeout(),
				this.idStrategy()       ,
				this.typeDictionary()
			);
		}
		
	}
		
}
