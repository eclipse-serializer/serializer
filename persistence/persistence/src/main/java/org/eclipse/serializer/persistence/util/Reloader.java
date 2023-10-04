package org.eclipse.serializer.persistence.util;

import static org.eclipse.serializer.util.X.notNull;

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


import java.util.function.Consumer;

import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceManager;
import org.eclipse.serializer.reference.Swizzling;
import org.eclipse.serializer.util.traversing.ObjectGraphTraverser;


/**
 * Utility to reload objects or object graphs.
 * Reload means that the in-memory state of the objects is reset to the state of the underlying storage.
 * <p>
 * Usage:
 * <pre>
 * EmbeddedStorageManager storage = ...;
 * Reloader reloader = Reloader.New(storage.persistenceManager());
 * reloader.reloadFlat(obj); // reloads only the given object, but not its references
 * reloader.reloadDeep(obj); // reloads the complete object graph
 * </pre>
 */
public interface Reloader
{
	/**
	 * Resets the state of the given instance to the state of the underlying storage. But not its references.
	 * 
	 * @param <T> type of the instance
	 * @param instance the object to reload
	 * @return the reloaded object, or <code>null</code> if it was not found in the storage
	 */
	public <T> T reloadFlat(T instance);

	/**
	 * Resets the state of the given instance and all of its references to the state of the underlying storage.
	 * 
	 * @param <T> type of the instance
	 * @param instance the object to reload
	 * @return the reloaded object, or <code>null</code> if it was not found in the storage
	 */
	public <T> T reloadDeep(T instance);


	/**
	 * Pseudo-constructor method to create a new {@link Reloader}.
	 * 
	 * @param persistenceManager the persistence manager to reload from
	 * @return a newly created {@link Reloader}
	 */
	public static Reloader New(final PersistenceManager<?> persistenceManager)
	{
		return new Reloader.Default(
			notNull(persistenceManager)
		);
	}


	public static class Default implements Reloader
	{
		private final PersistenceManager<?> persistenceManager;

		Default(
			final PersistenceManager<?> persistenceManager
		)
		{
			super();
			this.persistenceManager = persistenceManager;
		}

		private Object reloadObject(
			final Object            instance,
			final PersistenceLoader loader
		)
		{
			final long oid;
			return instance != null
				&& Persistence.IdType.OID.isInRange(oid = this.persistenceManager.lookupObjectId(instance))
				? loader.getObject(oid)  // Reload persisted object
				: instance               // Null instance, or constant - nothing to reload
			;
		}

		@SuppressWarnings("unchecked") // type safety ensured by logic
		@Override
		public <T> T reloadFlat(final T instance)
		{
			notNull(instance);

			return (T)this.reloadObject(
				instance,
				this.persistenceManager.createLoader()
			);
		}

		@SuppressWarnings("unchecked") // type safety ensured by logic
		@Override
		public <T> T reloadDeep(final T instance)
		{
			notNull(instance);

			final long oid;
			if(Swizzling.isNotFoundId(oid = this.persistenceManager.lookupObjectId(instance)))
			{
				return null;
			}

			final PersistenceLoader loader = this.persistenceManager.createLoader();
			final Consumer<Object>  logic  = object -> this.reloadObject(object, loader);

			// reload references
			ObjectGraphTraverser.Builder()
				.modeFull()
				.acceptorLogic(logic)
				.buildObjectGraphTraverser()
				.traverse(instance)
			;

			// reload instance
			return (T)loader.getObject(oid);
		}

	}

}
