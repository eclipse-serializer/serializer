package org.eclipse.serializer.communication.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.serializer.com.ComException;

public interface TLSTrustManagerProvider
{
	TrustManager[] get();
	
	/**
	 * uses system default trust manager
	 */
	public class Default implements TLSTrustManagerProvider
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
		public TrustManager[] get()
		{
			return null;
		}
	}
	
	/**
	 * 
	 * Provide a PKCS12 TrustManager
	 *
	 */
	public class PKCS12 implements TLSTrustManagerProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final TrustManagerFactory trustManagerFactory;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public PKCS12(final Path path, final char[] password)
		{
			final KeyStore keyStore;
			
			try
			{
				keyStore = KeyStore.getInstance("pkcs12");
			}
			catch (final KeyStoreException e)
			{
				throw new ComException("failed to create KeyStore instance", e);
			}
						
			try
			{
				keyStore.load(new FileInputStream(path.toString()), password);
				
				this.trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
				
				try
				{
					this.trustManagerFactory.init(keyStore);
				}
				catch (final KeyStoreException e)
				{
					throw new ComException("failed to initializeKey ManagerFactory", e);
				}
				
			}
			catch (NoSuchAlgorithmException | CertificateException | IOException e)
			{
				throw new ComException("failed to load keys from file", e);
			}
		}

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public TrustManager[] get()
		{
			return this.trustManagerFactory.getTrustManagers();
		}
	}
}
