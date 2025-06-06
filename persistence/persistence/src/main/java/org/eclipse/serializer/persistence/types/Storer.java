package org.eclipse.serializer.persistence.types;

import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionConsistencyObject;

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

/**
 * A type extending the simple {@link PersistenceStoring} to enable stateful store handling.
 * This can be used to do what is generally called "transactions": preprocess data to be stored and then store
 * either all or nothing.<br>
 * It can also be used to skip certain references. See {@link #skip(Object)}<br>
 * The deviating naming (missing "Persistence" prefix) is intentional to support convenience
 * on the application code level.
 *
 * 
 */
public interface Storer extends PersistenceStoring
{
	/**
	 * Ends the data collection process and causes all collected data to be persisted.
	 * <p>
	 * This is an atomatic all-or-nothing operation: either all collected data will be persisted successfully,
	 * or non of it will be persisted. Partially persisted data will be reverted / rolled back in case of a failure.
	 *
	 * @return some kind of status information, potentially null.
	 */
	public Object commit();

	/**
	 * Clears all internal state regarding collected data and/or registered skips.
	 */
	public void clear();

	/**
	 * Registers the passed {@literal instance} under the passed {@literal objectId} without persisting its data.
	 * <p>
	 * This skip means that if the passed {@literal instance} is encountered while collecting data to be persisted,
	 * its data will NOT be collected. References to the passed {@literal instance} will be persisted as the
	 * passed {@literal objectId}.
	 * <p>
	 * <u>Warning</u>:<br>
	 * This method can be very useful to rearrange object graphs on the persistence level, but it can also cause
	 * inconsistencies if not used perfectly correctly.<br>
	 * It is strongly advised to use one of the following alternatives instead:
	 * {@link #skip(Object)}
	 * {@link #skipNulled(Object)}
	 *
	 * @param instance the instance / reference to be skipped.
	 *
	 * @param objectId the objectId to be used as a reference to the skipped instance.
	 *
	 * @return {@literal true} if the instance has been newly registered, {@literal false} if it already was.
	 *
	 * @see #skip(Object)
	 * @see #skipNulled(Object)
	 */
	public boolean skipMapped(Object instance, long objectId);

	/**
	 * Registers the passed {@literal instance} to be skipped from the data persisting process.
	 * <p>
	 * This skip means that if the passed {@literal instance} is encountered while collecting data to be persisted,
	 * its data will NOT be collected. If the instance is already registered under a certain object id at the used
	 * {@link PersistenceObjectRegistry}, then is associated object id will be used. Otherwise, the null-Id will be
	 * used, effectively "nulling out" all references to this instance on the persistent level.<br>
	 * The latter behavior is exactly the same as {@link #skipNulled(Object)}.
	 *
	 * @param instance the instance / reference to be skipped.
	 *
	 * @return {@literal true} if the instance has been newly registered, {@literal false} if it already was.
	 *
	 * @see #skipNulled(Object)
	 * @see #skipMapped(Object, long)
	 */
	public boolean skip(Object instance);

	/**
	 * Registers the passed {@literal instance} to be skipped from the data persisting process.
	 * <p>
	 * This skip means that if the passed {@literal instance} is encountered while collecting data to be persisted,
	 * its data will NOT be collected. References to this instance will always be persisted as null, no matter if
	 * the instance is already registered for a certain object id at the used {@link PersistenceObjectRegistry}
	 * or not.<br>
	 * To make the skipping consider existing object id registrations, use {@link #skip(Object)}.
	 *
	 * @param instance the instance / reference to be skipped by using .
	 *
	 * @return {@literal true} if the instance has been newly registered, {@literal false} if it already was.
	 *
	 * @see #skip(Object)
	 * @see #skipMapped(Object, long)
	 */
	public boolean skipNulled(Object instance);

	/**
	 * @return the amount of unique instances / references that have already been registered by this
	 * {@link Storer} instance. This includes both instances encountered during the data collection process and
	 * instances that have explicitely been registered to be skipped.
	 *
	 * @see #skip(Object)
	 * @see #skipMapped(Object, long)
	 */
	public long size();

	/**
	 * Queries, whether this {@link Storer} instance has no instances / references registered.
	 * <p>
	 * Calling this method is simply an alias for {@code this.size() == 0L}.
	 *
	 * @return whether this {@link Storer} instance is empty.
	 */
	public default boolean isEmpty()
	{
		return this.size() == 0L;
	}

	/**
	 * Returns the internal state's value significant for its capacity of unique instances.
	 * Note that the exact meaning of this value is implementation dependant, e.g. it might just be a hash table's
	 * length, while the actual amount of unique instances that can be handled by that hash table might be
	 * much higher (infinite).
	 *
	 * @return the current implementation-specific "capacity" value.
	 */
	public long currentCapacity();

	/**
	 * The maximum value that {@link #currentCapacity()} can reach. For more explanation on the exact meaning of the
	 * capacity, see there.
	 *
	 * @return the maximum of the implementation-specific "capacity" value.
	 */
	public long maximumCapacity();

	/**
	 * Enforces the instance to be initialized, discarding any previous state (clearing it) if necessary.
	 *
	 * @return this.
	 */
	public Storer reinitialize();

	/**
	 * Enforces the instance to be initialized, discarding any previous state (clearing it) if necessary.
	 * 
	 * @param initialCapacity the amount of unique instances that this instance shall prepare to handle.
	 * @return this.
	 */
	public Storer reinitialize(long initialCapacity);

	/**
	 * Ensures that the instance's internal state is prepared for handling an amount of unique instance equal to
	 * the passed value. Note that is explicitly does not have to mean that the instance's internal state actually
	 * reserves as much space, only makes a best effort to prepare for that amount. Example: an internal hash table's
	 * hash length might still remain at 2^30, despite the passed value being much higher.
	 *
	 * @param desiredCapacity the amount of unique instances that this instance shall prepare to handle.
	 * @return this
	 */
	public Storer ensureCapacity(long desiredCapacity);

	
	public void registerCommitListener(PersistenceCommitListener listener);
		
	public void registerRegistrationListener(PersistenceObjectRegistrationListener listener);
	
	/**
	 * Stores the passed instance with the provided id and all referenced instances of persistable references recursively,
	 * but stores the passed instance and referenced instances only if they are newly encountered (e.g. don't have an id associated with
	 * them in the object registry, yet and are therefore required to be handled).
	 * <br><br>
	 * If the provided instance is allready persisted with an other id an {@link PersistenceExceptionConsistencyObject} exception
	 * will be thrown on commit.
	 * 
	 * @param instance the root instance of the subgraph of required instances to be stored.
	 * @param objectId the storage object id which shall be assigned to the passed instance.
	 * @return the object id representing the passed instance.
	 */
	public long store(Object instance, long objectId);

}
