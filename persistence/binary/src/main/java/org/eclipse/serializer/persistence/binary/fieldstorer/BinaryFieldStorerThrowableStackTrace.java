package org.eclipse.serializer.persistence.binary.fieldstorer;

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

import java.lang.reflect.Field;

import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryFieldStorer;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

/**
 * Custom {@link org.eclipse.serializer.persistence.binary.types.BinaryFieldStorer BinaryFieldStorer} for the
 * "stackTrace" field of {@link java.lang.Throwable Throwable}
 * 
 * This handler ensures that the throwables stacktrace is create before serialization.
 * 
 */
public class BinaryFieldStorerThrowableStackTrace implements BinaryFieldStorer<Throwable>
{
	private final Field field;
	
	public BinaryFieldStorerThrowableStackTrace(Field field)
	{
		this.field = field;
	}
	
	@Override
	public Field getField()
	{
		return this.field;
	}
	
	@Override
	public long storeValueFromMemory(Object source, long sourceOffset, long targetAddress,
		PersistenceStoreHandler<Binary> persister)
	{
		Throwable t = (Throwable)source;
		t.getStackTrace();
		XMemory.set_long(targetAddress, persister.apply(XMemory.getObject(source, sourceOffset)));
		return targetAddress + Binary.objectIdByteLength();
	}
	
}
