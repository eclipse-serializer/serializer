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
 * Sink for the textual type dictionary produced by
 * {@link PersistenceTypeDictionaryAssembler}. Paired with {@link PersistenceTypeDictionaryLoader} to read/write
 * the dictionary, and combined into {@link PersistenceTypeDictionaryIoHandler}.
 *
 * @see PersistenceTypeDictionaryLoader
 * @see PersistenceTypeDictionaryIoHandler
 * @see PersistenceTypeDictionaryAssembler
 */
public interface PersistenceTypeDictionaryStorer
{
	/**
	 * Writes the passed textual type dictionary to its persistent form, replacing any previously stored
	 * content.
	 *
	 * @param typeDictionaryString the textual type dictionary to persist.
	 */
	public void storeTypeDictionary(final String typeDictionaryString);
}
