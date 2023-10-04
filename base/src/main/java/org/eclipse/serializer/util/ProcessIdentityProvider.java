package org.eclipse.serializer.util;

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

import java.lang.management.ManagementFactory;

/**
 * Provides an arbitrary identity string that is unique for an individual process across any number of systems.
 *
 */
public interface ProcessIdentityProvider
{
	public String provideProcessIdentity();
	
	
	
	public static String queryProcessIdentity()
	{
		// quickly googled solution that is assumed to be "good enough" until proven otherwise.
		return ManagementFactory.getRuntimeMXBean().getName();
	}
	
	
	
	public static ProcessIdentityProvider New()
	{
		return new Default();
	}
	
	public final class Default implements ProcessIdentityProvider
	{
		Default()
		{
			super();
		}

		@Override
		public String provideProcessIdentity()
		{
			return ProcessIdentityProvider.queryProcessIdentity();
		}
		
	}
}
