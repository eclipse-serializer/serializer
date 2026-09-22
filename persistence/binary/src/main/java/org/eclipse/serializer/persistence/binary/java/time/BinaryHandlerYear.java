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

import java.time.Year;

import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.reflect.XReflect;

/**
 * Handler for {@link Year}.
 * <p>
 * Where {@link Year} is a value class, its instances cannot be created empty and populated
 * afterwards: the population write is not reliably visible on an identity-less instance. It is
 * therefore built from its persisted value through {@link Year#of(int)}.
 * <p>
 * The instance is created complete either way, since a plugin reusing the value-type handlers (e.g.
 * the REST viewer) relies on {@link #create} alone. Where the type is an ordinary class, an already
 * registered instance is still populated in {@link #updateState}, preserving the update behavior the
 * reflective handling had.
 * <p>
 * The persisted form is byte-identical to the one the reflective handling produced, under the same
 * type and member name, so existing data is unaffected.
 */
public final class BinaryHandlerYear extends AbstractBinaryHandlerCustomNonReferentialFixedLength<Year>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final long
		BINARY_OFFSET_YEAR = 0                                  ,
		BINARY_LENGTH      = BINARY_OFFSET_YEAR + Integer.BYTES
	;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryHandlerYear New()
	{
		return new BinaryHandlerYear();
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	// only needed where the type is an ordinary class and its instances are populated after creation.
	private final long memoryOffsetYear;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerYear()
	{
		super(
			Year.class,
			CustomFields(
				/* Qualified with the declaring type, so this description stays the one the reflective
				 * handling produced and data written before this handler existed still matches.
				 */
				CustomField(int.class, Year.class.getName(), "year")
			)
		);

		this.memoryOffsetYear = this.isValueClassType()
			? -1
			: XMemory.objectFieldOffset(XReflect.getAnyField(Year.class, "year"))
		;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(
		final Binary                          data    ,
		final Year                            instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		data.store_int(BINARY_OFFSET_YEAR, instance.getValue());
	}

	@Override
	public Year create(final Binary data, final PersistenceLoadHandler handler)
	{
		return Year.of(data.read_int(BINARY_OFFSET_YEAR));
	}

	@Override
	public void updateState(final Binary data, final Year instance, final PersistenceLoadHandler handler)
	{
		if(this.isValueClassType())
		{
			// already complete: it was built from its content rather than populated
			return;
		}

		XMemory.set_int(instance, this.memoryOffsetYear, data.read_int(BINARY_OFFSET_YEAR));
	}

}
