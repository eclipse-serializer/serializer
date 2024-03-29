package org.eclipse.serializer.collections.old;

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

import java.util.Set;

import org.eclipse.serializer.collections.types.XGettingSet;

public interface OldSet<E> extends Set<E>, OldCollection<E>
{
	@Override
	public XGettingSet<E> parent();
}
