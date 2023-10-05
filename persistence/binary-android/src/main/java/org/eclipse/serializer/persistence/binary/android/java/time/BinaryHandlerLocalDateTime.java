package org.eclipse.serializer.persistence.binary.android.java.time;

/*-
 * #%L
 * Eclipse Serializer Persistence Android
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.time.LocalDateTime;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerLocalDateTime extends AbstractBinaryHandlerCustomNonReferentialFixedLength<LocalDateTime>
{
	static final long BINARY_OFFSET_YEAR   =                                   0L;
	static final long BINARY_OFFSET_MONTH  = BINARY_OFFSET_YEAR   + Integer.BYTES;
	static final long BINARY_OFFSET_DAY    = BINARY_OFFSET_MONTH  + Short  .BYTES;
	static final long BINARY_OFFSET_HOUR   = BINARY_OFFSET_DAY    + Short  .BYTES;
	static final long BINARY_OFFSET_MINUTE = BINARY_OFFSET_HOUR   + Byte   .BYTES;
	static final long BINARY_OFFSET_SECOND = BINARY_OFFSET_MINUTE + Byte   .BYTES;
	static final long BINARY_OFFSET_NANO   = BINARY_OFFSET_SECOND + Byte   .BYTES;
	static final long BINARY_LENGTH        = BINARY_OFFSET_NANO   + Integer.BYTES;
	
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
			CustomFields(
				CustomField(int.class,   "year"  ),
				CustomField(short.class, "month" ),
				CustomField(short.class, "day"   ),
				CustomField(byte.class,  "hour"  ),
				CustomField(byte.class,  "minute"),
				CustomField(byte.class,  "second"),
				CustomField(int.class ,  "nano"  )
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	

	@Override
	public final void store(
		final Binary                          data    ,
		final LocalDateTime                   instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		
		data.store_int  (BINARY_OFFSET_YEAR  , instance.getYear());
		data.store_short(BINARY_OFFSET_MONTH , (short)instance.getMonthValue());
		data.store_short(BINARY_OFFSET_DAY   , (short)instance.getDayOfMonth());
		data.store_byte (BINARY_OFFSET_HOUR  , (byte)instance.getHour());
		data.store_byte (BINARY_OFFSET_MINUTE, (byte)instance.getMinute());
		data.store_byte (BINARY_OFFSET_SECOND, (byte)instance.getSecond());
		data.store_int  (BINARY_OFFSET_NANO  , instance.getNano());
	}

	@Override
	public final LocalDateTime create(final Binary data, final PersistenceLoadHandler handler)
	{
		return LocalDateTime.of(
			data.read_int  (BINARY_OFFSET_YEAR),
			data.read_short(BINARY_OFFSET_MONTH),
			data.read_short(BINARY_OFFSET_DAY),
			data.read_byte (BINARY_OFFSET_HOUR),
			data.read_byte (BINARY_OFFSET_MINUTE),
			data.read_byte (BINARY_OFFSET_SECOND),
			data.read_int  (BINARY_OFFSET_NANO)
		);
	}

	@Override
	public final void updateState(final Binary data, final LocalDateTime instance, final PersistenceLoadHandler handler)
	{
		// no-op
	}

}
