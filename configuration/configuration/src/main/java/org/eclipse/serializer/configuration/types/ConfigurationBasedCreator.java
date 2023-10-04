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

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface ConfigurationBasedCreator<T>
{
	public Class<T> resultType();
	
	public T create(Configuration configuration);
	
	
	@SuppressWarnings("unchecked") // type-safety ensured by logic
	public static <T> List<ConfigurationBasedCreator<T>> registeredCreators(
		final Class<T> resultType
	)
	{
		return StreamSupport.stream(
			ServiceLoader.load(ConfigurationBasedCreator.class).spliterator(),
			false
		)
		.filter(creator -> resultType.isAssignableFrom(creator.resultType()))
		.map(c -> (ConfigurationBasedCreator<T>)c)
		.collect(Collectors.toList());
	}
	
	
	public static abstract class Abstract<T> implements ConfigurationBasedCreator<T>
	{
		private final Class<T> resultType;

		protected Abstract(
			final Class<T> resultType
		)
		{
			super();
			this.resultType = notNull(resultType);
		}
		
		@Override
		public Class<T> resultType()
		{
			return this.resultType;
		}
				
	}
	
}
