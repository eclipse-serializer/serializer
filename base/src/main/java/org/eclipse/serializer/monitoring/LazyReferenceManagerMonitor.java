package org.eclipse.serializer.monitoring;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 - 2024 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.lang.ref.WeakReference;

import org.eclipse.serializer.reference.LazyReferenceManager;

public class LazyReferenceManagerMonitor implements LazyReferenceManagerMonitorMBean, MetricMonitor
{
	private final WeakReference<LazyReferenceManager> lazyReferenceManager;
	
	int lazyRefCount;
	int lazyRefLoadedCount;

	public LazyReferenceManagerMonitor(final LazyReferenceManager lazyReferenceManager)
	{
		super();
		this.lazyReferenceManager = new WeakReference<>(lazyReferenceManager);
	}

	@Override
	public int getRegisteredLazyReferencesCount()
	{
		return this.lazyRefCount;
	}
	
	@Override
	public int getLoadedLazyReferencesCount()
	{
		return this.lazyRefLoadedCount;
	}
	
	@Override
	public int getUnLoadedLazyReferencesCount()
	{
		return this.lazyRefCount - this.lazyRefLoadedCount;
	}
		
	@Override
	public void unloadAll()
	{
		this.lazyReferenceManager.get().clear();
	}
		
	@Override
	public String getName()
	{
		return "name=LazyReferenceManager";
	}

	public void update(int lazyReferences, int loadedLazyReferences)
	{
		this.lazyRefCount = lazyReferences;
		this.lazyRefLoadedCount = loadedLazyReferences;
	}
}
