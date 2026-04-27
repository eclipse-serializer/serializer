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

import org.eclipse.serializer.reference.ObjectSwizzling;

import java.time.Duration;

public interface Persister extends ObjectSwizzling, PersistenceStoring
{
	/**
	 * Returns the object instance associated with the passed objectId in this {@link Persister}'s
	 * {@link PersistenceObjectRegistry}, loading it from the persistent form if necessary.
	 * <p>
	 * The objectId is the value previously returned by {@link #store(Object)} (or by an earlier load
	 * of the same instance). For an unknown objectId the behavior is implementation-defined &mdash;
	 * typical implementations either return {@code null} or throw a persistence exception; consult
	 * the concrete implementation.
	 *
	 * @param objectId the objectId of the instance to retrieve.
	 *
	 * @return the instance associated with {@code objectId}.
	 *
	 * @see #store(Object)
	 */
	@Override
	public Object getObject(long objectId);

	/**
	 * Persists the passed object instance. The object's value fields are written and recursively
	 * referenced object instances are inspected as well.
	 * <p>
	 * <b>Lazy default semantics.</b> The default storer (see {@link #createLazyStorer()}) persists
	 * only referenced instances that are not yet known to this {@link Persister}'s
	 * {@link PersistenceObjectRegistry} (i.e. that do not yet have an objectId associated with them).
	 * Already-known references are <i>not</i> traversed and any modifications to their fields will
	 * <b>not</b> be picked up by this call. To persist modifications to an existing object, call
	 * {@code store(Object)} on that very object &mdash; the rule is: <i>"the object that has been
	 * modified has to be stored"</i>. To force recursive re-storing of already-known instances, use
	 * {@link #createEagerStorer()}.
	 * <p>
	 * <b>Atomicity.</b> A single {@code store(...)} call is atomic: either all data produced by it is
	 * committed, or none of it is. There are no partially applied stores.
	 * <p>
	 * <b>Durability.</b> When this {@link Persister} is backed by a persistent storage layer (e.g.
	 * EclipseStore Storage), the data written by this call is guaranteed to be physically committed
	 * by the time this method returns. A process crash before that point cannot leave the storage in
	 * an inconsistent state &mdash; the next start resumes from the last fully committed store.
	 * <p>
	 * <b>Concurrency.</b> {@code store(...)} does not synchronize the in-memory object graph for the
	 * caller. The mutation of an object and the corresponding {@code store(...)} call must be executed
	 * under the same lock as any concurrent reader or writer of the affected part of the graph.
	 * Otherwise other threads may observe partially modified state, or the persisted data may not
	 * reflect the intended change.
	 *
	 * @param instance the object instance to persist.
	 *
	 * @return the objectId associated with the passed instance. Stable across calls for the same
	 *         instance once registered.
	 *
	 * @see #storeAll(Object...)
	 * @see #storeAll(Iterable)
	 * @see #createLazyStorer()
	 * @see #createEagerStorer()
	 */
	@Override
	public long store(Object instance);

	/**
	 * Convenience overload that persists each of the passed instances using the same default-storer
	 * semantics as {@link #store(Object)}. The varargs array itself is <b>not</b> persisted &mdash;
	 * only its elements.
	 * <p>
	 * The whole call is atomic: either every element is committed, or none of them is.
	 * <p>
	 * See {@link #store(Object)} for the lazy-default rule, durability semantics, and the concurrency
	 * obligation that apply identically to each element.
	 *
	 * @param instances the object instances to persist.
	 *
	 * @return the objectIds of the passed instances, in the same order.
	 *
	 * @see #store(Object)
	 * @see #storeAll(Iterable)
	 */
	@Override
	public long[] storeAll(Object... instances);

	/**
	 * Convenience overload that persists each element produced by the passed {@link Iterable} using
	 * the same default-storer semantics as {@link #store(Object)}. The {@link Iterable} itself is
	 * <b>not</b> persisted &mdash; only its elements.
	 * <p>
	 * The whole call is atomic: either every iterated element is committed, or none of them is.
	 * <p>
	 * See {@link #store(Object)} for the lazy-default rule, durability semantics, and the concurrency
	 * obligation that apply identically to each element.
	 *
	 * @param instances the object instances to persist.
	 *
	 * @see #store(Object)
	 * @see #storeAll(Object...)
	 */
	@Override
	public void storeAll(Iterable<?> instances);


