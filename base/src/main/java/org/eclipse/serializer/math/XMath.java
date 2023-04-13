package org.eclipse.serializer.math;

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

import java.util.Random;

import org.eclipse.serializer.exceptions.NumberRangeException;

public final class XMath
{
	// CHECKSTYLE.OFF: MagicNumber: all magic numbers are intentional in this class

	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final transient Random RANDOM = new Random();

	// or "1 << 30" or 2^30. Highest int value that can be achieved by leftshifting 1.
	private static final transient int MAX_POW_2_INT = 1_073_741_824;
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final int pow2BoundMaxed(final int n)
	{
		if(n > MAX_POW_2_INT)
		{
			return Integer.MAX_VALUE;
		}
		int i = 1;
		while(i < n)
		{
			i <<= 1;
		}
		return i;
	}

	public static final int pow2BoundCapped(final int n)
	{
		if(n >= MAX_POW_2_INT)
		{
			return MAX_POW_2_INT;
		}
		int i = 1;
		while(i < n)
		{
			i <<= 1;
		}
		return i;
	}

	/**
	 * Common rounding variant for 2 decimals.
	 * @param value the decimal value to be rounded.
	 * @return the passed value rounded to 2 decimals.
	 */
	public static final double round2(final double value)
	{
		return StrictMath.floor(value * 100.0d + 0.5d) / 100.0d;
	}

	/**
	 * Common rounding variant for 3 decimals.
	 * @param value the decimal value to be rounded.
	 * @return the passed value rounded to 3 decimals.
	 */
	public static final double round3(final double value)
	{
		return StrictMath.floor(value * 1000.0d + 0.5d) / 1000.0d;
	}

	/**
	 * Common rounding variant for 6 decimals.
	 * @param value the decimal value to be rounded.
	 * @return the passed value rounded to 6 decimals.
	 */
	public static final double round6(final double value)
	{
		return StrictMath.floor(value * 1_000_000.0d + 0.5d) / 1_000_000.0d;
	}


	/**
	 * @return the random
	 */
	public static final Random random()
	{
		return RANDOM;
	}

	public static int positive(final int value) throws NumberRangeException
	{
		if(value > 0)
		{
			return value;
		}
		
		throw new NumberRangeException("Value is not positive: " + value);
	}

	public static int notNegative(final int value) throws NumberRangeException
	{
		if(value < 0)
		{
			throw new NumberRangeException("Value is negative: " + value);
		}
		return value;
	}

	public static Integer notNegative(final Integer value) throws NumberRangeException
	{
		if(value != null && value < 0)
		{
			throw new NumberRangeException("Value is negative: " + value);
		}
		return value;
	}

	public static long positive(final long value) throws NumberRangeException
	{
		if(value > 0)
		{
			return value;
		}
		
		throw new NumberRangeException("Value is not positive: " + value);
	}

	public static long notNegative(final long value) throws NumberRangeException
	{
		if(value < 0)
		{
			throw new NumberRangeException("Value is negative: " + value);
		}
		return value;
	}

	public static Long notNegative(final Long value) throws NumberRangeException
	{
		if(value != null && value < 0)
		{
			throw new NumberRangeException("Value is negative: " + value);
		}
		return value;
	}

	public static double positive(final double value) throws NumberRangeException
	{
		if(value > 0)
		{
			return value;
		}

		throw new NumberRangeException("Value is not positive: " + value);
	}

	public static double notNegative(final double value) throws NumberRangeException
	{
		if(value < 0)
		{
			throw new NumberRangeException("Value is negative: " + value);
		}
		return value;
	}

	public static float positive(final float value) throws NumberRangeException
	{
		if(value > 0.0f)
		{
			return value;
		}

		throw new NumberRangeException("Value is not positive: " + value);
	}
	
	public static long equal(final long value1, final long value2) throws IllegalArgumentException
	{
		if(value1 == value2)
		{
			return value1;
		}
		
		throw new IllegalArgumentException("Unequal values: " + value1 + " != " + value2);
	}

	public static final boolean isGreaterThanOrEqualHighestPowerOf2(final long value)
	{
		return value >= MAX_POW_2_INT;
	}

	public static final boolean isGreaterThanOrEqualHighestPowerOf2(final int value)
	{
		return value >= MAX_POW_2_INT;
	}

	public static final boolean isGreaterThanHighestPowerOf2(final int value)
	{
		return value > MAX_POW_2_INT;
	}

	public static final int highestPowerOf2_int()
	{
		return MAX_POW_2_INT;
	}

	public static long addCapped(final long l1, final long l2)
	{
		// does not account for negative values
		return Long.MAX_VALUE - l1 < l2
			? Long.MAX_VALUE
			: l1 + l2
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
	private XMath()
	{
		// static only
		throw new UnsupportedOperationException();
	}

	// CHECKSTYLE.ON: MagicNumber
}
