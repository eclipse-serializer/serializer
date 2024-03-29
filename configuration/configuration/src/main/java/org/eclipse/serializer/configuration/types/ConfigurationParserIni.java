package org.eclipse.serializer.configuration.types;

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

import static org.eclipse.serializer.util.X.notNull;

import java.util.HashMap;
import java.util.Map;


/**
 * INI format parser for configurations.
 * 
 */
public interface ConfigurationParserIni extends ConfigurationParser
{
	/**
	 * Pseudo-constructor to create a new INI parser.
	 * 
	 * @return a new INI parser
	 */
	public static ConfigurationParserIni New()
	{
		return new ConfigurationParserIni.Default(
			ConfigurationMapperMap.New()
		);
	}
	
	/**
	 * Pseudo-constructor to create a new INI parser.
	 * 
	 * @param mapper a custom mapper
	 * @return a new INI parser
	 */
	public static ConfigurationParserIni New(
		final ConfigurationMapperMap mapper
	)
	{
		return new ConfigurationParserIni.Default(
			notNull(mapper)
		);
	}
	
	
	public static class Default implements ConfigurationParserIni
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
		public Configuration.Builder parseConfiguration(
			final Configuration.Builder builder,
			final String  input
		)
		{
			final Map<String, String> map = new HashMap<>();
			
			nextLine:
			for(String line : input.split("\\r?\\n"))
			{
				line = line.trim();
				if(line.isEmpty())
				{
					continue nextLine;
				}

				switch(line.charAt(0))
				{
					case '#': // comment
					case ';': // comment
					case '[': // section
						continue nextLine;
					default: // fall-through
				}

				final int separatorIndex = line.indexOf('=');
				if(separatorIndex == -1)
				{
					continue nextLine; // no key=value pair, ignore
				}

				final String key   = line.substring(0, separatorIndex).trim();
				final String value = line.substring(separatorIndex + 1).trim();
				map.put(key, value);
			}
			
			return this.mapper.mapConfiguration(builder, map);
		}
		
	}
}
