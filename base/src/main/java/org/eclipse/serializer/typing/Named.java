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

import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.util.X;

import java.util.function.Consumer;


public interface Named
{
	String name();
	
	
	
	static <C extends Consumer<? super String>> C toNames(
		final Iterable<? extends Named> items,
		final C collector
	)
	{
		for(final Named named : items)
		{
			collector.accept(named.name());
		}
		
		return collector;
	}
	
	static XGettingCollection<String> toNames(final Iterable<? extends Named> items)
	{
		return toNames(items, X.List());
	}
	
}
