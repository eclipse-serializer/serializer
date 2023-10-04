package org.eclipse.serializer.persistence.binary.java.util.concurrent;

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

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.java.util.AbstractBinaryHandlerMap;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerConcurrentHashMap extends AbstractBinaryHandlerMap<ConcurrentHashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentHashMap<?, ?>> handledType()
	{
		return (Class)ConcurrentHashMap.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerConcurrentHashMap New()
	{
		return new BinaryHandlerConcurrentHashMap();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerConcurrentHashMap()
	{
		super(
			handledType()
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public ConcurrentHashMap<?, ?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new ConcurrentHashMap<>(
			X.checkArrayRange(getElementCount(data))
		);
	}
	
}
