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


public abstract class AbstractChainEntryLinkedKVHashed<K, V, EN extends AbstractChainEntryLinkedKVHashed<K, V, EN>>
extends AbstractChainEntryLinkedKV<K, V, EN>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final int hash; // the hash value of the hash-related value contained in this entry



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractChainEntryLinkedKVHashed(final int hash, final EN link)
	{
		super(link);
		this.hash = hash;
	}

}
