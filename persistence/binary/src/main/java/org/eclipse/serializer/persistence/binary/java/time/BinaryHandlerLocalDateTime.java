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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomComposedValueType;

/**
 * Handler for {@link LocalDateTime}, which holds its date and time parts in two references. Where it
 * is a value class, the instance is built through {@link LocalDateTime#of(LocalDate, LocalTime)};
 * see {@link AbstractBinaryHandlerCustomComposedValueType} for the mechanism.
 */
public final class BinaryHandlerLocalDateTime extends AbstractBinaryHandlerCustomComposedValueType<LocalDateTime>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryHandlerLocalDateTime New()
	{
		return new BinaryHandlerLocalDateTime();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerLocalDateTime()
	{
		super(
			LocalDateTime.class,
			Part.New(LocalDate.class, "date", LocalDateTime::toLocalDate),
			Part.New(LocalTime.class, "time", LocalDateTime::toLocalTime)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	protected LocalDateTime createFromParts(final Object[] parts)
	{
		return LocalDateTime.of(
			(LocalDate)parts[0],
			(LocalTime)parts[1]
		);
	}

}