	/**
	 * Creates a new {@link Storer} that uses the <b>lazy</b> storing strategy.
	 * <p>
	 * Lazy semantics: a {@code store(...)} call on the returned {@link Storer} persists the passed
	 * instance and recursively traverses its references, but it persists only those referenced
	 * instances that are not yet known to this {@link Persister}'s {@link PersistenceObjectRegistry}.
	 * Already-known references are skipped &mdash; modifications to their fields are <i>not</i>
	 * persisted by such a call. To persist a modification, call {@code store(...)} on the modified
	 * object itself.
	 * <p>
	 * This is the most common storing strategy and the basis for the default returned by
	 * {@link #createStorer()}. Choose it for the typical case of "store an updated instance and any
	 * newly created instances reachable from it; leave already-stored references untouched".
	 *
	 * @return a new lazy {@link Storer}.
	 *
	 * @see #createStorer()
	 * @see #createEagerStorer()
	 */
	public Storer createLazyStorer();

	/**
	 * Creates a new {@link Storer} using the implementation's configured default storing strategy.
	 * <p>
	 * Unless explicitly overridden in the configuration, this returns a lazy storer equivalent to
	 * {@link #createLazyStorer()}. Use this method when you want to follow the project's chosen
	 * default; use {@link #createLazyStorer()} / {@link #createEagerStorer()} explicitly when you
	 * want a specific strategy regardless of configuration.
	 * <p>
	 * The returned {@link Storer} is independent of any other storer and must be {@code commit()}-ed
	 * to actually persist its accumulated state.
	 *
	 * @return a new {@link Storer} with the default strategy.
	 *
	 * @see #createLazyStorer()
	 * @see #createEagerStorer()
	 */
	public Storer createStorer();

	/**
	 * Creates a new {@link Storer} that uses the <b>eager</b> storing strategy.
	 * <p>
	 * Eager semantics: a {@code store(...)} call on the returned {@link Storer} persists the passed
	 * instance and recursively traverses its references, persisting <i>every</i> reachable instance
	 * &mdash; including ones already known to this {@link Persister}'s
	 * {@link PersistenceObjectRegistry}. This guarantees that field-level modifications anywhere in
	 * the traversed sub-graph are picked up, at the cost of writing data that may not have changed.
	 * <p>
	 * Use this strategy when you cannot conveniently call {@code store(...)} on individual modified
	 * objects (e.g. encapsulated objects without accessors), or when bulk-storing immutable
	 * sub-graphs. For finer control over which specific reference fields are evaluated eagerly,
	 * configure a {@code PersistenceEagerStoringFieldEvaluator} instead of switching the global
	 * strategy.
	 *
	 * @return a new eager {@link Storer}.
	 *
	 * @see #createStorer()
	 * @see #createLazyStorer()
	 */
	public Storer createEagerStorer();

	/**
	 * Creates a new {@link BatchStorer} that buffers store requests and commits them in batches
	 * under the control of the passed {@link BatchStorer.Controller}.
	 * <p>
	 * A {@link BatchStorer} is useful when many small store operations would otherwise each trigger
	 * an individual commit round-trip. The controller decides when an accumulated batch is committed
	 * (e.g. by element count or accumulated size); {@code checkInterval} bounds how long a pending
	 * batch may sit unflushed before the controller is consulted again.
	 * <p>
	 * The lazy-default semantics described on {@link #store(Object)} apply to each individual store
	 * request submitted to the {@link BatchStorer}. Atomicity holds <i>per committed batch</i>, not
	 * per individual {@code store(...)} call submitted to it.
	 *
	 * @param controller    decides when a buffered batch is committed.
	 * @param checkInterval maximum time between controller evaluations of the pending batch.
	 *
	 * @return a new {@link BatchStorer}.
	 *
	 * @see #createStorer()
	 * @see BatchStorer
	 */
	public default BatchStorer createBatchStorer(
		final BatchStorer.Controller controller   ,
		final Duration               checkInterval
	)
	{
		throw new UnsupportedOperationException(
			this.getClass().getName() + " does not support batch storing."
		);
	}

	/**
	 * Returns a fluent builder for creating a new {@link BatchStorer}. The builder promotes
	 * {@code maxSize}, {@code flushCycle} and {@code checkInterval} to top-level methods and
	 * hides the {@link BatchStorer.Controller} abstraction from user code.
	 *
	 * @return a new {@link BatchStorer.Builder} bound to this persister.
	 */
	public default BatchStorer.Builder batchStorerBuilder()
	{
		return BatchStorer.Builder(this);
	}

}
