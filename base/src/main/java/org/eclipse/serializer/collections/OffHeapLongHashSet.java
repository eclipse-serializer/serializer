package org.eclipse.serializer.collections;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2026 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import org.eclipse.serializer.math.XMath;
import org.eclipse.serializer.memory.XMemory;

import java.nio.ByteBuffer;

/**
 * A hash set for primitive {@code long} values whose backing storage is allocated off-heap
 * via a direct {@link ByteBuffer}.
 * <p>
 * This implementation uses open addressing with linear probing. It is designed for the use case
 * of collecting a large number of {@code long} values and testing them for presence; it does
 * <i>not</i> support removal of elements. The value {@code 0L} is supported and tracked separately,
 * since {@code 0L} is also used internally as the sentinel for empty slots.
 * <p>
 * When the number of collisions during an insertion exceeds the configured thresholds,
 * the backing storage is doubled and all contained values are rehashed into the new storage.
 * The previous direct buffer is deallocated explicitly in order to release large off-heap
 * memory chunks immediately instead of waiting for garbage collection.
 * <p>
 * Instances are not safe for concurrent modification by multiple threads.
 *
 * @see XMemory
 */
public final class OffHeapLongHashSet
{
	/* TODO: OffHeapLongHashSet memory segmentation
	 * Currently, this implementation can theoretically grow very large (as long as enough
	 * native memory is available), but it has one problem:
	 * It needs to allocate larger and larger monolithic memory blocks, eventually exceeding
	 * the available native memory.
	 * A solution would be:
	 * - Switch from open addressing to chaining.
	 * - Chains are held in another allocated memory block. e.g. always 1024 chains per block (configurable).
	 * - Multiple chain blocks are allocated as needed, as long as the configuration does not require a storage enlargement.
	 * - Each chain has a pointer at its end, pointing to the next chain, if necessary.
	 *   This is important to not force a storage enlargement just because a single chain is full.
	 *
	 * This way, the set can be configured to have a maximum hash table length of a certain size (e.g. 1 GB)
	 * and from then on grow only by having larger and larger chains.
	 * This creates multiple memory segments that can be allocated and swapped step by step
	 * without the need to allocate one gigantic block that exceeds the available native memory.
	 *
	 * The tradeoff between hash table size and chain length can be perfectly configured
	 * using a few simple int values. (maximum hash table length, chain length, chains per block, etc.)
	 */


	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final long EMPTY = 0L;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Returns the default initial capacity ({@code 1024}) used by {@link #New()}
	 * when no explicit capacity is specified.
	 *
	 * @return the default initial capacity, always a power of two.
	 */
	public static long defaultInitialCapacity()
	{
		return 1L<<10;
	}

	/**
	 * Returns the default collision threshold, i.e. the number of collisions during an
	 * insertion that are tolerated before the {@linkplain #defaultExcessLimit() excess counter}
	 * starts to be incremented.
	 *
	 * @return the default collision threshold.
	 */
	public static int defaultCollisionThreshold()
	{
		return 8;
	}

	/**
	 * Returns the default collision limit, i.e. the number of collisions during a single
	 * insertion that immediately triggers an enlargement of the backing storage.
	 *
	 * @return the default collision limit, derived from the {@linkplain #defaultCollisionThreshold() default collision threshold}.
	 */
	public static int defaultCollisionLimit()
	{
		return deriveCollisionLimit(defaultCollisionThreshold());
	}

	/**
	 * Derives a collision limit from the given collision threshold. The limit is always
	 * a multiple of the threshold and represents the hard per-insertion limit after which
	 * the storage is enlarged regardless of the excess counter.
	 *
	 * @param collisionThreshold the collision threshold to derive the limit from.
	 * @return the derived collision limit.
	 */
	public static int deriveCollisionLimit(final int collisionThreshold)
	{
		return collisionThreshold * 8;
	}

	/**
	 * Returns the default excess limit, i.e. the number of insertions exceeding the
	 * {@linkplain #defaultCollisionThreshold() collision threshold} that are tolerated
	 * before the backing storage is enlarged.
	 *
	 * @return the default excess limit.
	 */
	public static int defaultExcessLimit()
	{
		return 64;
	}

	/**
	 * Rounds the given desired capacity up to the next power of two. Since the set relies on
	 * a power-of-two capacity for its hash masking, this method is used internally to normalize
	 * capacities supplied by callers.
	 *
	 * @param desiredCapacity the desired (minimum) capacity; must be positive.
	 * @return the smallest power of two that is greater than or equal to {@code desiredCapacity}.
	 * @throws IllegalArgumentException if {@code desiredCapacity} is not positive.
	 */
	public static long padCapacity(final long desiredCapacity)
	{
		XMath.positive(desiredCapacity);

		long capacity = 1;
		while(capacity < desiredCapacity)
		{
			capacity <<= 1;
		}

		return capacity;
	}



	/**
	 * Creates a new {@link OffHeapLongHashSet} with all-default configuration values.
	 *
	 * @return the new set instance.
	 * @see #defaultInitialCapacity()
	 * @see #defaultCollisionThreshold()
	 * @see #defaultCollisionLimit()
	 * @see #defaultExcessLimit()
	 */
	public static OffHeapLongHashSet New()
	{
		return new OffHeapLongHashSet(
			defaultInitialCapacity(),
			defaultCollisionThreshold(),
			defaultCollisionLimit(),
			defaultExcessLimit()
		);
	}

	/**
	 * Creates a new {@link OffHeapLongHashSet} with the given initial capacity and otherwise
	 * default configuration. The capacity is rounded up to the next power of two via
	 * {@link #padCapacity(long)}.
	 *
	 * @param desiredCapacity the desired initial capacity; must be positive.
	 * @return the new set instance.
	 */
	public static OffHeapLongHashSet New(final long desiredCapacity)
	{
		return new OffHeapLongHashSet(
			padCapacity(desiredCapacity),
			defaultCollisionThreshold(),
			defaultCollisionLimit(),
			defaultExcessLimit()
		);
	}

	/**
	 * Creates a new {@link OffHeapLongHashSet} with the given initial capacity and collision
	 * threshold. The collision limit is {@linkplain #deriveCollisionLimit(int) derived} from
	 * the threshold, and the excess limit defaults to {@link #defaultExcessLimit()}.
	 *
	 * @param desiredCapacity    the desired initial capacity; must be positive.
	 * @param collisionThreshold the collision threshold; must be positive.
	 * @return the new set instance.
	 */
	public static OffHeapLongHashSet New(final long desiredCapacity, final int collisionThreshold)
	{
		return new OffHeapLongHashSet(
			padCapacity(desiredCapacity),
			XMath.positive(collisionThreshold),
			deriveCollisionLimit(collisionThreshold),
			defaultExcessLimit()
		);
	}

	/**
	 * Creates a new {@link OffHeapLongHashSet} with fully custom configuration values.
	 *
	 * @param desiredCapacity    the desired initial capacity; must be positive. Rounded up to the next power of two.
	 * @param collisionThreshold the number of collisions during a single insertion tolerated before the excess counter is incremented; must be positive.
	 * @param collisionLimit     the hard per-insertion collision count that triggers a storage enlargement; must be positive.
	 * @param excessLimit        the number of excess incidents tolerated before a storage enlargement is triggered; must be positive.
	 * @return the new set instance.
	 */
	public static OffHeapLongHashSet New(
		final long desiredCapacity   ,
		final int  collisionThreshold,
		final int  collisionLimit    ,
		final int  excessLimit
	)
	{
		return new OffHeapLongHashSet(
			padCapacity(desiredCapacity),
			XMath.positive(collisionThreshold),
			XMath.positive(collisionLimit),
			XMath.positive(excessLimit)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	// configuration values.
	private final int collisionThreshold, collisionLimit, excessLimit; // It's a hash COLLISION! Not a "probe"!

	private ByteBuffer dbb;
	private long dbbBaseAddress;

	private long capacity ; // number of long value slots, always 2^n.
	private long hashRange; // shortcut for capacity - 1

	private boolean containsZero = false;
	private long    size         = 0;
	private int     excessCount  = 0;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	OffHeapLongHashSet(
		final long initialCapacity   ,
		final int  collisionThreshold,
		final int  collisionLimit    ,
		final int  excessLimit
	)
	{
		super();
		this.collisionThreshold = collisionThreshold;
		this.collisionLimit     = collisionLimit    ;
		this.excessLimit        = excessLimit       ;
		this.resize(initialCapacity);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	/**
	 * Returns whether this set contains the given value.
	 *
	 * @param value the value to look up.
	 * @return {@code true} if the value is contained, {@code false} otherwise.
	 */
	public boolean contains(final long value)
	{
		if(value == EMPTY)
		{
			return this.containsZero;
		}

		int collisions = 0;
		for(long i = this.hash(value); collisions < this.collisionLimit; i = this.advanceIndex(i))
		{
			final long slotValue = this.getValue(i);
			if(slotValue == EMPTY)
			{
				return false;
			}
			if(slotValue == value)
			{
				return true;
			}
			collisions++;
		}
		return false;
	}

	/**
	 * Returns whether this set contains all the given values.
	 *
	 * @param values the values to look up.
	 * @return {@code true} if every given value is contained, {@code false} if at least one is missing.
	 */
	public boolean containsAll(final long... values)
	{
		for(final long value : values)
		{
			if(!this.contains(value))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Inserts all given values into this set, skipping values that are already contained.
	 *
	 * @param values the values to insert.
	 * @return the number of values that were actually inserted (i.e. values not previously contained).
	 */
	public int putAll(final long... values)
	{
		int insertCount = 0;
		for(final long value : values)
		{
			if(this.put(value))
			{
				insertCount++;
			}
		}

		return insertCount;
	}
	/**
	 * Inserts the given value into this set if it is not already contained. If the configured
	 * collision thresholds are exceeded, the backing storage is enlarged and all contained
	 * values are rehashed before the insertion completes.
	 *
	 * @param value the value to insert.
	 * @return {@code true} if the value was inserted, {@code false} if it was already contained.
	 */
	public boolean put(final long value)
	{
		if(value == EMPTY)
		{
			return this.ensureInsertedZero();
		}

		/*!*\ NOTE:
		 * This algorithm does intentionally NOT use "tombstones"
		 * since the purpose of this implementation is only to collect and lookup.
		 * So if removal should be added, this must be enhanced accordingly.
		 * However, an enhanced version of this implementation should use
		 * a segmented chain hashing strategy instead, so using tombstones
		 * here is irrelevant anyway.
		 */

		// Outer loop: if a resize occurs during probing, the probe must restart with the new
		// hash range and a fresh collision count, otherwise the value could land in a slot
		// that is unreachable from hash(value) in the resized table (breaking future contains).
		while(true)
		{
			int collisions = 0;
			for(long i = this.hash(value); true; i = this.advanceIndex(i))
			{
				final long slotValue = this.getValue(i);
				if(slotValue == EMPTY)
				{
					this.insertAt(i, value); // empty slot found, insert and return.
					return true;
				}
				if(slotValue == value)
				{
					return false; // value already contained
				}
				if(this.checkForRebuild(++collisions))
				{
					break; // resize happened; restart probing from hash(value) for the new table.
				}
			}
		}
	}

	private long advanceIndex(final long index)
	{
		return index >= this.hashRange ? 0 : index + 1;
	}


	private long hash(final long value)
	{
		return calculateHashValue(value) & this.hashRange;
	}

	private void setValue(final long i, final long value)
	{
		XMemory.set_long(this.toAddress(i), value);
	}

	private long getValue(final long i)
	{
		return XMemory.get_long(this.toAddress(i));
	}

	private long toAddress(final long i)
	{
		return this.dbbBaseAddress + to_longByteSize(i);
	}

	private static long to_longByteSize(final long i)
	{
		return i << 3;
	}

	/**
	 * @return {@code true} if a resize was performed (caller must restart probing with a fresh hash),
	 *         {@code false} otherwise.
	 */
	private boolean checkForRebuild(final int collisions)
	{
		if(collisions < this.collisionThreshold)
		{
			return false; // Acceptable number of collisions.
		}

		if(collisions >= this.collisionLimit || ++this.excessCount >= this.excessLimit)
		{
			this.enlarge(); // Unacceptable number of collisions, enlarge storage.
			return true;
		}
		return false;
	}

	private boolean ensureInsertedZero()
	{
		if(this.containsZero)
		{
			return false; // zero already contained
		}

		this.containsZero = true;
		this.size++;

		return true;
	}

	private void insertAt(final long index, final long value)
	{
		this.setValue(index , value);
		this.size++;
	}

	private static long calculateHashValue(long v)
	{
		// Simple but reasonably good mixing for longs.
		v ^= v >>> 33;
		v *= 0xff51afd7ed558ccdL;
		v ^= v >>> 33;
		v *= 0xc4ceb9fe1a85ec53L;
		v ^= v >>> 33;

		return v;
	}



	///////////////////////////////////////////////////////////////////////////
	// resizing //
	/////////////

	private void enlarge()
	{
		this.resize(this.capacity << 1);
	}

	private void resize(final long newCapacity)
	{
		// DirectByteBuffer allocation
		final long newByteCapacity = to_longByteSize(newCapacity);
		final ByteBuffer newDbb = XMemory.allocateDirectNative(newByteCapacity);
		final long newBaseAddress = XMemory.getDirectByteBufferAddress(newDbb);
		XMemory.clearMemory(newBaseAddress, newByteCapacity);

		// rehash all existing elements
		if(this.size > 0 && this.dbb != null)
		{
			this.rebuildTo(newBaseAddress, newCapacity);
		}

		// DirectByteBuffer management
		XMemory.deallocateDirectByteBuffer(this.dbb); // explicit deallocation to free huge memory chunks before GC run.
		this.dbb = newDbb;
		this.dbbBaseAddress = newBaseAddress;

		// Updated logic state
		this.capacity = newCapacity;
		this.hashRange = newCapacity - 1;
		this.excessCount = 0;
	}

	private void rebuildTo(final long newBaseAddress, final long newCapacity)
	{
		for(long i = 0; i < this.capacity; i++)
		{
			final long value = this.getValue(i);
			if(value != EMPTY)
			{
				// All already contained values are guaranteed to fit nicely in an even bigger space.
				insertBlind(newBaseAddress, newCapacity, value);
			}
		}
	}

	private static void insertBlind(final long baseAddress, final long capacity, final long value)
	{
		final long hashRange = capacity - 1;
		for(long i = calculateHashValue(value) & hashRange; true; i = i >= hashRange ? 0 : i + 1)
		{
			final long current = XMemory.get_long(baseAddress + to_longByteSize(i));
			if(current == EMPTY)
			{
				XMemory.set_long(baseAddress + to_longByteSize(i), value);
				return;
			}
		}
	}



	/**
	 * Returns the number of values currently contained in this set.
	 *
	 * @return the current size.
	 */
	public long size()
	{
		return this.size;
	}

	/**
	 * Returns the current capacity of the backing off-heap storage, in number of {@code long} slots.
	 * The capacity is always a power of two and grows only via storage enlargement.
	 *
	 * @return the current capacity.
	 */
	public long capacity()
	{
		return this.capacity;
	}

	/**
	 * Returns the current load factor of this set, i.e. the ratio of {@linkplain #size() size}
	 * to {@linkplain #capacity() capacity}.
	 *
	 * @return a value in {@code [0.0, 1.0]}, or {@code 0.0} if the capacity is zero.
	 */
	public double currentLoad()
	{
		return this.capacity == 0 ? 0.0 : (double)this.size / this.capacity;
	}

}
