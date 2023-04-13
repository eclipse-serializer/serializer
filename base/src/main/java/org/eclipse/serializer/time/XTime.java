package org.eclipse.serializer.time;

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

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class XTime
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	/**
	 * Short-cut for {@code new Date(System.currentTimeMillis())}.
	 * Returns a new {@link Date} instance representing the current time in the current {@link TimeZone}
	 * and for the current {@link Locale}.
	 * @return right now!
	 */
	public static final Date now()
	{
		return new Date();
	}

	public static final long calculateNanoTimeBudgetBound(final long nanoTimeBudget)
	{
		final long timeBudgetBound = System.nanoTime() + nanoTimeBudget;

		// giving a very high or MAX_VALUE (unlimited) time budget will cause negative values
		return timeBudgetBound >= 0
			? timeBudgetBound
			: Long.MAX_VALUE
		;
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XTime()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
