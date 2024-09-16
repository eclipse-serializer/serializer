package org.eclipse.serializer.persistence.types;
/*-
 * #%L
 * Eclipse Serializer Persistence
 * %%
 * Copyright (C) 2024 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

public interface SubStoring
{
	/**
	 * Registers a subsidiary {@link Storer} instance.<br>
	 * When the current storing context (usually another {@link Storer}) is committed, it first commits all its
	 * registered subsidiary storers. Only if all of those commit successfully, the current storing context commits its
	 * collected data.<p>
	 * This is useful for tieing together multiple storers ("transactions") and commit them all at a defined point instead of
	 * independently one by one during the collection of data. A "transaction of transactions", so to speak.
	 * 
	 * @param subStorer the subsidiary {@link Storer} instance to be registered
	 * 
	 * @return <code>true</code> if the passed {@link Storer} instance has been newly registered. <code>false</code> if it already was registered.
	 */
	public boolean registerSubStorer(Storer subStorer);
}
