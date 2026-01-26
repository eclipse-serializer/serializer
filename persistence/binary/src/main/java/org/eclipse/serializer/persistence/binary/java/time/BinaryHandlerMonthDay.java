package org.eclipse.serializer.persistence.binary.java.time;

import java.time.MonthDay;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

/**
 * Binary Handler for java.time.MonthDay
 * Required for java >= 26 because of transient fields and
 * changed binary format of MonthDay implementation.
 * 
 * Binary Format must be compatibe with java < 26 versions!
 * 
 */
public class BinaryHandlerMonthDay extends AbstractBinaryHandlerCustomNonReferentialFixedLength<MonthDay>
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
	@Override
	public void updateState(final Binary data, final MonthDay instance, final PersistenceLoadHandler handler)
	{
		// no-op
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
		return MonthDay.of(
			data.read_int (BINARY_OFFSET_MONTH),
			data.read_int (BINARY_OFFSET_DAY));
	}

}
