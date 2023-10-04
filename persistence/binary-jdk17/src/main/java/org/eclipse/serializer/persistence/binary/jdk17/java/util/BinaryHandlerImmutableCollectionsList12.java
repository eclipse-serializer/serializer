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

import java.util.List;

/**
 * Specialized handler for immutable List implementations in Java 15 and later
 * as found in java.util.ImmutableCollections.List12<E>
 * <p></p>
 * the implementations are returned from List.of(), List.of(E e1) and  List.of(E e1, E e2)
 * <p></p>
 * The handler takes the internal constant java.util.ImmutableCollections.EMPTY
 * into account which must not be persisted.
 * 
 * @param <T> the handled type.
 */
public class BinaryHandlerImmutableCollectionsList12<T> extends AbstractBinaryHandlerGenericImmutableCollections12<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryHandlerImmutableCollectionsList12<?> New()
	{
		return new BinaryHandlerImmutableCollectionsList12<>(List.of(new Object()).getClass());
	}


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected BinaryHandlerImmutableCollectionsList12(final Class<T> type)
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
		return (T) List.of(new Object());
	}

}
