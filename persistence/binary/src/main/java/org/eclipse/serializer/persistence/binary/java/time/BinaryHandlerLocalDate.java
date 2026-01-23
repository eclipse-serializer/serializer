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

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public class BinaryHandlerLocalDate extends AbstractBinaryHandlerCustomNonReferentialFixedLength<LocalDate>
{
	static final long BINARY_OFFSET_YEAR   = 0L;
	static final long BINARY_OFFSET_MONTH  = BINARY_OFFSET_YEAR  + Integer.BYTES;
	static final long BINARY_OFFSET_DAY    = BINARY_OFFSET_MONTH + Short.BYTES;
	static final long BINARY_LENGTH        = BINARY_OFFSET_DAY   + Short.BYTES;
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerLocalDate New()
	{
		return new BinaryHandlerLocalDate();
	}

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected BinaryHandlerLocalDate()
	{
		super(
			LocalDate.class,
			CustomFields(
				CustomField(int.class,   "year"  ),
				CustomField(short.class, "month" ),
				CustomField(short.class, "day"   )
			)
		);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	///
	@Override
	public void updateState(final Binary data, final LocalDate instance, final PersistenceLoadHandler handler)
	{
		// no-op
	}

	@Override
	public void store(final Binary data, final LocalDate instance, final long objectId, final PersistenceStoreHandler<Binary> handler)
	{
		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		
		data.store_int  (BINARY_OFFSET_YEAR  , instance.getYear());
		data.store_short(BINARY_OFFSET_MONTH , (short)instance.getMonthValue());
		data.store_short(BINARY_OFFSET_DAY   , (short)instance.getDayOfMonth());
	}

	@Override
	public LocalDate create(final Binary data, final PersistenceLoadHandler handler)
	{
		return LocalDate.of(
			data.read_int  (BINARY_OFFSET_YEAR),
			data.read_short(BINARY_OFFSET_MONTH),
			data.read_short(BINARY_OFFSET_DAY));
	}

}
