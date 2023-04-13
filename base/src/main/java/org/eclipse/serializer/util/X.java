package org.eclipse.serializer.util;

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

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.Iterator;

import org.eclipse.serializer.branching.AbstractBranchingThrow;
import org.eclipse.serializer.branching.ThrowBreak;
import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.ConstList;
import org.eclipse.serializer.collections.Constant;
import org.eclipse.serializer.collections.Empty;
import org.eclipse.serializer.collections.EmptyTable;
import org.eclipse.serializer.collections.Singleton;
import org.eclipse.serializer.collections.interfaces.Sized;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XList;
import org.eclipse.serializer.collections.types.XReference;
import org.eclipse.serializer.exceptions.ArrayCapacityException;
import org.eclipse.serializer.exceptions.IndexBoundsException;
import org.eclipse.serializer.typing.KeyValue;

/**
 * Central class for general utility methods regarding collections, arrays and some basic general functionality that is
 * missing in Java like {@link #notNull(Object)} or {@link #ints(int...)}.<br>
 * <br>
 * This class uses the following sound extension of the java naming conventions:<br>
 * Static methods that resemble a constructor, begin with an upper case letter. This is consistent with existing naming
 * rules: method names begin with a lower case letter EXCEPT for constructor methods. This extension does nothing
 * more than applying the same exception to constructor-like static methods. Resembling a constructor means:
 * 1.) Indicating by name that a new instance is created. 2.) Always returning a new instance, without exception.
 * No caching, no casting. For example: {@link #empty()} is NOT constructor-like methods
 * because they do not (always) create new instances.
 *
 */
