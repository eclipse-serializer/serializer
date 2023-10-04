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

import org.eclipse.serializer.equality.Equalator;

public final class IsCustomEqual<E> implements Predicate<E>
{
	private final Equalator<? super E> equalator;
	private final E                    sample   ;

	public IsCustomEqual(final Equalator<? super E> equalator, final E sample)
	{
		super();
		this.equalator = equalator;
		this.sample    = sample   ;
	}

	@Override
	public final boolean test(final E e)
	{
		return this.equalator.equal(this.sample, e);
	}

}
