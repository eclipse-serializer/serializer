package org.eclipse.serializer.nativememory;

/*-
 * #%L
 * Eclipse Serializer NativeMemory
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

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.serializer.collections.HashTable;
import org.eclipse.serializer.exceptions.InstantiationRuntimeException;
import org.eclipse.serializer.exceptions.MemoryException;
import org.eclipse.serializer.functional.DefaultInstantiator;
import org.eclipse.serializer.memory.MemoryAccessor;
import org.eclipse.serializer.memory.MemoryStatistics;
import org.eclipse.serializer.memory.sun.JdkInstantiatorBlank;
import org.eclipse.serializer.typing.XTypes;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;


/**
 * Java 22 and greater MemoryAccessor implementation.
 * This implementation relies on native code to replace
 * obsolete api from misc.unsafe.
 */
public class NativeMemoryAccessor implements MemoryAccessor
{
	///////////////////////////////////////////////////////////////////////////
	// static fields //
	//////////////////
	
	private final static Logger logger = Logging.getLogger(NativeMemoryAccessor.class);
	
	///////////////////////////////////////////////////////////////////////////
	// fields //
	///////////
	
	private final HashTable<Class<?>, Field[]> objectFieldsRegistry = HashTable.New();
	
	private final DefaultInstantiator classInstantiator = JdkInstantiatorBlank.New();
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public static MemoryAccessor New() {
		NativeLibraryJarLoader.loadNativeLibrary();
		return new NativeMemoryAccessor();
	}
	
	public static MemoryAccessor New(String nativeLibrary) {
		NativeLibraryJarLoader.loadNativeLibrary(nativeLibrary);
		return new NativeMemoryAccessor();
	}
			
	public NativeMemoryAccessor() {
		super();
	}


	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	private static void notImplemented() {
		throw new RuntimeException("not implemented");
	}
	
	///////////////////////////////////////////////////////////////////////////
	// helper methods //
	////////////

	@Override
	public void guaranteeUsability() {
		//no-op
	}

	///////////////////////////////////////////////////////////////////////////
	// ByteBuffer //
	///////////////

	@Override
	public ByteBuffer allocateDirectNative(final int capacity) {
		return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
	}

	@Override
	public ByteBuffer allocateDirectNative(final long capacity) {
		return ByteBuffer.allocateDirect((int)capacity).order(ByteOrder.nativeOrder());
	}
	
	@Override
	public native long getDirectByteBufferAddress(final ByteBuffer directBuffer);

	@Override
	public boolean deallocateDirectByteBuffer(final ByteBuffer directBuffer) {
		//notImplemented();
		return false;
	}

	@Override
	public boolean isDirectByteBuffer(final ByteBuffer byteBuffer) {
		return XTypes.isDirectByteBuffer(byteBuffer);
	}

	@Override
	public ByteBuffer guaranteeDirectByteBuffer(final ByteBuffer directBuffer) {
		return XTypes.guaranteeDirectByteBuffer(directBuffer);
	}

	///////////////////////////////////////////////////////////////////////////
	// primitive values getter //
	////////////////////////////
	
	@Override
	public native long allocateMemory(final long bytes);

	@Override
	public native long reallocateMemory(final long address, final long bytes);

	@Override
	public native void freeMemory(final long address);

	@Override
	public native void fillMemory(final long targetAddress, final long length, final byte value);

	@Override
	public native byte get_byte(final long address);

	@Override
	public native boolean get_boolean(final long address);

	@Override
	public native short get_short(final long address);

	@Override
	public native char get_char(final long address);

	@Override
	public native int get_int(final long address);

	@Override
	public native float get_float(final long address);

	@Override
	public native long get_long(final long address);

	@Override
	public native double get_double(final long address);
	
	@Override
	public native byte get_byte(final Object instance, final long offset);

	@Override
	public native boolean get_boolean(final Object instance, final long offset);
	
	@Override
	public native short get_short(final Object instance, final long offset);

	@Override
	public native char get_char(final Object instance, final long offset);

	@Override
	public native int get_int(final Object instance, final long offset);

	@Override
	public native float get_float(final Object instance, final long offset);

	@Override
	public native long get_long(final Object instance, final long offset);

	@Override
	public native double get_double(final Object instance, final long offset);
	
	@Override
	public native Object getObject(final Object instance, final long offset);

	
	///////////////////////////////////////////////////////////////////////////
	// primitive values setter //
	////////////////////////////
	
	@Override
	public native void set_byte(final long address, final byte value);

	@Override
	public native void set_boolean(final long address, final boolean value);

	@Override
	public native void set_short(final long address, final short value);

	@Override
	public native void set_char(final long address, final char value);

	@Override
	public native void set_int(final long address, final int value);

	@Override
	public native void set_float(final long address, final float value);

	@Override
	public native void set_long(final long address, final long value);

	@Override
	public native void set_double(final long address, final double value);

	@Override
	public native void set_byte(final Object instance, final long offset, final byte value);

	@Override
	public native void set_boolean(final Object instance, final long offset, final boolean value);

