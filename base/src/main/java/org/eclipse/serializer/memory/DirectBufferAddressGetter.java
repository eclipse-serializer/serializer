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

import java.nio.ByteBuffer;

/**
 * Similar to {@link DirectBufferDeallocator} but to obtain the DirectBuffer's address value.
 *
 */
public interface DirectBufferAddressGetter
{
	public long getDirectBufferAddress(ByteBuffer directBuffer);
		
}
