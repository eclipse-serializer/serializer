package org.eclipse.serializer.collections;

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

import java.util.function.Predicate;

final class ElementIsContained<E> implements Predicate<E>
{
	/* note:
	 * - the term element for a collection element is fixed and will never change
	 * - the reference can never be ruined from outside as this function is always instantiated in a local scope
	 * hence, the field is intentionally declared public to improve local performance and readability
	 */
	E element;

	ElementIsContained()
	{
		super();
	}

	@Override
	public boolean test(final E e)
	{
		return e == this.element;
	}
}
