package org.eclipse.serializer.persistence.binary.java.time;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
 * %%
 * Copyright (C) 2023 - 2026 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.time.Duration;

/**
 * Handler for {@link Duration}, which holds its state in a second and a nano-of-second value. Where
 * it is a value class, the instance is built through {@link Duration#ofSeconds(long, long)}; see
 * {@link AbstractBinaryHandlerSecondsNanos} for the mechanism.
 */
public final class BinaryHandlerDuration extends AbstractBinaryHandlerSecondsNanos<Duration>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryHandlerDuration New()
	{
		return new BinaryHandlerDuration();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerDuration()
	{
		super(Duration.class, Duration::getSeconds, Duration::getNano);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	protected Duration createFromValues(final long seconds, final int nanos)
	{
		return Duration.ofSeconds(seconds, nanos);
	}

}
