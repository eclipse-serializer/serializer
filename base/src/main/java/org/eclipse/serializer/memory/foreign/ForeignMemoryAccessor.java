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
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.serializer.collections.HashTable;
import org.eclipse.serializer.collections.XArrays;
import org.eclipse.serializer.exceptions.InstantiationRuntimeException;
import org.eclipse.serializer.exceptions.MemoryException;
import org.eclipse.serializer.functional.DefaultInstantiator;
import org.eclipse.serializer.memory.MemoryAccessor;
import org.eclipse.serializer.memory.MemoryStatistics;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.memory.sun.JdkInstantiatorBlank;
import org.eclipse.serializer.memory.sun.JdkInternals;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.typing.XTypes;
import org.eclipse.serializer.util.X;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;


/**
 * Performance:
 * 
 * nextFreeID() is very slow if iterated from 0 to Integer.Max
 * freeMemory is also slow ..
 * 
 */
public class ForeignMemoryAccessor implements MemoryAccessor
{
	private static class DirectMemoryHandle {
		
		private final Arena arena;
		private final MemorySegment memorySegment;
		private StackTraceElement[] stackTrace;
		
		public DirectMemoryHandle(final Arena arena, final MemorySegment memorySegment) {
			super();
			this.arena = arena;
			this.memorySegment = memorySegment;
		}

