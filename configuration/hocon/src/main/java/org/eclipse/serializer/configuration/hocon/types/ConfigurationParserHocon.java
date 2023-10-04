package org.eclipse.serializer.configuration.hocon.types;

/*-
 * #%L
 * Eclipse Serializer Configuration Hocon
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

import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.configuration.types.Configuration.Builder;
import org.eclipse.serializer.configuration.types.ConfigurationParser;

import com.typesafe.config.ConfigFactory;

public interface ConfigurationParserHocon extends ConfigurationParser
{
	public static ConfigurationParserHocon New()
	{
		return new ConfigurationParserHocon.Default(
			ConfigurationMapperHocon.New()
		);
	}
	
	public static ConfigurationParserHocon New(
		final ConfigurationMapperHocon mapper
	)
	{
		return new ConfigurationParserHocon.Default(
			notNull(mapper)
		);
	}
	
	
	public static class Default implements ConfigurationParserHocon
	{
		private final ConfigurationMapperHocon mapper;

		Default(
			final ConfigurationMapperHocon mapper
		)
		{
			super();
			this.mapper = mapper;
		}
		
		@Override
		public Builder parseConfiguration(
			final Builder builder,
			final String  input
		)
		{
			return this.mapper.mapConfiguration(
				builder,
				ConfigFactory.parseString(input).root()
			);
		}
		
	}
	
}
