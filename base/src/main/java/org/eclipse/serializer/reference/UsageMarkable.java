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

        /*
         * Lazy-initialized field to ensure non-null instance after this instance has been restored
         * out of a persistent context.
         */
        private transient volatile HashEnum<Object> usageMarks;


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
            this.usageMarks().addAll(usageMarks);
        }



        ///////////////////////////////////////////////////////////////////////////
        // methods //
        ////////////

        private HashEnum<Object> usageMarks()
        {
            /*
             * Double-checked locking to reduce the overhead of acquiring a lock
             * by testing the locking criterion.
             * The field (this.usageMarks) has to be volatile.
             */
            HashEnum<Object> usageMarks = this.usageMarks;
            if(usageMarks == null)
            {
                synchronized(this)
                {
                    if((usageMarks = this.usageMarks) == null)
                    {
                        usageMarks = this.usageMarks = HashEnum.New();
                    }
                }
            }
            return usageMarks;
        }

        @Override
        public int markUsedFor(final Object instance)
        {
            // lock internal instance to avoid side effect deadlocks
            final HashEnum<Object> usageMarks;
            synchronized((usageMarks = this.usageMarks()))
            {
                final boolean added = usageMarks.add(instance);

                return usageMarks.intSize() * (added ? 1 : -1);
            }
        }

        @Override
        public int unmarkUsedFor(final Object instance)
        {
            // lock internal instance to avoid side effect deadlocks
            final HashEnum<Object> usageMarks;
            synchronized((usageMarks = this.usageMarks()))
            {
                final boolean removed = usageMarks.removeOne(instance);

                return usageMarks.intSize() * (removed ? 1 : -1);
            }
        }

        @Override
        public boolean isUsed()
        {
            // lock internal instance to avoid side effect deadlocks
            final HashEnum<Object> usageMarks;
            synchronized((usageMarks = this.usageMarks()))
            {
                return !usageMarks.isEmpty();
            }
        }

        @Override
        public int markUnused()
        {
            // lock internal instance to avoid side effect deadlocks
            final HashEnum<Object> usageMarks;
            synchronized((usageMarks = this.usageMarks()))
            {
                final int currentSize = usageMarks.intSize();
                usageMarks.clear();

                return currentSize;
            }
        }

        @Override
        public void accessUsageMarks(final Consumer<? super XGettingEnum<Object>> logic)
        {
            // lock internal instance to avoid side effect deadlocks
            final HashEnum<Object> usageMarks;
            synchronized((usageMarks = this.usageMarks()))
            {
                // no null check to give logic a chance to notice no-marks case.
                logic.accept(usageMarks);
            }
        }

    }
		
}
