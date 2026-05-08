package org.eclipse.serializer.persistence.binary.types;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.exceptions.BinaryPersistenceException;


/**
 * Walks a contiguous raw memory range containing concatenated binary items (entities and skip-comments)
 * and dispatches each entity to a {@link BinaryEntityRawDataAcceptor}. Items are length-prefixed: a
 * positive length marks an entity, a negative length is a comment to be skipped, and a zero length is
 * treated as a fatal corruption (it would otherwise hang the iteration).
 *
 * @see BinaryEntityRawDataAcceptor
 */
public interface BinaryEntityRawDataIterator
{
	/**
	 * Iterates entities in the range {@code [startAddress, boundAddress)}, forwarding each to
	 * {@code entityDataAcceptor}. Iteration stops early if the acceptor returns {@code false} or when the
	 * bound is reached.
	 *
	 * @param startAddress       the start address of the raw range.
	 * @param boundAddress       the exclusive bound address of the raw range.
	 * @param entityDataAcceptor the per-entity callback.
	 *
	 * @return the number of bytes left unprocessed at the end of the range (e.g. a partial trailing item).
	 */
	public long iterateEntityRawData(
		long                        startAddress      ,
		long                        boundAddress      ,
		BinaryEntityRawDataAcceptor entityDataAcceptor
	);


	/**
	 * @return a new default {@link BinaryEntityRawDataIterator}.
	 */
	public static BinaryEntityRawDataIterator New()
	{
		return new Default();
	}

	/**
	 * Default {@link BinaryEntityRawDataIterator} implementation that walks length-prefixed binary items.
	 */
	public final class Default implements BinaryEntityRawDataIterator
	{
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public long iterateEntityRawData(
			final long                        startAddress      ,
			final long                        boundAddress      ,
			final BinaryEntityRawDataAcceptor entityDataAcceptor
		)
		{
			// the loop condition must be safe to read the item length
			final long itemStartBoundAddress = boundAddress - Binary.lengthLength() + 1;
			
			long address = startAddress;
			try
			{
				while(address < itemStartBoundAddress)
				{
					final long itemLength = XMemory.get_long(address);
					if(itemLength > 0)
					{
						// if the logic did not accept the entity data, iteration is aborted at the start of that entity.
						if(!entityDataAcceptor.acceptEntityData(address, boundAddress))
						{
							break;
						}
						
						// otherwise, the iteration advances to the next item (comment or entity)
						address += itemLength;
					}
					else if(itemLength < 0)
					{
						// comments (indicated by negative length) just get skipped.
						address -= itemLength;
					}
					else
					{
						// entity length may never be 0 or the iteration will hang forever
						throw new BinaryPersistenceException("Zero length data item.");
					}
				}
			}
			catch(final Exception e)
			{
				throw new BinaryPersistenceException(
					"Exception at address offset " + (address - startAddress)
					+ " (bound offset = " + (boundAddress - startAddress) + ")"
					, e
				);
			}
			
			// the total length of processed items is returned so the calling context can validate/advance/etc.
			return boundAddress - address;
		}
		
	}
	
	/**
	 * @return a new default {@link Provider}.
	 */
	public static Provider Provider()
	{
		return new Provider.Default();
	}

	/**
	 * Pluggable factory for {@link BinaryEntityRawDataIterator} instances, kept on the foundation so the
	 * iteration strategy can be swapped without touching call sites.
	 */
	public interface Provider
	{
		/**
		 * @return a {@link BinaryEntityRawDataIterator} ready for use.
		 */
		public BinaryEntityRawDataIterator provideEntityDataIterator();

		/**
		 * Default {@link Provider} that yields a fresh {@link BinaryEntityRawDataIterator#New()} on each call.
		 */
		public final class Default implements Provider
		{
			@Override
			public BinaryEntityRawDataIterator provideEntityDataIterator()
			{
				return BinaryEntityRawDataIterator.New();
			}
			
		}
		
	}
		
}