public final class X
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private static final Empty<?> EMPTY = new Empty<>();

	private static final EmptyTable<?, ?> EMPTY_TABLE = new EmptyTable<>();
	
	/**
	 * {@link AbstractBranchingThrow} to indicate the abort of a loop or procedure, with a negative or unknown result.
	 */
	private static final transient ThrowBreak BREAK = new ThrowBreak();
		
	private static final long INTEGER_RANGE_BOUND = Integer.MAX_VALUE + 1L;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings("unchecked")
	public static final <T> Empty<T> empty()
	{
		return (Empty<T>)EMPTY;
	}

	@SuppressWarnings("unchecked")
	public static final <K, V> EmptyTable<K, V> emptyTable()
	{
		return (EmptyTable<K, V>)EMPTY_TABLE;
	}
	
	
	
	public static ThrowBreak BREAK()
	{
		/*
		 * "break()" is not possible, of course.
		 * "Break" collides with the constructor-method naming.
		 * "breaK" and the like look something between weird and ugly and might cause confusion when typing.
		 * So, since it's actually just an encapsulated constant, the most favorable variant is "BREAK()".
		 */
		return BREAK;
	}
	
	
	
	// hopefully, this can be removed at some point in the future ...
	/**
	 * Central validation point for Java's current technical limitation of max int as max array capacity.
	 * Note that because of dependencies of many types to arrays (e.g. toArray() methods, etc.), this limitation
	 * indirectly affects many other types, for example String, collections, ByteBuffers (which is extremely painful).
	 * <p>
	 *
	 * @param capacity the desired (array-dependent) capacity which may effectively be not greater than
	 * {@link Integer}.MAX_VALUE.
	 * @return the safely downcasted capacity as an int value.
	 * @throws ArrayCapacityException if the passed capacity is greater than {@link Integer}.MAX_VALUE
	 */
	public static final int checkArrayRange(final long capacity) throws ArrayCapacityException
	{
		// " >= " proved to be faster in tests than ">" (probably due to simple sign checking)
		if(capacity >= INTEGER_RANGE_BOUND)
		{
			throw new ArrayCapacityException(capacity);
		}
		return (int)capacity;
	}
	
	/**
	 * Transiently ensures the passed object to be not {@code null} by either returning it in case it is
	 * not {@code null} or throwing a {@link NullPointerException} otherwise.
	 * <p>
	 * <i>(Really, no idea why java.util.Objects.notNull got renamed to requireNotNull</i>
	 *
	 * @param <T> the type of the object to be ensured to be not {@code null}.
	 * @param object the object to be ensured to be not {@code null}.
	 * @return the passed object, guaranteed to be not {@code null}.
	 * @throws NullPointerException if {@code null} was passed.
	 */
	public static final <T> T notNull(final T object) throws NullPointerException
	{
		if(object == null)
		{
			throw UtilStackTrace.cutStacktraceByOne(new NullPointerException());
		}
		return object;
	}
	
	/**
	 * This method is a complete dummy, simply serving as a semantic counterpart to {@link #notNull(Object)}.<br>
	 * The use is small, but still there:<br>
	 * - the sourcecode is easier to read if the same structure is used next to a {@link #notNull(Object)} call
	 *   instead of missing method calls and comments (like "may be null" or "optional").
	 * - the IDE can search for all occurrences of this method, listing all places where something may be null.
	 * 
	 * @param <T> the object's type
	 * @param object the passed reference.
	 * @return the passed reference without doing ANYTHING else.
	 */
	public static final <T> T mayNull(final T object)
	{
		return object;
	}
	

	public static final <T> T coalesce(final T firstElement, final T secondElement)
	{
		return firstElement == null
			? secondElement
			: firstElement
		;
	}
	
	@SafeVarargs
	public static final <T> T coalesce(final T... elements)
	{
		for(int i = 0; i < elements.length; i++)
		{
			// spare foreach's unnecessary variable assignment on each check
			if(elements[i] != null)
			{
				return elements[i];
			}
		}
		return null;
	}
	
	
	public static final byte unbox(final Byte d)
	{
		return d == null
			? 0
			: d.byteValue()
		;
	}

	public static final boolean unbox(final Boolean d)
	{
		return d != null && d.booleanValue();
	}

	public static final short unbox(final Short d)
	{
		return d == null
			? 0
			: d.shortValue()
		;
	}

	public static final char unbox(final Character d)
	{
		return d == null
			? (char) 0
			: d.charValue()
		;
	}

	public static final int unbox(final Integer d)
	{
		return d == null
			? 0
			: d.intValue()
		;
	}

	public static final float unbox(final Float d)
	{
		return d == null
			? 0f
			: d.floatValue()
		;
	}

	public static final long unbox(final Long d)
	{
		return d == null
			? 0L
			: d.longValue()
		;
	}

	public static final double unbox(final Double d)
	{
		return d == null
			? 0.0D
			: d.doubleValue()
		;
	}

	public static final long[] unbox(final Long[] array)
	{
		return unbox(array, 0);
	}

	public static final long[] unbox(final Long[] array, final long nullReplacement)
	{
		if(array == null)
		{
			return null;
		}

		final long[] result = new long[array.length];
		for(int i = 0, length = result.length; i < length; i++)
		{
			final Long value = array[i];
			result[i] = value == null
				? nullReplacement
				: value.longValue()
			;
		}
		
		return result;
	}


	public static int[] ints(final int... elements)
	{
		return elements;
	}

	@SafeVarargs
	public static <T> T[] array(final T... elements)
	{
		return elements;
	}
	
	@SafeVarargs
	public static <E> XList<E> List(final E... elements)
	{
		if(elements == null || elements.length == 0)
		{
			return BulkList.New();
		}
		return BulkList.New(elements);
	}

	@SafeVarargs
	public static <E> ConstList<E> ConstList(final E... elements) throws NullPointerException
	{
		return ConstList.New(elements);
	}
	
	public static <E> Singleton<E> Singleton(final E element)
	{
		return Singleton.New(element);
	}

	public static <E> Constant<E> Constant(final E element)
	{
		return new Constant<>(element);
	}

	public static <T> XReference<T> Reference(final T object)
	{
		return Singleton.New(object);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Iterable<?> Iterable(final Object array)
	{
		return () -> new Iterator()
		{
			      int position = 0;
			final int length   = Array.getLength(array);
			
			@Override
			public boolean hasNext()
			{
				return this.position < this.length;
			}

			@Override
			public Object next()
			{
				return Array.get(array, this.position++);
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public static final <E> E[] ArrayOfSameType(final E[] sampleArray, final int length)
	{
		return (E[])Array.newInstance(sampleArray.getClass().getComponentType(), length);
	}
	
	@SuppressWarnings("unchecked")
	public static final <E> E[] ArrayOfSameType(final E[] sampleArray)
	{
		return (E[])Array.newInstance(sampleArray.getClass().getComponentType(), sampleArray.length);
	}
	
	public static final <E> E[] Array(final E element)
	{
		return Array(1, element);
	}
	
	public static final <E> E[] Array(final int length, final E element)
	{
		@SuppressWarnings("unchecked")
		final E[] newArray = (E[])Array.newInstance(element.getClass(), length);
		newArray[0] = element;
		
		return newArray;
	}

	@SuppressWarnings("unchecked")
	public static <E> E[] Array(final Class<E> componentType, final int length)
	{
		return (E[])Array.newInstance(componentType, length);
	}
	
	public static <T> WeakReference<T> WeakReference(final T referent)
	{
		return new WeakReference<>(referent);
	}
	
	@SuppressWarnings("unchecked") // damn type erasure
	public static <T> WeakReference<T>[] WeakReferences(final int length)
	{
		return new WeakReference[length];
	}
	
	/**
	 * Removes all <code>null</code> entries and entries with <code>null</code>-referents.
	 * 
	 * @param <T> the component type
	 * @param array the array to consolidate
	 * @return the consolidated array with non-null values
	 */
	public static <T> WeakReference<T>[] consolidateWeakReferences(final WeakReference<T>[] array)
	{
		int liveEntryCount = 0;
		for(int i = 0; i < array.length; i++)
		{
			if(array[i] == null)
			{
				continue;
			}
			if(array[i].get() == null)
			{
				array[i] = null;
				continue;
			}
			liveEntryCount++;
		}
		
		// check for no-op
		if(liveEntryCount == array.length)
		{
			return array;
		}
		
		final WeakReference<T>[] newArray = X.WeakReferences(liveEntryCount);
		for(int i = 0, n = 0; i < array.length; i++)
		{
			if(array[i] != null)
			{
				newArray[n++] = array[i];
			}
		}
		
		return newArray;
	}

	public static boolean hasNoContent(final XGettingCollection<?> collection)
	{
		return collection == null || collection.isEmpty();
	}

	public static final <S extends Sized> S notEmpty(final S sized)
	{
		if(sized.isEmpty())
		{
			throw UtilStackTrace.cutStacktraceByOne(new IllegalArgumentException());
		}
		return sized;
	}

	public static final <E> E[] notEmpty(final E[] array)
	{
		if(array.length == 0)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IllegalArgumentException());
		}
		return array;
	}

	public static <K, V> KeyValue<K, V> KeyValue(final K key, final V value)
	{
		return KeyValue.New(key, value);
	}
	
	public static final long validateRange(final long bound, final long startIndex, final long length)
	{
		if(startIndex < 0)
		{
			throw IndexBoundsException(0, bound, startIndex, "StartIndex < 0", 1);
		}
		if(startIndex >= bound)
		{
			throw IndexBoundsException(0, bound, startIndex, "StartIndex >= bound", 1);
		}
		
		if(length < 0)
		{
			throw IndexBoundsException(startIndex, bound, length, "Length < 0", 1);
		}
		if(startIndex + length > bound)
		{
			throw IndexBoundsException(0, bound, startIndex + length, "Range > bound", 1);
		}
		
		return startIndex + length;
	}
	
	public static final IndexBoundsException IndexBoundsException(
		final long   startIndex        ,
		final long   indexBound        ,
		final long   index             ,
		final String message           ,
		final int    stackTraceCutDepth
	)
	{
		return UtilStackTrace.cutStacktraceByN(
			new IndexBoundsException(startIndex, indexBound, index, message),
			stackTraceCutDepth + 1
		);
	}
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private X()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
