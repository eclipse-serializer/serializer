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
import org.eclipse.serializer.util.X;

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
	
	/**
	 * Returns whether at least one usage mark is currently registered.
	 * <p>
	 * A used-marked {@link Lazy} reference is exempt from evaluator-driven clearing
	 * (see {@link Lazy#clear(Lazy.ClearingEvaluator)}): the marks signal that the referent
	 * carries state that must not be dropped, e.g. changes that have not been persisted yet.
	 *
	 * @return <code>true</code> if any usage mark is registered, <code>false</code> otherwise.
	 */
	public boolean isUsed();

	/**
	 * Clears ALL usage marks.
	 * 
	 * @return the amount of removed marks.
	 */
	public int markUnused();

	/**
	 * Executes the passed logic with the internal usage marks collection.
	 * <p>
	 * <b>Lock-order constraint:</b> the logic runs while holding the internal marks monitor.
	 * It must not call back into synchronized methods of the marked instance (for a {@link Lazy},
	 * e.g. {@code peek()}, {@code get()}, {@code clear(...)}): evaluator-driven lazy clearing
	 * acquires the instance monitor first and queries {@link #isUsed()} (the marks monitor)
	 * second — a callback from here into the instance inverts that order and can deadlock.
	 *
	 * @param logic the logic to execute with the usage marks collection.
	 */
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
            // never marked: nothing to remove, nothing to allocate (see isUsed()).
            final HashEnum<Object> usageMarks = this.usageMarks;
            if(usageMarks == null)
            {
                return 0;
            }

            // lock internal instance to avoid side effect deadlocks
            synchronized(usageMarks)
            {
                final boolean removed = usageMarks.removeOne(instance);

                return usageMarks.intSize() * (removed ? 1 : -1);
            }
        }

        @Override
        public boolean isUsed()
        {
            /*
             * Deliberately NOT via usageMarks(): that would ALLOCATE the marks collection for
             * every instance this is called on. isUsed() is queried by evaluator-driven lazy
             * clearing for every stored+loaded reference in every check cycle - never-marked
             * instances (the vast majority) must answer false without any allocation.
             * The field is volatile: a null read means no mark was ever registered.
             */
            final HashEnum<Object> usageMarks = this.usageMarks;
            if(usageMarks == null)
            {
                return false;
            }

            // lock internal instance to avoid side effect deadlocks
            synchronized(usageMarks)
            {
                return !usageMarks.isEmpty();
            }
        }

        @Override
        public int markUnused()
        {
            // never marked: nothing to clear, nothing to allocate (see isUsed()).
            final HashEnum<Object> usageMarks = this.usageMarks;
            if(usageMarks == null)
            {
                return 0;
            }

            // lock internal instance to avoid side effect deadlocks
            synchronized(usageMarks)
            {
                final int currentSize = usageMarks.intSize();
                usageMarks.clear();

                return currentSize;
            }
        }

        @Override
        public void accessUsageMarks(final Consumer<? super XGettingEnum<Object>> logic)
        {
            /*
             * Never marked: the logic still gets a (shared immutable empty) collection so it can
             * notice the no-marks case, but the instance's own marks storage is not allocated.
             */
            final HashEnum<Object> usageMarks = this.usageMarks;
            if(usageMarks == null)
            {
                logic.accept(X.empty());
                return;
            }

            // lock internal instance to avoid side effect deadlocks
            synchronized(usageMarks)
            {
                logic.accept(usageMarks);
            }
        }

    }
		
}
