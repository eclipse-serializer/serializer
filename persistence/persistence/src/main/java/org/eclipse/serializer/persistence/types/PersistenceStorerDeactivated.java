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
 * A {@link org.eclipse.serializer.persistence.types.PersistenceStorer PersistenceStorer} implementation
 * that always throws {@link org.eclipse.serializer.persistence.exceptions.PersistenceExceptionStorerDeactivated PersistenceExceptionStorerDeactivated}.
 */
public class PersistenceStorerDeactivated implements PersistenceStorer
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Constructor
	 */
	public PersistenceStorerDeactivated()
	{
		super();
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public Object commit()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public void clear()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public boolean skipMapped(final Object instance, final long objectId)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public boolean skip(final Object instance)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public boolean skipNulled(final Object instance)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long size()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long currentCapacity()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long maximumCapacity()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long store(final Object instance)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long[] storeAll(final Object... instances)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public void storeAll(final Iterable<?> instances)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public PersistenceStorer reinitialize()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public PersistenceStorer reinitialize(final long initialCapacity)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public PersistenceStorer ensureCapacity(final long desiredCapacity)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}
	
	@Override
	public void registerCommitListener(final PersistenceCommitListener listener)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}
	
	@Override
	public boolean registerSubStorer(final Storer subStorer)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

}
