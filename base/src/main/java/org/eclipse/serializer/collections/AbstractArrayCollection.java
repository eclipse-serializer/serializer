package org.eclipse.serializer.collections;

/*-
 * #%L
 * Eclipse Serializer Base
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

import org.eclipse.serializer.math.XMath;
import org.eclipse.serializer.util.X;


/**
 * @param <E> type of contained elements
 * 
 *
 */
public abstract class AbstractArrayCollection<E> extends AbstractBaseCollection<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	// internal marker object for marking to be removed slots for batch removal and null ambiguity resolution
	private static final transient Object MARKER = new Object();



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings("unchecked")
	protected static <E> E marker()
	{
		return (E)MARKER;
	}


	@SuppressWarnings("unchecked")
	protected static <E> E[] newArray(final int length)
	{
		return (E[])new Object[length];
	}

	protected static <E> E[] newArray(final int length, final E[] oldData, final int oldDataLength)
	{
		final E[] newArray = newArray(length);
		System.arraycopy(oldData, 0, newArray, 0, oldDataLength);
		return newArray;
	}

	public static int pow2BoundMaxed(final long n)
	{
		return XMath.pow2BoundMaxed(X.checkArrayRange(n));
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/**
	 * This is an internal shortcut method to provide fast access to the various array-backed list implementations'
	 * storage arrays.<br>
	 * The purpose of this method is to allow access to the array only for read-only procedures, never for modifying
	 * accesses.
	 *
	 * @return the storage array used by the list, containing all elements in straight order.
	 */
	protected abstract E[] internalGetStorageArray();

}
