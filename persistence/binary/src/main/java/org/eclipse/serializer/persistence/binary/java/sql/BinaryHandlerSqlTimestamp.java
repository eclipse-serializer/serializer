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

import java.sql.Timestamp;

import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerDate;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

/**
 * Blunt copy of {@link BinaryHandlerDate} for the as good as superfluous type {@link java.sql.Timestamp}.
 *
 */
public final class BinaryHandlerSqlTimestamp extends AbstractBinaryHandlerCustomNonReferentialFixedLength<Timestamp>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerSqlTimestamp New()
	{
		return new BinaryHandlerSqlTimestamp();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerSqlTimestamp()
	{
		super(
			Timestamp.class,
			CustomFields(
				CustomField(long.class, "date"),
				CustomField(int.class, "nanos")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final void store(
		final Binary                          data    ,
		final Timestamp                       instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(Long.BYTES + Integer.BYTES, this.typeId(), objectId);
		
		data.store_long(instance.getTime());
		data.store_int(Long.BYTES, instance.getNanos());
	}

	@Override
	public final Timestamp create(final Binary data, final PersistenceLoadHandler handler)
	{
		long date = data.read_long(0);
		int nanos = data.read_int(Long.BYTES);
		Timestamp ts = new Timestamp(date);
		ts.setNanos(nanos);
		return ts;
	}

	@Override
	public final void updateState(final Binary data, final Timestamp instance, final PersistenceLoadHandler handler)
	{
		long date = data.read_long(0);
		int nanos = data.read_int(Long.BYTES);
		instance.setTime(date);
		instance.setNanos(nanos);
	}

}
