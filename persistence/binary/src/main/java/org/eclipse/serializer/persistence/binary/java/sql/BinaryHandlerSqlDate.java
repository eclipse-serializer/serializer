package org.eclipse.serializer.persistence.binary.java.sql;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import java.sql.Date;

import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerDate;
import org.eclipse.serializer.persistence.binary.internal.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

/**
 * Blunt copy of {@link BinaryHandlerDate} for the as good as superfluous type {@link java.sql.Date}.
 *
 */
public final class BinaryHandlerSqlDate extends AbstractBinaryHandlerCustomNonReferentialFixedLength<Date>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerSqlDate New()
	{
		return new BinaryHandlerSqlDate();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerSqlDate()
	{
		super(
			Date.class,
			CustomFields(
				CustomField(long.class, "timestamp")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static long instanceState(final Date instance)
	{
		return instance.getTime();
	}
	
	private static long binaryState(final Binary data)
	{
		return data.read_long(0);
	}

	@Override
	public final void store(
		final Binary                          data    ,
		final Date                            instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(Long.BYTES, this.typeId(), objectId);
		
		// the data content of a date is simple the timestamp long, nothing else
		data.store_long(instanceState(instance));
	}

	@Override
	public final Date create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new Date(binaryState(data));
	}

	@Override
	public final void updateState(final Binary data, final Date instance, final PersistenceLoadHandler handler)
	{
		instance.setTime(binaryState(data));
	}

}
