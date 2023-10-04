package org.eclipse.serializer.configuration.yaml.types;

/*-
 * #%L
 * Eclipse Serializer Configuration YAML
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
import org.eclipse.serializer.configuration.types.ConfigurationMapperMap;
import org.eclipse.serializer.configuration.types.ConfigurationParser;
import org.yaml.snakeyaml.Yaml;

public interface ConfigurationParserYaml extends ConfigurationParser
{
	public static ConfigurationParserYaml New()
	{
		return new ConfigurationParserYaml.Default(
			ConfigurationMapperMap.New()
		);
	}
	
	public static ConfigurationParserYaml New(
		final ConfigurationMapperMap mapper
	)
	{
		return new ConfigurationParserYaml.Default(
			notNull(mapper)
		);
	}
	
	
	public static class Default implements ConfigurationParserYaml
	{
		private final ConfigurationMapperMap mapper;
		
		Default(
			final ConfigurationMapperMap mapper
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
				new Yaml().load(input)
			);
		}
		
	}
}
