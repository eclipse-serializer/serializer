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


public class ConfigurationExceptionNoValueMapperFound extends ConfigurationException
{
	private final Class<?> type;
	
	public ConfigurationExceptionNoValueMapperFound(
		final Configuration configuration,
		final Class<?>      type
		
	)
	{
		super(
			configuration,
			"No configuration value mapper found for type " + type.getName()
		);
		this.type = type;
	}
	
	public Class<?> type()
	{
		return this.type;
	}
	
}
