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
import org.eclipse.serializer.typing.Immutable;

public interface ComProtocol extends ComProtocolData
{
	public static String protocolName()
	{
		return "ECLIPSE-SERIALIZER-COMCHANNEL";
	}
	
	public static String protocolVersion()
	{
		return "1.0";
	}
				
	public static ComProtocolCreator Creator()
	{
		return ComProtocolCreator.New();
	}
	
	public static ComProtocol New(
		final String                        name             ,
		final String                        version          ,
		final ByteOrder                     byteOrder        ,
		final int                           inactivityTimeout,
		final PersistenceIdStrategy         idStrategy       ,
		final PersistenceTypeDictionaryView persistenceTypeDictionaryView
	)
	{
		return new ComProtocol.Default(
			notNull(name)      ,
			notNull(version)   ,
			notNull(byteOrder) ,
			inactivityTimeout  ,
			notNull(idStrategy),
			persistenceTypeDictionaryView
		);
	}
	
	public final class Default implements ComProtocol, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String                        name             ;
		private final String                        version          ;
		private final ByteOrder                     byteOrder        ;
		private final int                           inactivityTimeOut;
		private final PersistenceIdStrategy         idStrategy       ;
		private final PersistenceTypeDictionaryView typeDictionary   ;
		
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final String                        name             ,
			final String                        version          ,
			final ByteOrder                     byteOrder        ,
			final int                           inactivityTimeOut,
			final PersistenceIdStrategy         idStrategy       ,
			final PersistenceTypeDictionaryView typeDictionary
			
		)
		{
			super();
			this.name              = name             ;
			this.version           = version          ;
			this.byteOrder         = byteOrder        ;
			this.inactivityTimeOut = inactivityTimeOut;
			this.idStrategy        = idStrategy       ;
			this.typeDictionary    = typeDictionary   ;
			
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
			return this.typeDictionary;
		}
		
		@Override
		public final int inactivityTimeout()
		{
			return this.inactivityTimeOut;
		}
		
	}
		
}
