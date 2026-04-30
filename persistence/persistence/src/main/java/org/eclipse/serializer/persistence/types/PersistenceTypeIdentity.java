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


/**
 * The full identity of a persistent type: its numeric {@link #typeId()} plus the textual
 * {@link #typeName()} (typically a Java fully qualified class name, but for dictionary entries that have
 * no runtime counterpart it can be any string preserved from the original definition).
 * <p>
 * Both halves of the identity are required: the {@code typeId} is what persisted data references and what
 * the dictionary uses as a primary key, while the {@code typeName} is what links the entry back to a
 * (possibly evolved) Java type and what refactoring mappings rewrite. Two identity instances are equal
 * only if both halves match &mdash; see {@link #equals(PersistenceTypeIdentity, PersistenceTypeIdentity)}.
 * <p>
 * {@link PersistenceTypeDescription} is the typical implementer; lookup tables that only need to compare
 * identity (without carrying a member sequence) can use {@link PersistenceTypeDescription#Identity(long, String)}
 * for a minimal instance.
 *
 * @see PersistenceTypeIdOwner
 * @see PersistenceTypeDescription
 */
public interface PersistenceTypeIdentity extends PersistenceTypeIdOwner
{
	@Override
	public long typeId();

	/**
	 * The textual name of the identified type. For runtime-bound types this is the fully qualified Java
	 * class name; for dictionary entries without a runtime counterpart it is the original textual name
	 * preserved for legacy resolution.
	 *
	 * @return the type's textual name.
	 */
	public String typeName();


	/**
	 * Combined hash of {@code typeId} and {@code typeName} suitable for keying identities in hash
	 * collections. Note: the implementation uses bitwise AND between the two component hashes, which is
	 * intentionally cheap rather than collision-optimal.
	 *
	 * @param typeIdentity the identity to hash.
	 *
	 * @return a hash code combining the typeId and typeName hashes.
	 */
	public static int hashCode(final PersistenceTypeIdentity typeIdentity)
	{
		return Long.hashCode(typeIdentity.typeId()) & typeIdentity.typeName().hashCode();
	}

	/**
	 * Null-safe equality on the full identity: returns {@code true} if both instances are the same
	 * reference, or both are non-{@code null} and agree on {@link #typeId()} and {@link #typeName()}.
	 *
	 * @param ti1 the first identity.
	 * @param ti2 the second identity.
	 *
	 * @return {@code true} if the two identities are equal.
	 */
	public static boolean equals(
		final PersistenceTypeIdentity ti1,
		final PersistenceTypeIdentity ti2
	)
	{
		return ti1 == ti2
			|| ti1 != null && ti2 != null
			&& ti1.typeId() == ti2.typeId()
			&& ti1.typeName().equals(ti2.typeName())
		;
	}

}
