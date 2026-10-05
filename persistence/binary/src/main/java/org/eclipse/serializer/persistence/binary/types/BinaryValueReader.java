package org.eclipse.serializer.persistence.binary.types;

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

import org.eclipse.serializer.persistence.binary.exceptions.BinaryPersistenceException;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;

/**
 * Reads a single persisted field value and returns it, as opposed to {@link BinaryValueSetter},
 * which writes it directly into an instance's memory.
 * <p>
 * This indirection is required wherever the values must exist <i>before</i> the instance does, i.e.
 * for constructor-based instantiation.
 *
 * @see BinaryHandlerGenericValueClass
 */
@FunctionalInterface
public interface BinaryValueReader
{
	/**
	 * Reads the value located at {@code offset} in the passed entity data.
	 *
	 * @param data    the entity data to read from.
	 * @param offset  the value's offset in the entity's content.
	 * @param handler the load handler used to resolve references.
	 *
	 * @return the read value, boxed if primitive.
	 */
	public Object readValue(Binary data, long offset, PersistenceLoadHandler handler);


	/**
	 * Provides the reader matching the passed field type.
	 * <p>
	 * Byte order needs no handling here: {@link Binary} reads through virtual accessors, and a load
	 * item for persisted data in a non-native byte order already reverses every value it reads.
	 *
	 * @param type the field type whose value is to be read.
	 *
	 * @return the matching reader.
	 */
	public static BinaryValueReader provideReader(final Class<?> type)
	{
		if(!type.isPrimitive())
		{
			return (data, offset, handler) -> handler.lookupObject(data.read_long(offset));
		}

		if(type == int.class)
		{
			return (data, offset, handler) -> Integer.valueOf(data.read_int(offset));
		}
		if(type == long.class)
		{
			return (data, offset, handler) -> Long.valueOf(data.read_long(offset));
		}
		if(type == boolean.class)
		{
			return (data, offset, handler) -> Boolean.valueOf(data.read_boolean(offset));
		}
		if(type == byte.class)
		{
			return (data, offset, handler) -> Byte.valueOf(data.read_byte(offset));
		}
		if(type == double.class)
		{
			return (data, offset, handler) -> Double.valueOf(data.read_double(offset));
		}
		if(type == float.class)
		{
			return (data, offset, handler) -> Float.valueOf(data.read_float(offset));
		}
		if(type == char.class)
		{
			return (data, offset, handler) -> Character.valueOf(data.read_char(offset));
		}
		if(type == short.class)
		{
			return (data, offset, handler) -> Short.valueOf(data.read_short(offset));
		}

		throw new BinaryPersistenceException("Unhandled primitive type: " + type.getName());
	}

}
