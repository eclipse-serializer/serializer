package org.eclipse.serializer.collections;

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

import static org.eclipse.serializer.util.X.ArrayOfSameType;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.util.X;
import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.exceptions.IndexBoundsException;
import org.eclipse.serializer.functional.XFunc;
import org.eclipse.serializer.typing.XTypes;
import org.eclipse.serializer.util.UtilStackTrace;

/**
 * Numerous utility methods for working with arrays, all of which are either missing in the JDK.
 *
 */
public final class XArrays
{
	public static final void validateRange0toUpperBound(final int upperBound, final int offset, final int length)
	{
		if(offset < 0 || offset >= upperBound)
		{
			throw new IndexExceededException(upperBound, offset);
		}

		if(length > 0 && offset + length > upperBound)
		{
			throw new IndexExceededException(upperBound, offset + length);
		}
		else if(length < 0 && offset + length < -1)
		{
			throw new IndexExceededException(-1, offset + length);
		}
	}

	private static String exceptionRange(final int size, final int startIndex, final int length)
	{
		return "Range [" + (length < 0 ? startIndex + length + 1 + ";" + startIndex
			: startIndex + ";" + (startIndex + length - 1)) + "] not in [0;" + (size - 1) + "]";
	}
	
	private static String exceptionIndexOutOfBounds(final int size, final int index)
	{
		return "Index: " + index + ", Size: " + size;
	}

	
	
	public static final int validateArrayIndex(final int arrayLength, final int index)
	{
		if(index < 0 || index >= arrayLength)
		{
			throw UtilStackTrace.cutStacktraceByOne(new ArrayIndexOutOfBoundsException(index));
		}
		return index;
	}

	public static final int validateArrayRange(
		final Object[] array ,
		final int      offset,
		final int      length
	)
	{
		return validateArrayRange(array.length, offset, length);
	}
	
