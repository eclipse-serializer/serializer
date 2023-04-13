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

import java.util.Comparator;

import org.eclipse.serializer.collections.types.XSortableSequence;
import org.eclipse.serializer.functional.ComparatorSequence;
import org.eclipse.serializer.math.FastRandom;


/**
 * 
 */
public final class XSort
{
	/*
	 * Implementing high-peformance low-level sorting algorithms has a few special cases as far as
	 * code quality is concerned.
	 *
	 * CHECKSTYLE.OFF: MethodLength: Sorting algorithms require a lot of redundant code for performance reasons
	 * CHECKSTYLE.OFF: MagicNumber : The low level sorting algorithms here use a lot of magic numbers intentionally
	 * CHECKSTYLE.OFF: EmptyBlock  : The empty while loops might be a little crazy, but work well.
	 * CHECKSTYLE.OFF: OneStatementPerLine: The statements in the huge algorithms are really tiny and help readability.
	 */

	///////////////////////////////////////////////////////////////////////////
	// Comparators //
	////////////////


	public static final int compare(final Long o1, final Long o2)
	{
		if(o1 == null)
		{
			return o2 == null ? 0 : -1;
		}
		else if(o2 == null)
		{
			return 1;
		}
		else
		{
			return o2.longValue() >= o1.longValue() ? o2.longValue() != o1.longValue() ? -1 : 0 : 1;
		}
	}

	public static final int compare(final String o1, final String o2)
	{
		if(o1 == null)
		{
			return o2 == null ? 0 : -1;
		}
		else if(o2 == null)
		{
			return 1;
		}
		else
		{
			return o1.compareTo(o2);
		}
	}

	@SafeVarargs
	public static final <E> Comparator<? super E> chain(final Comparator<? super E>... comparators)
	{
		return new ComparatorSequence<>(comparators);
	}



	///////////////////////////////////////////////////////////////////////////
	// Pseudo Random //
	//////////////////

	private static final transient int RANDOM_SEGMENTS = 32;
	private static final transient int R32_SHIFT = 5;
	private static final transient int R32_RANGE = 1 << R32_SHIFT;
	private static final transient int R32_SIZE = RANDOM_SEGMENTS * R32_RANGE;
	private static final transient int R32_MOD = R32_SIZE - 1;

	private static final transient int R04_SHIFT = 2;
	private static final transient int R04_RANGE = 1 << R04_SHIFT;
	private static final transient int R04_SIZE = RANDOM_SEGMENTS * R04_RANGE;
	private static final transient int R04_MOD = R04_SIZE - 1;

	private static final transient int[] RND32 = new int[R32_SIZE];
	private static final transient int[] RND04 = new int[R04_SIZE];
	private static transient int r;
	static
	{
		initPseudoRandom();
	}

	private static void initPseudoRandom()
	{
		// CHECKSTYLE.OFF: ArrayTypeStyle: a really rare case where that array style is not valid

		final FastRandom rnd = new FastRandom();
		for(int segment = 0, random[] = RND32; segment < RANDOM_SEGMENTS; segment++)
		{
			final int offset = segment * R32_RANGE, bound = offset + R32_RANGE;
			for(int i = offset; i < bound; i++)
			{
				random[i] = i - offset;
			}
			for(int i = offset; i < bound; i++)
			{
				swap(random, i, offset + rnd.nextInt(R32_RANGE));
			}
		}

		for(int segment = 0, random[] = RND04; segment < RANDOM_SEGMENTS; segment++)
		{
			final int offset = segment * R04_RANGE, bound = offset + R04_RANGE;
			for(int i = offset; i < bound; i++)
			{
				random[i] = i - offset;
			}
			for(int i = offset; i < bound; i++)
			{
				swap(random, i, offset + rnd.nextInt(R04_RANGE));
			}
		}

		// CHECKSTYLE.ON: ArrayTypeStyle
	}


	///////////////////////////////////////////////////////////////////////////
	// internal helper methods //
	////////////////////////////

	// note: private static helper methods showed no or even positive performance impact

	private static void swap(final int[] values, final int l, final int r)
	{
		final int t = values[l];
		values[l] = values[r];
		values[r] = t;
	}

