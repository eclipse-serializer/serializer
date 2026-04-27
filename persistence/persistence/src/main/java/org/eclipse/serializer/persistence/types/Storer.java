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

import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionConsistencyObject;

/**
 * A {@link PersistenceStoring} variant with stateful, transaction-like store handling.
 * <p>
 * Lifecycle of a {@link Storer} instance:
 * <ol>
 *   <li><b>Collect.</b> One or more {@code store(...)} / {@link #skip(Object)} /
 *       {@link #skipMapped(Object, long)} / {@link #skipNulled(Object)} calls accumulate state
 *       inside this {@link Storer} <i>without</i> writing anything to the underlying
 *       {@link PersistenceObjectRegistry} or persistent storage.</li>
 *   <li><b>Commit.</b> A single {@link #commit()} call atomically persists everything that has
 *       been accumulated. Either all of it is committed or none of it is.</li>
 *   <li><b>Reuse or discard.</b> After {@code commit()}, the storer can be reused
 *       ({@link #clear()} / {@link #reinitialize()}) or simply dropped.</li>
 * </ol>
 * The deviating naming (missing "Persistence" prefix) is intentional to support convenience on
 * the application code level.
 *
 * @see PersistenceStoring
 */
public interface Storer extends PersistenceStoring
{
	/**
	 * Ends the data collection process and persists all data accumulated by previous
	 * {@code store(...)} calls on this {@link Storer}.
	 * <p>
	 * This is an atomic all-or-nothing operation: either all collected data is persisted successfully,
	 * or none of it is. Partially persisted data is reverted on failure, leaving the underlying
	 * storage and the {@link PersistenceObjectRegistry} unchanged.
	 * <p>
	 * After {@code commit()} returns successfully, every instance accumulated by this storer has an
	 * objectId registered in the {@link PersistenceObjectRegistry}. Subsequent {@code store(...)}
	 * calls on those same instances by <i>any</i> storer will follow the lazy-default rule and skip
	 * them unless an eager strategy is used.
	 * <p>
	 * The method may be called at most once on a fresh storer state. To reuse the storer, call
	 * {@link #clear()} or {@link #reinitialize()} afterwards.
	 *
	 * @return implementation-specific status information about the commit, or {@code null} if the
	 *         implementation does not provide any.
	 *
	 * @see #clear()
	 * @see #reinitialize()
	 */
	public Object commit();

	/**
	 * Discards all accumulated state of this {@link Storer}: pending stores collected since the last
	 * {@code commit()} (or since construction) as well as any registered skips
	 * ({@link #skip(Object)} / {@link #skipMapped(Object, long)} / {@link #skipNulled(Object)}).
	 * <p>
	 * This rolls back uncommitted in-memory state only; data already committed by a previous
	 * {@link #commit()} call is unaffected. After {@code clear()} the storer is ready to start a new
	 * collection cycle.
	 *
	 * @see #commit()
	 * @see #reinitialize()
	 */
	public void clear();

	/**
	 * Registers the passed instance under the passed objectId without collecting its data for
	 * persistence.
	 * <p>
	 * If the instance is encountered while traversing references during the collection phase, its
	 * data is <b>not</b> collected; references to it are persisted using the passed
	 * {@code objectId} instead. This effectively redirects all references to the passed instance
	 * to whatever persistent record is associated with {@code objectId}.
	 * <p>
	 * <b>Warning.</b> This is a low-level mechanism that can be useful for rearranging object graphs
	 * on the persistence level (e.g. swapping the target of a reference, ID re-mapping during
	 * migration), but it can also create inconsistencies if the passed {@code objectId} does not
	 * point to a record of a compatible type. Prefer {@link #skip(Object)} or
	 * {@link #skipNulled(Object)} unless you specifically need ID-level remapping.
	 * <p>
	 * Skips are part of the storer's collection state and are discarded by {@link #clear()} or
	 * {@link #commit()}.
	 *
	 * @param instance the instance to be skipped.
	 * @param objectId the objectId to be used as a reference to the skipped instance.
	 *
	 * @return {@code true} if the instance was newly registered by this call, {@code false} if it
	 *         was already registered.
	 *
	 * @see #skip(Object)
	 * @see #skipNulled(Object)
	 */
	public boolean skipMapped(Object instance, long objectId);