	public static final int validateArrayRange(
		final int arrayLength,
		final int offset     ,
		final int length
	)
	{
		// elements array range checking
		if(length >= 0)
		{
			if(offset < 0 || offset + length > arrayLength)
			{
				throw new IndexOutOfBoundsException(exceptionRange(arrayLength, offset, length));
			}
			if(length == 0)
			{
				return 0;
			}
			return +1; // incrementing direction
		}
		else if(length < 0)
		{
			if(offset + length < -1 || offset >= arrayLength)
			{
				throw new IndexOutOfBoundsException(exceptionRange(arrayLength, offset, length));
			}
			return -1; // decrementing direction
		}
		else if(offset < 0 || offset >= arrayLength)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(arrayLength, offset));
		}
		else
		{
			// handle length 0 special case not as escape condition but as last case to ensure index checking
			return 0;
		}
	}

	/**
	 * Returns if the passed array is either null or has the length 0.
	 *
	 * @param array the array to check
	 * @return <code>true</code> if the passed array has no content
	 */
	public static boolean hasNoContent(final Object[] array)
	{
		return array == null || array.length == 0;
	}
	
	public static final int[] fill(final int[] array, final int fillElement)
	{
		final int length = array.length;
		for(int i = 0; i < length; i++)
		{
			array[i] = fillElement;
		}
		return array;
	}

	public static final double[] fill(final double[] array, final double fillElement)
	{
		final int length = array.length;
		for(int i = 0; i < length; i++)
		{
			array[i] = fillElement;
		}
		return array;
	}

	public static final <T> T[] subArray(final T[] array, final int offset, final int length)
	{
		final T[] newArray; // bounds checks are done by VM.
		System.arraycopy(
			array, offset, newArray = X.ArrayOfSameType(array, length), 0, length
			);
		return newArray;
	}

	/**
	 * Compares two Object arrays by reference of their content.
	 * <p>
	 * Note that specific equality of each element is situational and thus cannot be a concern
	 * of a generic array comparison, just as it cannot be the concern of the element's class directly.
	 *
	 * @param array1 the first array
	 * @param array2 the second array
	 * @return <code>true</code> if both arrays are equal
	 */
	public static boolean equals(final Object[] array1, final Object[] array2)
	{
		if(array1 == array2)
		{
			return true;
		}
		if(array1 == null || array2 == null)
		{
			return false;
		}

		final int length = array1.length;
		if(array2.length != length)
		{
			return false;
		}

		for(int i = 0; i < length; i++)
		{
			if(array1[i] != array2[i])
			{
				return false;
			}
		}
		return true;
	}

	public static final <E> boolean equals(
		final E[] array1,
		final int startIndex1,
		final E[] array2,
		final int startIndex2,
		final int length,
		final Equalator<? super E> comparator
		)
	{
		//all bounds exceptions are provoked intentionally because no harm will be done by this method in those cases
		int a = startIndex1, b = startIndex2;

		for(final int aBound = startIndex1 + length; a < aBound; a++, b++)
		{
			if(!comparator.equal(array1[a], array2[b]))
			{
				return false;
			}
		}
		return true;
	}
	
	public static <T> T[] add(final T[] array, final T element)
	{
		final T[] newArray = enlarge(array, array.length + 1);
		newArray[array.length] = element;
		
		return newArray;
	}
	
	public static <T> T[] remove(final T[] array, final int i)
	{
		final T[] newArray = ArrayOfSameType(array, array.length - 1);
		System.arraycopy(array, 0    , newArray, 0, i                   );
		System.arraycopy(array, i + 1, newArray, i, array.length - i - 1);
		
		return newArray;
	}

	public static final <T> T[] ensureContained(final T[] ts, final T t)
	{
		if(contains(ts, t))
		{
			return ts;
		}
		
		return add(ts, t);
	}

	/**
	 * This method checks if {@code array} contains {@code element} by object identity
	 *
	 * @param <E> any type
	 * @param array the array to be searched in
	 * @param element the element to be searched (by identity)
	 * @return <code>true</code> if {@code array} contains {@code element} by object identity, else <code>false</code>
	 */
	public static final <E> boolean contains(final E[] array, final E element)
	{
		for(final E e : array)
		{
			if(e == element)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Removed all occurrences of {@code e} from array {@code array}.
	 * 
	 * @param <E> the element type
	 * @param array the array containing all elements.
	 * @param start the starting offset (inclusive lower bound)
	 * @param bound the bounding offset (exclusive upper bound)
	 * @param e     the element to be removed
	 *
	 * @return the number of removed elements
	 */
	public static <E> int removeAllFromArray(final E[] array, final int start, final int bound, final E e)
		throws ArrayIndexOutOfBoundsException
	{
		// "base" index marking all stable elements (or "progress")
		int base = start;

		// compress array by moving non-subjects block by block
		for(int i = base; i < bound;)
		{
			while(i < bound && array[i] == e)
			{
				// determine move offset (next occurrence of non-e)
				i++;
			}
			final int moveOffset = i;

			while(i < bound && array[i] != e)
			{
				// determine move length (range until next e)
				i++;
			}
			final int moveLength = i - moveOffset;

			// execute move
			System.arraycopy(array, moveOffset, array, base, moveLength);
			base += moveLength;
		}

		// null out trailing slots until bound
		for(int i = base; i < bound; i++)
		{
			array[i] = null;
		}

		// calculate and return amount of removed elements
		return bound - base;
	}

	public static int removeAllFromArray(final int[] array, final int start, final int bound, final int e)
		throws ArrayIndexOutOfBoundsException
	{
		// "base" index marking all stable elements (or "progress")
		int base = start;

		// compress array by moving non-subjects block by block
		for(int i = base; i < bound;)
		{
			while(i < bound && array[i] == e)
			{
				// determine move offset (next occurance of non-e)
				i++;
			}
			final int moveOffset = i;

			while(i < bound && array[i] != e)
			{
				// determine move length (range until next e)
				i++;
			}
			final int moveLength = i - moveOffset;

			// execute move
			System.arraycopy(array, moveOffset, array, base, moveLength);
			base += moveLength;
		}

		// null out trailing slots until bound
		for(int i = base; i < bound; i++)
		{
			array[i] = Integer.MIN_VALUE;
		}

		// calculate and return amount of removed elements
		return bound - base;
	}

	static final class ArrayElementRemover<E> implements Consumer<E>
	{
		private final E marker;
		private final E[] array;
		private final int start;
		private final int bound;

		public ArrayElementRemover(final E[] array, final int start, final int bound, final E removeMarker)
		{
			super();
			this.array  = array       ;
			this.start  = start       ;
			this.bound  = bound       ;
			this.marker = removeMarker;
		}

		@Override
		public void accept(final E e)
		{
			final E   marker = this.marker;
			final E[] array  = this.array ;
			final int bound  = this.bound ;
			for(int i = this.start; i < bound; i++)
			{
				if(array[i] == e)
				{
					array[i] = marker;
				}
			}
		}

	}

	//!\\ NOTE: copy of single-object version with only contains part changed! Maintain by copying!
	public static <E> int removeAllFromArray(
		final XGettingCollection<? extends E> elements,
		final E[] array,
		final int start,
		final int bound
	)
		throws ArrayIndexOutOfBoundsException
	{
		if(elements.isEmpty())
		{
			return 0;
		}

		// use a random (the "first") element to be removed as the remove marker, may even be null
		final E removeMarker = elements.get();

		final int removeCount;
		try
		{
			elements.iterate(new ArrayElementRemover<>(array, start, bound, removeMarker));
		}
		finally
		{
			// must ensure that removemarker is removed in any case
			removeCount = removeAllFromArray(array, start, bound, removeMarker);
		}
		return removeCount;
	}

	public static <E> int removeAllFromArray(
		final E[] array,
		final int start,
		final int bound,
		final E   e    ,
		final Equalator<? super E> equalator
		)
			throws ArrayIndexOutOfBoundsException
	{
		// "base" index marking all stable elements (or "progress")
		int base = start;

		// compress array by moving non-subjects block by block
		for(int i = base; i < bound;)
		{
			while(i < bound && equalator.equal(array[i], e))
			{
				// determine move offset (next occurance of non-e)
				i++;
			}
			final int moveOffset = i;

			while(i < bound && !equalator.equal(array[i], e))
			{
				// determine move length (range until next e)
				i++;
			}
			final int moveLength = i - moveOffset;

			// execute move
			System.arraycopy(array, moveOffset, array, base, moveLength);
			base += moveLength;
		}

		// null out trailing slots until bound
		for(int i = base; i < bound; i++)
		{
			array[i] = null;
		}

		// calculate and return amount of removed elements
		return bound - base;
	}

	//!\\ NOTE: copy of single-object version with only contains part changed! Maintain by copying!
	public static <E> int removeAllFromArray(
		final E[] array,
		final int start,
		final int bound,
		final XGettingCollection<? extends E> elements,
		final Equalator<? super E> equalator
		)
			throws ArrayIndexOutOfBoundsException
	{
		//determine first move target index
		int currentMoveTargetIndex = start;
		//if dest is the same as src, skip all to be retained objects
		while(currentMoveTargetIndex < bound
			&& !elements.containsSearched(XFunc.predicate(array[currentMoveTargetIndex], equalator))
		)
		{
			currentMoveTargetIndex++;
		}

		int currentMoveSourceIndex = 0;
		int currentMoveLength = 0;
		int seekIndex = currentMoveTargetIndex;


		while(seekIndex < bound)
		{
			while(seekIndex < bound && elements.containsSearched(XFunc.predicate(array[seekIndex], equalator)))
			{
				seekIndex++;
			}
			currentMoveSourceIndex = seekIndex;

			while(seekIndex < bound && !elements.containsSearched(XFunc.predicate(array[seekIndex], equalator)))
			{
				seekIndex++;
			}
			currentMoveLength = seekIndex - currentMoveSourceIndex;

			System.arraycopy(array, currentMoveSourceIndex, array, currentMoveTargetIndex, currentMoveLength);
			currentMoveTargetIndex += currentMoveLength;
		}
		for(int i = currentMoveTargetIndex; i < bound; i++)
		{
			array[i] = null;
		}
		return bound - currentMoveTargetIndex;
	}

	public static final <T> T[] reverse(final T[] array)
	{
		final int halfSize = array.length >> 1;
		for(int i = 0, j = array.length - 1; i < halfSize; i++, j--)
		{
			final T e = array[i];
			array[i] = array[j];
			array[j] = e;
		}
		return array;
	}

	public static final <T> T[] toReversed(final T[] array)
	{
		final int len;
		final T[] rArray = X.ArrayOfSameType(array, len = array.length);
		for(int i = 0, r = len; i < len; i++)
		{
			rArray[--r] = array[i];
		}
		return rArray;
	}

	public static final <T> T[] toReversed(final T[] array, final int offset, final int length)
	{
		return length < 0
			? XArrays.reverseArraycopy(
				array,
				offset,
				X.ArrayOfSameType(array, -length),
				0,
				-length
				)
				: XArrays.reverseArraycopy(
					array,
					offset + length - 1,
					X.ArrayOfSameType(array, length),
					0,
					length
					)
					;
	}

	public static final <T> T[] copy(final T[] array)
	{
		final T[] newArray = X.ArrayOfSameType(array, array.length);
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}

	/**
	 * At least for Java 1.8, the types seem to not be checked.
	 * Passing a collection of Strings and a Number[] (meaning String extends Number) is not a compiler error.
	 *
	 * @param <E> the source element type
	 * @param <T> the target element type
	 * @param source the source collection
	 * @param target the target array
	 * @return the target array
	 */
	public static final <T, E extends T> T[] copyTo(
		final XGettingCollection<E> source,
		final T[]                   target
	)
		throws IndexBoundsException
	{
		return copyTo(source, target, 0);
	}

	/**
	 * At least for Java 1.8, the types seem to not be checked.
	 * Passing a collection of Strings and a Number[] (meaning String extends Number) is not a compiler error.
	 *
	 * @param <E> the source element type
	 * @param <T> the target element type
	 * @param source the source collection
	 * @param target the target array
	 * @param targetOffset the target start offset
	 * @return the target array
	 */
	public static final <T, E extends T> T[] copyTo(
		final XGettingCollection<E> source      ,
		final T[]                   target      ,
		final int                   targetOffset
	)
		throws IndexBoundsException
	{
		if(source.size() + targetOffset > target.length)
		{
			throw new IndexBoundsException(targetOffset, target.length, source.size() + targetOffset);
		}

		if(source instanceof AbstractSimpleArrayCollection)
		{
			final Object[] data = ((AbstractSimpleArrayCollection<?>)source).internalGetStorageArray();
			final int      size = ((AbstractSimpleArrayCollection<?>)source).internalSize();
			System.arraycopy(
				((AbstractSimpleArrayCollection<?>)source).internalGetStorageArray(),
				0,
				data,
				targetOffset,
				size
			);
		}
		else
		{
			int t = targetOffset - 1;
			for(final E e : source)
			{
				target[++t] = e;
			}
		}

		return target;
	}

	public static <T> T[] enlarge(final T[] array, final int newLength)
	{
		if(newLength <= array.length)
		{
			if(newLength == array.length)
			{
				return array;
			}
			throw new IllegalArgumentException();
		}
		
		final T[] newArray = ArrayOfSameType(array, newLength);
		System.arraycopy(array, 0, newArray, 0, array.length);
		
		return newArray;
	}
	
	public static <E> int replaceAllInArray(
		final E[] data,
		final int startLow,
		final int boundHigh,
		final E oldElement,
		final E newElement
		)
	{
		int replaceCount = 0;
		for(int i = startLow; i < boundHigh; i++)
		{
			if(data[i] == oldElement)
			{
				data[i] = newElement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	/**
	 * Reverse order counterpart to {@link System#arraycopy(Object, int, Object, int, int)}.
	 * <p>
	 * Copies source elements from {@code src}, starting at {@code srcPos} in negative direction ({@code -length}
	 * and copies them one by one to {@code dest}, starting at {@code destPos} in positive direction ({@code +length},
	 * thus effectively copying the elements in reverse order.
	 * 
	 * @param <S> the source element type
	 * @param <D> the destination element type
	 * @param src      the source array.
	 * @param srcPos   starting position in the source array (the <i>highest</i> index for reverse iteration).
	 * @param dest     the destination array.
	 * @param destPos  starting position in the destination data (the <i>lowest</i> index in the target array).
	 * @param length   the number of array elements to be copied in reverse order.
	 * @return the destination array
	 *
	 * @exception ArrayIndexOutOfBoundsException if copying would cause access of data outside array bounds.
	 * @exception ArrayStoreException if an element in the {@code src} array could not be stored into the
	 *            {@code dest} array because of a type mismatch.
	 * @exception NullPointerException if either {@code src} or  {@code dest} is {@code null}.
	 */
	public static <D, S extends D> D[] reverseArraycopy(
		final S[] src    ,
		final int srcPos ,
		final D[] dest   ,
		final int destPos,
		final int length
		)
	{
		if(srcPos >= src.length)
		{
			throw new ArrayIndexOutOfBoundsException(srcPos);
		}
		if(destPos < 0)
		{
			throw new ArrayIndexOutOfBoundsException(destPos);
		}
		if(length < 0)
		{
			throw new ArrayIndexOutOfBoundsException(length);
		}
		if(srcPos - length < -1)
		{
			throw new ArrayIndexOutOfBoundsException(srcPos - length);
		}
		if(destPos + length > dest.length)
		{
			throw new ArrayIndexOutOfBoundsException(destPos + length);
		}

		final int destBound = destPos + length;
		for(int s = srcPos, d = destPos; d < destBound; s--, d++)
		{
			dest[d] = src[s];
		}
		return dest;
	}


	public static final int arrayHashCode(final Object[] data, final int size)
	{
		int hashCode = 1;
		for(int i = 0; i < size; i++)
		{
			// CHECKSTYLE.OFF: MagicNumber: inherent algorithm component
			final Object obj;
			hashCode = 31 * hashCode + ((obj = data[i]) == null ? 0 : obj.hashCode());
			// CHECKSTYLE.ON: MagicNumber
		}
		return hashCode;
	}

	static final <E> boolean uncheckedContainsAll(
		final E[] subject,
		final int subjectLowOffset,
		final int subjectHighBound,
		final E[] elements,
		final int elementsLowOffset,
		final int elementsHighBound
		)
	{
		// cross-iterate both arrays
		main:
		for(int ei = elementsLowOffset; ei < elementsHighBound; ei++)
		{
			final E element = elements[ei];
			for(int di = subjectLowOffset; di < subjectHighBound; di++)
			{
				if(element == subject[di])
				{
					continue main;
				}
			}
			return false;  // one element was not found in the subject range, return false
		}
	return true;  // all elements have been found, return true
	}

	static final <E> boolean uncheckedContainsAll(
		final E[] subject,
		final int subjectLowOffset,
		final int subjectHighBound,
		final E[] elements,
		final int elementsLowOffset,
		final int elementsHighBound,
		final Equalator<? super E> equalator
		)
	{
		// cross-iterate both arrays
		main:
		for(int ei = elementsLowOffset; ei < elementsHighBound; ei++)
		{
			final E element = elements[ei];
			for(int di = subjectLowOffset; di < subjectHighBound; di++)
			{
				if(equalator.equal(element, subject[di]))
				{
					continue main;
				}
			}
			return false;  // one element was not found in the subject range, return false
		}
	return true;  // all elements have been found, return true
	}

	public static <E, I extends Consumer<? super E>> I iterate(
		final E[] elements,
		final I   iterator
	)
	{
		for(final E e : elements)
		{
			iterator.accept(e);
		}
		
		return iterator;
	}

	public static final <T, S> int indexOf(final S sample, final T[] array, final BiPredicate<T, S> predicate)
	{
		for(int i = 0; i < array.length; i++)
		{
			if(predicate.test(array[i], sample))
			{
				return i;
			}
		}
		
		return -1;
	}

	public static final <T> int indexOf(final T element, final T[] array)
	{
		for(int i = 0; i < array.length; i++)
		{
			if(array[i] == element)
			{
				return i;
			}
		}
		
		return -1;
	}


	public static final byte[] rebuild(final byte[] oldArray, final int newLength)
	{
		final byte[] newArray = new byte[newLength];
		System.arraycopy(oldArray, 0, newArray, 0, Math.min(oldArray.length, newLength));
		return newArray;
	}
	
	public static final <E> E[] rebuild(final E[] oldArray, final int newLength)
	{
		final E[] newArray = X.ArrayOfSameType(oldArray, newLength);
		System.arraycopy(oldArray, 0, newArray, 0, Math.min(oldArray.length, newLength));
		return newArray;
	}
	
	public static final void set_byteInBytes(final byte[] bytes, final int index, final byte value)
	{
		bytes[index] = value;
	}
	
	public static final void set_booleanInBytes(final byte[] bytes, final int index, final boolean value)
	{
		bytes[index] = XTypes.to_byte(value);
	}

	public static final void set_shortInBytes(final byte[] bytes, final int index, final short value)
	{
		XArrays.validateArrayIndex(bytes.length, index + 1);
		bytes[index    ] = (byte)(value >>> 0*Byte.SIZE);
		bytes[index + 1] = (byte)(value >>> 1*Byte.SIZE);
	}

	public static final void set_charInBytes(final byte[] bytes, final int index, final char value)
	{
		XArrays.validateArrayIndex(bytes.length, index + 1);
		bytes[index    ] = (byte)(value >>> 0*Byte.SIZE);
		bytes[index + 1] = (byte)(value >>> 1*Byte.SIZE);
	}

	public static final void set_intInBytes(final byte[] bytes, final int index, final int value)
	{
		XArrays.validateArrayIndex(bytes.length, index + 3);
		bytes[index + 0] = (byte)(value >>> 0*Byte.SIZE);
		bytes[index + 1] = (byte)(value >>> 1*Byte.SIZE);
		bytes[index + 2] = (byte)(value >>> 2*Byte.SIZE);
		bytes[index + 3] = (byte)(value >>> 3*Byte.SIZE);
	}

	public static final void set_longInBytes(final byte[] bytes, final int index, final long value)
	{
		XArrays.validateArrayIndex(bytes.length, index + 7);
		bytes[index + 0] = (byte)(value >>> 0*Byte.SIZE);
		bytes[index + 1] = (byte)(value >>> 1*Byte.SIZE);
		bytes[index + 2] = (byte)(value >>> 2*Byte.SIZE);
		bytes[index + 3] = (byte)(value >>> 3*Byte.SIZE);
		bytes[index + 4] = (byte)(value >>> 4*Byte.SIZE);
		bytes[index + 5] = (byte)(value >>> 5*Byte.SIZE);
		bytes[index + 6] = (byte)(value >>> 6*Byte.SIZE);
		bytes[index + 7] = (byte)(value >>> 7*Byte.SIZE);
	}


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XArrays()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
