package org.eclipse.serializer.persistence.binary.java.nio.file;

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

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.serializer.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;


// this is an "abstract type" TypeHandler that handles all classes implementing Path as Path, not as the actual class.
public final class BinaryHandlerPath extends AbstractBinaryHandlerCustomValueVariableLength<Path, String>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerPath New()
	{
		return new BinaryHandlerPath();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerPath()
	{
		super(
			Path.class,
			CustomFields(
				chars("uri")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static String instanceState(final Path instance)
	{
		return instance.toUri().toString();
	}
	
	private static String binaryState(final Binary data)
	{
		return data.buildString();
	}

	@Override
	public void store(
		final Binary                          data    ,
		final Path                            instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// uri starts with a schema specification that basically defines the type/implementation of the path.
		data.storeStringSingleValue(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public Path create(final Binary data, final PersistenceLoadHandler handler)
	{
		// the URI schema is responsible to trigger the correct resolving and produce an instance of the right type.
		return Paths.get(URI.create(binaryState(data)));
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	@Override
	public String getValidationStateFromInstance(final Path instance)
	{
		return instanceState(instance);
	}

	@Override
	public String getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

}
