package org.eclipse.serializer.persistence.binary.types;

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

import static org.eclipse.serializer.util.X.notNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Consumer;

import org.eclipse.serializer.collections.HashMapIdId;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.exceptions.BinaryPersistenceExceptionStateInvalidLength;
import org.eclipse.serializer.persistence.types.PersistenceObjectIdAcceptor;
import org.eclipse.serializer.util.BufferSizeProviderIncremental;
import org.eclipse.serializer.util.X;


public class ChunksBuffer extends Binary implements MemoryRangeReader
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final int DEFAULT_BUFFERS_CAPACITY = Long.BYTES;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static ChunksBuffer New(
		final ChunksBuffer[]                channelBuffers    ,
		final BufferSizeProviderIncremental bufferSizeProvider
	)
	{
		return new ChunksBuffer(
			notNull(channelBuffers),
			notNull(bufferSizeProvider),
			false
		);
	}

	public static ChunksBuffer New(
		final ChunksBuffer[]                channelBuffers        ,
		final BufferSizeProviderIncremental bufferSizeProvider    ,
		final boolean                       deduplicationEnabled
	)
	{
		return new ChunksBuffer(
			notNull(channelBuffers),
			notNull(bufferSizeProvider),
			deduplicationEnabled
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final ChunksBuffer[]                channelBuffers    ;
	private final BufferSizeProviderIncremental bufferSizeProvider;

	private ByteBuffer[] buffers                  ;
	private int          currentBuffersIndex      ;
	private ByteBuffer   currentBuffer            ;
	private long         currentBufferStartAddress;
	private long         currentAddress           ;
	private long         currentBound             ;
	private long         totalLength              ;

	// entity deduplication (only active when deduplicationEnabled is true)
	private final boolean      deduplicationEnabled;
	private       HashMapIdId  entityIndex   ;
	private       boolean      hasDuplicates ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	ChunksBuffer(
		final ChunksBuffer[]                channelBuffers        ,
		final BufferSizeProviderIncremental bufferSizeProvider    ,
		final boolean                       deduplicationEnabled
	)
	{
		super();
		this.channelBuffers        = channelBuffers       ;
		this.bufferSizeProvider    = bufferSizeProvider    ;
		this.deduplicationEnabled  = deduplicationEnabled  ;
		this.entityIndex           = deduplicationEnabled ? HashMapIdId.New() : null;
		this.setCurrent((this.buffers = new ByteBuffer[DEFAULT_BUFFERS_CAPACITY])[this.currentBuffersIndex = 0] =
			XMemory.allocateDirectNative(bufferSizeProvider.provideBufferSize()))
		;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final Binary channelChunk(final int channelIndex)
	{
		return this.channelBuffers[channelIndex];
	}
	
	@Override
	public final int channelCount()
	{
		return this.channelBuffers.length;
	}

	private void setCurrent(final ByteBuffer byteBuffer)
	{
		this.currentBufferStartAddress = XMemory.getDirectByteBufferAddress(this.currentBuffer = byteBuffer);
		this.currentBound = (this.currentAddress = this.currentBufferStartAddress) + byteBuffer.capacity();
		byteBuffer.clear();
	}
	
	private void updateCurrentBufferPosition()
	{
		final long contentLength = this.currentAddress - this.currentBufferStartAddress;
		
		this.currentBuffer.position(X.checkArrayRange(contentLength)).flip();
		
		this.totalLength += contentLength;
	}

	private boolean isEmptyCurrentBuffer()
	{
		return this.currentAddress == this.currentBufferStartAddress;
	}

	private void enlargeBufferCapacity(final int bufferCapacity)
	{
		// if current buffer is still empty, replace it instead of enqueuing a new one to avoid storing "dummy" chunks
		if(this.isEmptyCurrentBuffer())
		{
			XMemory.deallocateDirectByteBuffer(this.currentBuffer);
			this.allocateNewCurrent(bufferCapacity);
			return;
		}
		this.updateCurrentBufferPosition();
		this.addBuffer(bufferCapacity);
	}

	private int calculateNewBufferCapacity(final long requiredCapacity)
	{
		final long defaultBufferCapacity = this.bufferSizeProvider.provideIncrementalBufferSize();
		
		// never allocate less than the default, but more if needed.
		return X.checkArrayRange(Math.max(requiredCapacity, defaultBufferCapacity));
	}

	private void ensureFreeStoreCapacity(final long requiredCapacity)
	{
		if(this.currentAddress + requiredCapacity > this.currentBound)
		{
			this.enlargeBufferCapacity(this.calculateNewBufferCapacity(requiredCapacity));
		}
	}

	private void incrementBuffersCount()
	{
		if(++this.currentBuffersIndex >= this.buffers.length)
		{
			// shifting overflow is ignored because it is highly unlikely to ever reach 1 billion buffers ^^
			System.arraycopy(
				this.buffers,
				0,
				this.buffers = new ByteBuffer[this.buffers.length << 1],
				0,
				this.currentBuffersIndex
			);
		}
	}

	private void addBuffer(final int bufferCapacity)
	{
		this.incrementBuffersCount();
		this.allocateNewCurrent(bufferCapacity);
	}

	private void allocateNewCurrent(final int bufferCapacity)
	{
		this.setCurrent(this.buffers[this.currentBuffersIndex] = XMemory.allocateDirectNative(bufferCapacity));
	}

	@Override
	public final void clear()
	{
		final ByteBuffer[] buffers = this.buffers;
		for(int i = this.currentBuffersIndex; i >= 1; i--)
		{
			XMemory.deallocateDirectByteBuffer(buffers[i]);
			buffers[i] = null;
		}
		this.setCurrent(buffers[this.currentBuffersIndex = 0]);

		if(this.deduplicationEnabled)
		{
			this.entityIndex.clear();
			this.hasDuplicates = false;
		}
	}

	/**
	 * It is completely the caller's responsibility that the passed array contains
	 * a valid [LEN][TID][OID][data] byte sequence.
	 *
	 */
	@Override
	public void readMemory(final long address, final long length)
	{
		this.ensureFreeStoreCapacity(length);
		XMemory.copyRange(address, this.currentAddress, length);
		this.currentAddress += length;
	}

	@Override
	public final long storeEntityHeader(
		final long entityContentLength,
		final long entityTypeId       ,
		final long entityObjectId
	)
	{

		if(entityContentLength < 0)
		{
			// length has to be checked to avoid messing up the current address.
			throw new BinaryPersistenceExceptionStateInvalidLength(
				this.currentAddress, entityContentLength, entityTypeId, entityObjectId
			);
		}

		final long entityTotalLength = entityTotalLength(entityContentLength);
		this.ensureFreeStoreCapacity(entityTotalLength);

		if(this.deduplicationEnabled)
		{
			final long offsetInBuffer  = this.currentAddress - this.currentBufferStartAddress;
			final long encodedPosition = ((long)this.currentBuffersIndex << 32) | offsetInBuffer;
			if(!this.entityIndex.put(entityObjectId, encodedPosition))
			{
				this.hasDuplicates = true;
			}
		}

		this.storeEntityHeaderToAddress(this.currentAddress, entityTotalLength, entityTypeId, entityObjectId);

		// currentAddress is advanced to next entity, but this entity's content address has to be returned
		return this.address = (this.currentAddress += entityTotalLength) - entityContentLength;
	}
	
	@Override
	public final ByteBuffer[] buffers()
	{
		if(this.currentBuffer != null)
		{
			throw new IllegalStateException("Cannot return buffers of incomplete chunks");
		}

		// copy tiny array in any case to a) have no trailing nulls and b) to keep actual array hidden
		final ByteBuffer[] buffers = new ByteBuffer[this.currentBuffersIndex + 1];
		System.arraycopy(this.buffers, 0, buffers, 0, buffers.length);
		
		return buffers;
	}

	public final ChunksBuffer complete()
	{
		if(this.currentBuffer == null)
		{
			return this; // already completed
		}

		this.updateCurrentBufferPosition();

		if(this.hasDuplicates)
		{
			this.compactDuplicates();
		}

		this.currentBuffer             = null;
		this.currentBufferStartAddress =   0L;
		this.currentAddress            =   0L;
		this.address                   =   0L;
		this.currentBound              =   0L;

		return this;
	}

	private void compactDuplicates()
	{
		final ByteBuffer[] oldBuffers     = this.buffers;
		final int          oldBufferCount = this.currentBuffersIndex + 1;
		final HashMapIdId  index          = this.entityIndex;

		// allocate initial compacted buffer
		ByteBuffer[] newBuffers    = new ByteBuffer[DEFAULT_BUFFERS_CAPACITY];
		int          newBufIndex   = 0;
		ByteBuffer   newBuf        = XMemory.allocateDirectNative(this.bufferSizeProvider.provideBufferSize());
		newBuffers[newBufIndex]    = newBuf;
		long         newAddr       = XMemory.getDirectByteBufferAddress(newBuf);
		long         newBufStart   = newAddr;
		long         newBound      = newAddr + newBuf.capacity();
		long         newTotalLen   = 0L;

		for(int bi = 0; bi < oldBufferCount; bi++)
		{
			final ByteBuffer buf       = oldBuffers[bi];
			final long       startAddr = XMemory.getDirectByteBufferAddress(buf);
			final long       boundAddr = startAddr + buf.limit();

			for(long addr = startAddr; addr < boundAddr; )
			{
				final long entityLen = this.readEntityTotalLength(addr);
				final long entityOid = this.readEntityObjectId(addr);

				final long offsetInOldBuf = addr - startAddr;
				final long encodedPos     = ((long)bi << 32) | offsetInOldBuf;

				if(index.get(entityOid, encodedPos) == encodedPos)
				{
					// latest version (or untracked) — copy to compacted output
					if(newAddr + entityLen > newBound)
					{
						// finalize current new buffer
						final long written = newAddr - newBufStart;
						newBuf.position(X.checkArrayRange(written)).flip();
						newTotalLen += written;

						// allocate next buffer
						final int newCap = X.checkArrayRange(
							Math.max(entityLen, this.bufferSizeProvider.provideIncrementalBufferSize())
						);
						if(++newBufIndex >= newBuffers.length)
						{
							newBuffers = Arrays.copyOf(newBuffers, newBuffers.length << 1);
						}
						newBuf              = XMemory.allocateDirectNative(newCap);
						newBuffers[newBufIndex] = newBuf;
						newBufStart         = XMemory.getDirectByteBufferAddress(newBuf);
						newAddr             = newBufStart;
						newBound            = newBufStart + newBuf.capacity();
					}

					XMemory.copyRange(addr, newAddr, entityLen);
					newAddr += entityLen;
				}
				// else: superseded duplicate — skip

				addr += entityLen;
			}
		}

		// finalize last new buffer
		final long lastWritten = newAddr - newBufStart;
		newBuf.position(X.checkArrayRange(lastWritten)).flip();
		newTotalLen += lastWritten;

		// deallocate old buffers
		for(int i = 0; i < oldBufferCount; i++)
		{
			XMemory.deallocateDirectByteBuffer(oldBuffers[i]);
			oldBuffers[i] = null;
		}

		// replace with compacted buffers
		this.buffers             = newBuffers ;
		this.currentBuffersIndex = newBufIndex;
		this.totalLength         = newTotalLen;
	}

	protected long readEntityTotalLength(final long entityAddress)
	{
		return XMemory.get_long(entityAddress);
	}

	protected long readEntityObjectId(final long entityAddress)
	{
		// OID offset = 16: after 8-byte LEN + 8-byte TID
		return XMemory.get_long(entityAddress + Long.BYTES + Long.BYTES);
	}
	
	private void iterateEntityDataLocal(final BinaryEntityDataReader reader)
	{
		if(this.currentBuffer != null)
		{
			throw new IllegalStateException("Incomplete chunks");
		}

		final ByteBuffer[] buffers = this.buffers;
		final int     buffersCount = this.currentBuffersIndex + 1;
				
		for(int i = 0; i < buffersCount; i++)
		{
			// buffer is already flipped
			reader.readBinaryEntities(buffers[i]);
		}
	}
	
	@Override
	public void iterateEntityData(final BinaryEntityDataReader reader)
	{
		for(final ChunksBuffer channelBuffer : this.channelBuffers)
		{
			channelBuffer.iterateEntityDataLocal(reader);
		}
	}

	@Override
	public void iterateChannelChunks(final Consumer<? super Binary> logic)
	{
		for(final ChunksBuffer channelBuffer : this.channelBuffers)
		{
			logic.accept(channelBuffer);
		}
	}
	
	@Override
	public final boolean isEmpty()
	{
		return this.buffers[0] == null;
	}

	@Override
	public final long totalLength()
	{
		return this.totalLength;
	}

	/**
	 * Returns the total accumulated byte count including data in the current
	 * (not yet completed) buffer. Unlike {@link #totalLength()}, which only
	 * accounts for completed buffers, this method provides a real-time
	 * value suitable for size-based flush decisions.
	 *
	 * @return total bytes written so far, including the current buffer
	 */
	public final long currentTotalLength()
	{
		return this.totalLength + (this.currentAddress - this.currentBufferStartAddress);
	}

	@Override
	public final void copyToAddress(
		final long entityContentAddressOffset,
		final long targetAddress,
		final long length
	)
	{
		// address and currentAddress point to different offsets depending on the progress of storing logic, so hard to pick the right one.
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final void copyFromAddress(
		final long entityContentAddressOffset,
		final long sourceAddress,
		final long length
	)
	{
		XMemory.copyRange(sourceAddress, this.address + entityContentAddressOffset, length);
	}

	@Override
	public final long loadItemEntityContentAddress()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final void modifyLoadItem(
		final ByteBuffer directByteBuffer ,
		final long       offset           ,
		final long       entityTotalLength,
		final long       entityTypeId     ,
		final long       entityObjectId
	)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public long iterateReferences(
		final BinaryReferenceTraverser[]  traversers,
		final PersistenceObjectIdAcceptor acceptor
	)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void mark()
	{
		for(int i = 0; i <= this.currentBuffersIndex; i++)
		{
			this.buffers[i].mark();
		}
	}

	@Override
	public void reset()
	{
		for(int i = 0; i <= this.currentBuffersIndex; i++)
		{
			this.buffers[i].reset();
		}
	}
				
}
