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

import org.eclipse.serializer.collections.XUtilsCollection;
import org.eclipse.serializer.collections.types.XSequence;

/**
 * The minimal abstraction over anything that carries a {@code typeId} &mdash; the persistence-internal
 * numeric handle that uniquely identifies a type within a {@link PersistenceTypeDictionary}.
 * <p>
 * Implemented by {@link PersistenceTypeIdentity} (which adds the textual {@code typeName}), by
 * {@link PersistenceTypeDescription} (which adds the structural member sequence) and by
 * {@link PersistenceTypeDefinition} (which additionally binds the description to a runtime {@link Class}).
 * <p>
 * The interface exists so that ordering and lookup utilities can operate on heterogeneous collections
 * (descriptions, definitions, lineage entries) by typeId alone, without forcing a common heavyweight
 * supertype.
 *
 * @see PersistenceTypeIdentity
 * @see PersistenceTypeDescription
 */
public interface PersistenceTypeIdOwner
{
	/**
	 * The numeric type identifier owned by this instance. Stable across application runs once assigned
	 * by the {@link PersistenceTypeDictionary}.
	 *
	 * @return the type id.
	 */
	public long typeId();



	/**
	 * Ordering function that sorts {@link PersistenceTypeIdOwner}s by ascending {@link #typeId()}. Safe
	 * for the full {@code long} range &mdash; written as branching comparisons instead of subtraction to
	 * avoid overflow.
	 *
	 * @param o1 the first instance.
	 * @param o2 the second instance.
	 *
	 * @return negative if {@code o1.typeId() < o2.typeId()}, zero if equal, positive otherwise.
	 */
	public static int orderAscending(final PersistenceTypeIdOwner o1, final PersistenceTypeIdOwner o2)
	{
		return o2.typeId() >= o1.typeId() ? o2.typeId() > o1.typeId() ? -1 : 0 : +1;
	}


	/**
	 * Sorts the passed {@link XSequence} in place by ascending {@link #typeId()} using
	 * {@link #orderAscending(PersistenceTypeIdOwner, PersistenceTypeIdOwner)}.
	 *
	 * @param <E>      the element type.
	 * @param <C>      the sequence type.
	 * @param elements the sequence to sort.
	 *
	 * @return the same sequence, sorted in place.
	 */
	public static <E extends PersistenceTypeIdOwner, C extends XSequence<E>>
	C sortByTypeIdAscending(final C elements)
	{
		return XUtilsCollection.valueSort(elements, PersistenceTypeIdOwner::orderAscending);
	}

}
