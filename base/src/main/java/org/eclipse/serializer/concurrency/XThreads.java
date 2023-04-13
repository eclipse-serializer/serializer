package org.eclipse.serializer.concurrency;

/*-
 * #%L
 * Eclipse Serializer Base
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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class XThreads
{
	///////////////////////////////////////////////////////////////////////////
	// Throwable.getStackTraceElement workaround //
	//////////////////////////////////////////////

	// CHECKSTYLE.OFF: ConstantName: method names are intentionally unchanged

	private static final Method Throwable_getStackTraceElement = getThrowable_getStackTraceElement();

	// CHECKSTYLE.ON: ConstantName

	private static final Method getThrowable_getStackTraceElement()
	{
		try
		{
			final Method m = Throwable.class.getDeclaredMethod("getStackTraceElement", int.class);
			m.setAccessible(true);
			return m;
		}
		catch(final Exception e)
		{
			return null;
		}
	}
	
	public static final StackTraceElement getStackTraceElement(final Integer index)
	{
		try
		{
			return (StackTraceElement)Throwable_getStackTraceElement.invoke(new Throwable(), index);
		}
		catch(final InvocationTargetException e)
		{
			// hacky due to misconceived checked exception concept
			throw (RuntimeException)e.getCause();
		}
		catch(final Exception e)
		{
			// do it the slow way
			return new Throwable().getStackTrace()[index]; // NPE intentional
		}
	}
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XThreads()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}

