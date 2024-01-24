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

import org.eclipse.serializer.reference.LazyReferenceManager;

/**
 * JMX MBean definition that provides monitoring and metrics from
 * the {@link LazyReferenceManager}.
 */
@MonitorDescription("Provides monitoring and metrics data of the LazyReferenceManager.")
public interface LazyReferenceManagerMonitorMBean
{
	/**
	 * @return the number of registered lazy references.
	 */
	@MonitorDescription("the number of registered lazy references.")
	int getRegisteredLazyReferencesCount();

	/**
	 * @return the number of registered, not loaded lazy references.
	 */
	@MonitorDescription("The number of registered, not loaded lazy references.")
	int getUnLoadedLazyReferencesCount();

	/**
	 * @return the number of registered, loaded lazy references.
	 */
	@MonitorDescription("The number of registered, loaded lazy references.")
	int getLoadedLazyReferencesCount();
	
	/**
	 * try to unload all lazy references.
	 */
	@MonitorDescription("Try to unload all lazy references registered by the LazyReferenceManager")
	void unloadAll();
}
