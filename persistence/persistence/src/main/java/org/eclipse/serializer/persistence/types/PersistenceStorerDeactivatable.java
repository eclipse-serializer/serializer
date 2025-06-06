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

/**
 * A {@link org.eclipse.serializer.persistence.types.PersistenceStorer PersistenceStorer} implementation that allows
 * switching between the supplied {@code PersistenceStorer} instance and a
 * {@link org.eclipse.serializer.persistence.types.PersistenceStorerDeactivated PersistenceStorerDeactivated}
 * instance.
 *
 */
public class PersistenceStorerDeactivatable implements PersistenceStorer {

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private       PersistenceStorer actual;
	private final PersistenceStorer fullFeaturedStorer;
	private final PersistenceStorer noOpStorer;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public PersistenceStorerDeactivatable(final PersistenceStorer persistenceStorer)
	{
		this.fullFeaturedStorer = persistenceStorer;
		this.noOpStorer = new PersistenceStorerDeactivated();
		this.actual = this.fullFeaturedStorer;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	/**
	 * Enable or disable writing support.
	 * 
	 * @param enableWrites true to enable writing, false to disable.
	 */
	public void setWriteEnabled(final boolean enableWrites)
	{
		if(enableWrites)
		{
			this.actual = this.fullFeaturedStorer;
		}
		else
		{
			this.actual = this.noOpStorer;
		}
	}
	
	/**
	 * Check if writing support is enabled.
	 * 
	 * @return true if writes are enabled
	 */
	public boolean isWriteEnabled()
	{
		return this.actual == this.fullFeaturedStorer;
	}
	
	/**
	 * Enable writing support.
	 */
	public void enableWrites()
	{
		this.actual = this.fullFeaturedStorer;
	}
	
	/**
	 * Disable writing support
	 */
	public void disableWrites()
	{
		this.actual = this.noOpStorer;
	}

	@Override
	public PersistenceStorer reinitialize()
	{
		return this.actual.reinitialize();
	}

	@Override
	public long store(final Object instance)
	{
		return this.actual.store(instance);
	}
	
	@Override
	public long store(Object instance, long objectId)
	{
		return this.actual.store(instance, objectId);
	}

	@Override
	public PersistenceStorer reinitialize(final long initialCapacity)
	{
		return this.actual.reinitialize(initialCapacity);
	}

	@Override
	public Object commit()
	{
		return this.actual.commit();
	}

	@Override
	public PersistenceStorer ensureCapacity(final long desiredCapacity)
	{
		return this.actual.ensureCapacity(desiredCapacity);
	}

	@Override
	public void clear()
	{
		this.actual.clear();
	}

	@Override
	public long[] storeAll(final Object... instances)
	{
		return this.actual.storeAll(instances);
	}

	@Override
	public boolean skipMapped(final Object instance, final long objectId)
	{
		return this.actual.skipMapped(instance, objectId);
	}

	@Override
	public void storeAll(final Iterable<?> instances)
	{
		this.actual.storeAll(instances);
	}

	@Override
	public boolean skip(final Object instance)
	{
		return this.actual.skip(instance);
	}

	@Override
	public boolean skipNulled(final Object instance)
	{
		return this.actual.skipNulled(instance);
	}

	@Override
	public long size()
	{
		return this.actual.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.actual.isEmpty();
	}

	@Override
	public long currentCapacity()
	{
		return this.actual.currentCapacity();
	}

	@Override
	public long maximumCapacity()
	{
		return this.actual.maximumCapacity();
	}

	@Override
	public void registerCommitListener(final PersistenceCommitListener listener)
	{
		this.actual.registerCommitListener(listener);
	}


	@Override
	public void registerRegistrationListener(PersistenceObjectRegistrationListener listener)
	{
		this.actual.registerRegistrationListener(listener);
	}
	
}
