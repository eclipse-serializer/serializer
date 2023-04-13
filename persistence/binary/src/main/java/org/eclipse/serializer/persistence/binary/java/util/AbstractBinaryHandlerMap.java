package org.eclipse.serializer.persistence.binary.java.util;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
 * %%
 * Copyright (C) 2023 Eclipse Foundation
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

import java.util.Map;

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.collections.old.KeyValueFlatCollector;
import org.eclipse.serializer.collections.old.OldCollections;
import org.eclipse.serializer.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;


public abstract class AbstractBinaryHandlerMap<T extends Map<?, ?>>
extends AbstractBinaryHandlerCustomCollection<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_ELEMENTS = 0;

	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final long getElementCount(final Binary data)
	{
		return data.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public AbstractBinaryHandlerMap(final Class<T> type)
	{
		super(
			type,
			keyValuesFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// store elements simply as array binary form
		data.storeMapEntrySet(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance.entrySet()   ,
			handler
		);
	}

	@Override
	public void updateState(
		final Binary                 data    ,
		final T                      instance,
		final PersistenceLoadHandler handler
	)
	{
		instance.clear();
		final int elementCount = X.checkArrayRange(getElementCount(data));
		final KeyValueFlatCollector<Object, Object> collector = KeyValueFlatCollector.New(elementCount);
		data.collectKeyValueReferences(BINARY_OFFSET_ELEMENTS, elementCount, handler, collector);
		data.registerHelper(instance, collector.yield());
	}

	@Override
	public void complete(
		final Binary                 data    ,
		final T                      instance,
		final PersistenceLoadHandler handler
	)
	{
		OldCollections.populateMapFromHelperArray(instance, data.getHelper(instance));
	}
	
	@Override
	public void iterateInstanceReferences(
		final T                   instance,
		final PersistenceFunction iterator
	)
	{
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public void iterateLoadableReferences(
		final Binary                     data  ,
		final PersistenceReferenceLoader loader
	)
	{
		data.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, loader);
	}
		
}
