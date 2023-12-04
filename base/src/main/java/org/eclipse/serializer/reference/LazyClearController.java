package org.eclipse.serializer.reference;

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

/**
 * Classes that implement that interface can utilize {@link org.eclipse.serializer.reference.ControlledLazyReference.Default}
 * to gain control of the unloading of {@link Lazy} references.
 */
public interface LazyClearController
{
	/**
	 * Allow or deny clearing a lazy reference.
	 * 
	 * @return true if clearing the lazy reference is allowed, otherwise false.
	 */
	public boolean allowClear();
}
