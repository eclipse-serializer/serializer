package org.eclipse.serializer.util.similarity;

/*-
 * #%L
 * Eclipse Serializer Base
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
 * Since matching on similarity is a heuristic method, it can be necessary to have a validation callback logic
 * that can ultimately decide on potential matches.
 *
 * @param <E> the validated element's type
 */
@FunctionalInterface
public interface MatchValidator<E>
{
	public boolean isValidMatch(
		E      source              ,
		E      target              ,
		double similarity          ,
		int    sourceCandidateCount,
		int    targetCandidateCount
	);
}
