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

import java.time.Instant;

/**
 * Handler for {@link Instant}, which holds its state in a second and a nano-of-second value. Where it
 * is a value class, the instance is built through {@link Instant#ofEpochSecond(long, long)}; see
 * {@link AbstractBinaryHandlerSecondsNanos} for the mechanism.
 */
public final class BinaryHandlerInstant extends AbstractBinaryHandlerSecondsNanos<Instant>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryHandlerInstant New()
	{
		return new BinaryHandlerInstant();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerInstant()
	{
		super(Instant.class, Instant::getEpochSecond, Instant::getNano);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	protected Instant createFromValues(final long seconds, final int nanos)
	{
		return Instant.ofEpochSecond(seconds, nanos);
	}

}
