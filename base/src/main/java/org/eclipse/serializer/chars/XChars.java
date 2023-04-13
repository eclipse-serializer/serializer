package org.eclipse.serializer.chars;

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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.eclipse.serializer.branching.ThrowBreak;
import org.eclipse.serializer.functional._charPredicate;
import org.eclipse.serializer.functional._charProcedure;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.typing.XTypes;


/**
 * Static util class with character operation algorithms missing in or significantly superior to those in JDK.
 */
public final class XChars
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	// CHECKSTYLE.OFF: ConstantName: type names are intentionally unchanged
	// CHECKSTYLE.OFF: MagicNumber: The 1E7 is virtually already a constant.
	public static final transient char[]
		CHARS_MIN_VALUE_byte    = Integer.toString(Byte.MIN_VALUE)          .toCharArray(),
		CHARS_MAX_VALUE_byte    = Integer.toString(Byte.MAX_VALUE)          .toCharArray(),
		CHARS_MIN_VALUE_short   = Integer.toString(Short.MIN_VALUE)         .toCharArray(),
		CHARS_MAX_VALUE_short   = Integer.toString(Short.MAX_VALUE)         .toCharArray(),
		CHARS_MIN_VALUE_int     = Integer.toString(Integer.MIN_VALUE)       .toCharArray(),
		CHARS_MAX_VALUE_int     = Integer.toString(Integer.MAX_VALUE)       .toCharArray(),
		CHARS_MIN_VALUE_long    = Long   .toString(Long.MIN_VALUE)          .toCharArray(),
		CHARS_MAX_VALUE_long    = Long   .toString(Long.MAX_VALUE)          .toCharArray(),
		CHARS_ZERO              = Double .toString(0.0)                     .toCharArray(),
		CHARS_ONE               = Double .toString(1.0)                     .toCharArray(),
		CHARS_NAN               = Double .toString(Double.NaN)              .toCharArray(),
		CHARS_NEGATIVE_INFINITY = Double .toString(Double.NEGATIVE_INFINITY).toCharArray(),
		CHARS_POSITIVE_INFINITY = Double .toString(Double.POSITIVE_INFINITY).toCharArray(),
		CHARS_NORM_THRESH_HIGH  = Double .toString(1E7)                     .toCharArray()
	;
	// CHECKSTYLE.ON: MagicNumber

	static final transient int
		MAX_CHAR_COUNT_byte           = CHARS_MIN_VALUE_byte .length,
		MAX_CHAR_COUNT_boolean        = 5                           , // "false"
		MAX_CHAR_COUNT_short          = CHARS_MIN_VALUE_short.length,
		MAX_CHAR_COUNT_int            = CHARS_MIN_VALUE_int  .length,
		MAX_CHAR_COUNT_long           = CHARS_MIN_VALUE_long .length,
		// no reasonable way was found to derive floating point max string length programmatically, hence manual
		MAX_CHAR_COUNT_float          = 15, // 1 minus, 1 dot,  9 IEEE754 standard digits, 1 E, 1 minus, 2 exponent
		MAX_CHAR_COUNT_double         = 24, // 1 minus, 1 dot, 17 IEEE754 standard digits, 1 E, 1 minus, 3 exponent

		LITERAL_LENGTH_NULL           = 4,
		LITERAL_LENGTH_TRUE           = 4,
		LITERAL_LENGTH_FALSE          = 5
	;

	// CHECKSTYLE.ON: ConstantName

	public static final transient char
		DIGIT_LOWER_INDEX = '0'    , // for using " >= " and "<"
		DIGIT_UPPER_BOUND = '9' + 1  // for using " >= " and "<"
	;

	private static final char LOWEST_NON_WHITESPACE = ' ' + 1; // < and >= are faster than <= and >

	/*
	 * char tables are a caching trade-off to gain significant performance at the cost of minimal memory overhead.
	 */
	static final transient int DECIMAL_CHAR_TABLES_LENGTH = 100;

	public static final transient int DECIMAL_BASE               =  10;

	/*
	 * Character table containing the ten digit of each number between 00 and 99.
	 * generated procedurally, see below.
	 * '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
	 * '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
	 * '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
	 * '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
	 * '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
	 * '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
	 * '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
	 * '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
	 * '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
	 * '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
	 */
	public static final transient char[] DECIMAL_CHAR_TABLE_10S = new char[DECIMAL_CHAR_TABLES_LENGTH];

	/*
	 * Character table containing the one digit of each number between 00 and 99.
	 * generated procedurally, see below.
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 */
	public static final transient char[] DECIMAL_CHAR_TABLE_01S = new char[DECIMAL_CHAR_TABLES_LENGTH];

	// decimal tables initialization
	static
	{
		for(int i = 0; i < DECIMAL_CHAR_TABLES_LENGTH; i++)
		{
			DECIMAL_CHAR_TABLE_10S[i] = (char)(DIGIT_LOWER_INDEX + i / DECIMAL_BASE);
			DECIMAL_CHAR_TABLE_01S[i] = (char)(DIGIT_LOWER_INDEX + i % DECIMAL_BASE);
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final int maxCharCount_byte()
	{
		return MAX_CHAR_COUNT_byte;
	}

	public static final int maxCharCount_boolean()
	{
		return MAX_CHAR_COUNT_boolean;
	}

	public static final int maxCharCount_short()
	{
		return MAX_CHAR_COUNT_short;
	}

	public static final int maxCharCount_int()
	{
		return MAX_CHAR_COUNT_int;
	}

	public static final int maxCharCount_float()
	{
		return MAX_CHAR_COUNT_float;
	}

	public static final int maxCharCount_long()
	{
		return MAX_CHAR_COUNT_long;
	}

	public static final int maxCharCount_double()
	{
		return MAX_CHAR_COUNT_double;
	}

	public static final boolean isWhitespace(final char c)
	{
		return c < LOWEST_NON_WHITESPACE;
	}

	public static final boolean isNonWhitespace(final char c)
	{
		return c >= LOWEST_NON_WHITESPACE;
	}
	
	/**
	 * It's UTF-8!
	 * 
	 * @return {@link StandardCharsets#UTF_8}
	 */
	public static final Charset utf8()
	{
		return StandardCharsets.UTF_8;
	}

	/**
	 * Returns {@link StandardCharsets#UTF_8}.
	 * 
	 * @return {@code java.nio.charset.Charset.forName("UTF-8")}.
	 */
	public static final Charset standardCharset()
	{
		return utf8();
	}

	public static final void validateIndex(final char[] chars, final int index)
	{
		if(index < 0 || index >= chars.length)
		{
			throw new ArrayIndexOutOfBoundsException(index);
		}
	}

	public static final void validateRange(final char[] chars, final int offset, final int length)
	{
		validateIndex(chars, offset);
		if(length < 0)
		{
			throw new IllegalArgumentException();
		}
		validateIndex(chars, offset + length - 1);
	}

	public static final boolean isEqual(final String s1, final String s2)
	{
		if(s1 == null)
		{
			return s2 == null;
		}
		if(s2 == null)
		{
			return false;
		}
		return s1.equals(s2);
	}

	public static final boolean equals(final String string, final char[] chars, final int offset)
	{
		// ensure the given offset is valid before it is used in the algorithm
		validateIndex(chars, offset);

		final int    length = string.length();
		final char[] sChars = readChars(string);

		if(length != chars.length - offset)
		{
			return false; // range to compare does not match, can't be equal, abort.
		}

		for(int i = 0; i < length; i++)
		{
			if(sChars[i] != chars[offset + i])
			{
				return false;
			}
		}
		
		return true;
	}

	public static final boolean uncheckedEquals(
		final char[] chars1 ,
		final int    offset1,
		final char[] chars2 ,
		final int    offset2,
		final int    length
	)
	{
		for(int i = 0; i < length; i++)
		{
			if(chars1[offset1 + i] != chars2[offset2 + i])
			{
				return false;
			}
		}
		return true;
	}

	public static final char toHexadecimal(final int b) throws IllegalArgumentException
	{
		// CHECKSTYLE.OFF: MagicNumber: direct literals are better readable
		switch(b)
		{
			case  0: return '0';
			case  1: return '1';
			case  2: return '2';
			case  3: return '3';
			case  4: return '4';
			case  5: return '5';
			case  6: return '6';
			case  7: return '7';
			case  8: return '8';
			case  9: return '9';
			case 10: return 'A';
			case 11: return 'B';
			case 12: return 'C';
			case 13: return 'D';
			case 14: return 'E';
			case 15: return 'F';
			default: throw new IllegalArgumentException(b + " is no positive hexadecimal digit value");
		}
		// CHECKSTYLE.ON: MagicNumber
	}

	static final int uncheckedIndexOf(final char[] data, final int bound, final int offset, final char c)
	{
		for(int i = offset; i < bound; i++)
		{
			if(data[i] == c)
			{
				return i;
			}
		}
		return -1;
	}

	static final int uncheckedLastIndexOf(final char[] data, final int size, final char c)
	{
		for(int i = size; i-- > 0;)
		{
			if(data[i] == c)
			{
				return i;
			}
		}
		return -1;
	}

	static final void uncheckedReverse(final char[] data, final int size)
	{
		final int last = size - 1;

		//only swap until size/2 (rounded down, because center element can remain untouched)
		char loopSwapChar;
		for(int i = size >>> 1; i != 0; i--)
		{
			loopSwapChar = data[i];
			data[i] = data[last - i];
			data[last - i] = loopSwapChar;
		}
	}

	public static final int indexOf(
		final char[] source      ,
		final int    sourceOffset,
		final int    sourceCount ,
		final char[] target      ,
		final int    targetOffset,
		final int    targetCount ,
		final int    fromIndex
	)
	{
		// CHECKSTYLE.OFF: EmptyBlock: empty while loops skip elements and are documented accordingly

		// (14.08.2010)NOTE: Not sure why sourceOffset and fromIndex are two parameters
		if(fromIndex >= sourceCount)
		{
			return targetCount == 0 ? sourceCount : -1;
		}

		final int paddedFromIndex = fromIndex < 0 ? 0 : fromIndex;
		if(targetCount == 0)
		{
			return paddedFromIndex;
		}

		final char first         = target[targetOffset];
		final int  maxFirstIndex = sourceOffset + sourceCount - targetCount;

		for(int i = sourceOffset + paddedFromIndex; i <= maxFirstIndex; i++)
		{
			// Look for first character.
			if(source[i] != first)
			{
				while(++i <= maxFirstIndex && source[i] != first)
				{
					// skip
				}
			}

			// Found first character, now look at the rest of v2
			if(i <= maxFirstIndex)
			{
				int j = i + 1;
				final int end = j + targetCount - 1;
				for(int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++)
				{
					// skip
				}

				if(j == end)
				{
					// Found whole string
					return i - sourceOffset;
				}
			}
		}
		return -1;
		// CHECKSTYLE.ON: EmptyBlock
	}

	static final int uncheckedIndexOf(final char[] data, final int dataLength, final char[] subject)
	{
		if(subject.length == 0)
		{
			return 0;
		}

		final char firstChar = subject[0];
		final int scanBound = dataLength - subject.length + 1;

		// scan for first char, If matched, check the rest
		scan:
		for(int i = 0; i < scanBound; i++)
		{
			if(data[i] == firstChar)
			{
				for(int c = 1, i2 = i; c < subject.length; c++)
				{
					if(data[++i2] != subject[c])
					{
						continue scan;
					}
				}
				return i;
			}
		}

		return -1;
	}

	public static final int indexOf(final char[] data, final int dataOffset, final int dataLength, final char[] chars)
	{
		if(dataOffset < 0 || dataOffset >= data.length)
		{
			throw new ArrayIndexOutOfBoundsException(dataOffset);
		}
		return uncheckedIndexOf(data, dataOffset, dataLength, chars);
	}

	static final int uncheckedIndexOf(final char[] data, final int dataOffset, final int dataLength, final char[] chars)
	{
		if(chars.length == 0)
		{
			return dataOffset;
		}

		final char firstChar = chars[0];
		final int scanBound = dataOffset + dataLength - chars.length + 1; // normalized array index bound

		scan: // scan for first char. If matched, check the rest, continue on mismatch
		for(int s = dataOffset; s < scanBound; s++)
		{
			if(data[s] != firstChar)
			{
				continue scan;
			}
			for(int c = 1, j = s; c < chars.length; c++)
			{
				if(data[++j] != chars[c])
				{
					continue scan;
				}
			}
			return s;
		}
		return -1;
	}


	public static final int indexOf(
		final char[] source     ,
		final int    sourceCount,
		final char[] target     ,
		final int    targetCount,
		final int    fromIndex
	)
	{
		// CHECKSTYLE.OFF: EmptyBlock: empty while loops skip elements and are documented accordingly

		if(fromIndex >= sourceCount)
		{
			return targetCount == 0 ? sourceCount : -1;
		}

		final int paddedFromIndex = fromIndex < 0 ? 0 : fromIndex;
		if(targetCount == 0)
		{
			return paddedFromIndex;
		}

		final char first  = target[0];
		final int max = sourceCount - targetCount;

		for(int i = paddedFromIndex; i <= max; i++)
		{
			/* Look for first character. */
			if(source[i] != first)
			{
				while(++i <= max && source[i] != first)
				{
					// skip
				}
			}

			/* Found first character, now look at the rest of v2 */
			if(i <= max)
			{
				int j = i + 1;
				final int end = j + targetCount - 1;
				for(int k = 1; j < end && source[j] == target[k]; j++, k++)
				{
					// skip
				}

				if(j == end)
				{
					/* Found whole string. */
					return i;
				}
			}
		}
		return -1;
		// CHECKSTYLE.ON: EmptyBlock
	}

	static final int uncheckedReplaceFirst(
		final char[] chars      ,
		final int    offset     ,
		final int    length     ,
		final char   sample     ,
		final char   replacement
	)
	{
		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			if(chars[i] == sample)
			{
				chars[i] = replacement;
				return i;
			}
		}
		return -1;
	}

	static final int uncheckedReplaceAll(
		final char[] chars      ,
		final int    offset     ,
		final int    length     ,
		final char   sample     ,
		final char   replacement
	)
	{
		int count = 0;

		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			if(chars[i] == sample)
			{
				chars[i] = replacement;
				count++;
			}
		}
		return count;
	}

	static final void uncheckedRepeat(
		final char[] chars ,
		final int    offset,
		final int    count ,
		final char   c
	)
	{
		final int bound = offset + count;

		for(int i = offset; i < bound; i++)
		{
			chars[i] = c;
		}
	}

	static final void uncheckedRepeat(
		final char[] chars  ,
		final int    offset ,
		final int    count  ,
		final char[] subject
	)
	{
		final int bound = offset + count;
		int i = offset;
		while(i < bound)
		{
			i = XChars.put(subject, chars, i);
		}
	}

	public static final int count(
		final char[] input,
		final int    startIndex,
		final int    boundIndex,
		final char   c
	)
	{
		int count = 0;
		for(int i = startIndex; i < boundIndex; i++)
		{
			if(input[i] == c)
			{
				count++;
			}
		}
		
		return count;
	}

	public static final int count(
		final char[] data         ,
		final int    dataOffset   ,
		final int    dataCount    ,
		final char[] subject      ,
		final int    subjectOffset,
		final int    subjectCount
	)
	{
		int count = 0;
		for(int i = -1; (i = indexOf(data, dataOffset, dataCount, subject, subjectOffset, subjectCount, i + 1)) != -1;)
		{
			count++;
		}
		return count;
	}

	public static final boolean hasNoContent(final String s)
	{
		return s == null || s.isEmpty();
	}

	public static final VarString appendArraySeperated(
		final VarString vc,
		final char separator,
		final Object... elements
	)
	{
		if(elements == null)
		{
			return vc;
		}
		for (int i = 0; i < elements.length; i++)
		{
			vc.add(elements[i]).add(separator);
		}
		return vc.deleteLast();
	}

	public static final char[] toCharArray(final StringBuilder asb)
	{
		final char[] charArray;
		asb.getChars(0, asb.length(), charArray = new char[asb.length()], 0);
		return charArray;
	}

	public static final char[] toCharArray(final StringBuffer asb)
	{
		final char[] charArray;
		asb.getChars(0, asb.length(), charArray = new char[asb.length()], 0);
		return charArray;
	}

	private static int lcsLength(final char[] a, final char[] b)
	{
		final int lenA = a.length;
		final int lenB = b.length;
		int[] p = new int[lenB];
		int[] d = new int[lenB];
		int maxLen = 0;

		for(int i = 0; i < lenA; i++)
		{
			for(int j = 0; j < lenB; j++)
			{
				if((d[j] = a[i] != b[j] ? 0 : i == 0 || j == 0 ? 1 : p[j - 1] + 1) > maxLen)
				{
					maxLen = d[j];
				}
			}
			final int[] swap = p;
			p = d;
			d = swap;
		}
		return maxLen;
	}

	public static final int commonSubstringLength(final char[] a, final char[] b)
	{
		if(a.length == 0 || b.length == 0)
		{
			return 0;
		}
		// move shorter string to second position due to buffer array dependency
		return a.length > b.length ? lcsLength(a, b) : lcsLength(b, a);
	}

	public static final int commonPrefixLength(final char[] a, final char[] b)
	{
		final int len = a.length < b.length ? a.length : b.length;
		for(int i = 0; i < len; i++)
		{
			if(a[i] != b[i])
			{
				return i;
			}
		}
		return len;
	}

	public static final int commonSuffixLength(final char[] a, final char[] b)
	{
		final int lenA, lenB, len = (lenA = a.length) < (lenB = b.length) ? lenA : lenB;
		for(int i = 1; i <= len; i++)
		{
			if(a[lenA - i] != b[lenB - i])
			{
				return i - 1;
			}
		}
		return len;
	}

	public static final <C extends CharSequence> C notEmpty(final C charSequence)
	{
		// implicit NPE
		if(charSequence.length() == 0)
		{
			throw new IllegalArgumentException();
		}
		return charSequence;
	}

	public static final boolean isEmpty(final CharSequence cs)
	{
		return cs == null || cs.length() == 0;
	}

	public static final void iterate(final CharSequence chars, final _charProcedure iterator)
	{
		// optimization checks
		if(chars instanceof String)
		{
			iterate((String)chars, iterator);
			return;
		}
		if(chars instanceof VarString)
		{
			((VarString)chars).iterate(iterator);
			return;
		}
		// could add JDK string builder implementations here

		// generic default algorithm. However slow that may be for implementation of chars
		try
		{
			final int length = chars.length();
			for(int i = 0; i < length; i++)
			{
				iterator.accept(chars.charAt(i));
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	public static final void iterate(final String chars, final _charProcedure iterator)
	{
		iterate(readChars(chars), iterator);
	}

	public static final void iterate(final char[] chars, final _charProcedure iterator)
	{
		iterate(chars, 0, chars.length, iterator);
	}

	public static final void iterate(
		final char[]         chars   ,
		final int            offset  ,
		final int            length  ,
		final _charProcedure iterator
	)
	{
		try
		{
			for(int i = offset; i < length;)
			{
				iterator.accept(chars[i++]);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// toChars methods //
	////////////////////

	public static final int put(final byte value, final char[] target, final int offset)
	{
		return CharConversionIntegers.put_byte(value, target, offset);
	}

	public static final int putHexDec(final byte value, final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.

		// negative values are covered implicitly via >>> operator
		// CHECKSTYLE.OFF: MagicNumber: HexDec arithmetic
		target[offset    ] = XChars.toHexadecimal(value >>> 4 & 0b1111);
		target[offset + 1] = XChars.toHexadecimal(value       & 0b1111);
		// CHECKSTYLE.ON: MagicNumber
		return offset + 2;
	}

	public static final int put(final short value, final char[] target, final int offset)
	{
		return CharConversionIntegers.put_short(value, target, offset);
	}

	public static final int put(final int value, final char[] target, final int offset)
	{
		return CharConversionIntegers.put_int(value, target, offset);
	}

	public static final int put(final float value, final char[] target, final int offset)
	{
		return CharConversion_float.put(value, target, offset);
	}

	public static final int put(final long value, final char[] target, final int offset)
	{
		return CharConversionIntegers.put_long(value, target, offset);
	}

	public static final int put(final double value, final char[] target, final int offset)
	{
		return CharConversion_double.put(value, target, offset);
	}

	public static final int put(final String s, final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.

		s.getChars(0, s.length(), target, offset);
		return offset + s.length();
	}

	public static final int put(final char[] value, final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.

		System.arraycopy(value, 0, target, offset, value.length);
		return offset + value.length;
	}

	public static final int putNull(final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.

		// significantly faster than arraycopy
		// CHECKSTYLE.OFF: MagicNumber: No more constants. Those are arithmetical values. I won't replace "1" by "ONE"
		target[offset    ] = 'n';
		target[offset + 1] = 'u';
		target[offset + 2] = 'l';
		target[offset + 3] = 'l';
		// CHECKSTYLE.ON: MagicNumber

		return offset + LITERAL_LENGTH_NULL;
	}

	public static final int putTrue(final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.

		// significantly faster than arraycopy
		// CHECKSTYLE.OFF: MagicNumber: No more constants. Those are arithmetical values. I won't replace "1" by "ONE"
		target[offset    ] = 't';
		target[offset + 1] = 'r';
		target[offset + 2] = 'u';
		target[offset + 3] = 'e';
		// CHECKSTYLE.ON: MagicNumber

		return offset + LITERAL_LENGTH_TRUE;
	}

	public static final int putFalse(final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.

		// significantly faster than arraycopy
		// CHECKSTYLE.OFF: MagicNumber: No more constants. Those are arithmetical values. I won't replace "1" by "ONE"
		target[offset    ] = 'f';
		target[offset + 1] = 'a';
		target[offset + 2] = 'l';
		target[offset + 3] = 's';
		target[offset + 4] = 'e';
		// CHECKSTYLE.ON: MagicNumber

		return offset + LITERAL_LENGTH_FALSE;
	}
	
	public static final String String(final byte[] chars, final Charset charset)
	{
		return String(chars, 0, chars.length, charset);
	}
	
	public static final String String(
		final byte[]  chars  ,
		final int     offset ,
		final int     length ,
		final Charset charset
	)
	{
		return new String(chars, offset, length, charset);
	}
	
	public static final String String(final ByteBuffer chars, final Charset charset)
	{
		return new String(XMemory.toArray(chars), charset);
	}

	public static final String String(final int value)
	{
		// performance-optimized version with minimal instantiating and pointer indirection
		final char[] chars;
		return new String(
				chars = new char[MAX_CHAR_COUNT_int],
				0,
				CharConversionIntegers.put_int(value, chars, 0)
		);
	}

	public static final char[] readChars(final String s)
	{
		/*
		 * In ages past, this method called low-level mechanisms to access the string's internal char[] array
		 * in a safe read-only fashion to process the chars efficiently without the need to
		 * copy the complete string content just to read it.
		 */
		return s.toCharArray();
	}


	public static final boolean contains(final String s, final char c)
	{
		return s.indexOf(c) >= 0;
	}
	
	public static final boolean contains(
		final char[] data,
		final char   c
	)
	{
		return uncheckedContains(data, 0, data.length, c);
	}
	
	static final boolean uncheckedContains(
		final char[] data      ,
		final int    dataOffset,
		final int    dataLength,
		final char   c
	)
	{
		final int dataBound = dataOffset + dataLength;
		for(int i = dataOffset; i < dataBound; i++)
		{
			if(data[i] == c)
			{
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Utility method that replicates the JVM's intrinsic system string as defined in {@link Object#toString()}
	 * 
	 * @param instance the instance whose system string shall be generated.
	 * @return the system string for the passed instance.
	 */
	public static final String systemString(final Object instance)
	{
		return instance == null
			? null
			: instance.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(instance))
		;
	}
	
	public static final VarString addSystemString(final Object instance, final VarString vs)
	{
		return instance == null
			? vs.addNull()
			: vs
				.add(instance.getClass().getName())
				.add('@')
				.add(Integer.toHexString(System.identityHashCode(instance)))
		;
	}

	public static final String trimEmptyToNull(final String s)
	{
		// if the string is null in the first place, null is returned.
		if(s == null)
		{
			return null;
		}
		
		// if the string contains only one non-whitespace, a trimmed string (potentially itself) is returned
		final int length = s.length();
		for(int i = 0; i < length; i++)
		{
			if(s.charAt(i) > ' ')
			{
				return s.trim();
			}
		}
		
		// string contains solely whitespaces, hence return null
		return null;
	}
	
	/**
	 * Assembles the passed instance "cautiously" in the sense that only types recognizable via
	 * {@link XTypes#isValueType(Object)} get assembled using their {@link Object#toString()} method,
	 * while all others are assembled using {@link #systemString(Object)}
	 * (which behaves identical to the actual implementation of {@link Object#toString()}).
	 * 
	 * @param vs the {@link VarString} instance to hold the assembled string.
	 * @param object the instance to be assembled "cautiously" as described above.
	 * @return the passed vs instance (method-chaining viable).
	 * 
	 * @see VarString#add(Object, java.util.function.BiConsumer)
	 * @see XTypes#isValueType(Object)
	 * @see Object#toString()
	 * @see XChars#systemString(Object)
	 */
	public static final VarString assembleCautiously(final VarString vs, final Object object)
	{
		return vs.add(XTypes.isValueType(object)
			? object.toString()
			: XChars.systemString(object)
		);
	}
	
	public static final boolean applies(
		final CharSequence   chars     ,
		final int            startIndex,
		final _charPredicate predicate
	)
	{
		final int length = chars.length();
		if(length == 0)
		{
			// must check for the special case of no entries (predicate cannot apply).
			return false;
		}
		
		for(int i = startIndex; i < length; i++)
		{
			if(!predicate.test(chars.charAt(i)))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isDigit(final char c)
	{
		return c >= '0' && c <= '9';
	}

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XChars()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
