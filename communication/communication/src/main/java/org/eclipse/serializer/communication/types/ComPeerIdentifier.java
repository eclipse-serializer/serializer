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

import java.nio.ByteBuffer;

import org.eclipse.serializer.chars.XChars;

public interface ComPeerIdentifier
{

	public static ComPeerIdentifier New()
	{
		return new ComPeerIdentifier.Default();
	}

	public ByteBuffer getBuffer();
	
	public class Default implements ComPeerIdentifier
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private final String peerIdentifierString = "Eclipse Serializer OGC Client";
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default()
		{
			super();
		}

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public ByteBuffer getBuffer()
		{
			return XChars.standardCharset().encode(this.peerIdentifierString);
		}
		
	}
	
}
