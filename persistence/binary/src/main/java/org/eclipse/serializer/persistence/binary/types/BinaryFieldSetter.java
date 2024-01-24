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
 * BinaryFieldSetter are custom implementations of BinaryValueSetter
 * for a specific field. They are used by the automatically created binary type handlers
 * instead of the default BinaryValueSetter.
 * 
 * To register a custom BinaryFieldSetter see {@link org.eclipse.serializer.persistence.binary.types.BinaryFieldHandlerProvider#registerFieldSetterCreator(BinaryFieldSetterCreator) BinaryFieldHandlerProvider}
 * 
 * @param <T> The class the handled field belongs to.
 */
public interface BinaryFieldSetter<T> extends BinaryValueSetter
{
	/**
	 * Get the Field that is handled by this BinaryFieldSetter
	 * 
	 * @return the handled field.
	 */
	Field getField();
}
