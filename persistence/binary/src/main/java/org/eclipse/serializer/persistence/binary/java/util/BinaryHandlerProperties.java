package org.eclipse.serializer.persistence.binary.java.util;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import java.util.Properties;

import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;


/**
 * This type handler cannot handle a {@link Properties}' defaults values.
 * They simply left no way to query the defaults of a certain {@link Properties}
 * instance.<p>
 * For a type handler that provides this functionality, albeit specific to JDK 8 (and higher but still compatible JDKs),
 * see {@literal org.eclipse.serializer.persistence.binary.jdk8.java.util.BinaryHandlerProperties}.
 *
 */
public final class BinaryHandlerProperties extends AbstractBinaryHandlerMap<Properties>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerProperties New()
	{
		return new BinaryHandlerProperties();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerProperties()
	{
		super(Properties.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final Properties create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new Properties();
	}
	
}
