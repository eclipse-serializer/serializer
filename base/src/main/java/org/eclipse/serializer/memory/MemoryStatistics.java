
package org.eclipse.serializer.memory;

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

/**
 * 
 * 
 */
public interface MemoryStatistics
{
	/**
	 * Returns the maximum amount of memory in bytes that can be
	 * used for memory management.  This method returns <code>-1</code>
	 * if the maximum memory size is undefined.
	 *
	 * <p> This amount of memory is not guaranteed to be available
	 * for memory management if it is greater than the amount of
	 * committed memory.  The Java virtual machine may fail to allocate
	 * memory even if the amount of used memory does not exceed this
	 * maximum size.
	 *
	 * @return the maximum amount of memory in bytes;
	 * <code>-1</code> if undefined.
	 */
	public long max();
	
	/**
	 * Returns the amount of memory in bytes that is committed for
	 * the Java virtual machine to use.  This amount of memory is
	 * guaranteed for the Java virtual machine to use.
	 *
	 * @return the amount of committed memory in bytes.
	 *
	 */
	public long committed();
	
	/**
	 * Returns the amount of used memory in bytes.
	 *
	 * @return the amount of used memory in bytes.
	 *
	 */
	public long used();

	/**
	 * Returns the amount of used memory in bytes right after the most recent completed garbage collection.
	 * <p>
	 * Unlike {@link #used()}, this value does not contain the garbage allocated since that collection. Concurrent
	 * collectors (e.g. ZGC) let {@link #used()} climb up to the committed size before a cycle completes, although
	 * most of it is garbage.
	 * <p>
	 * Falls back to {@link #used()} if the value is not available.
	 *
	 * @return the amount of used memory in bytes after the last garbage collection.
	 */
	public default long usedAfterLastGc()
	{
		return this.used();
	}

	/**
	 * Returns the number of garbage collections completed so far, or <code>-1</code> if unknown.
	 * <p>
	 * Only meaningful for comparing two statistics instances: a higher value means that at least one
	 * collection completed in between.
	 *
	 * @return the number of completed garbage collections; <code>-1</code> if unknown.
	 */
	public default long gcCount()
	{
		return -1;
	}

	/**
	 * Returns the amount of memory in bytes used in the old (tenured) generation right after its most recent
	 * collection, or <code>-1</code> if unknown or not applicable.
	 * <p>
	 * Unlike the current usage, this value does not contain the garbage promoted since that collection: Parallel and
	 * Serial collect their old generation only in a full collection, so its current usage is mostly garbage shortly
	 * before one. Together with {@link #oldGenerationMax()}, it tells whether a fixed-size old generation is full of
	 * retained data long before the heap as a whole.
	 *
	 * @return the used old-generation memory in bytes after its last collection; <code>-1</code> if unknown.
	 */
	public default long oldGenerationUsedAfterLastGc()
	{
		return -1;
	}

	/**
	 * Returns the maximum size of the old (tenured) generation in bytes, or <code>-1</code> if unknown or not
	 * applicable.
	 *
	 * @return the maximum old-generation size in bytes; <code>-1</code> if unknown.
	 */
	public default long oldGenerationMax()
	{
		return -1;
	}


	public static MemoryStatistics New(
		final long   max      ,
		final long   committed,
		final long   used
	)
	{
		return New(max, committed, used, used, -1, -1, -1);
	}

	/**
	 * @param max                          see {@link #max()}
	 * @param committed                    see {@link #committed()}
	 * @param used                         see {@link #used()}
	 * @param usedAfterLastGc              see {@link #usedAfterLastGc()}
	 * @param gcCount                      see {@link #gcCount()}
	 * @param oldGenerationUsedAfterLastGc see {@link #oldGenerationUsedAfterLastGc()}
	 * @param oldGenerationMax             see {@link #oldGenerationMax()}
	 * @return a new statistics instance
	 */
	public static MemoryStatistics New(
		final long   max                         ,
		final long   committed                   ,
		final long   used                        ,
		final long   usedAfterLastGc             ,
		final long   gcCount                     ,
		final long   oldGenerationUsedAfterLastGc,
		final long   oldGenerationMax
	)
	{
		return new Default(max, committed, used, usedAfterLastGc, gcCount, oldGenerationUsedAfterLastGc, oldGenerationMax);
	}

	public static class Default implements MemoryStatistics
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final long   max                         ;
		private final long   committed                   ;
		private final long   used                        ;
		private final long   usedAfterLastGc             ;
		private final long   gcCount                     ;
		private final long   oldGenerationUsedAfterLastGc;
		private final long   oldGenerationMax            ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long   max                         ,
			final long   committed                   ,
			final long   used                        ,
			final long   usedAfterLastGc             ,
			final long   gcCount                     ,
			final long   oldGenerationUsedAfterLastGc,
			final long   oldGenerationMax
		)
		{
			super();

			this.max                          = max                         ;
			this.committed                    = committed                   ;
			this.used                         = used                        ;
			this.usedAfterLastGc              = usedAfterLastGc             ;
			this.gcCount                      = gcCount                     ;
			this.oldGenerationUsedAfterLastGc = oldGenerationUsedAfterLastGc;
			this.oldGenerationMax             = oldGenerationMax            ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public long max()
		{
			return this.max;
		}
		
		@Override
		public long committed()
		{
			return this.committed;
		}
		
		@Override
		public long used()
		{
			return this.used;
		}

		@Override
		public long usedAfterLastGc()
		{
			return this.usedAfterLastGc;
		}

		@Override
		public long gcCount()
		{
			return this.gcCount;
		}

		@Override
		public long oldGenerationUsedAfterLastGc()
		{
			return this.oldGenerationUsedAfterLastGc;
		}

		@Override
		public long oldGenerationMax()
		{
			return this.oldGenerationMax;
		}

	}
	
}
