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

/**
 * A utility interface to map values from arbitrary sources to a {@link Configuration#Builder()}.
 *
 * @param <S> the source type
 * @see Configuration.Builder#map(ConfigurationMapper, Object)
 * @see ConfigurationParser
 */
public interface ConfigurationMapper<S>
{
	/**
	 * Creates a {@link Configuration#Builder()} and adds all entries contained in the given source.
	 * 
	 * @param source the source to take the entries from
	 * @return a new {@link Configuration#Builder()}
	 */
	public default Configuration.Builder mapConfiguration(final S source)
	{
		return this.mapConfiguration(
			Configuration.Builder(),
			source
		);
	}
	
	/**
	 * Adds all entries contained in a source to the given {@link Configuration#Builder()}.
	 * 
	 * @param builder the builder to map the entries to
	 * @param source the source to take the entries from
	 * @return the given {@link Configuration#Builder()}
	 */
	public Configuration.Builder mapConfiguration(Configuration.Builder builder, S source);
	
}
