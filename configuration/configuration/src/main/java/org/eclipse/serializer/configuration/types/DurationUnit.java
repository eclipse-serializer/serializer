
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

import java.time.Duration;
import java.util.function.Function;

/**
 * Enumeration of time durations at a given unit of granularity.
 *
 */
public enum DurationUnit
{
	/**
	 * Nanoseconds
	 */
	NS (Duration::ofNanos  ),
	
	/**
	 * Milliseconds
	 */
	MS (Duration::ofMillis ),
	
	/**
	 * Seconds
	 */
	S  (Duration::ofSeconds),
	
	/**
	 * Minutes
	 */
	M  (Duration::ofMinutes),
	
	/**
	 * Hours
	 */
	H  (Duration::ofHours  ),
	
	/**
	 * Days
	 */
	D  (Duration::ofDays   );
	
	
	private final Function<Long, Duration> creator;

	private DurationUnit(
		final Function<Long, Duration> creator
	)
	{
		this.creator = creator;
	}
	
	public Duration create(final long amount)
	{
		return this.creator.apply(amount);
	}
	
}
