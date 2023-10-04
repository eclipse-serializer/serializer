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

import java.util.ArrayList;

import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerArrayList extends AbstractBinaryHandlerList<ArrayList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ArrayList<?>> handledType()
	{
		return (Class)ArrayList.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerArrayList New()
	{
		return new BinaryHandlerArrayList();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerArrayList()
	{
		super(handledType());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final ArrayList<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		final long elementCount = getElementCount(data);
		
		/*
		 * InitialCapacity 1 instead of default constructor is a workaround.
		 * Using the default constructor causes #ensureCapacity to yield incorrect behavior for values of
		 * 10 or below, which causes a subsequent array length validation exception.
		 * Also see https://bugs.openjdk.java.net/browse/JDK-8206945
		 * 
		 * However, having an actually zero-size instance should still cause the internal dummy array instance
		 * to be used instead of a redundant one that unnecessarily occupies memory. Hence, the if.
		 */
		return elementCount == 0
			? new ArrayList<>(0)
			: new ArrayList<>(1)
		;
	}

}
