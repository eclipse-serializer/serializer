package org.eclipse.serializer.persistence.binary.jdk8.types;

/*-
 * #%L
 * Eclipse Serializer Persistence JDK8
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
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerArrayList;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerHashMap;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerHashSet;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerHashtable;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerLinkedHashMap;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerLinkedHashSet;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerProperties;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerStack;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerVector;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustom;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceSizedArrayLengthController;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerRegistration;
import org.eclipse.serializer.util.X;

public final class BinaryHandlersJDK8
{
	public static <F extends PersistenceTypeHandlerRegistration.Executor<Binary>> F registerJDK8TypeHandlers(final F executor)
	{
		executor.executeTypeHandlerRegistration((r, c) -> r.registerTypeHandlers(jdk8TypeHandlers(c)));
		
		return executor;
	}


	public static XGettingCollection<AbstractBinaryHandlerCustom<? extends Object>> jdk8TypeHandlers(final PersistenceSizedArrayLengthController c)
	{
		return X.List(
			// JDK 1.0 collections
			BinaryHandlerVector.New(c)      ,
			BinaryHandlerHashtable.New()    ,
			BinaryHandlerStack.New(c)       ,
			BinaryHandlerProperties.New()   ,

			// JDK 1.2 collections
			BinaryHandlerArrayList.New(c)   ,
			BinaryHandlerHashSet.New()      ,
			BinaryHandlerHashMap.New()      ,

			// JDK 1.4 collections
			BinaryHandlerLinkedHashMap.New(),
			BinaryHandlerLinkedHashSet.New()
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
	protected BinaryHandlersJDK8()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
