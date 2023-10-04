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

import java.util.LinkedHashSet;

import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerLinkedHashSet extends AbstractBinaryHandlerSet<LinkedHashSet<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<LinkedHashSet<?>> handledType()
	{
		return (Class)LinkedHashSet.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerLinkedHashSet New()
	{
		return new BinaryHandlerLinkedHashSet();
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerLinkedHashSet()
	{
		super(
			handledType()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final LinkedHashSet<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new LinkedHashSet<>();
	}

}
