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

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;


class XTimeTest
{
	// -------------------------------------------------------------------------
	// now()
	// -------------------------------------------------------------------------

	@Test
	void now_returnsNonNullDate()
	{
		assertNotNull(XTime.now());
	}

	@Test
	void now_approximatesCurrentTime()
	{
		final long before = System.currentTimeMillis();
		final Date now    = XTime.now();
		final long after  = System.currentTimeMillis();
		assertTrue(now.getTime() >= before && now.getTime() <= after);
	}

	// -------------------------------------------------------------------------
	// timestamp() — month validation
	// -------------------------------------------------------------------------

	@Test
	void timestamp_monthZero_throwsIllegalArgument()
	{
		assertThrows(IllegalArgumentException.class,
			() -> XTime.timestamp(2023, 0, 1, 0, 0, 0, 0));
	}

	@Test
	void timestamp_month13_throwsIllegalArgument()
	{
		assertThrows(IllegalArgumentException.class,
			() -> XTime.timestamp(2023, 13, 1, 0, 0, 0, 0));
	}

	// -------------------------------------------------------------------------
	// timestamp() — day validation
	// -------------------------------------------------------------------------

	@Test
	void timestamp_dayZero_throwsIllegalArgument()
	{
		assertThrows(IllegalArgumentException.class,
			() -> XTime.timestamp(2023, 1, 0, 0, 0, 0, 0));
	}

	@Test
	void timestamp_day32_throwsIllegalArgument()
	{
		assertThrows(IllegalArgumentException.class,
			() -> XTime.timestamp(2023, 1, 32, 0, 0, 0, 0));
	}

	// -------------------------------------------------------------------------
	// timestamp() — hour, minute, second, millisecond validation
	// -------------------------------------------------------------------------

	@Test
	void timestamp_hourNegative_throwsIllegalArgument()
	{
		assertThrows(IllegalArgumentException.class,
			() -> XTime.timestamp(2023, 1, 1, -1, 0, 0, 0));
	}

	@Test
	void timestamp_hour24_throwsIllegalArgument()
	{
		assertThrows(IllegalArgumentException.class,
			() -> XTime.timestamp(2023, 1, 1, 24, 0, 0, 0));
	}

	@Test
	void timestamp_minuteNegative_throwsIllegalArgument()
	{
		assertThrows(IllegalArgumentException.class,
			() -> XTime.timestamp(2023, 1, 1, 0, -1, 0, 0));
	}

	@Test
	void timestamp_minute60_throwsIllegalArgument()
	{
		assertThrows(IllegalArgumentException.class,
			() -> XTime.timestamp(2023, 1, 1, 0, 60, 0, 0));
	}

	@Test
	void timestamp_secondNegative_throwsIllegalArgument()
	{
		assertThrows(IllegalArgumentException.class,
			() -> XTime.timestamp(2023, 1, 1, 0, 0, -1, 0));
	}

	@Test
	void timestamp_second60_throwsIllegalArgument()
	{
		assertThrows(IllegalArgumentException.class,
			() -> XTime.timestamp(2023, 1, 1, 0, 0, 60, 0));
	}

	@Test
	void timestamp_millisecondsNegative_throwsIllegalArgument()
	{
		assertThrows(IllegalArgumentException.class,
			() -> XTime.timestamp(2023, 1, 1, 0, 0, 0, -1));
	}

	@Test
	void timestamp_milliseconds1000_throwsIllegalArgument()
	{
		assertThrows(IllegalArgumentException.class,
			() -> XTime.timestamp(2023, 1, 1, 0, 0, 0, 1000));
	}

	// -------------------------------------------------------------------------
	// timestamp() — correct field values on valid input
	// -------------------------------------------------------------------------

	@Test
	void timestamp_validInput_producesCorrectCalendarFields()
	{
		final Date     result = XTime.timestamp(2023, 6, 15, 10, 30, 45, 500);
		final Calendar c      = Calendar.getInstance();
		c.setTime(result);
		assertAll(
			() -> assertEquals(2023, c.get(Calendar.YEAR)),
			() -> assertEquals(5,    c.get(Calendar.MONTH)),         // Calendar months are 0-based
			() -> assertEquals(15,   c.get(Calendar.DAY_OF_MONTH)),
			() -> assertEquals(10,   c.get(Calendar.HOUR_OF_DAY)),
			() -> assertEquals(30,   c.get(Calendar.MINUTE)),
			() -> assertEquals(45,   c.get(Calendar.SECOND)),
			() -> assertEquals(500,  c.get(Calendar.MILLISECOND))
		);
	}

	@Test
	void timestamp_midnight_allTimeFieldsAreZero()
	{
		final Date     result = XTime.timestamp(2023, 1, 1, 0, 0, 0, 0);
		final Calendar c      = Calendar.getInstance();
		c.setTime(result);
		assertAll(
			() -> assertEquals(0, c.get(Calendar.HOUR_OF_DAY)),
			() -> assertEquals(0, c.get(Calendar.MINUTE)),
			() -> assertEquals(0, c.get(Calendar.SECOND)),
			() -> assertEquals(0, c.get(Calendar.MILLISECOND))
		);
	}

