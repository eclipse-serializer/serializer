package org.eclipse.serializer.chars;

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
 * Function type that calculates the distance or difference or cost between two given characters.
 * <p>
 * This is useful mostly for string procedure algorithms (e.g. Levenshtein distance).
 */
@FunctionalInterface
public interface _charDistance
{
	/**
	 * Calculates the distance of the two given characters.
	 * <p>
	 * The meaning of the returned value depends on the function implementation and/or the context it is used in.<br>
	 * Typical string similarity values range from 0.0 to 1.0 .
	 *
	 * @param a the first character
	 * @param b the second character
	 * @return the distance/difference/cost of the two given characters.
	 */
	public float distance(char a, char b);

}
