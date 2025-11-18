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

public interface PersistenceTypeIdOwner
{
	public long typeId();



	public static int orderAscending(final PersistenceTypeIdOwner o1, final PersistenceTypeIdOwner o2)
	{
		return o2.typeId() >= o1.typeId() ? o2.typeId() > o1.typeId() ? -1 : 0 : +1;
	}


	public static <E extends PersistenceTypeIdOwner, C extends XSequence<E>>
	C sortByTypeIdAscending(final C elements)
	{
		return XUtilsCollection.valueSort(elements, PersistenceTypeIdOwner::orderAscending);
	}

}
