package org.eclipse.serializer.persistence.binary.java.util;

import java.lang.reflect.Field;
import java.util.BitSet;

import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomNonReferential;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.reflect.XReflect;

public class BinaryHandlerBitSet extends AbstractBinaryHandlerCustomNonReferential<BitSet>
{
	private static final long BINARY_OFFSET_WORDS_IN_USE   = 0;
	private static final long BINARY_OFFSET_SIZE_IS_STICKY = BINARY_OFFSET_WORDS_IN_USE + Integer.BYTES;
	private static final long BINARY_OFFSET_WORDS          = BINARY_OFFSET_SIZE_IS_STICKY + Byte.BYTES;
	
	private static long offsetWordsInUse;
	private static long offsetSizeIsSticky;
	private static Field fieldWords;

	public static BinaryHandlerBitSet New()
	{
		fieldWords = XReflect.getAnyField(BitSet.class, "words");
		XReflect.setAccessible(fieldWords);
		
		offsetWordsInUse   = XMemory.objectFieldOffset(XReflect.getAnyField(BitSet.class, "wordsInUse"));
		offsetSizeIsSticky = XMemory.objectFieldOffset(XReflect.getAnyField(BitSet.class, "sizeIsSticky"));
					
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
		int     wordsInUse = XMemory.get_int(instance, offsetWordsInUse);
		boolean isSticky   = XMemory.get_boolean(instance, offsetSizeIsSticky);
		long[]  words      = (long[]) XReflect.getFieldValue(fieldWords, instance);
						
		long wordsSize = Binary.toBinaryListTotalByteLength((long)words.length * Long.BYTES);
		long entityContentLenght = BINARY_OFFSET_WORDS + wordsSize;
				
		data.storeEntityHeader(entityContentLenght, this.typeId(), objectId);
		data.store_int(BINARY_OFFSET_WORDS_IN_USE, wordsInUse);
		data.store_boolean(BINARY_OFFSET_SIZE_IS_STICKY, isSticky);
		data.store_longs(words, BINARY_OFFSET_WORDS);
		
	}

	@Override
	public BitSet create(Binary data, PersistenceLoadHandler handler)
	{
		int     wordsInUse = data.read_int(BINARY_OFFSET_WORDS_IN_USE);
		boolean isSticky   = data.read_boolean(BINARY_OFFSET_SIZE_IS_STICKY);
		long[]  words      = data.build_longs(BINARY_OFFSET_WORDS);
				
		BitSet instance = BitSet.valueOf(words);
		
		XMemory.set_int(instance, offsetWordsInUse, wordsInUse);
		XMemory.set_boolean(instance, offsetSizeIsSticky, isSticky);
		
		return instance;
	}

}
