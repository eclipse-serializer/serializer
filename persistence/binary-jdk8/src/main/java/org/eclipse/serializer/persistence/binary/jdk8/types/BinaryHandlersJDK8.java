package org.eclipse.serializer.persistence.binary.jdk8.types;

/*-
 * #%L
 * Eclipse Serializer Persistence JDK8
 * %%
 * Copyright (C) 2023 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerArrayList;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerHashMap;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerHashSet;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerHashtable;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerLinkedHashMap;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerLinkedHashSet;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerProperties;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerStack;
import org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerVector;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerRegistration;

public final class BinaryHandlersJDK8
{
	public static <F extends PersistenceTypeHandlerRegistration.Executor<Binary>> F registerJDK8TypeHandlers(final F executor)
	{
		executor.executeTypeHandlerRegistration((r, c) ->
			r.registerTypeHandlers(X.List(
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
			))
		);
		
		return executor;
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
