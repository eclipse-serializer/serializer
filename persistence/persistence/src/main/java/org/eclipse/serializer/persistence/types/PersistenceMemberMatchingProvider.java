package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
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

import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.typing.TypeMappingLookup;
import org.eclipse.serializer.util.similarity.MatchValidator;
import org.eclipse.serializer.util.similarity.Similator;

/**
 * Supplies the equality, similarity, and validation strategies used by the legacy-mapping algorithm to
 * match members from an obsolete type definition against members of the current runtime type. The bundled
 * defaults plug in {@link PersistenceMemberSimilator} as the similarity function and leave the optional
 * equality and validation hooks as {@code null}; subclassing lets callers swap any of the three.
 *
 * @see PersistenceMemberSimilator
 * @see PersistenceLegacyTypeMapper
 */
//@FunctionalInterface - well, lol.
public interface PersistenceMemberMatchingProvider
{
	/**
	 * The optional equality function used to short-circuit member matching for trivially-equal pairs.
	 * Returning {@code null} (the default) disables the shortcut.
	 *
	 * @return the equality function, or {@code null}.
	 */
	public default Equalator<PersistenceTypeDefinitionMember> provideMemberMatchingEqualator()
	{
		// optional, null by default.
		return null;
	}

	/**
	 * The similarity function used to score how well a legacy member matches a current member. The default
	 * returns a fresh {@link PersistenceMemberSimilator} initialized with the passed type-similarity
	 * lookup.
	 *
	 * @param typeSimilarity the type-similarity lookup used to score member types.
	 *
	 * @return the similarity function.
	 */
	public default Similator<PersistenceTypeDefinitionMember> provideMemberMatchingSimilator(
		final TypeMappingLookup<Float> typeSimilarity
	)
	{
		return PersistenceMemberSimilator.New(typeSimilarity);
	}

	/**
	 * The optional validator used to reject candidate matches that pass the similarity threshold but
	 * violate additional constraints. Returning {@code null} (the default) skips this step.
	 *
	 * @return the match validator, or {@code null}.
	 */
	public default MatchValidator<PersistenceTypeDefinitionMember> provideMemberMatchValidator()
	{
		// optional, null by default.
		return null;
	}



	/**
	 * Creates a new {@link Default} provider that uses the bundled defaults for all three hooks.
	 *
	 * @return the newly created provider.
	 */
	public static PersistenceMemberMatchingProvider New()
	{
		return new PersistenceMemberMatchingProvider.Default();
	}

	/**
	 * Default {@link PersistenceMemberMatchingProvider}: stateless instance that relies entirely on the
	 * default-method implementations declared in the interface.
	 */
	public class Default implements PersistenceMemberMatchingProvider
	{
		// since default methods, the ability to instantiate stateless instances from interfaces is missing
	}

}
