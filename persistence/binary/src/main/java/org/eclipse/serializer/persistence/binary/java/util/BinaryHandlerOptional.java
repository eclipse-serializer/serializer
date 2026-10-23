package org.eclipse.serializer.persistence.binary.java.util;

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

import java.util.Optional;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomComposedValueType;

/**
 * Handler for {@link Optional}, which holds its content in a single reference. Where it is a value
 * class, the instance is built through {@link Optional#ofNullable(Object)}; see
 * {@link AbstractBinaryHandlerCustomComposedValueType} for the mechanism.
 * <p>
 * An absent value needs no marker of its own: a null reference reads back as
 * {@link Optional#empty()}, which is what {@link Optional#ofNullable(Object)} answers for it.
 */
public final class BinaryHandlerOptional extends AbstractBinaryHandlerCustomComposedValueType<Optional<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<Optional<?>> handledType()
	{
		// no way to get ".class" of a parameterized type otherwise
		return (Class)Optional.class;
	}

	public static BinaryHandlerOptional New()
	{
		return new BinaryHandlerOptional();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerOptional()
	{
		super(
			handledType(),
			Part.New(Object.class, "value", optional -> optional.orElse(null))
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	protected Optional<?> createFromParts(final Object[] parts)
	{
		return Optional.ofNullable(parts[0]);
	}

}
