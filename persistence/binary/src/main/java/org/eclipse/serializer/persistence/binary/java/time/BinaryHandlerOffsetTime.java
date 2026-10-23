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

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomComposedValueType;

/**
 * Handler for {@link OffsetTime}, which holds its time and offset in two references. Where it is a
 * value class, the instance is built through {@link OffsetTime#of(LocalTime, ZoneOffset)}; see
 * {@link AbstractBinaryHandlerCustomComposedValueType} for the mechanism.
 */
public final class BinaryHandlerOffsetTime extends AbstractBinaryHandlerCustomComposedValueType<OffsetTime>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryHandlerOffsetTime New()
	{
		return new BinaryHandlerOffsetTime();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerOffsetTime()
	{
		super(
			OffsetTime.class,
			Part.New(LocalTime.class , "time"  , OffsetTime::toLocalTime),
			Part.New(ZoneOffset.class, "offset", OffsetTime::getOffset  )
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	protected OffsetTime createFromParts(final Object[] parts)
	{
		return OffsetTime.of(
			(LocalTime) parts[0],
			(ZoneOffset)parts[1]
		);
	}

}
