package org.eclipse.serializer.persistence.binary.java.util;

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
	private static long fieldOffset_words;

	public static BinaryHandlerBitSet New()
	{
		fieldOffset_sizeIsSticky = XMemory.objectFieldOffset(XReflect.getAnyField(BitSet.class, "sizeIsSticky"));
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
	public void updateState(Binary data, BitSet instance, PersistenceLoadHandler handler)
	{
		// no-op
	}

	@Override
	public void store(Binary data, BitSet instance, long objectId, PersistenceStoreHandler<Binary> handler)
	{
		long entityContentLenght = BINARY_OFFSET_WORDS + Binary.toBinaryListTotalByteLength(instance.size() / BYTES_PER_WORD);
				
		data.storeEntityHeader(entityContentLenght, this.typeId(), objectId);
		
		data.store_boolean(
			BINARY_OFFSET_SIZE_IS_STICKY,
			XMemory.get_boolean(instance, fieldOffset_sizeIsSticky));
		
		data.store_longs(
			(long[]) XMemory.getObject(instance, fieldOffset_words),
			BINARY_OFFSET_WORDS);
				
	}

	@Override
	public BitSet create(Binary data, PersistenceLoadHandler handler)
	{
		BitSet instance = BitSet.valueOf(data.build_longs(BINARY_OFFSET_WORDS));
		XMemory.set_boolean(instance, fieldOffset_sizeIsSticky, data.read_boolean(BINARY_OFFSET_SIZE_IS_STICKY));
		
		return instance;
	}

}
