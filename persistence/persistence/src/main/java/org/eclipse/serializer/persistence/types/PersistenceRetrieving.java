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

import java.util.function.Consumer;

import org.eclipse.serializer.collections.Set_long;
import org.eclipse.serializer.reference.ObjectSwizzling;

/**
 * Application-facing read side of the persistence layer: "get" instances by id (or in bulk) without forcing
 * the caller to know whether the result was served from the in-memory registry or freshly loaded from disk.
 * Specializes {@link ObjectSwizzling} with collection helpers and a {@link #get()} convenience for the
 * configured root.
 * <p>
 * The naming choice is intentional &mdash; "get" rather than "load" &mdash; because the persistence layer
 * is designed around the assumption that the in-memory state is the authoritative one and the on-disk state
 * is just its persistent shadow. Concrete cache-bypassing "load" semantics are out of scope and would
 * require a different implementation.
 *
 * @see ObjectSwizzling
 * @see Persister
 */
public interface PersistenceRetrieving extends ObjectSwizzling
{
	/* Note on naming:
	 * The main use case on the application (business logic) level is to "get" instances.
	 * Wether they are cached or have to be loaded is a technical detail from this point of view.
	 * It may even be assumed to be the general case that a desired instance is cached and the
	 * retriever instance is simply the one getting it from the cache.
	 * Hence the generic and loading-independet naming "get".
	 *
	 * The use case of an intentionally cache-ignoring concrete "load" is not deemed relevant for application design
	 * but has to be implemented via a cache-ignoring implementation of this type.
	 * Design wise, it is assumed that in modern software development, the (server) memory always holds the
	 * business-logic-validated latest and relevant state of an instance and not some outside source (like a database).
	 * So, for example, the use case "I want to get the current state of the instance from the database
	 * in case it got updated there" is not relevant/possible by design since it would be a fatally bad architecture
	 * to allow modifications that bypass the application logic (its validation etc.).
	 */

	/**
	 * Returns the configured root instance, loading the entry graph if necessary.
	 *
	 * @return the root instance, or {@code null} if no root is set.
	 */
	public Object get();

	@Override
	public Object getObject(long objectId);

	/**
	 * Collects the instances for every passed object id into {@code collector}. The order of the collected
	 * elements matches the order of {@code objectIds}.
	 *
	 * @param <C>       the collector type, returned for fluent chaining.
	 * @param collector the destination collector.
	 * @param objectIds the ids to resolve.
	 *
	 * @return the same collector that was passed in.
	 */
	public <C extends Consumer<Object>> C collect(C collector, long... objectIds);

	/**
	 * Bulk variant of {@link #collect(Consumer, long...)} accepting a {@link Set_long} of ids.
	 *
	 * @param <C>       the collector type, returned for fluent chaining.
	 * @param collector the destination collector.
	 * @param objectIds the ids to resolve.
	 *
	 * @return the same collector that was passed in.
	 */
    public <C extends Consumer<Object>> C collect(C collector, Set_long objectIds);

//	public <T, C extends Collector<? super T>> C collectByType(C collector, Class<T> type);

}
