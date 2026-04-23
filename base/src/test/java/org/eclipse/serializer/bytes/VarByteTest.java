package org.eclipse.serializer.bytes;

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

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;


class VarByteTest
{
	// -------------------------------------------------------------------------
	// New() factory
	// -------------------------------------------------------------------------

	@Test
	void New_returnsNonNullInstance()
	{
		assertNotNull(VarByte.New());
	}

	@Test
	void New_initialSizeIsZero()
	{
		assertEquals(0, VarByte.New().size);
	}

	@Test
	void New_defaultCapacityIs64()
	{
		assertEquals(64, VarByte.New().data.length);
	}

	// -------------------------------------------------------------------------
	// New(initialCapacity) factory — capacity rounding to power of 2
	// -------------------------------------------------------------------------

	@Test
	void New_withCapacity0_roundsUpToMinCapacity4()
	{
		assertEquals(4, VarByte.New(0).data.length);
	}

	@Test
	void New_withCapacity1_roundsUpToMinCapacity4()
	{
		assertEquals(4, VarByte.New(1).data.length);
	}

	@Test
	void New_withCapacity4_stays4()
	{
		assertEquals(4, VarByte.New(4).data.length);
	}

	@Test
	void New_withCapacity5_roundsUpTo8()
	{
		assertEquals(8, VarByte.New(5).data.length);
	}

	@Test
	void New_withCapacity64_stays64()
	{
		assertEquals(64, VarByte.New(64).data.length);
	}

	@Test
	void New_withCapacity65_roundsUpTo128()
	{
		assertEquals(128, VarByte.New(65).data.length);
	}

	@Test
	void New_withNegativeCapacity_throwsIllegalArgument()
	{
		assertThrows(IllegalArgumentException.class, () -> VarByte.New(-1));
	}

	// -------------------------------------------------------------------------
	// append(byte[], int, int)
	// -------------------------------------------------------------------------

	@Test
	void append_storesBytes()
	{
		final VarByte vb = VarByte.New();
		vb.append(new byte[]{1, 2, 3}, 0, 3);
		assertEquals(3, vb.size);
		assertArrayEquals(new byte[]{1, 2, 3}, Arrays.copyOf(vb.data, vb.size));
	}

	@Test
	void append_withOffset_storesSlice()
	{
		final VarByte vb  = VarByte.New();
		final byte[]  src = {10, 20, 30, 40, 50};
		vb.append(src, 1, 3);
		assertEquals(3, vb.size);
		assertArrayEquals(new byte[]{20, 30, 40}, Arrays.copyOf(vb.data, vb.size));
	}

	@Test
	void append_returnsThis_allowsChaining()
	{
		final VarByte vb = VarByte.New();
		assertSame(vb, vb.append(new byte[]{7}, 0, 1));
	}

	@Test
	void append_multipleCallsAccumulateData()
	{
		final VarByte vb = VarByte.New();
		vb.append(new byte[]{1, 2}, 0, 2);
		vb.append(new byte[]{3, 4}, 0, 2);
		assertEquals(4, vb.size);
		assertArrayEquals(new byte[]{1, 2, 3, 4}, Arrays.copyOf(vb.data, vb.size));
	}

	@Test
	void append_zeroLength_doesNotChangeSize()
	{
		final VarByte vb = VarByte.New();
		vb.append(new byte[]{99}, 0, 0);
		assertEquals(0, vb.size);
	}

	// -------------------------------------------------------------------------
	// ensureFreeCapacity — growth
	// -------------------------------------------------------------------------

	@Test
	void ensureFreeCapacity_triggersGrowthWhenBufferFull()
	{
		final VarByte vb       = VarByte.New(4);
		final byte[]  fiveBytes = {1, 2, 3, 4, 5};
		vb.append(fiveBytes, 0, 5);
		assertTrue(vb.data.length >= 8, "capacity should have grown to at least 8");
		assertEquals(5, vb.size);
	}

	@Test
	void ensureFreeCapacity_preservesExistingData()
	{
		final VarByte vb      = VarByte.New(4);
		final byte[]  first   = {10, 20, 30, 40};
		final byte[]  second  = {50};
		vb.append(first, 0, 4);
		vb.append(second, 0, 1);
		assertArrayEquals(new byte[]{10, 20, 30, 40, 50}, Arrays.copyOf(vb.data, vb.size));
	}

	// -------------------------------------------------------------------------
	// clear()
	// -------------------------------------------------------------------------

	@Test
	void clear_resetsSizeToZero()
	{
		final VarByte vb = VarByte.New();
		vb.append(new byte[]{1, 2, 3}, 0, 3);
		vb.clear();
		assertEquals(0, vb.size);
	}

