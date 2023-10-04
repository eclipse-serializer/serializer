package org.eclipse.serializer.persistence.binary.java.util;

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

import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerCopyOnWriteArrayList extends AbstractBinaryHandlerList<CopyOnWriteArrayList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<CopyOnWriteArrayList<?>> handledType()
	{
		return (Class)CopyOnWriteArrayList.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerCopyOnWriteArrayList New()
	{
		return new BinaryHandlerCopyOnWriteArrayList();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerCopyOnWriteArrayList()
	{
		super(handledType());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final CopyOnWriteArrayList<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new CopyOnWriteArrayList<>();
	}

}
