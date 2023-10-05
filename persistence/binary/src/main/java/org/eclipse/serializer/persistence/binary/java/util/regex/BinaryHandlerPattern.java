package org.eclipse.serializer.persistence.binary.java.util.regex;

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

import java.util.regex.Pattern;

import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomValueVariableLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerPattern extends AbstractBinaryHandlerCustomValueVariableLength<Pattern, String>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static int binaryOffsetFlags()
	{
		// first value, hence offset 0.
		return 0;
	}
	
	public static int binaryOffsetPatternString()
	{
		// flags int requires one int of binary space.
		return Integer.BYTES;
	}
	
	
	
	public static BinaryHandlerPattern New()
	{
		return new BinaryHandlerPattern();
	}
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerPattern()
	{
		super(
			Pattern.class,
			CustomFields(
				CustomField(int.class, "flags"),
				chars("pattern")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary data    ,
		final Pattern                         instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		final char[] patternChars = XChars.readChars(instance.pattern());
		
		// content length is flags int plus pattern chars.
		data.storeEntityHeader(
			Integer.BYTES + Binary.calculateBinaryLengthChars(patternChars.length),
			this.typeId(),
			objectId
		);
		data.store_int(binaryOffsetFlags(), instance.flags());
		data.storeStringValue(binaryOffsetPatternString(), patternChars);
	}

	@Override
	public Pattern create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		final String pattern = data.buildString(binaryOffsetPatternString());
		final int    flags   = data.read_int(binaryOffsetFlags());
		
		return Pattern.compile(pattern, flags);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	@Override
	public String getValidationStateFromInstance(final Pattern instance)
	{
		return stateString(instance.pattern(), instance.flags());
	}
	
	@Override
	public String getValidationStateFromBinary(final Binary data)
	{
		return stateString(
			data.buildString(binaryOffsetPatternString()),
			data.read_int(binaryOffsetFlags())
		);
	}
	
	private static String stateString(final String pattern, final int flags)
	{
		return "pattern = " + pattern + ", flags = " + flags;
	}

}
