package org.eclipse.serializer.persistence.binary.jdk17.java.util;

/*-
 * #%L
 * Eclipse Serializer Persistence JDK17
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

import java.util.Set;

/**
 * Specialized handler for immutable Set implementations in Java 15 and later
 * as found in java.util.ImmutableCollections.Set12
 * <br><br>
 * the implementations are returned from Set.of(), Set.of(E e1) and  Set.of(E e1, E e2)
 * <br><br>
 * The handler takes the internal constant java.util.ImmutableCollections.EMPTY
 * into account which must not be persisted.
 * 
 * @param <T> the handled type.
 */
public class BinaryHandlerImmutableCollectionsSet12<T> extends AbstractBinaryHandlerGenericImmutableCollections12<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Create a new instance of the handler.
	 *
	 * @return the new instance.
	 */
	public static BinaryHandlerImmutableCollectionsSet12<?> New()
	{
		return new BinaryHandlerImmutableCollectionsSet12<>(Set.of(new Object()).getClass());
	}


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Constructor
	 *
	 * @param type the handled type.
	 */
	protected BinaryHandlerImmutableCollectionsSet12(final Class<T> type)
	{
		super(type);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@SuppressWarnings("unchecked")
	@Override
	protected T createInstance()
	{
		return (T) Set.of(new Object());
	}

}
