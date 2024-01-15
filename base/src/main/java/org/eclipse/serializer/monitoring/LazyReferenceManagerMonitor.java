package org.eclipse.serializer.monitoring;

import java.lang.ref.WeakReference;

import org.eclipse.serializer.reference.LazyReferenceManager;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
