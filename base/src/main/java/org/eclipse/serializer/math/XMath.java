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

    private static final transient int PERCENT = 100;
    
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
	
	public static boolean isMathematicalInteger(final double value)
    {
        return !Double.isNaN(value)
                && !Double.isInfinite(value)
                && value == Math.rint(value)
                ;
    }


    public static final int log2pow2(final int pow2Value)
    {
        switch(pow2Value)
        {
            case          1: return  0;
            case          2: return  1;
            case          4: return  2;
            case          8: return  3;
            case         16: return  4;
            case         32: return  5;
            case         64: return  6;
            case        128: return  7;
            case        256: return  8;
            case        512: return  9;
            case       1024: return 10;
            case       2048: return 11;
            case       4096: return 12;
            case       8192: return 13;
            case      16384: return 14;
            case      32768: return 15;
            case      65536: return 16;
            case     131072: return 17;
            case     262144: return 18;
            case     524288: return 19;
            case    1048576: return 20;
            case    2097152: return 21;
            case    4194304: return 22;
            case    8388608: return 23;
            case   16777216: return 24;
            case   33554432: return 25;
            case   67108864: return 26;
            case  134217728: return 27;
            case  268435456: return 28;
            case  536870912: return 29;
            case 1073741824: return 30;
            default:
                throw new IllegalArgumentException("Not a power-of-2 value: " + pow2Value);
        }
    }

    /**
     * Determines if the passed value is a power-of-2 value.
     *
     * @param value the value to be tested.
     *
     * @return {@code true} for any n in [0;30] that satisfies {@code value = 2^n}.
     */
    public static final boolean isPow2(final int value)
    {
        // lookup-switch should be faster than binary search with 4-5 ifs (I hope).
        switch(value)
        {
            case          1: return true;
            case          2: return true;
            case          4: return true;
            case          8: return true;
            case         16: return true;
            case         32: return true;
            case         64: return true;
            case        128: return true;
            case        256: return true;
            case        512: return true;
            case       1024: return true;
            case       2048: return true;
            case       4096: return true;
            case       8192: return true;
            case      16384: return true;
            case      32768: return true;
            case      65536: return true;
            case     131072: return true;
            case     262144: return true;
            case     524288: return true;
            case    1048576: return true;
            case    2097152: return true;
            case    4194304: return true;
            case    8388608: return true;
            case   16777216: return true;
            case   33554432: return true;
            case   67108864: return true;
            case  134217728: return true;
            case  268435456: return true;
            case  536870912: return true;
            case 1073741824: return true;
            default        : return false;
        }
    }


    public static final double fractionToPercent(final double decimalFractionValue)
    {
        return decimalFractionValue * PERCENT;
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
