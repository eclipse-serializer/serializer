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

import org.eclipse.serializer.typing.TypeMappingLookup;

/**
 * Read-only view onto a (source-type, target-type) &rarr; {@link BinaryValueSetter} mapping. Used by
 * {@link BinaryValueTranslatorProvider} to resolve a translator for a member pairing without having to
 * own the underlying mapping table.
 *
 * @see BinaryValueTranslatorMappingProvider
 */
public interface BinaryValueTranslatorLookupProvider
{
	/**
	 * @param switchByteOrder whether to return the byte-reversed translator table.
	 *
	 * @return the matching translator lookup table.
	 */
	public TypeMappingLookup<BinaryValueSetter> mapping(boolean switchByteOrder);
}
