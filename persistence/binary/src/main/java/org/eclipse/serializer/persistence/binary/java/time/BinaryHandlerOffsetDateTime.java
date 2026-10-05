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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomComposedValueType;

/**
 * Handler for {@link OffsetDateTime}, which holds its date-time and offset in two references. Where
 * it is a value class, the instance is built through
 * {@link OffsetDateTime#of(LocalDateTime, ZoneOffset)}; see
 * {@link AbstractBinaryHandlerCustomComposedValueType} for the mechanism.
 */
public final class BinaryHandlerOffsetDateTime extends AbstractBinaryHandlerCustomComposedValueType<OffsetDateTime>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryHandlerOffsetDateTime New()
	{
		return new BinaryHandlerOffsetDateTime();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerOffsetDateTime()
	{
		super(
			OffsetDateTime.class,
			Part.New(LocalDateTime.class, "dateTime", OffsetDateTime::toLocalDateTime),
			Part.New(ZoneOffset.class   , "offset"  , OffsetDateTime::getOffset      )
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	protected OffsetDateTime createFromParts(final Object[] parts)
	{
		return OffsetDateTime.of(
			(LocalDateTime)parts[0],
			(ZoneOffset)   parts[1]
		);
	}

}