	@Override
	public native void set_short(final Object instance, final long offset, final short value);

	@Override
	public native void set_char(final Object instance, final long offset, final char value);

	@Override
	public native void set_int(final Object instance, final long offset, final int value);

	@Override
	public native void set_float(final Object instance, final long offset, final float value);

	@Override
	public native void set_long(final Object instance, final long offset, final long value);

	@Override
	public native void set_double(final Object instance, final long offset, final double value);

	@Override
	public native void setObject(final Object instance, final long offset, final Object value);
	

	@Override
	public final synchronized void set_byteInBytes(final byte[] bytes, final int index, final byte value)
	{
		final var segment = MemorySegment.ofArray(bytes);
		segment.set(ValueLayout.JAVA_BYTE, index, value);
	}
	
	@Override
	public final synchronized void set_booleanInBytes(final byte[] bytes, final int index, final boolean value)
	{
		final var segment = MemorySegment.ofArray(bytes);
		segment.set(ValueLayout.JAVA_BOOLEAN, index, value);
	}

	@Override
	public final synchronized void set_shortInBytes(final byte[] bytes, final int index, final short value)
	{
		final var segment = MemorySegment.ofArray(bytes);
		segment.set(ValueLayout.JAVA_SHORT_UNALIGNED, index, value);
	}

	@Override
	public final synchronized void set_charInBytes(final byte[] bytes, final int index, final char value)
	{
		final var segment = MemorySegment.ofArray(bytes);
		segment.set(ValueLayout.JAVA_CHAR_UNALIGNED, index, value);
	}

	@Override
	public final synchronized void set_intInBytes(final byte[] bytes, final int index, final int value)
	{
		final var segment = MemorySegment.ofArray(bytes);
		segment.set(ValueLayout.JAVA_INT_UNALIGNED, index, value);
	}

	@Override
	public final synchronized void set_floatInBytes(final byte[] bytes, final int index, final float value)
	{
		final var segment = MemorySegment.ofArray(bytes);
		segment.set(ValueLayout.JAVA_FLOAT_UNALIGNED, index, value);
	}

	@Override
	public final synchronized void set_longInBytes(final byte[] bytes, final int index, final long value)
	{
		final var segment = MemorySegment.ofArray(bytes);
		segment.set(ValueLayout.JAVA_LONG_UNALIGNED, index, value);
	}

	@Override
	public final synchronized void set_doubleInBytes(final byte[] bytes, final int index, final double value)
	{
		final var segment = MemorySegment.ofArray(bytes);
		segment.set(ValueLayout.JAVA_DOUBLE_UNALIGNED, index, value);
	}
		
	
	///////////////////////////////////////////////////////////////////////////
	// range and arrays //
	/////////////////////
	
