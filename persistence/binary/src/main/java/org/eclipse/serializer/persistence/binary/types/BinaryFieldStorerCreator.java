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
 * BinaryFieldStorerCreator implementations are responsible to create
 * BinaryFieldStorer for a specific field of a class.
 * 
 * The should provide implementations for lazy/eager storing and
 * consider the required byte order.
 * 
 * @param <T> type
 */
public interface BinaryFieldStorerCreator<T>
{
	/**
	 * Get the Field that is handled by this BinaryFieldStorerCreator
	 * 
	 * @return the handled field.
	 */
	Field getField();

	/**
	 * Create a BinaryFieldStorer thats applicable for the specified
	 * byte order and lazy or eager storing behavior.
	 * 
	 * @param isEager true if eager storing behavior.
	 * @param switchByteOrder true if byte order switch.
	 * @return a new BinaryFieldStorer.
	 */
	BinaryFieldStorer<?> create(boolean isEager, boolean switchByteOrder);
}
