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

import java.time.ZoneId;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomComposedValueType;

/**
 * Handler for {@code java.time.ZoneRegion}, the package-private {@link ZoneId} implementation for
 * region-based zone ids. It holds nothing but its id; the zone rules are transient and re-resolved
 * from the id.
 * <p>
 * The instance is built completely through {@link ZoneId#of(String)} on every JVM, with its creation
 * deferred until the id can be resolved. Populating instead would leave the rules unresolved, and a
 * complete instance is what handlers building their own instances from a resolved zone need, e.g.
 * for {@code ZonedDateTime}: a blank zone cannot answer for its rules. See
 * {@link AbstractBinaryHandlerCustomComposedValueType} for the mechanism.
 * <p>
 * The persisted form is byte-identical to the one the reflective handling produced, under the same
 * type and member name, so existing data is unaffected.
 */
public final class BinaryHandlerZoneRegion extends AbstractBinaryHandlerCustomComposedValueType<ZoneId>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final String TYPE_NAME = "java.time.ZoneRegion";



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings("unchecked")
	private static Class<ZoneId> handledType()
	{
		try
		{
			// the class is package-private, so it can only be resolved by name. Every instance is a ZoneId.
			return (Class<ZoneId>)Class.forName(TYPE_NAME);
		}
		catch(final ClassNotFoundException e)
		{
			throw new Error("JDK class " + TYPE_NAME + " not found.", e);
		}
	}

	public static BinaryHandlerZoneRegion New()
	{
		return new BinaryHandlerZoneRegion();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerZoneRegion()
	{
		super(
			handledType(),
			Part.New(String.class, "id", ZoneId::getId)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public boolean isCreationDeferred()
	{
		// built from its resolved id instead of being populated, see the type comment.
		return true;
	}

	@Override
	protected ZoneId createFromParts(final Object[] parts)
	{
		return ZoneId.of((String)parts[0]);
	}

}