	/**
	 * Registers the passed instance to be skipped during this storer's data collection.
	 * <p>
	 * A skipped instance encountered while traversing references is <b>not</b> collected for
	 * persistence. The reference itself is resolved as follows:
	 * <ul>
	 *   <li>If the instance is already registered for an objectId in the
	 *       {@link PersistenceObjectRegistry}, that existing objectId is used (i.e. the reference
	 *       still points to the previously persisted instance).</li>
	 *   <li>Otherwise, the null-id is used &mdash; effectively writing the reference as {@code null}
	 *       in the persistent form. This matches the behavior of {@link #skipNulled(Object)}.</li>
	 * </ul>
	 * Skips are part of the storer's collection state and are discarded by {@link #clear()} or
	 * {@link #commit()}.
	 *
	 * @param instance the instance to be skipped.
	 *
	 * @return {@code true} if the instance was newly registered as skipped by this call, {@code false}
	 *         if it was already registered.
	 *
	 * @see #skipNulled(Object)
	 * @see #skipMapped(Object, long)
	 */
	public boolean skip(Object instance);

	/**
	 * Registers the passed instance to be skipped during this storer's data collection, with all
	 * references to it persisted as {@code null}.
	 * <p>
	 * Unlike {@link #skip(Object)}, this method <b>ignores</b> any existing registration of the
	 * instance in the {@link PersistenceObjectRegistry}: references will be written as {@code null}
	 * unconditionally. Use {@link #skip(Object)} if you want existing object-id registrations to be
	 * honored.
	 * <p>
	 * Skips are part of the storer's collection state and are discarded by {@link #clear()} or
	 * {@link #commit()}.
	 *
	 * @param instance the instance to be skipped.
	 *
	 * @return {@code true} if the instance was newly registered as nulled by this call,
	 *         {@code false} if it was already registered.
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

	/**
	 * Registers a {@link PersistenceCommitListener} that will be notified when this storer's
	 * {@link #commit()} successfully completes.
	 * <p>
	 * Listeners are invoked synchronously on the thread that called {@code commit()} after all data
	 * has been persisted and the {@link PersistenceObjectRegistry} has been updated. They are
	 * therefore suitable for hooking post-commit work (cache invalidation, notifications, metrics)
	 * that must observe the committed state.
	 * <p>
	 * Listeners registered on this storer are scoped to this storer instance and are <i>not</i>
	 * carried over to other storers. They are retained across {@link #clear()} but discarded by
	 * {@link #reinitialize()}.
	 *
	 * @param listener the listener to register.
	 */
	public void registerCommitListener(PersistenceCommitListener listener);

	/**
	 * Registers a {@link PersistenceObjectRegistrationListener} that will be notified for every
	 * object that this storer registers in the {@link PersistenceObjectRegistry} during the
	 * collection phase.
	 * <p>
	 * The listener's {@code onObjectRegistration(long, Object)} method is called once per object as
	 * an objectId is assigned to it. This is useful for diagnostics, auditing, or building
	 * objectId &rarr; instance maps for committed graphs (see the EclipseStore Storage user docs
	 * &mdash; "Best Practice / Get objects that are persisted by a storer").
	 * <p>
	 * <b>Performance.</b> The listener is invoked synchronously inside the storer's hot path; a
	 * non-trivial implementation will measurably slow down the collection phase. Keep the callback
	 * cheap and non-blocking.
	 * <p>
	 * Listeners registered on this storer are scoped to this storer instance and are <i>not</i>
	 * carried over to other storers.
	 *
	 * @param listener the listener to register.
	 */
	public void registerRegistrationListener(PersistenceObjectRegistrationListener listener);

	/**
	 * {@inheritDoc}
	 * <p>
	 * On a {@link Storer}, this call <b>accumulates</b> the instance and its newly encountered
	 * references in this storer's internal state without committing anything. The actual persistence
	 * happens on {@link #commit()}; until then nothing is registered in the
	 * {@link PersistenceObjectRegistry} and nothing is written to the underlying storage layer.
	 * Calling {@code store(...)} on the same instance multiple times before {@code commit()} has no
	 * additional effect.
	 *
	 * @see #commit()
	 * @see #skip(Object)
	 */
	@Override
	public long store(Object instance);

	/**
	 * Accumulates the passed instance &mdash; together with all newly encountered referenced
	 * instances reachable from it &mdash; for persistence under the passed {@code objectId}.
	 * <p>
	 * Lazy-default semantics apply: only references not yet known to the
	 * {@link PersistenceObjectRegistry} are traversed; already-known references are skipped.
	 * <p>
	 * If the instance is already registered under a <i>different</i> objectId in the
	 * {@link PersistenceObjectRegistry}, a {@link PersistenceExceptionConsistencyObject} is thrown
	 * on {@link #commit()}, not from this method.
	 *
	 * @param instance the root instance of the sub-graph to be stored.
	 * @param objectId the objectId to be assigned to the passed instance on commit.
	 *
	 * @return the passed {@code objectId} (the same value that will be used on commit).
	 *
	 * @see #store(Object)
	 * @see #commit()
	 */
	public long store(Object instance, long objectId);

}
