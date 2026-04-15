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

import org.eclipse.serializer.math.XMath;
import org.eclipse.serializer.typing.Composition;

/**
 * Primitive (read: fast) hash map implementation that maps {@code long} keys to {@code long} values
 * without any boxing overhead.
 *
 * @see HashMapIdObject
 */
public final class HashMapIdId implements Composition
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static Entry[] newHashSlots(final int length)
	{
		return new Entry[length];
	}

	public static HashMapIdId New()
	{
		return new HashMapIdId(1);
	}

	public static HashMapIdId New(final int initialSlotLength)
	{
		return new HashMapIdId(XMath.pow2BoundCapped(initialSlotLength));
	}


	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private Entry[] hashSlots;
	private int     hashRange;
	private int     size     ;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	HashMapIdId(final int initialSlotLength)
	{
		super();
		this.hashSlots = newHashSlots(initialSlotLength);
		this.hashRange = initialSlotLength - 1;
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public final long size()
	{
		return this.size;
	}

	public final boolean isEmpty()
	{
		return this.size == 0;
	}

	@SuppressWarnings("unchecked")
	private void rebuild()
	{
		final int newModulo;
		final Entry[] newSlots = new Entry[(newModulo = (this.hashRange + 1 << 1) - 1) + 1];
		for(Entry entry : this.hashSlots)
		{
			for(Entry next; entry != null; entry = next)
			{
				next = entry.link;
				entry.link = newSlots[(int)(entry.id & newModulo)];
				newSlots[(int)(entry.id & newModulo)] = entry;
			}
		}
		this.hashSlots = newSlots;
		this.hashRange = newModulo;
	}

	private void putEntry(final int index, final Entry entry)
	{
		this.hashSlots[index] = entry;
		if(++this.size >= this.hashRange)
		{
			this.rebuild();
		}
	}

	/**
	 * Associates the specified value with the specified key.
	 * If the key was already present, the old value is replaced.
	 *
	 * @param id    the key
	 * @param value the value to associate
	 * @return {@code true} if this was a new key, {@code false} if an existing key was replaced
	 */
	public final boolean put(final long id, final long value)
	{
		final int index;
		Entry entry;
		if((entry = this.hashSlots[index = (int)(id & this.hashRange)]) == null)
		{
			this.putEntry(index, new Entry(id, value));
			return true;
		}

		do
		{
			if(entry.id == id)
			{
				entry.value = value;
				return false;
			}
		}
		while((entry = entry.link) != null);

		this.putEntry(index, new Entry(id, value, this.hashSlots[index]));
		return true;
	}

	/**
	 * Returns the value associated with the specified key,
	 * or the provided {@code notFoundValue} if the key is not present.
	 *
	 * @param id            the key to look up
	 * @param notFoundValue the value to return when the key is absent
	 * @return the associated value, or {@code notFoundValue}
	 */
	public final long get(final long id, final long notFoundValue)
	{
		for(Entry entry = this.hashSlots[(int)(id & this.hashRange)]; entry != null; entry = entry.link)
		{
			if(entry.id == id)
			{
				return entry.value;
			}
		}
		return notFoundValue;
	}

	public final void clear()
	{
		final Entry[] slots = this.hashSlots;
		for(int i = 0, len = slots.length; i < len; i++)
		{
			slots[i] = null;
		}
		this.size = 0;
	}


	static final class Entry
	{
		final long id   ;
		long       value;
		Entry      link ;

		Entry(final long id, final long value, final Entry link)
		{
			super();
			this.id    = id   ;
			this.value = value;
			this.link  = link ;
		}

		Entry(final long id, final long value)
		{
			super();
			this.id    = id   ;
			this.value = value;
			this.link  = null ;
		}
	}

}