		public DirectMemoryHandle(final Arena arena, final MemorySegment segment, final StackTraceElement[] stackTrace) {
			super();
			this.arena = arena;
			this.memorySegment = segment;
			this.stackTrace = stackTrace;
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
		
		this.exec = Executors.newFixedThreadPool(1);
		this.closingQueue = new LinkedBlockingDeque<DirectMemoryHandle>();
		
		this.exec.submit(()->{
			while(true)
			{
				try {
					this.closingQueue.takeFirst().close();
					this.closedSegments++;

					//if(1 == this.closingQueue.size() % 1000) {
						logger.info("MemorySegments to be closed: {}", this.closingQueue.size());
					//}

				} catch (final InterruptedException e) {
					//Suppress exception
					logger.trace("INTERRUPTETD CLOSE");
					return;
				}
			}
		});
	
		this.bufferCreations = new AtomicLong();
		this.nativeCreations = new AtomicLong();
		this.bufferDeletions = new AtomicLong();
		this.nativeDeletions = new AtomicLong();
		this.allocatedBufferMemory = new AtomicLong();
		this.allocatedNativeMemory = new AtomicLong();
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
	
	//Don't use buffers as key, their hashCode is contented dependent!
	private final Map<Integer, Integer> bufferRegistry = new Hashtable<Integer, Integer>(
			//Collections.reverseOrder()
		);
	
	
	private final HashTable<Class<?>, Field[]> objectFieldsRegistry = HashTable.New();
	
	
	private final DefaultInstantiator classInstantiator = JdkInstantiatorBlank.New();

	private int nextFreeID;
	
	ExecutorService exec;
	BlockingDeque<DirectMemoryHandle> closingQueue;

	long closedSegments;
	
	AtomicLong bufferCreations;
	AtomicLong nativeCreations;
	AtomicLong nativeDeletions;
	AtomicLong bufferDeletions;
	AtomicLong allocatedNativeMemory;
	AtomicLong allocatedBufferMemory;
	
	///////////////////////////////////////////////////////////////////////////
	// buffer id <--> address coding //
	//////////////////////////////////
	
	//instead of working with a absolute memory address
	//the "address" is coded as an id of the segement and the relative position
	//(offset) in that segment.
	//lower bytes are offset
	//upper bytes are id
	
	// 23Bit for id == 8.388.607 ids
	// 40Bit for offset ==
	
	private static final long ADDRESS_MASK = 0xFFFF_FFFFl;
	private static final int  ADDRESS_BITS = 40;
	private static final long ID_MASK      = 0x7FFF_FFFF_0000_0000l;
		
	public static long getOffset(final long address) {
		return address & ADDRESS_MASK;
	}
	
	public static int getID(final long address) {
		return (int) (address >>> 32);
	}
	
	public long encodeAddress(final int id) {
		return ((long)id) << 32;
	}
		
	public synchronized int findNextFreeID() {
		if(this.nextFreeID == Integer.MAX_VALUE) {
			this.nextFreeID = 0;
			logger.info("Free ID overflow");
		}
		
		for(int i = this.nextFreeID; i < Integer.MAX_VALUE; i++) {
			if(!this.memorySegments.containsKey(i)) {
				this.nextFreeID = i;
				return i;}
		}
		
		return -1;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public static void notImplemented() {
		//noOp
	}
	
	@Override
	public void guaranteeUsability() {
		notImplemented();
	}
	
	public synchronized MemorySegment getMemorySegment(final int id) {
				
		if(id != 18)
			logger.trace("Try getting  memory handle with id {}", id);
		final DirectMemoryHandle handle = this.memorySegments.get(id);
		if(handle == null) {
			return null;
		}
		return handle.getMemorySegment();
	}

	///////////////////////////////////////////////////////////////////////////
	// ByteBuffer //
	///////////////
	
	@Override
	public synchronized long getDirectByteBufferAddress(final ByteBuffer directBuffer) {
		
		final int id = this.bufferRegistry.get(System.identityHashCode(directBuffer));
		return this.encodeAddress(id);
	}

	@Override
	public synchronized boolean deallocateDirectByteBuffer(final ByteBuffer directBuffer) {
						
		final Integer id = this.bufferRegistry.remove(System.identityHashCode(directBuffer));
		
		logger.trace("deallocating native segment (id: {}) and buffer {}", id, directBuffer);
		
		if(id == null) {
			logger.trace("buffer not registered {}", directBuffer);
			return false;
		}
				
		final DirectMemoryHandle handle = this.memorySegments.remove(id);
		this.bufferDeletions.incrementAndGet();
		this.allocatedBufferMemory.addAndGet(-handle.memorySegment.byteSize());
		this.closingQueue.offer(handle);
		
		
		
		return true;
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
	// MemoryAllocation //
	/////////////////////
	
	@Override
	public synchronized ByteBuffer allocateDirectNative(final int capacity)
	{
		final Arena arena = Arena.ofShared();
		final MemorySegment segment = arena.allocate(capacity);
						
		final int id = this.findNextFreeID();
		this.memorySegments.put(id, new DirectMemoryHandle(arena, segment, new Exception("StackTrace").getStackTrace()));
		
		final ByteBuffer byteBuffer = segment.asByteBuffer().order(ByteOrder.nativeOrder());
		
		this.bufferRegistry.put(System.identityHashCode(byteBuffer), id);
	
		logger.debug("Registered native segment and buffer {} with id {} and size {}", byteBuffer, id, capacity);
		
		this.bufferCreations.incrementAndGet();
		
		this.allocatedBufferMemory.addAndGet(capacity);
		
		return byteBuffer;
	}
	
	@Override
	public synchronized ByteBuffer allocateDirectNative(final long capacity) {
		return this.allocateDirectNative(
				X.checkArrayRange(capacity));
	}

	@Override
	public synchronized long allocateMemory(final long bytes) {
		final Arena arena = Arena.ofAuto();
		final MemorySegment segment = arena.allocate(bytes);
						
		final int id = this.findNextFreeID();
		this.memorySegments.put(id, new DirectMemoryHandle(arena, segment));
		logger.trace("Registered native segment with id {}, {} bytes", id, bytes);
		
		this.nativeCreations.incrementAndGet();
		
		this.allocatedNativeMemory.addAndGet(bytes);
		
		return this.encodeAddress(id);
	}

	@Override
	public synchronized long reallocateMemory(final long address, final long bytes) {
		this.freeMemory(address);
		this.allocateMemory(bytes);
		return 0;
	}

	@Override
	public synchronized void freeMemory(final long address) {
		final int id = getID(address);
		logger.trace("closing memory handle with id {}", id);
		final DirectMemoryHandle memoryHandle = this.memorySegments.get(id);
		this.nativeDeletions.incrementAndGet();
		this.allocatedNativeMemory.addAndGet(-memoryHandle.memorySegment.byteSize());
		this.memorySegments.remove(id);
		//this.closingQueue.offer(memoryHandle);
		logger.trace("closed memory handle with id {}", id);;
	}

	@Override
	public synchronized void fillMemory(final long targetAddress, final long length, final byte value) {
		final int id = getID(targetAddress);
		final DirectMemoryHandle memoryHandle = this.memorySegments.get(id);
		memoryHandle.memorySegment.fill(value);
		logger.trace("Segment {}, memory fill", id);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// getter methods //
	///////////////////
	
	@Override
	public synchronized byte get_byte(final long address) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_BYTE, offset);
	}

	@Override
	public synchronized  boolean get_boolean(final long address) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_BOOLEAN, offset);
	}