	private static void swap(final long[] values, final int l, final int r)
	{
		final long t = values[l];
		values[l] = values[r];
		values[r] = t;
	}

	private static void swap(final Object[] values, final int l, final int r)
	{
		final Object t = values[l];
		values[l] = values[r];
		values[r] = t;
	}

	private static void checkRange(
		final int[] values,
		final int   start ,
		final int   bound
	)
	{
		if(start < 0)
		{
			throw new ArrayIndexOutOfBoundsException(start);
		}
		if(start >= bound)
		{
			throw new IllegalArgumentException("invalid sorting range");
		}
		if(bound > values.length)
		{
			throw new ArrayIndexOutOfBoundsException(bound);
		}
	}

	private static boolean checkRange(
		final Object[] values,
		final int start,
		final int bound
	)
	{
		if(start < 0)
		{
			throw new ArrayIndexOutOfBoundsException(start);
		}
		if(bound > values.length)
		{
			throw new ArrayIndexOutOfBoundsException(bound);
		}
		if(start > bound)
		{
			throw new IllegalArgumentException("invalid sorting range");
		}
		return bound - start > 1; // distance of 0 to bound means zero-length sort range, distance of 1 means trivial
	}

	///////////////////////////////////////////////////////////////////////////
	// internal substitute sorts //
	//////////////////////////////

	private static void insertionsort0(final int[] values, final int start, final int bound)
	{
		for(int i = start; i < bound; i++)
		{
			for(int j = i; j > start && values[j - 1] > values[j]; j--)
			{
				swap(values, j, j - 1);
			}
		}
	}

	private static void insertionsort0(final long[] values, final int start, final int bound)
	{
		for(int i = start; i < bound; i++)
		{
			for(int j = i; j > start && values[j - 1] > values[j]; j--)
			{
				swap(values, j, j - 1);
			}
		}
	}

	private static <E> void insertionsort0(final E[] values, final int start, final int bound, final Comparator<? super E> comparator)
	{
		for(int i = start; i < bound; i++)
		{
			for(int j = i; j > start && comparator.compare(values[j - 1], values[j]) > 0; j--)
			{
				swap(values, j, j - 1);
			}
		}
	}


	///////////////////////////////////////////////////////////////////////////
	// Adaptive Mergesort //
	///////////////////////
	
	@SuppressWarnings("unchecked")
	public static <V> void valueSort(final XSortableSequence<V> values, final Comparator<? super V> comparator)
	{
		if(values instanceof AbstractSimpleArrayCollection)
		{
			valueSort(
				((AbstractSimpleArrayCollection<V>)values).internalGetStorageArray(),
				0,
				((AbstractSimpleArrayCollection<?>)values).internalSize(),
				comparator
			);
			return;
		}
		
		// (12.10.2017 TM)TODO: more value sorts or refactoring for other solution.
		values.sort(comparator);
	}

	/**
	 * Sorts the passed array as values (i.e. with an unstable sorting algorithm).
	 * <p>
	 * This method is best used for sorting arrays where stability is not important of that consist only of
	 * distinct values of equal values that actually are just duplicate references to the same instance.
	 * <p>
	 * For a subranged version, see {@link #valueSort(Object[], int, int, Comparator)}.
	 * <p>
	 * The used algorithm works inplace, i.e. does not instantiate any additional instances.
	 * <p>
	 *
	 * @param <V> the type of the values to be sorted.
	 * @param values the values to be sorted.
	 * @param comparator the {@link Comparator} defining the sortation order of the values.
	 */
	public static <V> void valueSort(final V[] values, final Comparator<? super V> comparator)
	{
		dualPivotQuicksort(values, 0, values.length - 1, comparator);
	}

	/**
	 * Subranged version of {@link #valueSort(Object[], Comparator)}.
	 * <p>
	 * Example: {@code valueSort(myValues, 0, 5, myvalueComparator} sorts the first 5 values of array
	 * {@code myElements} (indices 0 to 4).
	 * <p>
	 *
	 * @param <V> the type of the values to be sorted.
	 * @param values the values to be sorted.
	 * @param start the starting index (inclusive) of the subrange to be sorted.
	 * @param bound the bounding index (exclusive) of the subrange to be sorted.
	 * @param comparator the {@link Comparator} defining the sortation order of the elements.
	 */
	public static <V> void valueSort(final V[] values, final int start, final int bound, final Comparator<? super V> comparator)
	{
		if(checkRange(values, start, bound))
		{
			dualPivotQuicksort(values, start, bound - 1, comparator);
		}
	}


