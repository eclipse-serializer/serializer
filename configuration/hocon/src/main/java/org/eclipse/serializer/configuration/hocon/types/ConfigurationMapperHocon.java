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

import org.eclipse.serializer.configuration.types.Configuration;
import org.eclipse.serializer.configuration.types.Configuration.Builder;
import org.eclipse.serializer.configuration.types.ConfigurationMapper;

import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

public interface ConfigurationMapperHocon extends ConfigurationMapper<ConfigObject>
{
	public static ConfigurationMapperHocon New()
	{
		return new ConfigurationMapperHocon.Default();
	}
	
	
	public static class Default implements ConfigurationMapperHocon
	{
		Default()
		{
			super();
		}
		
		@Override
		public Builder mapConfiguration(
			final Builder      builder,
			final ConfigObject source
		)
		{
			this.mapConfiguration(builder, source, "");
			
			return builder;
		}
		
		private void mapConfiguration(
			final Builder      builder,
			final ConfigObject source ,
			final String       prefix
		)
		{
			source.entrySet().forEach(e ->
			{
				final String      key   = prefix.concat(e.getKey());
				final ConfigValue value = e.getValue();
				if(value instanceof ConfigObject)
				{
					this.mapConfiguration(
						builder,
						(ConfigObject)value,
						key + Configuration.KEY_SEPARATOR
					);
				}
				else if(value != null)
				{
					builder.set(key, value.unwrapped().toString());
				}
			});
		}
		
	}
	
}