	@Override
	public synchronized short get_short(final long address) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_SHORT_UNALIGNED, offset);
	}

	@Override
	public synchronized char get_char(final long address) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_CHAR_UNALIGNED, offset);
	}

	@Override
	public synchronized int get_int(final long address) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_INT_UNALIGNED, offset);
	}

	@Override
	public synchronized float get_float(final long address) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_FLOAT_UNALIGNED, offset);
	}

	@Override
	public synchronized long get_long(final long address) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_LONG_UNALIGNED, offset);
	}

	@Override
	public synchronized double get_double(final long address) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		return segment.get(ValueLayout.JAVA_DOUBLE_UNALIGNED, offset);
	}

	@Override
	public synchronized byte get_byte(final Object instance, final long offset) {
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getByte(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public synchronized boolean get_boolean(final Object instance, final long offset) {
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getBoolean(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public synchronized short get_short(final Object instance, final long offset) {
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getShort(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public synchronized char get_char(final Object instance, final long offset) {
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getChar(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public synchronized int get_int(final Object instance, final long offset) {
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getInt(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public synchronized float get_float(final Object instance, final long offset) {
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getFloat(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public synchronized long get_long(final Object instance, final long offset) {
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
	public synchronized double get_double(final Object instance, final long offset) {
		try
		{
			return this.objectField(instance.getClass(), (int)offset).getDouble(instance);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public synchronized Object getObject(final Object instance, final long offset) {
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
	public synchronized void set_byte(final long address, final byte value) {
		
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_BYTE, offset, value);
	}

	@Override
	public synchronized void set_boolean(final long address, final boolean value) {
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_BOOLEAN, offset, value);
	}

	@Override
	public synchronized void set_short(final long address, final short value) {
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_SHORT_UNALIGNED, offset, value);
	}

	@Override
	public synchronized void set_char(final long address, final char value) {
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_CHAR_UNALIGNED, offset, value);
	}

	@Override
	public synchronized void set_int(final long address, final int value) {
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_INT_UNALIGNED, offset, value);
	}

	@Override
	public synchronized void set_float(final long address, final float value) {
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_FLOAT_UNALIGNED, offset, value);
	}

	@Override
	public synchronized void set_long(final long address, final long value) {
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_LONG_UNALIGNED, offset, value);
	}

	@Override
	public synchronized void set_double(final long address, final double value) {
		final int id = getID(address);
		final MemorySegment segment = this.getMemorySegment(id);
		final long offset = getOffset(address);
				
		segment.set(ValueLayout.JAVA_DOUBLE_UNALIGNED, offset, value);
	}

	@Override
	public synchronized void set_byte(final Object instance, final long offset, final byte value) {
		try
		{
			this.objectField(instance.getClass(), (int)offset).setByte(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public synchronized void set_boolean(final Object instance, final long offset, final boolean value) {
		try
		{
			this.objectField(instance.getClass(), (int)offset).setBoolean(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public synchronized void set_short(final Object instance, final long offset, final short value) {
		try
		{
			this.objectField(instance.getClass(), (int)offset).setShort(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public synchronized void set_char(final Object instance, final long offset, final char value) {
		try
		{
			this.objectField(instance.getClass(), (int)offset).setChar(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public synchronized void set_int(final Object instance, final long offset, final int value) {
		try
		{
			this.objectField(instance.getClass(), (int)offset).setInt(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public synchronized void set_float(final Object instance, final long offset, final float value) {
		try
		{
			this.objectField(instance.getClass(), (int)offset).setFloat(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public synchronized void set_long(final Object instance, final long offset, final long value) {
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
	public synchronized void set_double(final Object instance, final long offset, final double value) {
		try
		{
			this.objectField(instance.getClass(), (int)offset).setDouble(instance, value);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
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

	
	// transformative byte array primitive value setters //
	
	@Override
	public final synchronized void set_byteInBytes(final byte[] bytes, final int index, final byte value)
	{
		XArrays.set_byteInBytes(bytes, index, value);
	}
	
	@Override
	public final synchronized void set_booleanInBytes(final byte[] bytes, final int index, final boolean value)
	{
		XArrays.set_booleanInBytes(bytes, index, value);
	}

	@Override
	public final synchronized void set_shortInBytes(final byte[] bytes, final int index, final short value)
	{
		// since XArrays inherently works only with sane byte order, the insane case has to be checked and handled here.
		XArrays.set_shortInBytes(bytes, index, XMemory.isBigEndianNativeOrder() ? Short.reverseBytes(value) : value);
	}

	@Override
	public final synchronized void set_charInBytes(final byte[] bytes, final int index, final char value)
	{
		// since XArrays inherently works only with sane byte order, the insane case has to be checked and handled here.
		XArrays.set_charInBytes(bytes, index, XMemory.isBigEndianNativeOrder() ? Character.reverseBytes(value) : value);
	}

	@Override
	public final synchronized void set_intInBytes(final byte[] bytes, final int index, final int value)
	{
		// since XArrays inherently works only with sane byte order, the insane case has to be checked and handled here.
		XArrays.set_intInBytes(bytes, index, XMemory.isBigEndianNativeOrder() ? Integer.reverseBytes(value) : value);
	}

	@Override
	public final synchronized void set_floatInBytes(final byte[] bytes, final int index, final float value)
	{
		// byte order check inside
		this.set_intInBytes(bytes, index, Float.floatToRawIntBits(value));
	}

	@Override
	public final synchronized void set_longInBytes(final byte[] bytes, final int index, final long value)
	{
		// since XArrays inherently works only with sane byte order, the insane case has to be checked and handled here.
		XArrays.set_longInBytes(bytes, index, XMemory.isBigEndianNativeOrder() ? Long.reverseBytes(value) : value);
	}

	@Override
	public final synchronized void set_doubleInBytes(final byte[] bytes, final int index, final double value)
	{
		// byte order check inside
		this.set_longInBytes(bytes, index, Double.doubleToRawLongBits(value));
	}

	@Override
	public synchronized void copyRange(final long sourceAddress, final long targetAddress, final long length) {
		final int srcId = getID(sourceAddress);
		final long srcOffset = getOffset(sourceAddress);
		final MemorySegment srcSegment = this.getMemorySegment(srcId);
		
		final int dstId = getID(targetAddress);
		final long dstOffset = getOffset(targetAddress);
		final MemorySegment dstSegment = this.getMemorySegment(dstId);
		
		MemorySegment.copy(srcSegment, srcOffset, dstSegment, dstOffset, length);
	}
	@Override
	public synchronized void copyRangeToArray(final long sourceAddress, final byte[] target) {
		final int id = getID(sourceAddress);
		final long offset = getOffset(sourceAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, offset, target, 0, target.length);
	}

//	/**
//	 * Does not support boolean[], see
//	 * https://bugs.openjdk.org/browse/JDK-8345976
//	 */
//	@Override
//	public synchronized void copyRangeToArray(final long sourceAddress, final boolean[] target) {
//		final int id = getID(sourceAddress);
//		final long offset = getOffset(sourceAddress);
//		final MemorySegment segment = this.getMemorySegment(id);
//
//		MemorySegment.copy(segment, ValueLayout.JAVA_BOOLEAN, offset, target, 0, target.length);
//	}
	
	@Override
	public synchronized void copyRangeToArray(final long sourceAddress, final boolean[] target) {
		final int id = getID(sourceAddress);
		final long offset = getOffset(sourceAddress);
		final MemorySegment segment = this.getMemorySegment(id);
				
		for(int i = 0; i < target.length; i++)
		{
			target[i] = segment.get(ValueLayout.JAVA_BOOLEAN, i + offset);
		}
	}

	@Override
	public synchronized void copyRangeToArray(final long sourceAddress, final short[] target) {
		final int id = getID(sourceAddress);
		final long offset = getOffset(sourceAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(segment, ValueLayout.JAVA_SHORT_UNALIGNED, offset, target, 0, target.length);
	}

	@Override
	public synchronized void copyRangeToArray(final long sourceAddress, final char[] target) {
		final int id = getID(sourceAddress);
		final long offset = getOffset(sourceAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(segment, ValueLayout.JAVA_CHAR_UNALIGNED, offset, target, 0, target.length);
	}

	@Override
	public synchronized void copyRangeToArray(final long sourceAddress, final int[] target) {
		final int id = getID(sourceAddress);
		final long offset = getOffset(sourceAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(segment, ValueLayout.JAVA_INT_UNALIGNED, offset, target, 0, target.length);
	}

	@Override
	public synchronized void copyRangeToArray(final long sourceAddress, final float[] target) {
		final int id = getID(sourceAddress);
		final long offset = getOffset(sourceAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(segment, ValueLayout.JAVA_FLOAT_UNALIGNED, offset, target, 0, target.length);
	}

	@Override
	public synchronized void copyRangeToArray(final long sourceAddress, final long[] target) {
		final int id = getID(sourceAddress);
		final long offset = getOffset(sourceAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(segment, ValueLayout.JAVA_LONG_UNALIGNED, offset, target, 0, target.length);
	}

	@Override
	public synchronized  void copyRangeToArray(final long sourceAddress, final double[] target) {
		final int id = getID(sourceAddress);
		final long offset = getOffset(sourceAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(segment, ValueLayout.JAVA_DOUBLE_UNALIGNED, offset, target, 0, target.length);
	}

	@Override
	public synchronized void copyArrayToAddress(final byte[] array, final long targetAddress) {
		final int id = getID(targetAddress);
		final long offset = getOffset(targetAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_BYTE, offset, array.length);
	}

//	/**
//	 * Does not support boolean[], see
//	 * https://bugs.openjdk.org/browse/JDK-8345976
//	 */
//	@Override
//	public synchronized void copyArrayToAddress(final boolean[] array, final long targetAddress) {
//		final int id = getID(targetAddress);
//		final long offset = getOffset(targetAddress);
//		final MemorySegment segment = this.getMemorySegment(id);
//
//		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_BOOLEAN, offset, array.length);
//	}
	
	@Override
	public synchronized void copyArrayToAddress(final boolean[] array, final long targetAddress) {
		final int id = getID(targetAddress);
		final long offset = getOffset(targetAddress);
		final MemorySegment segment = this.getMemorySegment(id);
			
		for (int i = 0; i < array.length; i++) {
			segment.set(ValueLayout.JAVA_BOOLEAN, offset+i, array[i]);
		}
	}

	@Override
	public synchronized void copyArrayToAddress(final short[] array, final long targetAddress) {
		final int id = getID(targetAddress);
		final long offset = getOffset(targetAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_SHORT_UNALIGNED, offset, array.length);
	}

	@Override
	public synchronized void copyArrayToAddress(final char[] array, final long targetAddress) {
		final int id = getID(targetAddress);
		final long offset = getOffset(targetAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_CHAR_UNALIGNED, offset, array.length);
	}

	@Override
	public synchronized void copyArrayToAddress(final int[] array, final long targetAddress) {
		final int id = getID(targetAddress);
		final long offset = getOffset(targetAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_INT_UNALIGNED, offset, array.length);
	}

	@Override
	public synchronized void copyArrayToAddress(final float[] array, final long targetAddress) {
		final int id = getID(targetAddress);
		final long offset = getOffset(targetAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_FLOAT_UNALIGNED, offset, array.length);
	}

	@Override
	public synchronized void copyArrayToAddress(final long[] array, final long targetAddress) {
		final int id = getID(targetAddress);
		final long offset = getOffset(targetAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_LONG_UNALIGNED, offset, array.length);
	}

	@Override
	public synchronized void copyArrayToAddress(final double[] array, final long targetAddress) {
		final int id = getID(targetAddress);
		final long offset = getOffset(targetAddress);
		final MemorySegment segment = this.getMemorySegment(id);
		
		MemorySegment.copy(array, 0, segment, ValueLayout.JAVA_DOUBLE_UNALIGNED, offset, array.length);
	}

	
	///////////////////////////////////////////////////////////////////////////
	// Fields    //
	//////////////
	///
	@Override
	public synchronized long objectFieldOffset(final Field field) {
		return this.objectFieldOffset(field.getDeclaringClass(), field);
	}

	@Override
	public synchronized long[] objectFieldOffsets(final Field... fields) {
		final Class<?> mostSpecificDeclaringClass = determineMostSpecificDeclaringClass(fields);
		return this.objectFieldOffsets(mostSpecificDeclaringClass, fields);
	}

	@Override
	public synchronized long objectFieldOffset(final Class<?> objectClass, final Field field) {
		final Field[] objectFields = this.ensureRegisteredObjectFields(objectClass);

		return ForeignMemoryAccessor.objectFieldOffset(objectFields, field);
	}

	@Override
	public synchronized long[] objectFieldOffsets(final Class<?> objectClass, final Field... fields) {
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
	
	public static final Class<?> determineMostSpecificDeclaringClass(final Field[] fields)
	{
		if(XArrays.hasNoContent(fields))
		{
			return null;
		}
		
		Class<?> c = fields[0].getDeclaringClass();
		for(int i = 1; i < fields.length; i++)
		{
			// if the current declaring class is not c, but c is a super class, then the current must be more specific.
			if(fields[i].getDeclaringClass() != c && c.isAssignableFrom(fields[i].getDeclaringClass()))
			{
				c = fields[i].getDeclaringClass();
			}
		}
		
		// at this point, c point to the most specific ("most childish"? :D) class of all fields' declaring classes.
		return c;
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
		return JdkInternals.createHeapMemoryStatistics();
	}

	@Override
	public MemoryStatistics createNonHeapMemoryStatistics() {
		return JdkInternals.createNonHeapMemoryStatistics();
	}

	@Override
	public synchronized long volatileGet_long(final Object subject, final long address) {
		
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		
		final VarHandle handle = ValueLayout.JAVA_LONG.varHandle();
		return (long) handle.getVolatile(segment, offset);
	}

	@Override
	public synchronized  void volatileSet_long(final Object subject, final long address, final long value) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		
		final VarHandle handle = ValueLayout.JAVA_LONG.varHandle();
		handle.setVolatile(segment, offset, value);
	}

	@Override
	public synchronized boolean compareAndSwap_int(final Object subject, final long address, final int expected, final int replacement) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		
		final VarHandle handle = ValueLayout.JAVA_INT.varHandle();
		return handle.compareAndSet(segment, offset, expected, replacement);
	}

	@Override
	public synchronized boolean compareAndSwap_long(final Object subject, final long address, final long expected, final long replacement) {
		final int id = getID(address);
		final long offset = getOffset(address);
		final MemorySegment segment = this.getMemorySegment(id);
		
		final VarHandle handle = ValueLayout.JAVA_LONG.varHandle();
		return handle.compareAndSet(segment, offset, expected, replacement);
		
	}

	@Override
	public synchronized boolean compareAndSwapObject(final Object subject, final long offset, final Object expected, final Object replacement) {
		notImplemented();
		return false;
	}

	public void info() {
		final int bufferCount = this.bufferRegistry.size();
		final int segmentsCount = this.memorySegments.size();
		
		logger.info(""
				+ "\n\tcurrent Buffers: {}, segments: {}"
				+ "\n\tbufferCreations {}, bufferDeletions {}"
				+ "\n\tnativeCreations {}, nativeDeletions {}"
				+ "\n\tallocatedBufferMemory: {}, allocatedNativeMemory: {}"
				+ "\n\tdeletedSegments: {}",
				bufferCount, segmentsCount,
				this.bufferCreations, this.bufferDeletions,
				this.nativeCreations, this.nativeDeletions,
				this.allocatedBufferMemory, this.allocatedNativeMemory,
				this.closedSegments
			);
		
		final TreeMap<Long, Integer> segmentSizes = new TreeMap<Long, Integer>();
		this.memorySegments.forEach( (k,v) ->
				segmentSizes.merge(v.memorySegment.byteSize(), 1, (k1, v1) -> k1+v1	));
		
		logger.debug("SegmentSizes: {}", segmentSizes);
				
	}

	public void waitForCleanup() {
				
		while(!this.closingQueue.isEmpty()) {
			try {
				Thread.sleep(Duration.ofMillis(50));
			} catch(final InterruptedException e) {
				logger.debug("Thread sleep interrupted!");
			}
		}
		logger.debug("closingQueue size {}", this.closingQueue.size());
		
	}

}
