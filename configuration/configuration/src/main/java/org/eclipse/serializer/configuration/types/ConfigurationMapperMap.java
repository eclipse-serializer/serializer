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

import java.util.Map;
import java.util.function.Function;

/**
 * Mapper which maps entries from a {@link Map} to a {@link Configuration#Builder()}.
 *
 */
public interface ConfigurationMapperMap extends ConfigurationMapper<Map<String, ?>>
{
	/**
	 * Pseudo-constructor to create a new mapper.
	 * 
	 * @return a new mapper
	 */
	public static ConfigurationMapperMap New()
	{
		return new ConfigurationMapperMap.Default(Object::toString);
	}
	
	/**
	 * Pseudo-constructor to create a new mapper.
	 * 
	 * @param toStringMapper function which converts values from the map to String values
	 * @return a new mapper
	 */
	public static ConfigurationMapperMap New(
		final Function<Object, String> toStringMapper
	)
	{
		return new ConfigurationMapperMap.Default(
			notNull(toStringMapper)
		);
	}
	
	
	public static class Default implements ConfigurationMapperMap
	{
		private final Function<Object, String> toStringMapper;
		
		Default(
			final Function<Object, String> toStringMapper
		)
		{
			super();
			this.toStringMapper = toStringMapper;
		}
		
		@Override
		public Configuration.Builder mapConfiguration(
			final Configuration.Builder builder,
			final Map<String, ?> source
		)
		{
			this.mapConfiguration(builder, source, "");
			
			return builder;
		}
		
		@SuppressWarnings("unchecked")
		private void mapConfiguration(
			final Configuration.Builder builder,
			final Map<String, ?> source ,
			final String         prefix
		)
		{
			source.entrySet().forEach(e ->
			{
				final String key   = prefix.concat(e.getKey());
				final Object value = e.getValue();
				if(value instanceof Map)
				{
					this.mapConfiguration(
						builder,
						(Map<String, ?>)value,
						key + Configuration.KEY_SEPARATOR
					);
				}
				else if(value != null)
				{
					builder.set(
						key,
						this.toStringMapper.apply(value)
					);
				}
			});
		}
		
	}
	
}
