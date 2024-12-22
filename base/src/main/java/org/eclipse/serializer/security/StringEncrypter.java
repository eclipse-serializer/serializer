package org.eclipse.serializer.security;

public interface StringEncrypter
{
	public byte[] encrypt(String string);
	
	public String decrypt(byte[] bytes);
	
	
	public static interface Provider
	{
		public StringEncrypter provideStringEncrypter();
	}
	
	
	public final static class Static
	{
		private static StringEncrypter.Provider provider;
		
		public static void setProvider(final StringEncrypter.Provider provider)
		{
			Static.provider = provider;
		}
		
		public static StringEncrypter.Provider getProvider()
		{
			return provider;
		}
		
		
		private Static()
		{
			throw new UnsupportedOperationException();
		}
		
	}
	
}
