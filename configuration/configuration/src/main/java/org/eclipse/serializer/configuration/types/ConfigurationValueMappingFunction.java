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

import org.eclipse.serializer.configuration.exceptions.ConfigurationExceptionValueMappingFailed;

/**
 * Function which maps String values from {@link Configuration}s to a certain type.
 *
 * @param <T> the target type
 */
@FunctionalInterface
public interface ConfigurationValueMappingFunction<T>
{
	/**
	 * Maps the given value of a {@link Configuration} to the target type.
	 * 
	 * @param config source configuration
	 * @param key the assigned key
	 * @param value the value to map
	 * @return the mapped value
	 * @throws ConfigurationExceptionValueMappingFailed if the mapping failed
	 */
	public T map(Configuration config, String key, String value);
}
