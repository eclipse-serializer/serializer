package org.eclipse.serializer.configuration.exceptions;

/*-
 * #%L
 * Eclipse Serializer Configuration
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

import org.eclipse.serializer.configuration.types.Configuration;
import org.eclipse.serializer.exceptions.BaseException;

public class ConfigurationException extends BaseException
{
	private final Configuration configuration;
	
	public ConfigurationException(final Configuration configuration)
	{
		super();
		this.configuration = configuration;
	}

	public ConfigurationException(final Configuration configuration, final Throwable cause)
	{
		super(cause);
		this.configuration = configuration;
	}

	public ConfigurationException(final Configuration configuration, final String message)
	{
		super(message);
		this.configuration = configuration;
	}

	public ConfigurationException(final Configuration configuration, final String message, final Throwable cause)
	{
		super(message, cause);
		this.configuration = configuration;
	}

	public ConfigurationException(
		final Configuration configuration     ,
		final String        message           ,
		final Throwable     cause             ,
		final boolean       enableSuppression ,
		final boolean       writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.configuration = configuration;
	}
	
	public Configuration configuration()
	{
		return this.configuration;
	}
	
}
