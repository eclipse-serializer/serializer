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

import java.time.MonthDay;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.ValidatingBinaryHandler;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

/**
 * Binary Handler for java.time.MonthDay
 * Required for java &ge; 26 because of transient fields and
 * changed binary format of MonthDay implementation.
 * 
 * Binary Format must be compatibe with java &lt; 26 versions!
 * 
 */
public class BinaryHandlerMonthDay extends AbstractBinaryHandlerCustomNonReferentialFixedLength<MonthDay>
implements ValidatingBinaryHandler<MonthDay, MonthDay>
{
	static final long BINARY_OFFSET_MONTH = 0L;
	static final long BINARY_OFFSET_DAY   = BINARY_OFFSET_MONTH + Integer.BYTES;
	static final long BINARY_LENGTH       = BINARY_OFFSET_DAY   + Integer.BYTES;
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerMonthDay New()
	{
		return new BinaryHandlerMonthDay();
	}

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected BinaryHandlerMonthDay()
	{
		super(
			MonthDay.class,
			CustomFields(
				CustomField(int.class, "month" ),
				CustomField(int.class, "day"  )
			)
		);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	///
	/**
	 * This handler transfers state in {@link #create} alone, so an instance it is handed here was
	 * created elsewhere and cannot be updated. Validating instead of ignoring keeps a divergence from
	 * dropping the persisted state silently, e.g. for an explicitly set root.
	 */
	@Override
	public void updateState(final Binary data, final MonthDay instance, final PersistenceLoadHandler handler)
	{
		this.validateState(data, instance, handler);
	}

	@Override
	public MonthDay getValidationStateFromInstance(final MonthDay instance)
	{
		return instance;
	}

	@Override
	public MonthDay getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

	@Override
	public void store(final Binary data, final MonthDay instance, final long objectId, final PersistenceStoreHandler<Binary> handler)
	{
		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		
		data.store_int (BINARY_OFFSET_MONTH , instance.getMonthValue());
		data.store_int (BINARY_OFFSET_DAY  , instance.getDayOfMonth());
	}

	@Override
	public MonthDay create(final Binary data, final PersistenceLoadHandler handler)
	{
		return binaryState(data);
	}

	private static MonthDay binaryState(final Binary data)
	{
		return MonthDay.of(
			data.read_int (BINARY_OFFSET_MONTH),
			data.read_int (BINARY_OFFSET_DAY));
	}

}
