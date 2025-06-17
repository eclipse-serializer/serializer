package org.eclipse.serializer.persistence.binary.java.time;

import java.time.Period;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public class BinaryHandlerPeriod extends AbstractBinaryHandlerCustomNonReferentialFixedLength<Period>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerPeriod New()
	{
		return new BinaryHandlerPeriod();
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	BinaryHandlerPeriod()
	{
		super(
			Period.class,
			CustomFields(
				CustomField(int.class, "years"),
				CustomField(int.class, "months"),
				CustomField(int.class, "days")
			)
		);
	}


	@Override
	public void updateState(Binary data, Period instance, PersistenceLoadHandler handler)
	{
		//no-op
	}

	@Override
	public void store(Binary data, Period instance, long objectId, PersistenceStoreHandler<Binary> handler)
	{
		data.storeEntityHeader(Integer.BYTES * 3, this.typeId(), objectId);
		data.store_int(0, instance.getYears());
		data.store_int(Integer.BYTES, instance.getMonths());
		data.store_int(Integer.BYTES * 2, instance.getDays());
	}

	@Override
	public Period create(Binary data, PersistenceLoadHandler handler)
	{
		int years = data.read_int(0);
		int months = data.read_int(Integer.BYTES);
		int days = data.read_int(Integer.BYTES * 2);
		
		return Period.of(years, months, days);
	}

}