	@Override
	public void copyRange(final long sourceAddress, final long targetAddress, final long length) {
		final MemorySegment srcSegment = MemorySegment.ofAddress(sourceAddress).reinterpret(length);
		final MemorySegment dstSegment = MemorySegment.ofAddress(targetAddress).reinterpret(length);
		MemorySegment.copy(srcSegment, 0, dstSegment, 0, length);
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final byte[] target) {
		final MemorySegment segment = MemorySegment.ofAddress(sourceAddress).reinterpret(target.length);
		MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0, target, 0, target.length);
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final boolean[] target) {
		final MemorySegment segment = MemorySegment.ofAddress(sourceAddress).reinterpret(target.length);

		for(int i = 0; i < target.length; i++)
		{
			target[i] = segment.get(ValueLayout.JAVA_BOOLEAN, i + 0);
		}
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final short[] target) {
		final MemorySegment segment = MemorySegment.ofAddress(sourceAddress).reinterpret(target.length * Short.BYTES);
		MemorySegment.copy(segment, ValueLayout.JAVA_SHORT_UNALIGNED, 0, target, 0, target.length);
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final char[] target) {
		final MemorySegment segment = MemorySegment.ofAddress(sourceAddress).reinterpret(target.length * Character.BYTES);
		MemorySegment.copy(segment, ValueLayout.JAVA_CHAR_UNALIGNED, 0, target, 0, target.length);
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final int[] target) {
		final MemorySegment segment = MemorySegment.ofAddress(sourceAddress).reinterpret(target.length * Integer.BYTES);
		MemorySegment.copy(segment, ValueLayout.JAVA_INT_UNALIGNED, 0, target, 0, target.length);
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final float[] target) {
		final MemorySegment segment = MemorySegment.ofAddress(sourceAddress).reinterpret(target.length * Float.BYTES);
		MemorySegment.copy(segment, ValueLayout.JAVA_FLOAT_UNALIGNED, 0, target, 0, target.length);
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final long[] target) {
		final MemorySegment segment = MemorySegment.ofAddress(sourceAddress).reinterpret(target.length * Long.BYTES);
		MemorySegment.copy(segment, ValueLayout.JAVA_LONG_UNALIGNED, 0, target, 0, target.length);
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final double[] target) {
		final MemorySegment segment = MemorySegment.ofAddress(sourceAddress).reinterpret(target.length * Double.BYTES);
		MemorySegment.copy(segment, ValueLayout.JAVA_DOUBLE_UNALIGNED, 0, target, 0, target.length);
	}
	
	@Override
	public void copyArrayToAddress(final byte[] array, final long targetAddress) {
		final MemorySegment segment = MemorySegment.ofAddress(targetAddress).reinterpret(array.length);
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_BYTE, 0, array.length);
	}

	@Override
	public void copyArrayToAddress(final boolean[] array, final long targetAddress) {

		final MemorySegment segment = MemorySegment.ofAddress(targetAddress).reinterpret(array.length);

		for (int i = 0; i < array.length; i++) {
			segment.set(ValueLayout.JAVA_BOOLEAN, i, array[i]);
		}
	}

	@Override
	public void copyArrayToAddress(final short[] array, final long targetAddress) {
		final MemorySegment segment = MemorySegment.ofAddress(targetAddress).reinterpret(array.length * Short.BYTES);
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_SHORT_UNALIGNED, 0, array.length);
	}

	@Override
	public void copyArrayToAddress(final char[] array, final long targetAddress) {
		final MemorySegment segment = MemorySegment.ofAddress(targetAddress).reinterpret(array.length * Character.BYTES);
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_CHAR_UNALIGNED, 0, array.length);
	}

	@Override
	public void copyArrayToAddress(final int[] array, final long targetAddress) {
		final MemorySegment segment = MemorySegment.ofAddress(targetAddress).reinterpret(array.length * Integer.BYTES);
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_INT_UNALIGNED, 0, array.length);
	}

	@Override
	public void copyArrayToAddress(final float[] array, final long targetAddress) {
		final MemorySegment segment = MemorySegment.ofAddress(targetAddress).reinterpret(array.length * Float.BYTES);
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_FLOAT_UNALIGNED, 0, array.length);
	}

	@Override
	public void copyArrayToAddress(final long[] array, final long targetAddress) {
		final MemorySegment segment = MemorySegment.ofAddress(targetAddress).reinterpret(array.length * Long.BYTES);
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_LONG_UNALIGNED, 0, array.length);
	}

	@Override
	public void copyArrayToAddress(final double[] array, final long targetAddress) {
		final MemorySegment segment = MemorySegment.ofAddress(targetAddress).reinterpret(array.length * Double.BYTES);
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_DOUBLE_UNALIGNED, 0, array.length);
	}

	///////////////////////////////////////////////////////////////////////////
	// Field access    //
	////////////////////
	
	@Override
	public native long objectFieldOffset(final Field field);
	
	@Override
	public long[] objectFieldOffsets(final Field... fields)
	{
		final long[] fieldIDs = new long[fields.length];
		
		for(int i = 0; i < fields.length; i++)
		{
			fieldIDs[i] = this.objectFieldOffset(fields[i]);
		}
		
		return fieldIDs;
	}

	@Override
	public final long objectFieldOffset(final Class<?> objectClass, final Field field)
	{
		return this.objectFieldOffset(field);
	}
	
	@Override
	public final long[] objectFieldOffsets(final Class<?> objectClass, final Field... fields)
	{
		return this.objectFieldOffsets(fields);
	}
	

	///////////////////////////////////////////////////////////////////////////
	// class initialization //
	/////////////////////////

	private static void validateObjectFieldsNotNull(final Field[] objectFields, final Class<?> c)
	{
		if(objectFields == null)
		{
			throw new MemoryException("No object fields registered for " + c + ".");
		}
	}
	
	private static RuntimeException createInvalidOffsetException(
			final Field[]  objectFields,
			final Class<?> c           ,
			final int      offset
		)
	{
		return new MemoryException("Unknown object field offset " + offset + " for " + c + ".");
	}
	
	@Override
	public void ensureClassInitialized(final Class<?> c) {
		try
		{
			Class.forName(c.getName(), true, c.getClassLoader());
		}
		catch(final ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T instantiateBlank(final Class<T> c) throws InstantiationRuntimeException {
		return this.classInstantiator.instantiate(c);
	}
	

	///////////////////////////////////////////////////////////////////////////
	// volatile //
	/////////////


	@Override
	public long volatileGet_long(final Object subject, final long address) {
		notImplemented();
		return 0;
	}

	@Override
	public void volatileSet_long(final Object subject, final long address, final long value) {
		notImplemented();
		return;
	}

	@Override
	public boolean compareAndSwap_int(final Object subject, final long address, final int expected, final int replacement) {
		notImplemented();
		return false;
	}

	@Override
	public boolean compareAndSwap_long(final Object subject, final long address, final long expected, final long replacement) {
		notImplemented();
		return false;
	}

	@Override
	public boolean compareAndSwapObject(final Object subject, final long address, final Object expected, final Object replacement) {
		notImplemented();
		return false;
	}

	
	@Override
	public MemoryStatistics createHeapMemoryStatistics() {
		notImplemented();
		return null;
	}

	@Override
	public MemoryStatistics createNonHeapMemoryStatistics() {
		notImplemented();
		return null;
	}
}
