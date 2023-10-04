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

public class ConfigurationExceptionValueMappingFailed extends ConfigurationException
{
	private final String key  ;
	private final String value;
	
	public ConfigurationExceptionValueMappingFailed(
		final Configuration configuration,
		final String key,
		final String value
	)
	{
		super(configuration);
		this.key   = key;
		this.value = value;
	}

	public ConfigurationExceptionValueMappingFailed(
		final Configuration configuration,
		final Throwable cause,
		final String key,
		final String value
	)
	{
		super(configuration, cause);
		this.key   = key;
		this.value = value;
	}
	
	
	public String key()
	{
		return this.key;
	}
	
	public String value()
	{
		return this.value;
	}
	
}
