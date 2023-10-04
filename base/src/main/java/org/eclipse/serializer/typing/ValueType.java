package org.eclipse.serializer.typing;

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

import org.eclipse.serializer.hashing.HashImmutable;


/**
 * A type whose instances represents (effectively) immutable values that should only be primarily handled as values
 * instead of objects (e.g. for determining equality and comparison). String, primitive wrappers, etc. should have been
 * marked with an interface like that. Sadly, they aren't. Nevertheless, here is a proper marker interface
 * to mark self defined types as being value types.
 * <p>
 * Value types are the only types where inherently implemented equals() and hashCode() are properly applicable.
 * As Java is sadly missing a SELF typing, the untyped equals(Object obj) can't be defined more specific
 * (like for example public boolean equals(SELF obj) or such).
 *
 * <p>
 * Also see:
 * @see HashImmutable
 * @see Immutable
 * @see Stateless
 *
 * 
 *
 */
public interface ValueType extends HashImmutable
{
	// marker interface
}
