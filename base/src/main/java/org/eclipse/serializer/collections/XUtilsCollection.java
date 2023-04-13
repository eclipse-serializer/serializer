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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.serializer.collections.types.XCollection;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XProcessingSequence;
import org.eclipse.serializer.collections.types.XSettingList;
import org.eclipse.serializer.collections.types.XSortableSequence;
import org.eclipse.serializer.util.X;
import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.functional.AggregateMax;
import org.eclipse.serializer.functional.AggregateMin;
import org.eclipse.serializer.functional.IndexedAcceptor;
import org.eclipse.serializer.functional.IsCustomEqual;
import org.eclipse.serializer.functional.IsGreater;
import org.eclipse.serializer.functional.IsSmaller;
import org.eclipse.serializer.meta.NotImplementedYetError;
import org.eclipse.serializer.typing.XTypes;

public final class XUtilsCollection
{

	private static final Object MARKER = new Object();


	public static <E, C extends XSortableSequence<E>> C valueSort(
		final C                     collection,
		final Comparator<? super E> order
	)
	{
		if(collection instanceof AbstractSimpleArrayCollection<?>)
		{
			XSort.valueSort(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)collection),
				0,
				XTypes.to_int(collection.size()),
				order
			);
		}
		else
		{
			collection.sort(order); // sorting a non-array collection (i.e. chain) by value yields no advantage
		}
		return collection;
	}


	public static <E, C extends XCollection<E>> C subtract(
		final C                               elements,
		final XGettingCollection<? extends E> other
	)
	{
		elements.removeAll(other);
		return elements;
	}


	public static <E> boolean rngContainsAll(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final XGettingCollection<? extends E> elements
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedContainsAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				elements
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> E rngMax(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedAggregate(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				new AggregateMax<>(comparator)
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> E rngMin(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedAggregate(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				new AggregateMin<>(comparator)
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E>
	int rngIndexOF(final XGettingSequence<E> sequence, final long offset, final long length, final E element)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedIndexOF(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E>
	int rngCount(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final E element
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedCount(AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngIsSorted(
		final XGettingSequence<E>   sequence  ,
		final long                  offset    ,
		final long                  length    ,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedIsSorted(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				comparator
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends Consumer<? super E>> C rngCopyTo(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final C target
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedCopyTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				target
			);
		}

		AbstractArrayStorage.validateRange0toUpperBound(
			XTypes.to_int(sequence.size()),
			XTypes.to_int(offset),
			XTypes.to_int(length)
		);
		sequence.iterate(
			new Consumer<E>()
			{
				long ofs = offset, len = length;

				@Override
				public void accept(final E e)
				{
					if(this.ofs != 0)
					{
						this.ofs--;
						return;
					}
					if(this.len-- == 0)
					{
						throw X.BREAK();
					}
					target.accept(e);
				}
			}
		);
		return target;
	}

	public static <E, C extends Consumer<? super E>>
	C rngCopyTo(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final C target,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedCopyTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				target,
				predicate
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends XGettingSequence<E>>
	C rngIterate(final C sequence, final long offset, final long length, final Consumer<? super E> procedure)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedIterate(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				procedure
			);
			return sequence;
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends XGettingSequence<E>> C rngIterate(
		final C                         sequence ,
		final long                      offset   ,
		final long                      length   ,
		final IndexedAcceptor<? super E> procedure
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedIterate(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				procedure
			);
			return sequence;
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, A> A rngJoin(
		final XGettingSequence<E>               sequence ,
		final long                              offset   ,
		final long                              length   ,
		final BiConsumer<? super E, ? super A> joiner   ,
		final A                                 aggregate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedJoin(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				joiner,
				aggregate
			);
			return aggregate;
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngContains(
		final XGettingSequence<E> sequence,
		final long                offset  ,
		final long                length  ,
		final E                   element
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedContainsSame(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngContainsId(
		final XGettingSequence<E> sequence,
		final long                offset  ,
		final long                length  ,
		final E                   element
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedContainsSame(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngContainsNull(final XGettingSequence<E> sequence, final long offset, final long length)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedContainsNull(
				AbstractSimpleArrayCollection.internalGetStorageArray(
					(AbstractSimpleArrayCollection<?>)sequence),
					XTypes.to_int(sequence.size()),
					XTypes.to_int(offset),
					XTypes.to_int(length)
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngApplies(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final Predicate<? super E> predicate
	)
	{
		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngAppliesAll(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedApplies(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngCount(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedConditionalCount(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngIndexOf(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedConditionalIndexOf(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngScan(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedScan(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> E rngGet(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final E                    sample   ,
		final Equalator<? super E> equalator
	)
	{
		// implementation-specific optimized alternatives
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedQueryElement(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				new IsCustomEqual<>(equalator, sample), null
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> E rngSearch(
		final XGettingSequence<E>  sequence ,
		final long                 offset   ,
		final long                 length   ,
		final Predicate<? super E> predicate
	)
	{
		// implementation-specific optimized alternatives
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedQueryElement(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate, null
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngEqualsContent(
		final XGettingSequence<E>           sequence ,
		final long                          offset   ,
		final long                          length   ,
		final XGettingSequence<? extends E> other    ,
		final Equalator<? super E>          equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedEqualsContent(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				other, equalator
			);
		}

		// (13.03.2011)TODO: rngEqualsContent() ... tricky
		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngMaxIndex(
		final XGettingSequence<E>   sequence  ,
		final long                  offset    ,
		final long                  length    ,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedScan(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				new IsGreater<>(comparator)
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E>
	int rngMinIndex(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedScan(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				new IsSmaller<>(comparator)
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends Consumer<? super E>>
	C rngDistinct(final XGettingSequence<E> sequence, final long offset, final long length, final C target)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedDistinct(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				target
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends Consumer<? super E>> C rngDistinct(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final C target,
		final Equalator<? super E> equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedDistinct(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				target,
				equalator
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends Consumer<? super E>> C rngIntersect(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedIntersect(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				samples,
				equalator,
				target
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E, C extends Consumer<? super E>> C rngUnion(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedUnion(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				samples,
				equalator,
				target
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet

	}

	public static <E, C extends Consumer<? super E>> C rngExcept(
		final XGettingSequence<E> sequence,
		final long offset,
		final long length,
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedExcept(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				samples,
				equalator,
				target
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	///////////////////////////////////////////////////////////////////////////
	// removing //
	/////////////

	@SuppressWarnings("unchecked")
	public static <E> E rngRetrieve(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final E element
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRetrieve(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element,
				(E)MARKER
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> E rngRetrieve(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRetrieve(
				AbstractSimpleArrayCollection.<E>internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate,
				(E)MARKER
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> boolean rngRemoveOne(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final E element
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemoveOne(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngRemoveNull(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemoveNull(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length)
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngRemove(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final E element
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemove(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				element
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> int rngRemoveAll(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final XGettingCollection<? extends E> elements
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemoveAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				elements
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> int rngRetainAll(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final XGettingCollection<? extends E> elements
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRetainAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				(XGettingCollection<E>)elements,
				(E)MARKER
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> int rngRemoveDuplicates(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final Equalator<? super E> equalator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemoveDuplicates(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				equalator,
				(E)MARKER
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> int rngRemoveDuplicates(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedRemoveDuplicates(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				(E)MARKER
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> int rngReduce(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.rangedReduce(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				predicate,
				(E)MARKER
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E, C extends Consumer<? super E>> C rngMoveTo(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final C target,
		final Predicate<? super E> predicate
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedMoveTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				target,
				predicate,
				(E)MARKER
			);
			return target;
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	@SuppressWarnings("unchecked")
	public static <E> XProcessingSequence<E> rngProcess(
		final XProcessingSequence<E> sequence,
		final long offset,
		final long length,
		final Consumer<? super E> procedure
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedProcess(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				procedure,
				(E)MARKER
			);
			return sequence;
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> void rngSort(
		final XSettingList<E> sequence,
		final long offset,
		final long length,
		final Comparator<? super E> comparator
	)
	{
		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			AbstractArrayStorage.rangedSort(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				XTypes.to_int(sequence.size()),
				XTypes.to_int(offset),
				XTypes.to_int(length),
				comparator
			);
		}

		throw new NotImplementedYetError(); // FIXME not implemented yet
	}

	public static <E> VarString appendTo(final XGettingCollection<E> collection, final VarString vc, final char separator)
	{
		if(collection instanceof AbstractSimpleArrayCollection<?>)
		{
			return AbstractArrayStorage.appendTo(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)collection), XTypes.to_int(collection.size()), vc, separator
			);
		}

		if(XTypes.to_int(collection.size()) == 0)
		{
			return vc;
		}

		collection.iterate(e -> vc.add(e).append(separator));

		return vc.deleteLast();
	}

	// counting add and put //


	public static <E, S extends E> E[] toArray(
		final XGettingCollection<S> collection        ,
		final Class<E>              arrayComponentType
	)
	{
		final E[] array = X.Array(arrayComponentType, X.checkArrayRange(collection.size()));
		XArrays.copyTo(collection, array);

		return array;
	}


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XUtilsCollection()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
