package org.eclipse.serializer.reference;

/*-
 * #%L
 * Eclipse Serializer Persistence
 * %%
 * Copyright (C) 2024 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.util.function.Consumer;

import org.eclipse.serializer.collections.HashEnum;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XGettingEnum;

public interface UsageMarkable
{
	/**
	 * Marks this instance as being in use for the passed instance.
	 * <p>
	 * Returns the amount of instances registered as usage marks AFTER the operation.
	 * This value is <b>positive</b> if the instance has been newly registered,
	 * <b>negative</b> if it already was registered, i.e. the operation had no effect.
	 * 
	 * @param instance the instance this instance is used/needed for.
	 * 
	 * @return the resulting amount of instances registered as usage marks with the sign indicating the operation's effect.
	 */
	public int markUsedFor(Object instance);
	
	/**
	 * Unmarks this instance as being in use for the passed instance.
	 * <p>
	 * Returns the amount of instances registered as usage marks AFTER the operation.
	 * This value is <b>positive</b> if the instance has been actually removed,
	 * <b>negative</b> if it was not registered in the first place, i.e. the operation had no effect.
	 * 
	 * @param instance the instance this instance is used/needed for.
	 * 
	 * @return the resulting amount of instances registered as usage marks with the sign indicating the operation's effect.
	 */
	public int unmarkUsedFor(Object instance);
	
	public boolean isUsed();
	
	/**
	 * Clears ALL usage marks.
	 * 
	 * @return the amount of removed marks.
	 */
	public int markUnused();

	public void accessUsageMarks(Consumer<? super XGettingEnum<Object>> logic);

	

	/**
	 * Alias for {@code instance.markUsedFor(instance)} when no distinction between different usage instances is required.
	 * 
	 * @return the resulting amount of instances registered as usage marks with the sign indicating the operation's effect.
	 */
	public default int markUsed()
	{
		return this.markUsedFor(this);
	}
	
	public static UsageMarkable New()
	{
		return new UsageMarkable.Default();
	}
	
	
	public class Default implements UsageMarkable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final transient HashEnum<Object> usageMarks = HashEnum.New();
		
				
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default()
		{
			super();
		}
		
		protected Default(final XGettingCollection<Object> usageMarks)
		{
			super();
			this.usageMarks.addAll(this.usageMarks);
		}
		
		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public int markUsedFor(final Object instance)
		{
			// lock internal instance to avoid side effect deadlocks
			synchronized(this.usageMarks)
			{
				final boolean added = this.usageMarks.add(instance);
				
				return this.usageMarks.intSize() * (added ? 1 : -1);
			}
		}
		
		@Override
		public int unmarkUsedFor(final Object instance)
		{
			// lock internal instance to avoid side effect deadlocks
			synchronized(this.usageMarks)
			{
				final boolean removed = this.usageMarks.removeOne(instance);
				
				return this.usageMarks.intSize() * (removed ? 1 : -1);
			}
		}
		
		@Override
		public boolean isUsed()
		{
			// lock internal instance to avoid side effect deadlocks
			synchronized(this.usageMarks)
			{
				return this.usageMarks != null && !this.usageMarks.isEmpty();
			}
		}

		@Override
		public int markUnused()
		{
			// lock internal instance to avoid side effect deadlocks
			synchronized(this.usageMarks)
			{
				final int currentSize = this.usageMarks.intSize();
				this.usageMarks.clear();
				
				return currentSize;
			}
		}

		@Override
		public void accessUsageMarks(final Consumer<? super XGettingEnum<Object>> logic)
		{
			// lock internal instance to avoid side effect deadlocks
			synchronized(this.usageMarks)
			{
				// no null check to give logic a chance to notice no-marks case.
				logic.accept(this.usageMarks);
			}
		}
	}
		
}
