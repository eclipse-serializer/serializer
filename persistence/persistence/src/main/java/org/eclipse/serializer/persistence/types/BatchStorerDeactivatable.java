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
 * Lifecycle operations ({@link #close()}) and read-only queries
 * ({@link #hasPendingData()}) always delegate to the underlying batch storer.
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
		this.batchStorer.close();
	}

}