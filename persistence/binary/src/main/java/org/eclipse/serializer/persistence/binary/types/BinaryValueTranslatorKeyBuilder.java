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

import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;

/**
 * Strategy for building the lookup key that selects a custom {@link BinaryValueSetter} from the
 * {@link BinaryValueTranslatorProvider}'s registered translator map. Multiple key builders are tried in
 * order; the first one whose key resolves to a registered translator wins.
 *
 * @see BinaryValueTranslatorProvider
 */
@FunctionalInterface
public interface BinaryValueTranslatorKeyBuilder
{
	/**
	 * Builds a translator-lookup key for the given source/target member pairing.
	 *
	 * @param sourceLegacyType  the legacy type definition of the source.
	 * @param sourceMember      the legacy member being translated.
	 * @param targetCurrentType the current type handler of the target.
	 * @param targetMember      the current member receiving the value.
	 *
	 * @return the lookup key, or {@code null} to defer to the next key builder.
	 */
	public String buildTranslatorLookupKey(
		PersistenceTypeDefinition         sourceLegacyType ,
		PersistenceTypeDescriptionMember  sourceMember     ,
		PersistenceTypeHandler<Binary, ?> targetCurrentType,
		PersistenceTypeDescriptionMember  targetMember
	);
}
