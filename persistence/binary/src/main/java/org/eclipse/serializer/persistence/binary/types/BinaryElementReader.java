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


/**
 * Functional callback used by collection-like binary handlers to read a single element entry from a
 * persisted list at the given offset within the surrounding entity's binary form.
 */
@FunctionalInterface
public interface BinaryElementReader
{
	/**
	 * @param binary the persisted entity data.
	 * @param offset the offset of the element within the entity's binary content.
	 */
	public void readElement(Binary binary, long offset);
}
