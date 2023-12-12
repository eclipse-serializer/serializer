package org.eclipse.serializer.communication.tls;

/*-
 * #%L
 * Eclipse Serializer Communication Binary
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
