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

import static org.eclipse.serializer.util.X.notNull;

import java.util.Queue;

import org.eclipse.serializer.exceptions.NoSuchMethodRuntimeException;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.reflect.XReflect;


public class BinaryHandlerGenericQueue<T extends Queue<?>> extends AbstractBinaryHandlerQueue<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final <T extends Queue<?>> BinaryHandlerGenericQueue<T> New(
		final Class<T> type
	)
		throws NoSuchMethodRuntimeException
	{
		final org.eclipse.serializer.functional.Instantiator<T> instantiator = XReflect.WrapDefaultConstructor(type);
		
		return New(type, l ->
			instantiator.instantiate()
		);
	}
	
	public static final <T extends Queue<?>> BinaryHandlerGenericQueue<T> New(
		final Class<T>        type,
		final Instantiator<T> instantiator
	)
	{
		return new BinaryHandlerGenericQueue<>(
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

	protected BinaryHandlerGenericQueue(final Class<T> type, final Instantiator<T> instantiator)
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
		return this.instantiator.instantiateQueue(
			getElementCount(data)
		);
	}
	
	
	
	public interface Instantiator<T extends Queue<?>>
	{
		public T instantiateQueue(long elementCount);
	}

}
