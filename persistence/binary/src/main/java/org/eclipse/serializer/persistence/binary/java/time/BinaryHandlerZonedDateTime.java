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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomComposedValueType;

/**
 * Handler for {@link ZonedDateTime}, which holds its date-time, offset and zone in three references.
 * Where it is a value class, the instance is built through
 * {@link ZonedDateTime#ofInstant(LocalDateTime, ZoneOffset, ZoneId)}: for a stored state whose
 * offset is valid for its zone that reproduces the state exactly, and should the zone's rules have
 * changed since storing, it preserves the instant instead of failing. Asking the zone for its rules
 * requires a complete zone instance, which its handler's deferred creation provides. See
 * {@link AbstractBinaryHandlerCustomComposedValueType} for the mechanism.
 */
public final class BinaryHandlerZonedDateTime extends AbstractBinaryHandlerCustomComposedValueType<ZonedDateTime>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryHandlerZonedDateTime New()
	{
		return new BinaryHandlerZonedDateTime();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerZonedDateTime()
	{
		super(
			ZonedDateTime.class,
			Part.New(LocalDateTime.class, "dateTime", ZonedDateTime::toLocalDateTime),
			Part.New(ZoneOffset.class   , "offset"  , ZonedDateTime::getOffset      ),
			Part.New(ZoneId.class       , "zone"    , ZonedDateTime::getZone        )
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	protected ZonedDateTime createFromParts(final Object[] parts)
	{
		return ZonedDateTime.ofInstant(
			(LocalDateTime)parts[0],
			(ZoneOffset)   parts[1],
			(ZoneId)       parts[2]
		);
	}

}
