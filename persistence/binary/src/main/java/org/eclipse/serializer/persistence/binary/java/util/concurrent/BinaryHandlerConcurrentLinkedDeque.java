package org.eclipse.serializer.persistence.binary.java.util.concurrent;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
 * %%
 * Copyright (C) 2023 MicroStream Software
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

import java.util.concurrent.ConcurrentLinkedDeque;

import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.java.util.AbstractBinaryHandlerQueue;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerConcurrentLinkedDeque extends AbstractBinaryHandlerQueue<ConcurrentLinkedDeque<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentLinkedDeque<?>> handledType()
	{
		return (Class)ConcurrentLinkedDeque.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerConcurrentLinkedDeque New()
	{
		return new BinaryHandlerConcurrentLinkedDeque();
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerConcurrentLinkedDeque()
	{
		super(
			handledType()
		);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public ConcurrentLinkedDeque<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new ConcurrentLinkedDeque<>();
	}
	
}
