package org.eclipse.serializer.functional;

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

import org.eclipse.serializer.branching.ThrowBreak;

public final class IsNull<E> implements Predicate<E>
{
	@Override
	public final boolean test(final E e) throws ThrowBreak
	{
		return e == null;
	}

}
