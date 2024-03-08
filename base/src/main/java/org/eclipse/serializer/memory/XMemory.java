package org.eclipse.serializer.memory;

/*-
 * #%L
 * Eclipse Serializer Base
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

import static org.eclipse.serializer.util.X.notNull;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Predicate;

import org.eclipse.serializer.exceptions.InstantiationRuntimeException;
import org.eclipse.serializer.exceptions.UnhandledPlatformError;
import org.eclipse.serializer.memory.android.AndroidAdapter;
import org.eclipse.serializer.memory.sun.JdkMemoryAccessor;
import org.eclipse.serializer.util.X;


/**
 * Util class for low-level VM memory operations and information that makes the call site independent of
 * a certain JVM implementation (e.g. java.misc.Unsafe).
 *
 */
public final class XMemory
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	public static MemoryAccessor       MEMORY_ACCESSOR         ;
	public static MemoryAccessor       MEMORY_ACCESSOR_REVERSED;
	static MemorySizeProperties MEMORY_SIZE_PROPERTIES  ;

	static
	{
		initializeMemoryAccess();
	}

	private static VmCheck[] createVmChecks()
	{
		return X.array
		(
			/* See:
			 * https://developer.android.com/reference/java/lang/System#getProperties()
			 * https://stackoverflow.com/questions/4519556/how-to-determine-if-my-app-is-running-on-android
			 */
			VmCheckEquality("Supported Standard Android",
                            AndroidAdapter::setupFull,
                            entry("java.vendor"   , "The Android Project"),
                            entry("java.vm.vendor", "The Android Project")
			),

			/*
			 * There are non-standard, "cheap", "hacked", whatever implementations of Android that
			 * differ from the standard android. Since those are not reliable to support the required
			 * functionality, they are filtered out, here.
			 * Of course there is still the possibility that an implementation returns the correct
			 * vendor but is not "compatible enough". But then that's simply a platform insufficiency
			 * that can't be handled here.
			 * The purpose of this check is to create a more informative exception for recognizably
			 * diverging cases instead of just defaulting to the JdkInternals and getting a
			 * weird error of it not working.
			 */
			VmCheckContained("ERROR: UNHANDLED Android",
				XMemory::throwUnhandledPlatformException,
				entry("java.vendor"   , "Android"),
				entry("java.vm.vendor", " Android")
			),
			
			/*
			 * See
			 * https://www.graalvm.org/sdk/javadoc/index.html?constant-values.html
			 * 
			 * Check if we run in a GraalVM native image, if so we need to switch to the android Memory accessing/handling
			 * 
			 */
			VmCheckNotBlank("GraalVM native image",
				AndroidAdapter::setupFull,
				entry("org.graalvm.nativeimage.imagecode")
			)

			// add additional checks here
		);
	}

	private static String systemPropertyToString(final String key)
	{
		return key + ": " + System.getProperty(key, "[null]");
	}

	static final void throwUnhandledPlatformException()
	{
		throw new UnhandledPlatformError(
			"Unhandled Java platform: "
			+ systemPropertyToString("java.vendor") + ", "
			+ systemPropertyToString("java.vm.vendor")
		);
	}

	private static String entry(final String key)
	{
		return key;
	}
	
	private static String[] entry(final String key, final String value)
	{
		return new String[]{key, value};
	}

	private static void initializeMemoryAccess()
	{
		// no sense in permanently occupying memory with data that is only used exactly once during initialization.
		final VmCheck[] vmChecks = createVmChecks();

		for(final VmCheck vmCheck : vmChecks)
		{
			// can either set an Memory accessing/handling implementation or throw an Error.
			if(vmCheck.check())
			{
				return;
			}
		}

		/* (18.11.2019 TM)NOTE:
		 * If no specific vm check applied, the default initialization is used, assuming a fully
		 * JDK/-Unsafe-compatible JVM. It might not seem that way, but this is actually the normal case.
		 * Tests showed that almost all Java VM vendors fully support Unsafe. This is quite plausible:
		 * They want to draw Java developers/applications onto their platform, so they try to provide
		 * as much compatibility as possible, including Unsafe.
		 * So far, the only known Java VM to not fully support Unsafe is Android.
		 */
		setMemoryHandling(JdkMemoryAccessor.New());
	}

	private static VmCheck VmCheckEquality(
		final String      name                        ,
		final Runnable    action                      ,
		final String[]... systemPropertyChecksEquality
	)
	{
		return new VmCheck(
			name,
			SystemPropertyCheckEquality(
				systemPropertyChecksEquality
			),
			action
		);
	}

	private static VmCheck VmCheckContained(
		final String      name                         ,
		final Runnable    action                       ,
		final String[]... systemPropertyChecksContained
	)
	{
		return new VmCheck(
			name,
			SystemPropertyCheckContained(
				systemPropertyChecksContained
			),
			action
		);
	}
	
	private static VmCheck VmCheckNotBlank(
		final String      name                         ,
		final Runnable    action                       ,
		final String...   systemPropertyChecksNotNull
	)
	{
		return new VmCheck(
			name,
			systemPropertyCheckNotBlank(
				systemPropertyChecksNotNull
			),
			action
		);
	}

	private static Predicate<VmCheck> SystemPropertyCheckEquality(
		final String[][] systemPropertyChecksEquality
	)
	{
		return SystemPropertyCheck(systemPropertyChecksEquality, new String[0][], new String[0]);
	}

	private static Predicate<VmCheck> SystemPropertyCheckContained(
		final String[][] systemPropertyChecksContained
	)
	{
		return SystemPropertyCheck(new String[0][], systemPropertyChecksContained, new String[0]);
	}
	
	private static Predicate<VmCheck> systemPropertyCheckNotBlank(
		final String[] systemPropertyChecksNotBlank
	)
	{
		return SystemPropertyCheck(new String[0][], new String[0][], systemPropertyChecksNotBlank);
	}

	private static Predicate<VmCheck> SystemPropertyCheck(
		final String[][] systemPropertyChecksEquality,
		final String[][] systemPropertyChecksContained,
		final String[]   systemPropertyChecksNotBlank
	)
	{
		return check ->
		{
			for(final String[] s : systemPropertyChecksEquality)
			{
				if(s == null)
				{
					continue;
				}
				if(System.getProperty(s[0], "").equals(s[1]))
				{
					return true;
				}
			}

			for(final String[] s : systemPropertyChecksContained)
			{
				if(s == null)
				{
					continue;
				}
				if(System.getProperty(s[0], "").toUpperCase().contains(s[1].toUpperCase()))
				{
					return true;
				}
			}
			
			for(final String s: systemPropertyChecksNotBlank)
			{
				if(s == null)
				{
					continue;
				}
				if(!System.getProperty(s, "").isEmpty())
				{
					return true;
				}
			}

			// no check applied
			return false;
		};
	}


	static final class VmCheck
	{
		final String                   name  ;
		final Predicate<VmCheck> tester;
		final Runnable                 action;

		VmCheck(
			final String                   name  ,
			final Predicate<VmCheck> tester,
			final Runnable                 action
		)
		{
			super();
			this.name   = name  ;
			this.tester = tester;
			this.action = action;
		}

		final boolean test()
		{
			return this.tester.test(this);
		}

		final boolean check()
		{
			if(this.test())
			{
				this.action.run();
				return true;
			}

			return false;
		}
	}

	public static final synchronized <H extends MemoryAccessor & MemorySizeProperties> void setMemoryHandling(
		final H memoryHandler
	)
	{
		setMemoryHandling(memoryHandler, memoryHandler);
	}

	public static final synchronized void setMemoryAccessor(
		final MemoryAccessor memoryAccessor
	)
	{
		setMemoryHandling(memoryAccessor, MemorySizeProperties.Unsupported());
	}

	public static final synchronized void setMemoryHandling(
		final MemoryAccessor       memoryAccessor      ,
		final MemorySizeProperties memorySizeProperties
	)
	{
		MEMORY_ACCESSOR          = notNull(memoryAccessor);
		MEMORY_ACCESSOR_REVERSED = notNull(memoryAccessor.toReversing());
		MEMORY_SIZE_PROPERTIES   = notNull(memorySizeProperties);
	}

	public static final synchronized MemoryAccessor memoryAccessor()
	{
		return MEMORY_ACCESSOR;
	}

	public static final void guaranteeUsability()
	{
		MEMORY_ACCESSOR.guaranteeUsability();
	}



	// direct byte buffer handling //

	public static final long getDirectByteBufferAddress(final ByteBuffer directBuffer)
	{
		return MEMORY_ACCESSOR.getDirectByteBufferAddress(directBuffer);
	}

	public static final boolean deallocateDirectByteBuffer(final ByteBuffer directBuffer)
	{
		return MEMORY_ACCESSOR.deallocateDirectByteBuffer(directBuffer);
	}


	// memory size querying logic //

	/**
	 * Arbitrary value that coincidentally matches most hardware's standard page
	 * sizes without being hard-tied to an actual pageSize system value.
	 * So this value is an educated guess and almost always a "good" value when
	 * paged-sized-ish buffer sizes are needed, while still not being at the
	 * mercy of an OS's JVM implementation.
	 *
	 * @return a "good" value for a paged-sized-ish default buffer size.
	 */
	public static final int defaultBufferSize()
	{
		// source: https://en.wikipedia.org/wiki/Page_(computer_memory)
		return 4096;
	}

	public static final int byteSizeInstance(final Class<?> c)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeInstance(c);
	}

	public static final int byteSizeObjectHeader(final Class<?> c)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeObjectHeader(c);
	}

	public static final long byteSizeArrayObject(final long elementCount)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeArrayObject(elementCount);
	}

	public static final int byteSizePrimitive(final Class<?> type)
	{
		// Missing JDK functionality. Roughly ordered by probability.
		if(type == int.class)
		{
			return byteSize_int();
		}
		if(type == long.class)
		{
			return byteSize_long();
		}
		if(type == double.class)
		{
			return byteSize_double();
		}
		if(type == char.class)
		{
			return byteSize_char();
		}
		if(type == boolean.class)
		{
			return byteSize_boolean();
		}
		if(type == byte.class)
		{
			return byteSize_byte();
		}
		if(type == float.class)
		{
			return byteSize_float();
		}
		if(type == short.class)
		{
			return byteSize_short();
		}

		// intentionally covers void.class
		throw new IllegalArgumentException();
	}

	public static final int byteSize_byte()
	{
		return Byte.BYTES;
	}

	public static final int byteSize_boolean()
	{
		return Byte.BYTES;
	}

	public static final int byteSize_short()
	{
		return Short.BYTES;
	}

	public static final int byteSize_char()
	{
		return Character.BYTES;
	}

	public static final int byteSize_int()
	{
		return Integer.BYTES;
	}

	public static final int byteSize_float()
	{
		return Float.BYTES;
	}

	public static final int byteSize_long()
	{
		return Long.BYTES;
	}

	public static final int byteSize_double()
	{
		return Double.BYTES;
	}

	public static final int byteSizeReference()
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeReference();
	}

	// field offset abstraction //

	public static final long objectFieldOffset(final Field field)
	{
		return MEMORY_ACCESSOR.objectFieldOffset(field);
	}

	public static final long[] objectFieldOffsets(final Class<?> c, final Field[] fields)
	{
		return MEMORY_ACCESSOR.objectFieldOffsets(c, fields);
	}



	// address-based getters for primitive values //

	public static final byte get_byte(final long address)
	{
		return MEMORY_ACCESSOR.get_byte(address);
	}

	public static final boolean get_boolean(final long address)
	{
		return MEMORY_ACCESSOR.get_boolean(address);
	}

	public static final short get_short(final long address)
	{
		return MEMORY_ACCESSOR.get_short(address);
	}

	public static final char get_char(final long address)
	{
		return MEMORY_ACCESSOR.get_char(address);
	}

	public static final int get_int(final long address)
	{
		return MEMORY_ACCESSOR.get_int(address);
	}

	public static final float get_float(final long address)
	{
		return MEMORY_ACCESSOR.get_float(address);
	}

	public static final long get_long(final long address)
	{
		return MEMORY_ACCESSOR.get_long(address);
	}

	public static final double get_double(final long address)
	{
		return MEMORY_ACCESSOR.get_double(address);
	}

	// note: getting a pointer from a non-Object-relative address makes no sense.



	// object-based getters for primitive values and references //

	public static final byte get_byte(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_byte(instance, offset);
	}

	public static final boolean get_boolean(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_boolean(instance, offset);
	}

	public static final short get_short(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_short(instance, offset);
	}

	public static final char get_char(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_char(instance, offset);
	}

	public static final int get_int(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_int(instance, offset);
	}

	public static final float get_float(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_float(instance, offset);
	}

	public static final long get_long(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_long(instance, offset);
	}

	public static final double get_double(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_double(instance, offset);
	}

	public static final Object getObject(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.getObject(instance, offset);
	}



	// address-based setters for primitive values //

	public static final void set_byte(final long address, final byte value)
	{
		MEMORY_ACCESSOR.set_byte(address, value);
	}

	public static final void set_boolean(final long address, final boolean value)
	{
		// where the heck is Unsafe#putBoolean(long, boolean)? Forgot to implement? Wtf?
		MEMORY_ACCESSOR.set_boolean(address, value);
	}

	public static final void set_short(final long address, final short value)
	{
		MEMORY_ACCESSOR.set_short(address, value);
	}

	public static final void set_char(final long address, final char value)
	{
		MEMORY_ACCESSOR.set_char(address, value);
	}

	public static final void set_int(final long address, final int value)
	{
		MEMORY_ACCESSOR.set_int(address, value);
	}

	public static final void set_float(final long address, final float value)
	{
		MEMORY_ACCESSOR.set_float(address, value);
	}

	public static final void set_long(final long address, final long value)
	{
		MEMORY_ACCESSOR.set_long(address, value);
	}

	public static final void set_double(final long address, final double value)
	{
		MEMORY_ACCESSOR.set_double(address, value);
	}

	// note: setting a pointer to a non-Object-relative address makes no sense.



	// object-based setters for primitive values and references //

	public static final void set_byte(final Object instance, final long offset, final byte value)
	{
		MEMORY_ACCESSOR.set_byte(instance, offset, value);
	}

	public static final void set_boolean(final Object instance, final long offset, final boolean value)
	{
		MEMORY_ACCESSOR.set_boolean(instance, offset, value);
	}

	public static final void set_short(final Object instance, final long offset, final short value)
	{
		MEMORY_ACCESSOR.set_short(instance, offset, value);
	}

	public static final void set_char(final Object instance, final long offset, final char value)
	{
		MEMORY_ACCESSOR.set_char(instance, offset, value);
	}

	public static final void set_int(final Object instance, final long offset, final int value)
	{
		MEMORY_ACCESSOR.set_int(instance, offset, value);
	}

	public static final void set_float(final Object instance, final long offset, final float value)
	{
		MEMORY_ACCESSOR.set_float(instance, offset, value);
	}

	public static final void set_long(final Object instance, final long offset, final long value)
	{
		MEMORY_ACCESSOR.set_long(instance, offset, value);
	}

	public static final void set_double(final Object instance, final long offset, final double value)
	{
		MEMORY_ACCESSOR.set_double(instance, offset, value);
	}

	public static final void setObject(final Object instance, final long offset, final Object value)
	{
		MEMORY_ACCESSOR.setObject(instance, offset, value);
	}



	// transformative byte array primitive value setters //

	public static final void set_shortInBytes(final byte[] bytes, final int index, final short value)
	{
		MEMORY_ACCESSOR.set_shortInBytes(bytes, index, value);
	}

	public static final void set_charInBytes(final byte[] bytes, final int index, final char value)
	{
		MEMORY_ACCESSOR.set_charInBytes(bytes, index, value);
	}

	public static final void set_intInBytes(final byte[] bytes, final int index, final int value)
	{
		MEMORY_ACCESSOR.set_intInBytes(bytes, index, value);
	}

	public static final void set_floatInBytes(final byte[] bytes, final int index, final float value)
	{
		MEMORY_ACCESSOR.set_floatInBytes(bytes, index, value);
	}

	public static final void set_longInBytes(final byte[] bytes, final int index, final long value)
	{
		MEMORY_ACCESSOR.set_longInBytes(bytes, index, value);
	}

	public static final void set_doubleInBytes(final byte[] bytes, final int index, final double value)
	{
		MEMORY_ACCESSOR.set_doubleInBytes(bytes, index, value);
	}



	// generic variable-length range copying //

	public static final void copyRange(final long sourceAddress, final long targetAddress, final long length)
	{
		MEMORY_ACCESSOR.copyRange(sourceAddress, targetAddress, length);
	}



	// address-to-array range copying //

	public static final void copyRangeToArray(final long sourceAddress, final byte[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}

	public static final void copyRangeToArray(final long sourceAddress, final boolean[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}

	public static final void copyRangeToArray(final long sourceAddress, final short[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}

	public static final void copyRangeToArray(final long sourceAddress, final char[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}

	public static final void copyRangeToArray(final long sourceAddress, final int[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}

	public static final void copyRangeToArray(final long sourceAddress, final float[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}

	public static final void copyRangeToArray(final long sourceAddress, final long[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}

	public static final void copyRangeToArray(final long sourceAddress, final double[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}



	// array-to-address range copying //

	public static final void copyArrayToAddress(final byte[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}

	public static final void copyArrayToAddress(final boolean[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}

	public static final void copyArrayToAddress(final short[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}

	public static final void copyArrayToAddress(final char[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}

	public static final void copyArrayToAddress(final int[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}

	public static final void copyArrayToAddress(final float[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}

	public static final void copyArrayToAddress(final long[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}

	public static final void copyArrayToAddress(final double[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}



	// special system methods, not really memory-related //

	public static final void ensureClassInitialized(final Class<?> c)
	{
		MEMORY_ACCESSOR.ensureClassInitialized(c);
	}

	public static final void ensureClassInitialized(final Class<?> c, final Iterable<Field> usedFields)
	{
		MEMORY_ACCESSOR.ensureClassInitialized(c, usedFields);
	}

	public static final <T> T instantiateBlank(final Class<T> c) throws InstantiationRuntimeException
	{
		return MEMORY_ACCESSOR.instantiateBlank(c);
	}


	public static final ByteOrder nativeByteOrder()
	{
		return ByteOrder.nativeOrder();
	}

	public static final boolean isBigEndianNativeOrder()
	{
		return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
	}

	/**
	 * Alias for {@code ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder())}.
	 * See {@link ByteBuffer#allocateDirect(int)} for details.
	 *
	 * @param capacity
	 *         The new buffer's capacity, in bytes
	 *
	 * @return a newly created direct byte buffer with the specified capacity and the platform's native byte order.
	 *
	 * @throws IllegalArgumentException
	 *         If the {@code capacity} is a negative integer.
	 *
	 * @see ByteBuffer#allocateDirect(int)
	 * @see ByteBuffer#order(ByteOrder)
	 */
	public static final ByteBuffer allocateDirectNative(final int capacity) throws IllegalArgumentException
	{
		return ByteBuffer
			.allocateDirect(capacity)
			.order(ByteOrder.nativeOrder())
		;
	}

	public static final ByteBuffer allocateDirectNative(final long capacity) throws IllegalArgumentException
	{
		return allocateDirectNative(
			X.checkArrayRange(capacity)
		);
	}

	public static final byte[] toArray(final ByteBuffer source)
	{
		final int currentSourcePosition = source.position();

		final byte[] bytes = new byte[source.remaining()];
		source.get(bytes, 0, bytes.length);

		// why would a querying methode intrinsically increase the position? WHY?
		source.position(currentSourcePosition);

		return bytes;
	}
	
	public static final byte[] toArray(final ByteBuffer[] sources)
	{
		int overallLength = 0;
		for(final ByteBuffer source : sources)
		{
			overallLength += source.remaining();
		}
		final byte[] bytes = new byte[overallLength];
		int pos = 0;
		for(final ByteBuffer source : sources)
		{
			final int length                = source.remaining();
			final int currentSourcePosition = source.position();
			
			source.get(bytes, pos, length);
			pos += length;
			
			// why would a querying methode intrinsically increase the position? WHY?
			source.position(currentSourcePosition);
		}
		return bytes;
	}
	
	public static final ByteBuffer toDirectByteBuffer(final byte[] bytes)
	{
		final ByteBuffer buffer = allocateDirectNative(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		return buffer;
	}
	
	public static final ByteBuffer slice(final ByteBuffer source, final long position, final long limit)
	{
		final ByteBuffer tmp = source.duplicate();
		tmp.limit((int)(position + limit)).position((int)position);
		return tmp.slice();
	}

	public static final long getPositionLimit(final ByteBuffer buffer)
	{
		return ((long)buffer.position() << Integer.SIZE) + buffer.limit();
	}

	public static final ByteBuffer setPositionLimit(final ByteBuffer buffer, final long positionLimit)
	{
		return setPositionLimit(buffer, (int)(positionLimit >>> Integer.SIZE), (int)positionLimit);
	}

	public static final ByteBuffer setPositionLimit(final ByteBuffer buffer, final int position, final int limit)
	{
		// must set limit first because position is validated against it!
		buffer.limit(limit);
		buffer.position(position);

		return buffer;
	}

    public static final ByteBuffer allocateDirectNativeDefault()
    {
        return allocateDirectNative(XMemory.defaultBufferSize());
    }

    // memory allocation //

    public static final long allocate(final long bytes)
    {
        return MEMORY_ACCESSOR.allocateMemory(bytes);
    }
    
    public static final long allocateCleared(final long bytes)
	{
		final long address = allocate(bytes);
		clearMemory(address, bytes);
		
		return address;
	}
    
    public static final long reallocate(final long address, final long bytes)
	{
		return MEMORY_ACCESSOR.reallocateMemory(address, bytes);
	}

	public static final void free(final long address)
	{
		MEMORY_ACCESSOR.freeMemory(address);
	}

	public static final void fillMemory(final long address, final long length, final byte value)
	{
		MEMORY_ACCESSOR.fillMemory(address, length, value);
	}
	
	public static final void clearMemory(final long address, final long length)
	{
		fillMemory(address, length, (byte)0);
	}

    /**
     * Parses a {@link String} instance to a {@link ByteOrder} instance according to {@code ByteOrder#toString()}
     * or throws an {@link IllegalArgumentException} if the passed string does not match exactly one of the
     * {@link ByteOrder} constant instances' string representation.
     *
     * @param name the string representing the {@link ByteOrder} instance to be parsed.
     * @return the recognized {@link ByteOrder}
     * @throws IllegalArgumentException if the string can't be recognized as a {@link ByteOrder} constant instance.
     * @see ByteOrder#toString()
     */
    public static final ByteOrder parseByteOrder(final String name)
    {
        if(name.equals(ByteOrder.BIG_ENDIAN.toString()))
        {
            return ByteOrder.BIG_ENDIAN;
        }
        if(name.equals(ByteOrder.LITTLE_ENDIAN.toString()))
        {
            return ByteOrder.LITTLE_ENDIAN;
        }

        throw new IllegalArgumentException("Unknown ByteOrder: \"" + name + "\"");
    }

    public static final byte[] toArray(final ByteBuffer source, final int position, final int length)
    {
        final long plState = XMemory.getPositionLimit(source);
        XMemory.setPositionLimit(source, position, position + length);

        final byte[] bytes = new byte[length];
        source.get(bytes, 0, length);

        // why would a querying methode intrinsically increase the position? WHY?
        XMemory.setPositionLimit(source, plState);

        return bytes;
    }

	// get volatile //

	public static final long volatileGet_long(final Object subject, final long offset)
	{
		return MEMORY_ACCESSOR.volatileGet_long(subject, offset);
	}


	// set volatile //

	public static final void volatileSet_long(final Object subject, final long offset, final long value)
	{
		MEMORY_ACCESSOR.volatileSet_long(subject, offset, value);
	}


	// compare and swap //

	public static final boolean compareAndSwap_int(
		final Object subject    ,
		final long   offset     ,
		final int    expected   ,
		final int    replacement
	)
	{
		return MEMORY_ACCESSOR.compareAndSwap_int(subject, offset, expected, replacement);
	}

	public static final boolean compareAndSwap_long(
		final Object subject    ,
		final long   offset     ,
		final long   expected   ,
		final long   replacement
	)
	{
		return MEMORY_ACCESSOR.compareAndSwap_long(subject, offset, expected, replacement);
	}

	public static final boolean compareAndSwapObject(
		final Object subject    ,
		final long   offset     ,
		final Object expected   ,
		final Object replacement
	)
	{
		return MEMORY_ACCESSOR.compareAndSwapObject(subject, offset, expected, replacement);
	}
    
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 *
	 * @throws UnsupportedOperationException when called
	 */
	private XMemory()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
