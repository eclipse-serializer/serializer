package org.eclipse.serializer.persistence.binary.jdk17.types;

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

import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.persistence.binary.jdk17.java.util.BinaryHandlerImmutableCollectionsList12;
import org.eclipse.serializer.persistence.binary.jdk17.java.util.BinaryHandlerImmutableCollectionsSet12;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustom;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerRegistration;
import org.eclipse.serializer.util.X;

/**
 * Utility class for registering all JDK 17 type handlers.
 */
public final class BinaryHandlersJDK17
{

	/**
	 * Registers all JDK 17 type handlers.
	 * @param executor the executor to register the type handlers with
	 * @return the executor
	 * @param <F> the type of the executor
	 */
	public static <F extends PersistenceTypeHandlerRegistration.Executor<Binary>> F registerJDK17TypeHandlers(final F executor)
	{
		executor.executeTypeHandlerRegistration((r, c) -> r.registerTypeHandlers(jdk17TypeHandlers()));

		return executor;
	}

	/**
	 * Returns a collection of all JDK 17 type handlers.
	 *
	 * @return the collection of JDK 17 type handlers.
	 */
	public static XGettingCollection<AbstractBinaryHandlerCustom<? extends Object>> jdk17TypeHandlers()
	{
		return X.List(
			BinaryHandlerImmutableCollectionsSet12.New(),
			BinaryHandlerImmutableCollectionsList12.New()
		);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 *
	 * @throws UnsupportedOperationException when called
	 */
	protected BinaryHandlersJDK17()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
