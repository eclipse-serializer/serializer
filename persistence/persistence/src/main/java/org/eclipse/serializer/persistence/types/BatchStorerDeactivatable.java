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

import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionStorerDeactivated;

/**
 * A {@link BatchStorer} wrapper that honors global write-disable via
 * {@link PersistenceStorerDeactivatable}.
 * <p>
 * Store, commit, and flush operations are blocked when writes are disabled.
 * Read-only queries ({@link #hasPendingData()}) always delegate to the
 * underlying batch storer.
 * <p>
 * {@link #close()} always releases lifecycle resources (e.g. background
 * threads). When writes are disabled, any pending data is discarded rather
 * than flushed, so that close never bypasses the write-disable contract.
 */
public class BatchStorerDeactivatable extends PersistenceStorerDeactivatable implements BatchStorer
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final BatchStorer batchStorer;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BatchStorerDeactivatable(final BatchStorer batchStorer)
	{
		super(batchStorer);
		this.batchStorer = batchStorer;
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void flush()
	{
		if(!this.isWriteEnabled())
		{
			throw new PersistenceExceptionStorerDeactivated();
		}
		this.batchStorer.flush();
	}

	@Override
	public boolean hasPendingData()
	{
		return this.batchStorer.hasPendingData();
	}

	@Override
	public void close()
	{
		if(!this.isWriteEnabled())
		{
			// Drop pending data so the underlying close() performs only
			// lifecycle cleanup (scheduler shutdown) without flushing.
			this.batchStorer.clear();
		}
		this.batchStorer.close();
	}

}