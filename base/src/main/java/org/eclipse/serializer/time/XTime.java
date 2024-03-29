package org.eclipse.serializer.time;

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

import java.util.*;


public final class XTime
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private static final int
		MIN_MONTH        =   1,
		MIN_DAY_IN_MONTH =   1,
		MIN_HOUR         =   0,
		MIN_MINUTE       =   0,
		MIN_SECOND       =   0,
		MIN_MILLI        =   0,
		MAX_MONTH        =  12,
		MAX_DAY_IN_MONTH =  31,
		MAX_HOUR         =  23,
		MAX_MINUTE       =  59,
		MAX_SECOND       =  59,
		MAX_MILLI        = 999
			;
	
	
	
	/**
	 * Short-cut for {@code new Date(System.currentTimeMillis())}.
	 * Returns a new {@link Date} instance representing the current time in the current {@link TimeZone}
	 * and for the current {@link Locale}.
	 * @return right now!
	 */
	public static Date now()
	{
		return new Date();
	}
	
	public static Date timestamp(
		final int year,
		final int month,
		final int day,
		final int hour,
		final int minute,
		final int second,
		final int milliseconds
	)
	{
		if(month < MIN_MONTH || month > MAX_MONTH)
		{
			throw new IllegalArgumentException("Invalid month: " + month);
		}
		if(day < MIN_DAY_IN_MONTH || day > MAX_DAY_IN_MONTH)
		{
			throw new IllegalArgumentException("Invalid day: " + day);
		}
		if(hour < MIN_HOUR || hour > MAX_HOUR)
		{
			throw new IllegalArgumentException("Invalid hour: " + hour);
		}
		if(minute < MIN_MINUTE || minute > MAX_MINUTE)
		{
			throw new IllegalArgumentException("Invalid minute: " + minute);
		}
		if(second < MIN_SECOND || second > MAX_SECOND)
		{
			throw new IllegalArgumentException("Invalid second: " + second);
		}
		if(milliseconds < MIN_MILLI || milliseconds > MAX_MILLI)
		{
			throw new IllegalArgumentException("Invalid milliseconds: " + milliseconds);
		}
		
		final Calendar c = Calendar.getInstance();
		c.clear();
		c.set(year, month - 1, day, hour, minute, second);
		c.set(Calendar.MILLISECOND, milliseconds);
		return c.getTime();
	}
	
	public static Date timestamp(
		final int year,
		final int month,
		final int day,
		final int hour,
		final int minute,
		final int second
	)
	{
		return timestamp(year, month, day, hour, minute, second, 0);
	}
	
	public static Date date(
		final int year,
		final int month,
		final int day
	)
	{
		return timestamp(year, month, day);
	}
	
	public static Date timestamp(
		final int year,
		final int month,
		final int day
	)
	{
		return timestamp(year, month, day, 0, 0, 0, 0);
	}
	
	
	public static GregorianCalendar asGregCal(final Date date)
	{
		final GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(date);
		return gc;
	}
	
	public static GregorianCalendar asGregCal(final long timestamp)
	{
		final GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp);
		return gc;
	}
	
	
	public static int currentYear()
	{
		return Calendar.getInstance().get(Calendar.YEAR);
	}
	
	
	
	public static long calculateNanoTimeBudgetBound(final long nanoTimeBudget)
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
