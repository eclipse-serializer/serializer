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

import static org.eclipse.serializer.util.X.notNull;

import java.util.List;

import org.eclipse.serializer.exceptions.NoSuchMethodRuntimeException;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.reflect.XReflect;


public class BinaryHandlerGenericList<T extends List<?>> extends AbstractBinaryHandlerList<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final <T extends List<?>> BinaryHandlerGenericList<T> New(
		final Class<T> type
	)
		throws NoSuchMethodRuntimeException
	{
		final org.eclipse.serializer.functional.Instantiator<T> instantiator = XReflect.WrapDefaultConstructor(type);
		
		return New(type, l ->
			instantiator.instantiate()
		);
	}
	
	public static final <T extends List<?>> BinaryHandlerGenericList<T> New(
		final Class<T>        type,
		final Instantiator<T> instantiator
	)
	{
		return new BinaryHandlerGenericList<>(
			notNull(type),
			notNull(instantiator)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Instantiator<T> instantiator;
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected BinaryHandlerGenericList(final Class<T> type, final Instantiator<T> instantiator)
	{
		super(type);
		this.instantiator = notNull(instantiator);
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public T create(final Binary data, final PersistenceLoadHandler handler)
	{
		return this.instantiator.instantiateList(
			getElementCount(data)
		);
	}

	
	
	public interface Instantiator<T extends List<?>>
	{
		public T instantiateList(long elementCount);
	}

}
