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
 * <p>
 * Since JEP 401 the language says this directly: a {@code value class} has no identity, so {@code ==} is
 * substitutability and the inherited {@code equals}/{@code hashCode} are state-based by construction. Such a
 * type needs no marker, and marking it changes nothing.
 * <p>
 * What this interface is for is everything that cannot be one: a type that must be handled as a value but
 * has to keep its identity - because it is not final, because something synchronizes on it or holds it
 * weakly, or simply because it is compiled for a JDK without value classes. Implementing it is a statement
 * about intended handling, not about the JVM's notion of a value.
 * <p>
 * Note that {@link XTypes#isValueType(Class)}, which is what reads this marker, does <b>not</b> recognise a
 * JEP 401 value class - the two notions are separate. See that method for what the answer is used for.
 *
 * <p>
 * Also see:
 * @see XTypes#isValueType(Class)
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
