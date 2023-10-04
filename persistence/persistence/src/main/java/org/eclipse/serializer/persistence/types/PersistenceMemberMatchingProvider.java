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

//@FunctionalInterface - well, lol.
public interface PersistenceMemberMatchingProvider
{
	public default Equalator<PersistenceTypeDefinitionMember> provideMemberMatchingEqualator()
	{
		// optional, null by default.
		return null;
	}
	
	public default Similator<PersistenceTypeDefinitionMember> provideMemberMatchingSimilator(
		final TypeMappingLookup<Float> typeSimilarity
	)
	{
		return PersistenceMemberSimilator.New(typeSimilarity);
	}
	
	public default MatchValidator<PersistenceTypeDefinitionMember> provideMemberMatchValidator()
	{
		// optional, null by default.
		return null;
	}
	
	
	
	public static PersistenceMemberMatchingProvider New()
	{
		return new PersistenceMemberMatchingProvider.Default();
	}
	
	public class Default implements PersistenceMemberMatchingProvider
	{
		// since default methods, the ability to instantiate stateless instances from interfaces is missing
	}
	
}
