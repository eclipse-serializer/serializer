package org.eclipse.serializer.communication.types;

/*-
 * #%L
 * Eclipse Serializer Communication Parent
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

import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTransfer;
import org.eclipse.serializer.persistence.types.PersistenceChannel;
import org.eclipse.serializer.persistence.types.PersistenceIdSet;

/**
 * 
 * @param <C> the communication layer type
 * @param <D> the data type
 */
public interface ComPersistenceChannel<C, D> extends PersistenceChannel<D>
{
	
	public abstract class Abstract<C, D> implements ComPersistenceChannel<C, D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final C connection;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final C connection)
		{
			super();
			this.connection = connection;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final C getConnection()
		{
			return this.connection;
		}
		
		protected abstract XGettingCollection<? extends D> internalRead(C connection)
			 throws PersistenceExceptionTransfer;
		
		protected abstract void internalWrite(C channel, D data)
			 throws PersistenceExceptionTransfer;

		@Override
		public XGettingCollection<? extends D> read() throws PersistenceExceptionTransfer
		{
			return this.internalRead(this.connection);
		}

		@Override
		public void write(final D data) throws PersistenceExceptionTransfer
		{
			this.internalWrite(this.connection, data);
		}
		
		@Override
		public synchronized void prepareChannel()
		{
			this.prepareSource();
			this.prepareTarget();
		}
		
		@Override
		public synchronized void closeChannel()
		{
			this.closeTarget();
			this.closeSource();
		}
		
		@Override
		public abstract void prepareSource();
		
		@Override
		public abstract void prepareTarget();
		
		@Override
		public abstract void closeSource();
		
		@Override
		public abstract void closeTarget();
		
		@Override
		public XGettingCollection<? extends D> readByObjectIds(final PersistenceIdSet[] objectIds)
			throws PersistenceExceptionTransfer
		{
			/* (08.08.2018 TM)NOTE:
			 * Makes sense in principle. One side of a network connection requests data specified by a set of OIDs.
			 * That also means that every batch of sent data must be inherently complete, i.e. may never trigger this
			 * method to request missing data for unresolvable OIDs.
			 * 
			 * However, such a function is not supported for the current simple proof-of-concept.
			 */
			
			// TODO NetworkPersistenceConnection<D>#readByObjectIds()
			throw new org.eclipse.serializer.meta.NotImplementedYetError();
		}
		
	}
	
}
