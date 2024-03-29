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

import java.util.function.Function;

import org.eclipse.serializer.collections.types.XGettingTable;

/**
 * Mapper which maps entries from a {@link XGettingTable} to a {@link Configuration#Builder()}.
 * 
 * @param <V> the value type
 */
public interface ConfigurationMapperTable<V> extends ConfigurationMapper<XGettingTable<String, V>>
{
	/**
	 * Pseudo-constructor to create a new mapper.
	 * 
	 * @param <V> the value type
	 * @return a new mapper
	 */
	public static <V> ConfigurationMapperTable<V> New()
	{
		return new ConfigurationMapperTable.Default<>(Object::toString);
	}
	
	/**
	 * Pseudo-constructor to create a new mapper.
	 * 
	 * @param <V> the value type
	 * @param toStringMapper function which converts values from the table to String values
	 * @return a new mapper
	 */
	public static <V> ConfigurationMapperTable<V> New(
		final Function<Object, String> toStringMapper
	)
	{
		return new ConfigurationMapperTable.Default<>(
			notNull(toStringMapper)
		);
	}
	
	
	public static class Default<V> implements ConfigurationMapperTable<V>
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
			final XGettingTable<String, V> source
		)
		{
			this.mapConfiguration(builder, source, "");
			
			return builder;
		}
		
		@SuppressWarnings("unchecked")
		private void mapConfiguration(
			final Configuration.Builder builder,
			final XGettingTable<String, V> source ,
			final String                   prefix
		)
		{
			source.iterate(kv ->
			{
				final String key   = prefix.concat(kv.key());
				final Object value = kv.value();
				if(value instanceof XGettingTable)
				{
					this.mapConfiguration(
						builder,
						(XGettingTable<String, V>)value,
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
