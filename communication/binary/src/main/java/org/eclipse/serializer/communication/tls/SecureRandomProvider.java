package org.eclipse.serializer.communication.tls;

import java.security.SecureRandom;

public interface SecureRandomProvider
{
	public SecureRandom get();
	
	/**
	 *  returns a null secureRandom to use the system default SecureRandom
	 *
	 */
	public final class Default implements SecureRandomProvider
	{
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
		public SecureRandom get()
		{
			//to use system default return null
			return null;
		}


		
	}
}
