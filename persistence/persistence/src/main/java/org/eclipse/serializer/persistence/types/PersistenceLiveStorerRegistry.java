package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
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

import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.HashEnum;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public interface PersistenceLiveStorerRegistry extends PersistenceStorer.CreationObserver
{
	@Override
	public default void observeCreatedStorer(final PersistenceStorer storer)
	{
		this.registerStorer(storer);
	}

	public void registerStorer(PersistenceStorer storer);

	public boolean clearGroupAndAdvance(long oldGroupId, long newGroupId);



	public static PersistenceLiveStorerRegistry New()
	{
		return new Default();
	}

	public final class Default implements PersistenceLiveStorerRegistry
	{
		private final static Logger logger = Logging.getLogger(PersistenceLiveStorerRegistry.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final EqHashTable<Long, HashEnum<PersistenceStorer>> storerGroups = EqHashTable.New();
		private long currentGroupId;



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
		public synchronized void registerStorer(final PersistenceStorer storer)
		{
			HashEnum<PersistenceStorer> storerGroup = this.storerGroups.get(this.currentGroupId);
			if(storerGroup == null)
			{
				this.storerGroups.add(this.currentGroupId, storerGroup = HashEnum.New());
			}

			logger.debug("Registering storer " + XChars.systemString(storer) + " to id Group " + this.currentGroupId);
			
			storerGroup.add(storer);
		}

		@Override
		public synchronized boolean clearGroupAndAdvance(final long oldGroupId, final long newGroupId)
		{
			final long removeCount = this.storerGroups.removeBy(e -> e.key() <= oldGroupId);

			logger.debug(Thread.currentThread() + " removed " + removeCount + " idGroups with id <= " + oldGroupId + ".");

			this.currentGroupId = newGroupId;

			return removeCount > 0;
		}

	}

}
