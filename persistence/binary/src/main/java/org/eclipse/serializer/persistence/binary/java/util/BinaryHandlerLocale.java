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

import java.util.Locale;

import org.eclipse.serializer.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerLocale extends AbstractBinaryHandlerCustomValueVariableLength<Locale, String>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerLocale New()
	{
		return new BinaryHandlerLocale();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerLocale()
	{
		super(
			Locale.class,
			CustomFields(
				chars("languageTag") // a little content hint down to the type dictionary level.
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static String instanceState(final Locale instance)
	{
		return instance.toLanguageTag();
	}
	
	private static String binaryState(final Binary data)
	{
		return data.buildString();
	}

	@Override
	public final void store(
		final Binary                          data    ,
		final Locale                          instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// for once, they managed to do a kind of proper de/serialization logic.
		data.storeStringSingleValue(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public Locale create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		return Locale.forLanguageTag(binaryState(data));
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	@Override
	public String getValidationStateFromInstance(final Locale instance)
	{
		return instanceState(instance);
	}
	
	@Override
	public String getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

}
