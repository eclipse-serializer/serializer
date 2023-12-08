package org.eclipse.serializer.communication.tls;

import javax.net.ssl.SSLParameters;

public interface TLSParametersProvider
{
	/**
	 * Provides the SSLParameters Object for the SSLEngine
	 * 
	 * @return SSLParameters
	 */
	SSLParameters getSSLParameters();
	
	/**
	 * provide the SSL protocol as defined in <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#SSLContext">Standard Algorithm Name Documentation</a>
	 * 
	 * @return SSL protocol
	 */
	String getSSLProtocol();

	/**
	 * Timeout for read operations during the TLS handshake in milliseconds
	 * 
	 * @return returns the timeout for the TLS handshake
	 */
	int getHandshakeReadTimeOut();
	
	/**
	 * 
	 * Provides a nearly empty SSLParameters object.
	 * <p>
	 * all configuration values are null except
	 * <p>
	 * needClientAuth = true
	 *
	 */
	public final class Default implements TLSParametersProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private static final String TLS_PROTOCOL_STRING        = "TLSv1.2";
		private static final int    SSL_HANDSHAKE_READ_TIMEOUT = 1000;
		
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
		public SSLParameters getSSLParameters()
		{
			final SSLParameters sslParameters = new SSLParameters();
			sslParameters.setNeedClientAuth(true);
						
			return sslParameters;
		}
		
		@Override
		public String getSSLProtocol()
		{
			return Default.TLS_PROTOCOL_STRING;
		}


		@Override
		public int getHandshakeReadTimeOut()
		{
			return SSL_HANDSHAKE_READ_TIMEOUT;
		}
		
	}

}
