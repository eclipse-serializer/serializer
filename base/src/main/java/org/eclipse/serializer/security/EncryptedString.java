package org.eclipse.serializer.security;

import static org.eclipse.serializer.util.X.notNull;

/*-
 * #%L
 * Eclipse Serializer Base
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


public interface EncryptedString extends CharSequence
{
	public String decrypt();
	
	
	public static EncryptedString New(final String string)
	{
		return New(
			string,
			StringEncrypter.Static.getProvider().provideStringEncrypter()
		);
	}
	
	public static EncryptedString New(final String string, final StringEncrypter encrypter)
	{
		return new EncryptedString.Default(
			notNull(string),
			notNull(encrypter)
		);
	}
	
	
	public static class Default implements EncryptedString
	{
		private final byte[]                    encryptedData;
		private final transient StringEncrypter encrypter;
		private       transient Integer         length;
	
		Default(final String string, final StringEncrypter encrypter)
		{
			super();
			this.encryptedData = encrypter.encrypt(string);
			this.encrypter     = encrypter                ;
			this.length        = string.length()          ;
		}
		
		Default(final byte[] encryptedData, final StringEncrypter encrypter)
		{
			this.encryptedData = encryptedData;
			this.encrypter     = encrypter    ;
		}
		
		@Override
		public String decrypt()
		{
			return this.encrypter.decrypt(this.encryptedData);
		}
		
		@Override
		public int length()
		{
			if(this.length == null)
			{
				this.length = this.decrypt().length();
			}
			
			return this.length;
		}

		@Override
		public char charAt(final int index)
		{
			return this.decrypt().charAt(index);
		}

		@Override
		public CharSequence subSequence(final int start, final int end)
		{
			return this.decrypt().subSequence(end, end);
		}
		
	}
	
}
