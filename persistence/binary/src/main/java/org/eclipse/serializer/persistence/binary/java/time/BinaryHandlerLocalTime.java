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

import java.time.LocalTime;

import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.reflect.XReflect;

/**
 * Handler for {@link LocalTime}.
 * <p>
 * Where {@link LocalTime} is a value class, its instances cannot be created empty and populated
 * afterwards, and its constructor takes {@code int} parameters where the fields are {@code byte},
 * so it cannot be constructed generically from its fields either. It is therefore built from its
 * persisted values through {@link LocalTime#of(int, int, int, int)}.
 * <p>
 * The instance is created complete either way, since a plugin reusing the value-type handlers (e.g.
 * the REST viewer) relies on {@link #create} alone. Where the type is an ordinary class, an already
 * registered instance is still populated in {@link #updateState}, preserving the update behavior the
 * reflective handling had.
 * <p>
 * The persisted form is byte-identical to the one the reflective handling produced, under the same
 * type and member names, so existing data is unaffected.
 */
public final class BinaryHandlerLocalTime extends AbstractBinaryHandlerCustomNonReferentialFixedLength<LocalTime>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final long
		BINARY_OFFSET_HOUR   = 0                                  ,
		BINARY_OFFSET_MINUTE = BINARY_OFFSET_HOUR   + Byte.BYTES   ,
		BINARY_OFFSET_SECOND = BINARY_OFFSET_MINUTE + Byte.BYTES   ,
		BINARY_OFFSET_NANO   = BINARY_OFFSET_SECOND + Byte.BYTES   ,
		BINARY_LENGTH        = BINARY_OFFSET_NANO   + Integer.BYTES
	;

	private static final String TYPE_NAME = LocalTime.class.getName();



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryHandlerLocalTime New()
	{
		return new BinaryHandlerLocalTime();
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	// only needed where the type is an ordinary class and its instances are populated after creation.
	private final long memoryOffsetHour  ;
	private final long memoryOffsetMinute;
	private final long memoryOffsetSecond;
	private final long memoryOffsetNano  ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerLocalTime()
	{
		super(
			LocalTime.class,
			CustomFields(
				/* Qualified with the declaring type, so this description stays the one the reflective
				 * handling produced and data written before this handler existed still matches.
				 */
				CustomField(byte.class, TYPE_NAME, "hour"  ),
				CustomField(byte.class, TYPE_NAME, "minute"),
				CustomField(byte.class, TYPE_NAME, "second"),
				CustomField(int.class , TYPE_NAME, "nano"  )
			)
		);

		final boolean populated = !this.isValueClassType();
		this.memoryOffsetHour   = populated
			? XMemory.objectFieldOffset(XReflect.getAnyField(LocalTime.class, "hour"))
			: -1
		;
		this.memoryOffsetMinute = populated
			? XMemory.objectFieldOffset(XReflect.getAnyField(LocalTime.class, "minute"))
			: -1
		;
		this.memoryOffsetSecond = populated
			? XMemory.objectFieldOffset(XReflect.getAnyField(LocalTime.class, "second"))
			: -1
		;
		this.memoryOffsetNano   = populated
			? XMemory.objectFieldOffset(XReflect.getAnyField(LocalTime.class, "nano"))
			: -1
		;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(
		final Binary                          data    ,
		final LocalTime                       instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		data.store_byte(BINARY_OFFSET_HOUR  , (byte)instance.getHour()  );
		data.store_byte(BINARY_OFFSET_MINUTE, (byte)instance.getMinute());
		data.store_byte(BINARY_OFFSET_SECOND, (byte)instance.getSecond());
		data.store_int (BINARY_OFFSET_NANO  , instance.getNano()        );
	}

	@Override
	public LocalTime create(final Binary data, final PersistenceLoadHandler handler)
	{
		return LocalTime.of(
			data.read_byte(BINARY_OFFSET_HOUR)  ,
			data.read_byte(BINARY_OFFSET_MINUTE),
			data.read_byte(BINARY_OFFSET_SECOND),
			data.read_int (BINARY_OFFSET_NANO)
		);
	}

	@Override
	public void updateState(final Binary data, final LocalTime instance, final PersistenceLoadHandler handler)
	{
		if(this.isValueClassType())
		{
			// already complete: it was built from its content rather than populated
			return;
		}

		XMemory.set_byte(instance, this.memoryOffsetHour  , data.read_byte(BINARY_OFFSET_HOUR)  );
		XMemory.set_byte(instance, this.memoryOffsetMinute, data.read_byte(BINARY_OFFSET_MINUTE));
		XMemory.set_byte(instance, this.memoryOffsetSecond, data.read_byte(BINARY_OFFSET_SECOND));
		XMemory.set_int (instance, this.memoryOffsetNano  , data.read_int (BINARY_OFFSET_NANO)  );
	}

}
