package org.eclipse.serializer.communication.types;

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
