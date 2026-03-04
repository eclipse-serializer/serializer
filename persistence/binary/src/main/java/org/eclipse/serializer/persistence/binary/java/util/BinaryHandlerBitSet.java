package org.eclipse.serializer.persistence.binary.java.util;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
 * %%
 * Copyright (C) 2023 - 2025 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.util.BitSet;

import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomNonReferential;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.reflect.XReflect;

public class BinaryHandlerBitSet extends AbstractBinaryHandlerCustomNonReferential<BitSet>
{
	private static final int  BYTES_PER_WORD               = 8;
	private static final long BINARY_OFFSET_SIZE_IS_STICKY = 0;
	private static final long BINARY_OFFSET_WORDS          = BINARY_OFFSET_SIZE_IS_STICKY + Byte.BYTES;
	
	private static long fieldOffset_sizeIsSticky;
	private static long fieldOffset_wordsInUse;
	private static long fieldOffset_words;

	public static BinaryHandlerBitSet New()
	{
		fieldOffset_sizeIsSticky = XMemory.objectFieldOffset(XReflect.getAnyField(BitSet.class, "sizeIsSticky"));
		fieldOffset_wordsInUse   = XMemory.objectFieldOffset(XReflect.getAnyField(BitSet.class, "wordsInUse"));
		fieldOffset_words        = XMemory.objectFieldOffset(XReflect.getAnyField(BitSet.class, "words"));
					
		return new BinaryHandlerBitSet();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected BinaryHandlerBitSet() {
		super(BitSet.class,
			CustomFields(
				CustomField(int.class, "wordsInUse"),
				CustomField(boolean.class, "sizeIsSticky"),
				Complex(
					"words",
					CustomField(long.class, "word")
				)
			)
		);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary data, final BitSet instance, final long objectId, final PersistenceStoreHandler<Binary> handler)
	{
		final long entityContentLength = BINARY_OFFSET_WORDS +
			Binary.toBinaryListTotalByteLength(instance.size() / BYTES_PER_WORD)
		;
				
		data.storeEntityHeader(entityContentLength, this.typeId(), objectId);
		
		data.store_boolean(
			BINARY_OFFSET_SIZE_IS_STICKY,
			XMemory.get_boolean(instance, fieldOffset_sizeIsSticky));
		
		data.store_longs(
			(long[]) XMemory.getObject(instance, fieldOffset_words),
			BINARY_OFFSET_WORDS);
				
	}

	@Override
	public BitSet create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new BitSet();
	}

	@Override
	public void updateState(final Binary data, final BitSet instance, final PersistenceLoadHandler handler)
	{
		final long[] words = data.build_longs(BINARY_OFFSET_WORDS);

		int wordsInUse = words.length;
		while(wordsInUse > 0 && words[wordsInUse - 1] == 0)
		{
			wordsInUse--;
		}

		XMemory.setObject(instance, fieldOffset_words, words);
		XMemory.set_int(instance, fieldOffset_wordsInUse, wordsInUse);
		XMemory.set_boolean(instance, fieldOffset_sizeIsSticky, data.read_boolean(BINARY_OFFSET_SIZE_IS_STICKY));
	}

}
