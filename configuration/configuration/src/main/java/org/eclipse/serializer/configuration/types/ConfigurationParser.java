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
 * A utility interface to parse values from external formats to a {@link Configuration#Builder()}.
 *
 * @see Configuration.Builder#load(ConfigurationLoader, ConfigurationParser)
 * @see ConfigurationMapper
 */
@FunctionalInterface
public interface ConfigurationParser
{
	/**
	 * Creates a {@link Configuration#Builder()} and adds all entries contained in the given input.
	 * 
	 * @param input the source to parse the entries from
	 * @return a new {@link Configuration#Builder()}
	 */
	public default Configuration.Builder parseConfiguration(final String input)
	{
		return this.parseConfiguration(
			Configuration.Builder(),
			input
		);
	}
	
	/**
	 * Parses all entries contained in the input to the given {@link Configuration#Builder()}.
	 * 
	 * @param builder the builder to map the entries to
	 * @param input the source to parse the entries from
	 * @return the given {@link Configuration#Builder()}
	 */
	public Configuration.Builder parseConfiguration(Configuration.Builder builder, String input);
		
}
