package org.eclipse.serializer.memory.foreign;

/*-
 * #%L
 * Eclipse Serializer Base
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

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.serializer.collections.HashTable;
import org.eclipse.serializer.exceptions.InstantiationRuntimeException;
import org.eclipse.serializer.exceptions.MemoryException;
import org.eclipse.serializer.functional.DefaultInstantiator;
import org.eclipse.serializer.memory.MemoryAccessor;
import org.eclipse.serializer.memory.MemoryStatistics;
import org.eclipse.serializer.memory.sun.JdkInstantiatorBlank;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.util.X;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public class ForeignMemoryAccessor implements MemoryAccessor
{
	private static class DirectMemoryHandle {
		
		private final Arena arena;
		private final MemorySegment memorySegment;
		
		public DirectMemoryHandle(final Arena arena, final MemorySegment memorySegment) {
			super();
			this.arena = arena;
			this.memorySegment = memorySegment;
		}

		public final Arena getArena() {
			return this.arena;
		}

		public final MemorySegment getMemorySegment() {
			return this.memorySegment;
		}
				
		public void close() {
			this.arena.close();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public static MemoryAccessor New() {
		return new ForeignMemoryAccessor();
	}

	public ForeignMemoryAccessor() {
		super();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// static fields //
	//////////////////
	
	private final static Logger logger = Logging.getLogger(ForeignMemoryAccessor.class);
	
	///////////////////////////////////////////////////////////////////////////
	// fields //
	///////////
	
	private final TreeMap<Integer, DirectMemoryHandle> memorySegments = new TreeMap<Integer, ForeignMemoryAccessor.DirectMemoryHandle>(
			//Collections.reverseOrder()
		);
	
	//dont use buffers as key, their hashCode is contentend dependend!
	private final Map<Integer, Integer> bufferRegistry = new Hashtable<Integer, Integer>(
			//Collections.reverseOrder()
		);
	
	
	private final HashTable<Class<?>, Field[]> objectFieldsRegistry = HashTable.New();
	
	
	private final DefaultInstantiator classInstantiator = JdkInstantiatorBlank.New();
	
	///////////////////////////////////////////////////////////////////////////
	// buffer id <--> address coding //
	//////////////////////////////////
	
	//instead of working with a absolute memory address
	//the "address" is coded as an id of the segement and the relative position
	//(offset) in that segement.
	//lower bytes are offest
	//upper bytes are id
	
	// 23Bit for id == 8.388.607 ids
	// 40Bit for offset ==
	
	private static final long ADDRESS_MASK = 0xFF_FFFF_FFFFl;
	private static final int  ADDRESS_BITS = 40;
	private static final long ID_MASK      = 0x7FFF_FF00_0000_0000l;
		
	public static long getOffset(final long address) {
		return address & ADDRESS_MASK;
	}
	
	public static int getID(final long address) {
		return (int) (address >>> 40);
	}
	
	public long encodeAddress(final int id) {
		return ((long)id) << 40;
	}
	
	public int findNextFreeID() {
		
		for(int i = 0; i < Integer.MAX_VALUE; i++) {
			if(!this.memorySegments.containsKey(i)) return i;
		}
		
		return -1;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public static void EXIT() {
		logger.error("not yet implemented");
		throw new RuntimeException("TO BE IMPLEMENTED!");
	}
	
	@Override
	public void guaranteeUsability() {
		// TODO Auto-generated method stub
	}
	
	public MemorySegment getMemorySegment(final int id) {
						
		 final DirectMemoryHandle handle = this.memorySegments.get(id);
		 return handle.getMemorySegment();
	}

	///////////////////////////////////////////////////////////////////////////
	// ByteBuffer //
	///////////////
	
	@Override
	public long getDirectByteBufferAddress(final ByteBuffer directBuffer) {
		
		final int id = this.bufferRegistry.get(System.identityHashCode(directBuffer));
		return this.encodeAddress(id);
	}

	@Override
	public boolean deallocateDirectByteBuffer(final ByteBuffer directBuffer) {
						
		final Integer id = this.bufferRegistry.remove(System.identityHashCode(directBuffer));
		
		logger.debug("deallocating native segment (id: {}) and buffer {}", id, directBuffer);
		
		if(id == null) {
			logger.debug("buffer not registered {}", directBuffer);
			return false;
		}
		
		final DirectMemoryHandle handle = this.memorySegments.remove(id);
		handle.close();
		
		return true;
	}

	@Override
	public boolean isDirectByteBuffer(final ByteBuffer byteBuffer) {
		// TODO Auto-generated method stub
				EXIT();
		return false;
	}

	@Override
	public ByteBuffer guaranteeDirectByteBuffer(final ByteBuffer directBuffer) {
		// TODO Auto-generated method stub
				EXIT();
		return null;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// MemoryAllocation //
	/////////////////////
	
	@Override
	public ByteBuffer allocateDirectNative(final int capacity)
	{
		final Arena arena = Arena.ofShared();
		final MemorySegment segment = arena.allocate(capacity);
				
		final int id = this.findNextFreeID();
		this.memorySegments.put(id, new DirectMemoryHandle(arena, segment));
		
		final ByteBuffer byteBuffer = segment.asByteBuffer().order(ByteOrder.nativeOrder());
		
		this.bufferRegistry.put(System.identityHashCode(byteBuffer), id);
	
		logger.debug("Registered native segment and buffer {} with id {}", byteBuffer, id);
		
		return byteBuffer;
	}
	
	@Override
	public ByteBuffer allocateDirectNative(final long capacity) {
		return this.allocateDirectNative(
				X.checkArrayRange(capacity));
	}

	@Override
	public long allocateMemory(final long bytes) {
		final Arena arena = Arena.ofShared();
		final MemorySegment segment = arena.allocate(bytes);
				
		final int id = this.findNextFreeID();
		this.memorySegments.put(id, new DirectMemoryHandle(arena, segment));
		logger.debug("Registered native segment with id {}, {} bytes", id, bytes);
		
		return this.encodeAddress(id);
	}

	@Override
	public long reallocateMemory(final long address, final long bytes) {
		// TODO Auto-generated method stub
				EXIT();
		return 0;
	}

	@Override
	public void freeMemory(final long address) {
		final int id = getID(address);
		final DirectMemoryHandle memoryHandle = this.memorySegments.get(id);
		memoryHandle.close();
		logger.debug("closed memory handle with id {}", id);;
	}

	@Override
	public void fillMemory(final long targetAddress, final long length, final byte value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}
	
	///////////////////////////////////////////////////////////////////////////
	// getter methods //
	///////////////////
	
	@Override
	public byte get_byte(final long address) {
		
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_BYTE, offset);
	}

	@Override
	public boolean get_boolean(final long address) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_BOOLEAN, offset);
	}

	@Override
	public short get_short(final long address) {
		// TODO Auto-generated method stub
				EXIT();
		return 0;
	}

	@Override
	public char get_char(final long address) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_CHAR_UNALIGNED, offset);
	}

	@Override
	public int get_int(final long address) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_INT_UNALIGNED, offset);
	}

	@Override
	public float get_float(final long address) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_FLOAT_UNALIGNED, offset);
	}

	@Override
	public long get_long(final long address) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_LONG_UNALIGNED, offset);
	}

	@Override
	public double get_double(final long address) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_DOUBLE_UNALIGNED, offset);
	}

	@Override
	public byte get_byte(final Object instance, final long offset) {
		// TODO Auto-generated method stub
				EXIT();
		return 0;
	}

	@Override
	public boolean get_boolean(final Object instance, final long offset) {
		// TODO Auto-generated method stub
				EXIT();
		return false;
	}

	@Override
	public short get_short(final Object instance, final long offset) {
		// TODO Auto-generated method stub
				EXIT();
		return 0;
	}

	@Override
	public char get_char(final Object instance, final long offset) {
		// TODO Auto-generated method stub
				EXIT();
		return 0;
	}

	@Override
	public int get_int(final Object instance, final long offset) {
		// TODO Auto-generated method stub
				EXIT();
		return 0;
	}

	@Override
	public float get_float(final Object instance, final long offset) {
		// TODO Auto-generated method stub
				EXIT();
		return 0;
	}

	@Override
	public long get_long(final Object instance, final long offset) {
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getLong(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public double get_double(final Object instance, final long offset) {
		// TODO Auto-generated method stub
				EXIT();
		return 0;
	}

	@Override
	public Object getObject(final Object instance, final long offset) {
		try
		{
			return this.objectField(instance.getClass(), (int)offset).get(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public void set_byte(final long address, final byte value) {
		
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_BYTE, offset, value);
	}

	@Override
	public void set_boolean(final long address, final boolean value) {
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_BOOLEAN, offset, value);
		
	}

	@Override
	public void set_short(final long address, final short value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void set_char(final long address, final char value) {
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_CHAR_UNALIGNED, offset, value);
	}

	@Override
	public void set_int(final long address, final int value) {
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_INT_UNALIGNED, offset, value);
	}

	@Override
	public void set_float(final long address, final float value) {
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_FLOAT_UNALIGNED, offset, value);
	}

	@Override
	public void set_long(final long address, final long value) {
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_LONG_UNALIGNED, offset, value);
	}

	@Override
	public void set_double(final long address, final double value) {
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_DOUBLE_UNALIGNED, offset, value);
	}

	@Override
	public void set_byte(final Object instance, final long offset, final byte value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void set_boolean(final Object instance, final long offset, final boolean value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void set_short(final Object instance, final long offset, final short value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void set_char(final Object instance, final long offset, final char value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void set_int(final Object instance, final long offset, final int value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void set_float(final Object instance, final long offset, final float value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void set_long(final Object instance, final long offset, final long value) {
		try
		{
			this.objectField(instance.getClass(), (int)offset).setLong(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public void set_double(final Object instance, final long offset, final double value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void setObject(final Object instance, final long offset, final Object value) {
		try
		{
			this.objectField(instance.getClass(), (int)offset).set(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
		
	}

	@Override
	public void set_byteInBytes(final byte[] bytes, final int index, final byte value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void set_booleanInBytes(final byte[] bytes, final int index, final boolean value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void set_shortInBytes(final byte[] bytes, final int index, final short value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void set_charInBytes(final byte[] bytes, final int index, final char value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void set_intInBytes(final byte[] bytes, final int index, final int value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void set_floatInBytes(final byte[] bytes, final int index, final float value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void set_longInBytes(final byte[] bytes, final int index, final long value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void set_doubleInBytes(final byte[] bytes, final int index, final double value) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void copyRange(final long sourceAddress, final long targetAddress, final long length) {
		
		final int srcId = getID(sourceAddress);
		final long srcOffset = getOffset(sourceAddress);
		final MemorySegment srcSegment = this.getMemorySegment(srcId);
		
		final int dstId = getID(targetAddress);
		final long dstOffset = getOffset(targetAddress);
		final MemorySegment dstSegment = this.getMemorySegment(dstId);
		
		MemorySegment.copy(srcSegment, srcOffset, dstSegment, dstOffset, length);
		
	}
	@Override
	public void copyRangeToArray(final long sourceAddress, final byte[] target) {
		
		final int id = getID(sourceAddress);
		final long offset = getOffset(sourceAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, offset, target, 0, target.length);
		
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final boolean[] target) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final short[] target) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final char[] target) {
		final int id = getID(sourceAddress);
		final long offset = getOffset(sourceAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(segment, ValueLayout.JAVA_CHAR_UNALIGNED, offset, target, 0, target.length);
		
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final int[] target) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final float[] target) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final long[] target) {
		final int id = getID(sourceAddress);
		final long offset = getOffset(sourceAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(segment, ValueLayout.JAVA_LONG_UNALIGNED, offset, target, 0, target.length);
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final double[] target) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void copyArrayToAddress(final byte[] array, final long targetAddress) {
		
		final int id = getID(targetAddress);
		final long offset = getOffset(targetAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_BYTE, offset, array.length);
						
	}

	@Override
	public void copyArrayToAddress(final boolean[] array, final long targetAddress) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void copyArrayToAddress(final short[] array, final long targetAddress) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void copyArrayToAddress(final char[] array, final long targetAddress) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void copyArrayToAddress(final int[] array, final long targetAddress) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void copyArrayToAddress(final float[] array, final long targetAddress) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void copyArrayToAddress(final long[] array, final long targetAddress) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	@Override
	public void copyArrayToAddress(final double[] array, final long targetAddress) {
		// TODO Auto-generated method stub
				EXIT();
		
	}

	
	///////////////////////////////////////////////////////////////////////////
	// Fields    //
	//////////////
	///
	@Override
	public long objectFieldOffset(final Field field) {
		return this.objectFieldOffset(field.getDeclaringClass(), field);
	}

	@Override
	public long[] objectFieldOffsets(final Field... fields) {
		// TODO Auto-generated method stub
			EXIT();
		return null;
	}

	@Override
	public long objectFieldOffset(final Class<?> objectClass, final Field field) {
		final Field[] objectFields = this.ensureRegisteredObjectFields(objectClass);

		return ForeignMemoryAccessor.objectFieldOffset(objectFields, field);
	}

	@Override
	public long[] objectFieldOffsets(final Class<?> objectClass, final Field... fields) {
		final Field[] objectFields = this.ensureRegisteredObjectFields(objectClass);

		final long[] offsets = new long[fields.length];
		for(int i = 0; i < fields.length; i++)
		{
			if(Modifier.isStatic(fields[i].getModifiers()))
			{
				throw new IllegalArgumentException("Not an object field: " + fields[i]);
			}
			offsets[i] = objectFieldOffset(objectFields, fields[i]);
		}
		
		return offsets;
	}
	
	private Field[] ensureRegisteredObjectFields(final Class<?> objectClass)
	{
		final Field[] objectFields = this.objectFieldsRegistry.get(objectClass);
		if(objectFields != null)
		{
			return objectFields;
		}
		
		return this.registerCollectedObjectFields(objectClass);
	}
	
	private Field[] registerCollectedObjectFields(final Class<?> objectClass)
	{
		/*
		 * Note on algorithm:
		 * Each class in a class hierarchy gets its own registry entry, even if that means redundancy.
		 * This is necessary to make the offset-to-field lookup quick
		 */
		final Field[] fields = XReflect.collectInstanceFields(objectClass);

		this.registerObjectFields(objectClass, fields);
		
		return fields;
	}
	
	final static long objectFieldOffset(final Field[] objectFields, final Field field)
	{
		final Class<?> declaringClass = field.getDeclaringClass();
		final String   fieldName      = field.getName();
		
		for(int i = 0; i < objectFields.length; i++)
		{
			if(objectFields[i].getDeclaringClass() == declaringClass && objectFields[i].getName().equals(fieldName))
			{
				return i;
			}
		}
		
		throw new MemoryException(
			"Inconsistent object fields registration for " + declaringClass.getName() + "#" + fieldName
		);
	}
	
	private Field[] registerObjectFields(final Class<?> objectClass, final Field[] fields)
	{
		for(final Field field : fields)
		{
			XReflect.setAccessible(objectClass, field);
		}
				
		if(!this.objectFieldsRegistry.add(objectClass, fields))
		{
			throw new MemoryException("Object fields already registered for " + objectClass);
		}
		
		return fields;
	}
	
	private Field objectField(final Class<?> c, final int offset)
	{
		final Field[] objectFields = this.objectFieldsRegistry.get(c);
		validateObjectFieldsNotNull(objectFields, c);

		if(offset >= 0 && offset < objectFields.length)
		{
			return objectFields[offset];
		}

		throw createInvalidOffsetException(objectFields, c, offset);
	}
	
	
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

	@Override
	public MemoryStatistics createHeapMemoryStatistics() {
		// TODO Auto-generated method stub
		EXIT();
		return null;
	}

	@Override
	public MemoryStatistics createNonHeapMemoryStatistics() {
		// TODO Auto-generated method stub
		EXIT();
		return null;
	}

	@Override
	public long volatileGet_long(final Object subject, final long offset) {
		// TODO Auto-generated method stub
		EXIT();
		return 0;
	}

	@Override
	public void volatileSet_long(final Object subject, final long offset, final long value) {
		// TODO Auto-generated method stub
		EXIT();
	}

	@Override
	public boolean compareAndSwap_int(final Object subject, final long offset, final int expected, final int replacement) {
		// TODO Auto-generated method stub
		EXIT();
		return false;
	}

	@Override
	public boolean compareAndSwap_long(final Object subject, final long offset, final long expected, final long replacement) {
		// TODO Auto-generated method stub
		EXIT();
		return false;
	}

	@Override
	public boolean compareAndSwapObject(final Object subject, final long offset, final Object expected, final Object replacement) {
		// TODO Auto-generated method stub
		EXIT();
		return false;
	}

}
