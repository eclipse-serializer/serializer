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

import static org.eclipse.serializer.util.X.notNull;

import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.reflect.XReflect;

/**
 * Base for handlers of a {@code java.time} type that holds its state in a {@code long seconds} and an
 * {@code int nanos} field, i.e. {@link java.time.Duration} and {@link java.time.Instant}. Both are
 * value classes on a suitably recent JVM.
 * <p>
 * Where the handled type is a value class, its instances cannot be created empty and populated
 * afterwards: the population write is not reliably visible on an identity-less instance. The instance
 * is therefore built from its persisted values by {@link #createFromValues(long, int)}.
 * <p>
 * The instance is created complete either way, since a plugin reusing the value-type handlers (e.g.
 * the REST viewer) relies on {@link #create} alone. Where the type is an ordinary class, an already
 * registered instance is still populated in {@link #updateState}, preserving the update behavior the
 * reflective handling had.
 * <p>
 * The persisted form is the two values under their field names qualified with the declaring type, so
 * it is byte-identical to the one the reflective handling produced and existing data is unaffected.
 *
 * @param <T> the handled type.
 */
public abstract class AbstractBinaryHandlerSecondsNanos<T>
extends AbstractBinaryHandlerCustomNonReferentialFixedLength<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final long
		BINARY_OFFSET_SECONDS = 0                                     ,
		BINARY_OFFSET_NANOS   = BINARY_OFFSET_SECONDS + Long.BYTES    ,
		BINARY_LENGTH         = BINARY_OFFSET_NANOS   + Integer.BYTES
	;

	private static final String
		FIELD_NAME_SECONDS = "seconds",
		FIELD_NAME_NANOS   = "nanos"
	;



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final ToLongFunction<T> seconds;
	private final ToIntFunction<T>  nanos  ;

	// only needed where the type is an ordinary class and its instances are populated after creation.
	private final long memoryOffsetSeconds;
	private final long memoryOffsetNanos  ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractBinaryHandlerSecondsNanos(
		final Class<T>          type   ,
		final ToLongFunction<T> seconds,
		final ToIntFunction<T>  nanos
	)
	{
		super(
			type,
			CustomFields(
				/* Qualified with the declaring type, so this description stays the one the reflective
				 * handling produced and data written before this handler existed still matches.
				 */
				CustomField(long.class, type.getName(), FIELD_NAME_SECONDS),
				CustomField(int.class , type.getName(), FIELD_NAME_NANOS  )
			)
		);

		this.seconds = notNull(seconds);
		this.nanos   = notNull(nanos)  ;

		final boolean populated = !this.isValueClassType();
		this.memoryOffsetSeconds = populated
			? XMemory.objectFieldOffset(XReflect.getAnyField(type, FIELD_NAME_SECONDS))
			: -1
		;
		this.memoryOffsetNanos   = populated
			? XMemory.objectFieldOffset(XReflect.getAnyField(type, FIELD_NAME_NANOS))
			: -1
		;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/**
	 * Builds an instance from its persisted values. The persisted nano value is always within
	 * {@code [0, 999_999_999]}, so a factory normalizing its arguments reproduces the state exactly.
	 *
	 * @param seconds the persisted second value.
	 * @param nanos   the persisted nano-of-second value.
	 *
	 * @return the built instance.
	 */
	protected abstract T createFromValues(long seconds, int nanos);



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		data.store_long(BINARY_OFFSET_SECONDS, this.seconds.applyAsLong(instance));
		data.store_int (BINARY_OFFSET_NANOS  , this.nanos.applyAsInt(instance)   );
	}

	@Override
	public T create(final Binary data, final PersistenceLoadHandler handler)
	{
		return this.createFromValues(
			data.read_long(BINARY_OFFSET_SECONDS),
			data.read_int (BINARY_OFFSET_NANOS)
		);
	}

	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		if(this.isValueClassType())
		{
			// already complete: it was built from its content rather than populated
			return;
		}

		XMemory.set_long(instance, this.memoryOffsetSeconds, data.read_long(BINARY_OFFSET_SECONDS));
		XMemory.set_int (instance, this.memoryOffsetNanos  , data.read_int (BINARY_OFFSET_NANOS)  );
	}

}