	@Test
	void timestamp_endOfYear_allFieldsAtMaximum()
	{
		final Date     result = XTime.timestamp(2023, 12, 31, 23, 59, 59, 999);
		final Calendar c      = Calendar.getInstance();
		c.setTime(result);
		assertAll(
			() -> assertEquals(2023, c.get(Calendar.YEAR)),
			() -> assertEquals(11,   c.get(Calendar.MONTH)),
			() -> assertEquals(31,   c.get(Calendar.DAY_OF_MONTH)),
			() -> assertEquals(23,   c.get(Calendar.HOUR_OF_DAY)),
			() -> assertEquals(59,   c.get(Calendar.MINUTE)),
			() -> assertEquals(59,   c.get(Calendar.SECOND)),
			() -> assertEquals(999,  c.get(Calendar.MILLISECOND))
		);
	}

	// -------------------------------------------------------------------------
	// timestamp() overloads
	// -------------------------------------------------------------------------

	@Test
	void timestamp_withoutMilliseconds_equalsZeroMilliseconds()
	{
		assertEquals(
			XTime.timestamp(2023, 3, 10, 8, 0, 0, 0),
			XTime.timestamp(2023, 3, 10, 8, 0, 0)
		);
	}

	@Test
	void timestamp_dateOnly_timeIsSetToMidnight()
	{
		final Date     result = XTime.timestamp(2023, 3, 10);
		final Calendar c      = Calendar.getInstance();
		c.setTime(result);
		assertAll(
			() -> assertEquals(0, c.get(Calendar.HOUR_OF_DAY)),
			() -> assertEquals(0, c.get(Calendar.MINUTE)),
			() -> assertEquals(0, c.get(Calendar.SECOND)),
			() -> assertEquals(0, c.get(Calendar.MILLISECOND))
		);
	}

	// -------------------------------------------------------------------------
	// date() — alias
	// -------------------------------------------------------------------------

	@Test
	void date_isEquivalentToTimestampDateOnly()
	{
		assertEquals(XTime.timestamp(2023, 7, 4), XTime.date(2023, 7, 4));
	}

	// -------------------------------------------------------------------------
	// asGregCal(Date)
	// -------------------------------------------------------------------------

	@Test
	void asGregCal_fromDate_epochMillisMatch()
	{
		final Date              date = XTime.timestamp(2023, 6, 15, 12, 0, 0, 0);
		final GregorianCalendar gc   = XTime.asGregCal(date);
		assertEquals(date.getTime(), gc.getTimeInMillis());
	}

	// -------------------------------------------------------------------------
	// asGregCal(long)
	// -------------------------------------------------------------------------

	@Test
	void asGregCal_fromLong_epochMillisMatch()
	{
		final long              millis = XTime.timestamp(2023, 6, 15, 12, 0, 0, 0).getTime();
		final GregorianCalendar gc     = XTime.asGregCal(millis);
		assertEquals(millis, gc.getTimeInMillis());
	}

	// -------------------------------------------------------------------------
	// currentYear()
	// -------------------------------------------------------------------------

	@Test
	void currentYear_matchesCalendarInstanceYear()
	{
		assertEquals(Calendar.getInstance().get(Calendar.YEAR), XTime.currentYear());
	}

	@Test
	void currentYear_returnsPlausibleValue()
	{
		final int year = XTime.currentYear();
		assertTrue(year >= 2020 && year <= 2100,
			"currentYear() returned implausible value: " + year);
	}

	// -------------------------------------------------------------------------
	// calculateNanoTimeBudgetBound()
	// -------------------------------------------------------------------------

	@Test
	void calculateNanoTimeBudgetBound_zeroBudget_returnsApproximatelyNow()
	{
		final long before = System.nanoTime();
		final long bound  = XTime.calculateNanoTimeBudgetBound(0);
		final long after  = System.nanoTime();
		// bound = nanoTime() + 0 captured inside the call, must lie in [before, after]
		assertTrue(bound >= before && bound <= after,
			"Budget of 0 should return approximately System.nanoTime()");
	}

	@Test
	void calculateNanoTimeBudgetBound_smallBudget_boundEqualsNowPlusBudget()
	{
		final long budget = 1_000_000_000L; // 1 second in nanos
		final long before = System.nanoTime();
		final long bound  = XTime.calculateNanoTimeBudgetBound(budget);
		final long after  = System.nanoTime();
		// bound = internal_nanoTime + budget, where before <= internal_nanoTime <= after
		assertTrue(bound >= before + budget,
			"Bound must be at least before + budget");
		assertTrue(bound <= after + budget,
			"Bound must not exceed after + budget");
	}

	@Test
	void calculateNanoTimeBudgetBound_maxValueBudget_returnsMaxValue()
	{
		// Long.MAX_VALUE budget always overflows → fallback must be Long.MAX_VALUE
		assertEquals(Long.MAX_VALUE, XTime.calculateNanoTimeBudgetBound(Long.MAX_VALUE));
	}

	@Test
	void calculateNanoTimeBudgetBound_nearMaxValueBudget_overflowReturnsMaxValue()
	{
		// Any JVM running > 1 ms means nanoTime > 1_000_000, which causes overflow
		final long nearMax = Long.MAX_VALUE - 1_000_000L;
		assertEquals(Long.MAX_VALUE, XTime.calculateNanoTimeBudgetBound(nearMax));
	}
}
