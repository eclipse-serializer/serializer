package org.eclipse.serializer.equality;

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
 * Marker interface to indicate that an {@link Equalator} implementation uses identity comparison for determining
 * equality.
 *
 */
public interface IdentityEqualator<E> extends Equalator<E>
{
	// marker interface
	
	@Override
	public default boolean isReferentialEquality()
	{
		return true;
	}
}