	///////////////////////////////////////////////////////////////////////////
	// Primitive Sorting //
	//////////////////////

	public static final void sort(final long[] values) throws NullPointerException
	{
		dualPivotQuicksort(values, 0, values.length - 1);
	}


	///////////////////////////////////////////////////////////////////////////
	// Quicksort //
	//////////////

	public static final <E> void quicksort(
		final E[] values,
		final int start,
		final int bound,
		final Comparator<? super E> comparator
	)
		throws NullPointerException
	{
		if(checkRange(values, start, bound))
		{
			dualPivotQuicksort(values, start, bound - 1, comparator);
		}
	}


	///////////////////////////////////////////////////////////////////////////
	// Mergesort //
	//////////////

	public static <E> void mergesort(final E[] values, final int start, final int bound, final Comparator<? super E> comparator)
	{
		if(checkRange(values, start, bound))
		{
			mergesort0(values.clone(), values, start, bound, comparator);
		}
	}

	private static final <E> void mergesort0(
		final E[]                   buffer,
		final E[]                   values,
		final int                   start ,
		final int                   bound ,
		final Comparator<? super E> c
	)
	{
		if(bound - start < 7)
		{
			insertionsort0(values, start, bound, c);
			return;
		}

		final int mid;
		mergesort0(values, buffer, start, mid = start + bound >>> 1, c);
		mergesort0(values, buffer, mid, bound, c);

		if(c.compare(buffer[mid - 1], buffer[mid]) <= 0)
		{
			System.arraycopy(buffer, start, values, start, bound - start);
		}
		else
		{
			for(int l, i = l = start, r = mid; i < bound; i++)
			{
				if(r >= bound || l < mid && c.compare(buffer[l], buffer[r]) <= 0)
				{
					values[i] = buffer[l];
					l++;
				}
				else
				{
					values[i] = buffer[r];
					r++;
				}
			}
		}
	}


	///////////////////////////////////////////////////////////////////////////
	// Dual Pivot Quicksort //
	/////////////////////////

