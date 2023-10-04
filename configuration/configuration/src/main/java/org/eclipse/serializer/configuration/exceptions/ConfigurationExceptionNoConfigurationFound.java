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

public class ConfigurationExceptionNoConfigurationFound extends ConfigurationException
{
	public ConfigurationExceptionNoConfigurationFound(
		final String message,
		final Throwable cause,
		final boolean enableSuppression,
		final boolean writableStackTrace
	)
	{
		super(null, message, cause, enableSuppression, writableStackTrace);
	}

	public ConfigurationExceptionNoConfigurationFound(
		final String message,
		final Throwable cause
	)
	{
		super(null, message, cause);
	}

	public ConfigurationExceptionNoConfigurationFound(
		final String message
	)
	{
		super(null, message);
	}

	public ConfigurationExceptionNoConfigurationFound(
		final Throwable cause
	)
	{
		super(null, cause);
	}

	public ConfigurationExceptionNoConfigurationFound()
	{
		super(null);
	}
	
}
