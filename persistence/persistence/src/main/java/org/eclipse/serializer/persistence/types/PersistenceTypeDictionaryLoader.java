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
 * Source of the textual on-disk type dictionary, returned as a single string. Paired with
 * {@link PersistenceTypeDictionaryStorer} to read/write the dictionary, and combined into
 * {@link PersistenceTypeDictionaryIoHandler}.
 *
 * @see PersistenceTypeDictionaryStorer
 * @see PersistenceTypeDictionaryIoHandler
 * @see PersistenceTypeDictionaryParser
 */
public interface PersistenceTypeDictionaryLoader
{
	/**
	 * Reads the persisted type dictionary and returns its textual form, or an empty string if no dictionary
	 * has been written yet.
	 *
	 * @return the textual type dictionary.
	 */
	public String loadTypeDictionary();
}
