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

/**
 * Functional supplier for the number of parallel channels the binary persistence layer should partition
 * its work across. The returned count drives chunk-buffer arrays, channel-hashed
 * {@link LoadItemsChain}s, and any other channel-partitioned bookkeeping.
 */
@FunctionalInterface
public interface BinaryChannelCountProvider
{
	/**
	 * @return the number of parallel channels.
	 */
	public int getChannelCount();
}