	private static void dualPivotQuicksort(final long[] a, final int low, final int high)
	{
		if(high - low < 31)
		{
			// note TM: removed length variable as it is only used 3 times.
			insertionsort0(a, low, high + 1);
			return;
		}

		final int seventh = (high - low + 1 >>> 3) + (high - low + 1 >>> 6) + 1; // Inexpensive approx. of length / 7
		final int e3 = low + high >>> 1; // The midpoint
		final int e2 = e3 - seventh;
		final int e1 = e2 - seventh;
		final int e4 = e3 + seventh;
		final int e5 = e4 + seventh;

		if(a[e2] < a[e1])
		{
			final long t = a[e2]; a[e2] = a[e1]; a[e1] = t;
		}
		if(a[e3] < a[e2])
		{
			final long t = a[e3]; a[e3] = a[e2]; a[e2] = t;
			if(t < a[e1])
			{
				a[e2] = a[e1]; a[e1] = t;
			}
		}
		if(a[e4] < a[e3])
		{
			final long t = a[e4]; a[e4] = a[e3]; a[e3] = t;
			if(t < a[e2])
			{
				a[e3] = a[e2]; a[e2] = t;
				if(t < a[e1])
				{
					a[e2] = a[e1]; a[e1] = t;
				}
			}
		}
		if(a[e5] < a[e4])
		{
			final long t = a[e5]; a[e5] = a[e4]; a[e4] = t;
			if(t < a[e3])
			{
				a[e4] = a[e3]; a[e3] = t;
				if(t < a[e2])
				{
					a[e3] = a[e2]; a[e2] = t;
					if(t < a[e1])
					{
						a[e2] = a[e1]; a[e1] = t;
					}
				}
			}
		}

		final long pivot1, pivot2;
		int left  = low;
		int right = high;
		if((pivot1 = a[e2]) != (pivot2 = a[e4]))
		{
			a[e2] = a[low];
			a[e4] = a[high];
			while(a[++left] < pivot1)
			{
				/*_*/
			}
			while(a[--right] > pivot2)
			{
				/*_*/
			}
			outer:
			for(int k = left; k <= right; k++)
			{
				final long ak = a[k];
				if(ak < pivot1)
				{
					a[k] = a[left];
					a[left] = ak;
					left++;
				}
				else if(ak > pivot2)
				{
					while(a[right] > pivot2)
					{
						if(right-- == k)
						{
							break outer;
						}
					}
					if(a[right] < pivot1)
					{
						a[k] = a[left];
						a[left] = a[right];
						left++;
					}
					else
					{
						a[k] = a[right];
					}
					a[right] = ak;
					right--;
				}
			}
			a[low]  = a[left - 1]; a[left - 1] = pivot1;
			a[high] = a[right + 1]; a[right + 1] = pivot2;
			dualPivotQuicksort(a, low, left - 2);
			dualPivotQuicksort(a, right + 2, high);

			if(left < e1 && e5 < right)
			{
				while(a[left] == pivot1)
				{
					left++;
				}
				while(a[right] == pivot2)
				{
					right--;
				}
				outer:
				for(int k = left; k <= right; k++)
				{
					final long ak = a[k];
					if(ak == pivot1)
					{
						a[k] = a[left];
						a[left] = ak;
						left++;
					}
					else if(ak == pivot2)
					{
						while(a[right] == pivot2)
						{
							if(right-- == k)
							{
								break outer;
							}
						}
						if(a[right] == pivot1)
						{
							a[k] = a[left];
							a[left] = pivot1;
							left++;
						}
						else
						{
							a[k] = a[right];
						}
						a[right] = ak;
						right--;
					}
				}
			}
			dualPivotQuicksort(a, left, right);
		}
		else
		{
			// Pivots are equal
			for(int k = low; k <= right; k++)
			{
				if(a[k] == pivot1)
				{
					continue;
				}
				final long ak = a[k];
				if(ak < pivot1)
				{
					a[k] = a[left];
					a[left] = ak;
					left++;
				}
				else
				{
					while(a[right] > pivot1)
					{
						right--;
					}
					if(a[right] < pivot1)
					{
						a[k] = a[left];
						a[left] = a[right];
						left++;
					}
					else
					{
						a[k] = pivot1;
					}
					a[right] = ak;
					right--;
				}
			}
			dualPivotQuicksort(a, low, left - 1);
			dualPivotQuicksort(a, right + 1, high);
		}
	}

