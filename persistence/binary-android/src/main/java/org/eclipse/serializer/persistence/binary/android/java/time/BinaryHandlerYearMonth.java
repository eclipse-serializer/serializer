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

import java.time.YearMonth;

import org.eclipse.serializer.persistence.binary.internal.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerYearMonth extends AbstractBinaryHandlerCustomNonReferentialFixedLength<YearMonth>
{
	static final long BINARY_OFFSET_YEAR  =                                  0L;
	static final long BINARY_OFFSET_MONTH = BINARY_OFFSET_YEAR  + Integer.BYTES;
	static final long BINARY_LENGTH       = BINARY_OFFSET_MONTH + Integer.BYTES;
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerYearMonth New()
	{
		return new BinaryHandlerYearMonth();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerYearMonth()
	{
		super(
			YearMonth.class,
			CustomFields(
				CustomField(int.class, "year" ),
				CustomField(int.class, "month")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	

	@Override
	public final void store(
		final Binary                          data    ,
		final YearMonth                       instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);

		data.store_int(BINARY_OFFSET_YEAR , instance.getYear());
		data.store_int(BINARY_OFFSET_MONTH, instance.getMonthValue());
	}

	@Override
	public final YearMonth create(final Binary data, final PersistenceLoadHandler handler)
	{
		return YearMonth.of(
			data.read_int(BINARY_OFFSET_YEAR),
			data.read_int(BINARY_OFFSET_MONTH)
		);
	}

	@Override
	public final void updateState(final Binary data, final YearMonth instance, final PersistenceLoadHandler handler)
	{
		// no-op
	}

}
