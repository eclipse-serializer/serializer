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

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.serializer.collections.interfaces.ChainStorage;


public abstract class AbstractChainStorage<E, K, V, EN extends AbstractChainEntry<E, K, V, EN>>
implements ChainStorage<E, K, V, EN>
{
	protected abstract EN head();

	protected abstract void disjoinEntry(EN entry);

	protected abstract boolean moveToStart(EN entry);

	protected abstract boolean moveToEnd(EN entry);
	
	protected abstract void replace(EN doomedEntry, EN keptEntry);
	
	protected abstract long substitute(Function<? super E, ? extends E> mapper, BiConsumer<EN, E> callback);
}
