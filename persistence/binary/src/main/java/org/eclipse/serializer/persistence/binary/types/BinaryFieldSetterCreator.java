package org.eclipse.serializer.persistence.binary.types;

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

import java.lang.reflect.Field;

/**
 * BinaryFieldSetterCreator implementations are responsible to create
 * BinaryFieldSetter for a specific field of a class.
 * 
 * The should provide implementations consider the required byte order.
 * 
 * @param <T> type
 */
public interface BinaryFieldSetterCreator<T>
{
	/**
	 * Get the Field that is handled by this BinaryFieldStorerCreator
	 * 
	 * @return the handled field.
	 */
	Field getField();

	/**
	 * Create a BinaryFieldSetter thats applicable for the specified
	 * byte order.
	 * 
	 * @param switchByteOrder true if byte order switch.
	 * @return a new BinaryFieldSetter.
	 */
	BinaryFieldSetter<?> create(boolean switchByteOrder);
}