	private static <E> void dualPivotQuicksort(
		final E[]                   a   ,
		final int                   low ,
		final int                   high,
		final Comparator<? super E> cmp
	)
	{
		// note TM: removed length variable as it is only used 3 times.
		if(high - low < 16)
		{
			// note TM: sentinel stuff and length 32 for objects turned out to be slower than this
			insertionsort0(a, low, high + 1, cmp);
			return;
		}

		final int seventh = (high - low + 1 >>> 3) + (high - low + 1 >>> 6) + 1; // Inexpensive approx. of length / 7
		final int e3 = low + high >>> 1;
		final int e2 = e3 - seventh;
		final int e1 = e2 - seventh;
		final int e4 = e3 + seventh;
		final int e5 = e4 + seventh;

		if(cmp.compare(a[e2], a[e1]) < 0)
		{
			final E t = a[e2]; a[e2] = a[e1]; a[e1] = t;
		}
		if(cmp.compare(a[e3], a[e2]) < 0)
		{
			final E t = a[e3]; a[e3] = a[e2]; a[e2] = t;
			if(cmp.compare(t, a[e1]) < 0)
			{
				a[e2] = a[e1]; a[e1] = t;
			}
		}
		if(cmp.compare(a[e4], a[e3]) < 0)
		{
			final E t = a[e4]; a[e4] = a[e3]; a[e3] = t;
			if(cmp.compare(t, a[e2]) < 0)
			{
				a[e3] = a[e2]; a[e2] = t;
				if(cmp.compare(t, a[e1]) < 0)
				{
					a[e2] = a[e1]; a[e1] = t;
				}
			}
		}
		if(cmp.compare(a[e5], a[e4]) < 0)
		{
			final E t = a[e5]; a[e5] = a[e4]; a[e4] = t;
			if(cmp.compare(t, a[e3]) < 0)
			{
				a[e4] = a[e3]; a[e3] = t;
				if(cmp.compare(t, a[e2]) < 0)
				{
					a[e3] = a[e2]; a[e2] = t;
					if(cmp.compare(t, a[e1]) < 0)
					{
						a[e2] = a[e1]; a[e1] = t;
					}
				}
			}
		}

		final E pivot1, pivot2;
		int left  = low;
		int right = high;
		if(cmp.compare(pivot1 = a[e2], pivot2 = a[e4]) != 0)
		{
			a[e2] = a[low];
			a[e4] = a[high];
			while(cmp.compare(a[++left], pivot1) < 0)
			{
				/*_*/
			}
			while(cmp.compare(a[--right], pivot2) > 0)
			{
				/*_*/
			}
			outer:
			for(int k = left; k <= right; k++)
			{
				final E ak = a[k];
				if(cmp.compare(ak, pivot1) < 0)
				{
					a[k] = a[left];
					a[left] = ak;
					left++;
				}
				else if(cmp.compare(ak, pivot2) > 0)
				{
					while(cmp.compare(a[right], pivot2) > 0)
					{
						if(right-- == k)
						{
							break outer;
						}
					}
					if(cmp.compare(a[right], pivot1) < 0)
					{
						a[k] = a[left];
						a[left] = a[right];
						left++;
					}
					else
					{
						a[k] = a[right];
					}
					a[right] = ak;
					right--;
				}
			}
			a[low      ] = a[left - 1] ;
			a[left -  1] = pivot1      ;
			a[high     ] = a[right + 1];
			a[right + 1] = pivot2      ;
			dualPivotQuicksort(a, low, left - 2, cmp);
			dualPivotQuicksort(a, right + 2, high, cmp);

			if(left < e1 && e5 < right)
			{
				while(cmp.compare(a[left ], pivot1) == 0)
				{
					left++;
				}
				while(cmp.compare(a[right], pivot2) == 0)
				{
					right--;
				}
				outer:
				for(int k = left; k <= right; k++)
				{
					final E ak = a[k];
					if(cmp.compare(ak, pivot1) == 0)
					{
						a[k] = a[left];
						a[left] = ak;
						left++;
					}
					else if(cmp.compare(ak, pivot2) == 0)
					{
						while(cmp.compare(a[right], pivot2) == 0)
						{
							if(right-- == k)
							{
								break outer;
							}
						}
						if(cmp.compare(a[right], pivot1) == 0)
						{
							a[k] = a[left];
							a[left] = pivot1;
							left++;
						}
						else
						{
							a[k] = a[right];
						}
						a[right] = ak;
						right--;
					}
				}
			}
			dualPivotQuicksort(a, left, right, cmp);
		}
		else
		{
			// Pivots are equal
			for(int k = low; k <= right; k++)
			{
				if(cmp.compare(a[k], pivot1) == 0)
				{
					continue;
				}
				final E ak = a[k];
				if(cmp.compare(ak, pivot1) < 0)
				{
					a[k] = a[left];
					a[left] = ak;
					left++;
				}
				else
				{
					// a[k] > pivot1 - Move a[k] to right part
					while(cmp.compare(a[right], pivot1) > 0)
					{
						right--;
					}
					if(cmp.compare(a[right], pivot1) < 0)
					{
						a[k] = a[left];
						a[left] = a[right];
						left++;
					}
					else
					{
						a[k] = pivot1;
					}
					a[right] = ak;
					right--;
				}
			}
			dualPivotQuicksort(a, low, left - 1, cmp);
			dualPivotQuicksort(a, right + 1, high, cmp);
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
	private XSort()
	{
		// static only
		throw new UnsupportedOperationException();
	}

	/*
	 * CHECKSTYLE.ON: MethodLength
	 * CHECKSTYLE.ON: MagicNumber
	 * CHECKSTYLE.ON: EmptyBlock
	 * CHECKSTYLE.ON: OneStatementPerLine
	 */
}
