package org.eclipse.serializer.persistence.binary.types;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;


/**
 * Skeletal base for handlers of iterable types that persist their elements as a single simple binary list
 * at offset 0 (no header fields, no per-element key/value structure). Suitable for &ldquo;flat&rdquo;
 * collections like {@code ArrayList}, {@code HashSet}, etc., where the entire persisted form is just an
 * element list.
 * <p>
 * The default {@link #store} writes the element list via {@link Binary#storeIterableAsList}; subclasses
 * only need to implement {@link #getElementCount(Iterable)}}, the {@link #create} logic, and any post-create
 * loading.
 *
 * @param <T> the iterable runtime type handled.
 */
public abstract class AbstractBinaryHandlerCustomIterableSimpleListElements<T extends Iterable<?>>
extends AbstractBinaryHandlerCustomIterable<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_ELEMENTS = 0;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	protected static final long getElementCount(final Binary data)
	{
		return data.getListElementCountReferences(BINARY_OFFSET_ELEMENTS);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCustomIterableSimpleListElements(final Class<T> type)
	{
		super(
			type,
			SimpleArrayFields()
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected long binaryOffsetElements()
	{
		return BINARY_OFFSET_ELEMENTS;
	}
	
	protected abstract long getElementCount(T instance);
	
	@Override
	public void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// store elements simply as array binary form
		data.storeIterableAsList(
			this.typeId()                 ,
			objectId                      ,
			this.binaryOffsetElements()   ,
			instance                      ,
			this.getElementCount(instance),
			handler
		);
	}

	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		data.iterateListElementReferences(this.binaryOffsetElements(), iterator);
	}
	
}
