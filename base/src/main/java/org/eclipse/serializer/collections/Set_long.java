package org.eclipse.serializer.collections;

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

import org.eclipse.serializer.collections.interfaces.Sized;
import org.eclipse.serializer.functional._longIterable;
import org.eclipse.serializer.functional._longPredicate;
import org.eclipse.serializer.functional._longProcedure;
import org.eclipse.serializer.math.XMath;
import org.eclipse.serializer.typing.Composition;

public interface Set_long extends Composition, _longIterable, Sized
{
	public boolean add(long element);

	public boolean contains(long element);

	public void clear();

	public void truncate();

	public Set_long filter(_longPredicate selector);



	public static Set_long New()
	{
		return new Set_long.Default(
			Default.defaultSlotLength(),
			Default.defaultChainLength(),
			Default.defaultChainGrowthFactor()
		);
	}

	public static Set_long New(final int slotSize)
	{
		return new Set_long.Default(
			slotSize,
			Default.defaultChainLength(),
			Default.defaultChainGrowthFactor()
		);
	}

	public static Set_long New(
		final int   slotSize          ,
		final int   chainDefaultLength,
		final float chainGrowthFactor
	)
	{
		return new Set_long.Default(slotSize, chainDefaultLength, chainGrowthFactor);
	}


	public final class Default implements Set_long
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		/**
		 * Sentinel value used in the chain arrays to mark an empty slot.
		 * Since this collides with the legitimate value {@code 0L}, the presence of {@code 0L}
		 * as an actual element is tracked separately via {@link #containsZero}.
		 */
		private static final long EMPTY = 0L;



		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static int defaultSlotLength()
		{
			return 1;
		}

		public static int defaultChainLength()
		{
			return 1;
		}

