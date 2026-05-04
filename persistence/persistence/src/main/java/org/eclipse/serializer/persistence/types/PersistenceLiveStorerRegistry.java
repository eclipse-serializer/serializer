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

/**
 * Registers every {@link PersistenceStorer} that is currently in flight, grouping them by an arbitrary
 * "group id" so the storage layer can later identify which storers belong to which logical batch (e.g. a
 * single transaction or commit window). As a {@link PersistenceStorer.CreationObserver}, it can be wired
 * directly into the storer creator pipeline to observe new storers as they are created.
 * <p>
 * {@link #clearGroupAndAdvance(long, long)} drops every group up to and including {@code oldGroupId} and
 * sets the current group to {@code newGroupId}, so subsequent {@link #registerStorer(PersistenceStorer)}
 * calls are recorded under the new group.
 *
 * @see PersistenceStorer
 * @see PersistenceStorer.CreationObserver
 */
public interface PersistenceLiveStorerRegistry extends PersistenceStorer.CreationObserver
{
	@Override
	public default void observeCreatedStorer(final PersistenceStorer storer)
	{
		this.registerStorer(storer);
	}

	/**
	 * Registers {@code storer} under the registry's current group id.
	 *
	 * @param storer the storer to register.
	 */
	public void registerStorer(PersistenceStorer storer);

	/**
	 * Drops every group with id {@code <= oldGroupId} and advances the current group to {@code newGroupId}.
	 *
	 * @param oldGroupId the highest id of the groups to be discarded (inclusive).
	 * @param newGroupId the new current group id under which subsequent registrations are recorded.
	 *
	 * @return {@code true} if at least one group was discarded.
	 */
	public boolean clearGroupAndAdvance(long oldGroupId, long newGroupId);



	/**
	 * Creates a new empty {@link Default} registry with current group id {@code 0}.
	 *
	 * @return the newly created registry.
	 */
	public static PersistenceLiveStorerRegistry New()
	{
		return new Default();
	}

	/**
	 * Default {@link PersistenceLiveStorerRegistry}. Stores groups in an ordered hash table keyed by group
	 * id; all public methods synchronize on the registry instance.
	 */
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
