package org.eclipse.serializer.persistence.binary.java.lang;

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

import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerStringBuffer extends AbstractBinaryHandlerAbstractStringBuilder<StringBuffer>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerStringBuffer New()
	{
		return new BinaryHandlerStringBuffer();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerStringBuffer()
	{
		super(StringBuffer.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final StringBuffer                    instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		this.storeData(data, XChars.toCharArray(instance), instance.capacity(), objectId, handler);
	}

	@Override
	public final StringBuffer create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new StringBuffer(this.readCapacity(data));
	}

	@Override
	public void updateState(final Binary data, final StringBuffer instance, final PersistenceLoadHandler handler)
	{
		// because clear() does not exist.
		instance.delete(0, instance.length());
		
		instance.ensureCapacity(this.readCapacity(data));
		instance.append(this.readChars(data));
	}

}