		public static float defaultChainGrowthFactor()
		{
			// grow by 1 for small chains but grow bigger chains by 10%.
			return 1.1f;
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private long[][] hashSlots;
		private int      hashRange;
		private long     size     ;
		private boolean  containsZero;

		private final int chainInitialLength;
		private final float chainGrowthFactor;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final int slotSize, final int chainDefaultLength, final float chainGrowthFactor)
		{
			super();
			this.hashSlots = new long[(this.hashRange = XMath.pow2BoundCapped(slotSize) - 1) + 1][];
			this.chainInitialLength = XMath.positive(chainDefaultLength);
			// chainGrowthFactor below 1.0 would shrink the chain on enlargement; clamp to avoid pathological shrinking.
			this.chainGrowthFactor  = Math.max(1.0f, XMath.positive(chainGrowthFactor));
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		//////////////////////

		@Override
		public final long size()
		{
			return this.size;
		}

		@Override
		public final boolean isEmpty()
		{
			return this.size == 0;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private void rebuild(final int newLength)
		{
			if(newLength <= 0 || newLength == this.hashSlots.length)
			{
				return;
			}

			final int      newRange = newLength - 1;
			final long[][] oldSlots = this.hashSlots;
			final long[][] newSlots = new long[newLength][];

			// chopped nested for loops into separate methods for JIT'ability.
			for(int i = 0; i < oldSlots.length; i++)
			{
				if(oldSlots[i] == null)
				{
					continue;
				}
				this.redistributeElements(newSlots, newRange, oldSlots[i]);
			}

			this.hashSlots = newSlots;
			this.hashRange = newRange;
		}

		private void redistributeElements(final long[][] newSlots, final int newRange, final long[] oldChain)
		{
			for(final long element : oldChain)
			{
				if(element == EMPTY)
				{
					// elements are packed from index 0; a sentinel means the rest is empty.
					break;
				}
				this.addElement(newSlots, hash(element,  newRange), element);
			}
		}

		private static int hash(final long element, final int hashRange)
		{
			// MurmurHash3 64-bit finalizer (fmix64) by Austin Appleby — diffuses every input bit
			// into every output bit so the low bits used for bucketing don't mirror the raw input.
			// See https://github.com/aappleby/smhasher/blob/master/src/MurmurHash3.cpp (fmix64).
			long v = element;
			v ^= v >>> 33;
			v *= 0xff51afd7ed558ccdL;
			v ^= v >>> 33;
			v *= 0xc4ceb9fe1a85ec53L;
			v ^= v >>> 33;
			return (int)(v & hashRange);
		}

		private boolean addElement(final long[][] hashSlots, final int hashIndex, final long element)
		{
			final long[] chain;
			if((chain = hashSlots[hashIndex]) != null)
			{
				// normal case: search for an empty slot (EMPTY) in an existing chain
				for(int n = 0; n < chain.length; n++)
				{
					if(chain[n] == EMPTY)
					{
						// found empty slot after the last element
						chain[n] = element;
						return true;
					}
					if(chain[n] == element)
					{
						return false;
					}
				}

				// edge case: chain array is full. Enlarge and add element at the first free index.
				hashSlots[hashIndex] = this.enlargeChain(chain, element);
				return true;
			}

			// corner case: no chain at all, yet
			(hashSlots[hashIndex] = new long[this.chainInitialLength])[0] = element;
			return true;
		}

		private long[] enlargeChain(final long[] array, final long newElement)
		{
			final int newLength = (int)(array.length * this.chainGrowthFactor);

			final long[] newArray = new long[Math.max(newLength, array.length + 1)];
			System.arraycopy(array, 0, newArray, 0, array.length);
			newArray[array.length] = newElement;

			return newArray;
		}

		@Override
		public final boolean add(final long element)
		{
			if(element == EMPTY)
			{
				// EMPTY (= 0L) is the chain-array sentinel, so an actual 0L must be tracked separately.
				if(this.containsZero)
				{
					return false;
				}
				this.containsZero = true;
				this.size++;
				return true;
			}

			if(!this.addElement(this.hashSlots, hash(element, this.hashRange), element))
			{
				return false;
			}

			if(++this.size >= this.hashRange)
			{
				this.rebuild((int)(this.hashSlots.length * 2.0f));
			}

			return true;
		}

		@Override
		public final boolean contains(final long element)
		{
			if(element == EMPTY)
			{
				return this.containsZero;
			}

			final long[] chain = this.hashSlots[hash(element, this.hashRange)];
			if(chain == null)
			{
				return false;
			}
			for(final long e : chain)
			{
				if(e == element)
				{
					return true;
				}
				if(e == EMPTY)
				{
					// chain elements are packed from index 0; a sentinel means no further elements.
					return false;
				}
			}

			return false;
		}

		@Override
		public void iterate(final _longProcedure procedure)
		{
			if(this.containsZero)
			{
				procedure.accept(EMPTY);
			}
			final long[][] hashSlots = this.hashSlots;
			for(int i = 0; i < hashSlots.length; i++)
			{
				if(hashSlots[i] == null)
				{
					continue;
				}
				for(final long e : hashSlots[i])
				{
					if(e == EMPTY)
					{
						break;
					}
					procedure.accept(e);
				}
			}
		}

		/**
		 * Optimizes the internal storage and returns the remaining number of entries.
		 * @return the number of entries after the optimization has been completed.
		 */
		public long optimize()
		{
			// +1 so that an exact power-of-two size doesn't leave us at size >= hashRange
			// and trigger an immediate re-enlarge on the next add.
			final long desired = this.size + 1L;
			final int targetLength = desired >= Integer.MAX_VALUE
				? Integer.MAX_VALUE
				: XMath.pow2BoundCapped((int)desired);
			this.rebuild(targetLength);
			return this.size;
		}

		@Override
		public void clear()
		{
			final long[][] slots = this.hashSlots;
			for(int i = 0, len = slots.length; i < len; i++)
			{
				slots[i] = null;
			}
			this.size = 0;
			this.containsZero = false;
		}

		@Override
		public void truncate()
		{
			this.hashSlots    = new long[1][];
			this.hashRange    = 0;
			this.size         = 0;
			this.containsZero = false;
		}

		@Override
		public Set_long.Default filter(final _longPredicate selector)
		{
			// Seed with the current slot count so a large source set does not cause log(n) rebuilds
			// during the filter pass.
			final Set_long.Default result = new Set_long.Default(
				this.hashSlots.length, this.chainInitialLength, this.chainGrowthFactor
			);

			this.iterate(e ->
			{
				if(selector.test(e))
				{
					result.add(e);
				}
			});

			return result;
		}

	}

}
