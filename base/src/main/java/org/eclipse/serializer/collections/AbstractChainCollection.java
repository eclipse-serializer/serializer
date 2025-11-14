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

import org.eclipse.serializer.collections.interfaces.ChainStorage;



public abstract class AbstractChainCollection<E, K, V, EN extends AbstractChainEntry<E, K, V, EN>>
extends AbstractBaseCollection<E>
{
	protected abstract ChainStorage<E, K, V, EN> getInternalStorageChain();
	
	protected abstract void internalRemoveEntry(EN chainEntry);

	protected abstract int internalRemoveNullEntries();

	protected abstract int internalClear();

}