	@Test
	void clear_zeroesEntireBackingArray_notJustActiveRegion()
	{
		final VarByte vb = VarByte.New(4);
		vb.append(new byte[]{1, 2, 3, 4}, 0, 4);
		vb.clear();
		for(final byte b : vb.data)
		{
			assertEquals(0, b, "all bytes in backing array must be zeroed after clear()");
		}
	}

	@Test
	void clear_returnsThis()
	{
		final VarByte vb = VarByte.New();
		assertSame(vb, vb.clear());
	}

	// -------------------------------------------------------------------------
	// toString()
	// -------------------------------------------------------------------------

	@Test
	void toString_emptyReturnsEmptyString()
	{
		assertEquals("", VarByte.New().toString());
	}

	@Test
	void toString_returnsStringFromBytes() throws Exception
	{
		final VarByte vb    = VarByte.New();
		final byte[]  hello = "hello".getBytes(StandardCharsets.US_ASCII);
		vb.append(hello, 0, hello.length);
		assertEquals("hello", vb.toString(StandardCharsets.US_ASCII));
	}

	// -------------------------------------------------------------------------
	// writeExternal / readExternal — direct roundtrip (small data)
	// -------------------------------------------------------------------------

	@Test
	void writeExternal_readExternal_roundtripSmallData() throws Exception
	{
		final VarByte original = VarByte.New();
		original.append(new byte[]{10, 20, 30}, 0, 3);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try(final ObjectOutputStream oos = new ObjectOutputStream(baos))
		{
			original.writeExternal(oos);
		}

		final VarByte restored = VarByte.New();
		try(final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())))
		{
			restored.readExternal(ois);
		}

		assertEquals(3, restored.size);
		assertArrayEquals(new byte[]{10, 20, 30}, Arrays.copyOf(restored.data, restored.size));
	}

	/**
	 * BUG: writeExternal uses out.write(int) which writes only the low 8 bits of size.
	 * For a VarByte with 256+ bytes, the stored size wraps around:
	 *   size=256 is written as 0, size=257 as 1, etc.
	 * On readExternal the size read back is truncated, causing data loss.
	 */
	@Test
	void writeExternal_readExternal_silentlyCorruptsDataWhenSizeExceeds255_bug() throws Exception
	{
		final int    dataSize = 256;
		final byte[] bigData  = new byte[dataSize];
		Arrays.fill(bigData, (byte) 42);

		final VarByte original = VarByte.New(dataSize);
		original.append(bigData, 0, dataSize);
		assertEquals(dataSize, original.size);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try(final ObjectOutputStream oos = new ObjectOutputStream(baos))
		{
			original.writeExternal(oos);
		}

		final VarByte restored = VarByte.New();
		try(final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())))
		{
			restored.readExternal(ois);
		}

		// size=256 truncated to 8 bits = 0; data is silently lost
		assertEquals(dataSize, restored.size,
			"roundtrip should preserve all " + dataSize + " bytes but size is truncated to 8 bits");
	}

	/**
	 * BUG: VarByte implements Externalizable but its no-arg constructor is private.
	 * Java's Externalizable contract requires a public no-arg constructor so that
	 * ObjectInputStream can instantiate the class before calling readExternal().
	 * ObjectInputStream.readObject() therefore throws InvalidClassException.
	 */
	@Test
	void objectInputStream_readObject_failsBecauseNoArgConstructorIsPrivate_bug() throws IOException
	{
		final VarByte original = VarByte.New();
		original.append(new byte[]{1, 2, 3}, 0, 3);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try(final ObjectOutputStream oos = new ObjectOutputStream(baos))
		{
			oos.writeObject(original);
		}

		assertThrows(InvalidClassException.class, () ->
		{
			try(final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())))
			{
				ois.readObject();
			}
		});
	}

	/**
	 * BUG: readExternal reads size with in.read() which returns -1 on end-of-stream.
	 * When -1 is stored as this.size, subsequent calls such as toString() will
	 * throw StringIndexOutOfBoundsException.
	 */
	@Test
	void readExternal_onEof_storesNegativeSize_bug() throws Exception
	{
		// Simulate an ObjectInput whose read() immediately returns -1 (EOF)
		final ObjectInput eofInput = new ObjectInputStream(new ByteArrayInputStream(createObjectStreamHeader()))
		{
			@Override
			public int read() { return -1; }
		};

		final VarByte vb = VarByte.New();
		vb.readExternal(eofInput);

		assertTrue(vb.size >= 0,
			"size must not be negative after readExternal; in.read() returned -1 (EOF) and was stored as-is");
	}

	// -------------------------------------------------------------------------
	// helpers
	// -------------------------------------------------------------------------

	/** Returns the 4-byte ObjectOutputStream stream header so ObjectInputStream can be constructed. */
	private static byte[] createObjectStreamHeader() throws IOException
	{
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new ObjectOutputStream(baos).close();
		return baos.toByteArray();
	}
}
