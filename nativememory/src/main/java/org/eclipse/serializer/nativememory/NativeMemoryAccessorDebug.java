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

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.LongRange;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public class NativeMemoryAccessorDebug extends NativeMemoryAccessor
{
	private final static Logger logger = Logging.getLogger(NativeMemoryAccessor.class);
	
	private AtomicLong allocationCount;
	private AtomicLong freeCount;
	
	HashSet<LongRange> allocations;
	List<Long> deallocs;
	
	HashMap<Long, WeakReference<Field>> objectFieldIds;
	
	public NativeMemoryAccessorDebug() {
		super();
		this.allocationCount = new AtomicLong();
		this.freeCount = new AtomicLong();
		this.allocations = new HashSet<>();
		this.deallocs = new ArrayList<>();
		this.objectFieldIds = new HashMap<>();
	}

	
	private boolean addressInAllocations(long address) {
		return this.allocations.stream().anyMatch(r -> r.contains(address));
	}
	
	private List<LongRange> findAddressInAllocations(long address) {
		return this.allocations.stream().filter(r -> r.contains(address)).toList();
	}
	
	@Override
	public long allocateMemory(long bytes) {
		this.allocationCount.incrementAndGet();
		long address =  super.allocateMemory(bytes);
		logger.debug("Allocating at {} size: {}", address, bytes);
		if(!this.addressInAllocations(address)) {
			this.allocations.add(LongRange.of(address, address + bytes));
		}else {
			throw new Error("new alloc address "+ address + " in existing range!");
		}
		return address;
	}
	
	@Override
	public void freeMemory(long address) {
		this.freeCount.incrementAndGet();
		logger.debug("Freeing address {}", address);
		
		
		List<LongRange> existing = this.findAddressInAllocations(address);
		if(existing.size() == 0) {
			throw new Error("address not in known allocations" + address);
		}
		
		if(existing.size() == 0) {
			throw new Error("address in multiple allocations" + address);
		}
		
		if(existing.size() == 1) {
			if(this.deallocs.contains(address)) {
				logger.warn("multiple free of address {}", address);
			}
			else {
				this.deallocs.add(address);
				this.allocations.removeAll(existing);
				super.freeMemory(address);
			}
		}
		
	}

	@Override
	public long objectFieldOffset(Field field) {
		long fieldOffset = super.objectFieldOffset(field);
		logger.info("FieldId of {} is {}", field, fieldOffset);
		this.objectFieldIds.put(fieldOffset, new WeakReference<>(field));
		return fieldOffset;
	}
	

	@Override
	public String toString() {
		return "NativeMemoryAccessorDebug [allocationCount=" + this.allocationCount.get() + ", freeCount=" + this.freeCount.get()
				+ ", allocations=" + this.allocations + ", deallocs=" + this.deallocs + "]";
	}

//	@Override
//	public String toString() {
//		return "NativeMemoryAccessorDebug [allocationCount=" + this.allocationCount.get() + ", freeCount=" + this.freeCount.get() + "]";
//	}
	
	
	
	
}
