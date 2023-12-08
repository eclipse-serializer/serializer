package org.eclipse.serializer.communication.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

import org.eclipse.serializer.com.ComException;

public interface TLSKeyManagerProvider
{
	KeyManager[] get();
	
	/**
	 * uses system default key manager
	 */
	public class Default implements TLSKeyManagerProvider
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
		public KeyManager[] get()
		{
			return null;
		}
	}
	
	/**
	 * 
	 * Provide a PKCS12 KeyManger
	 *
	 */
	public class PKCS12 implements TLSKeyManagerProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final KeyManagerFactory keyManagerFactory;
		
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
				
				this.keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
				
				try
				{
					this.keyManagerFactory.init(keyStore, password);
				}
				catch (UnrecoverableKeyException | KeyStoreException e)
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
		public KeyManager[] get()
		{
			return this.keyManagerFactory.getKeyManagers();
		}
		
	}
}
