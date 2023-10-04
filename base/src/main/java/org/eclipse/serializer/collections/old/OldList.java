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

import java.util.List;

import org.eclipse.serializer.collections.types.XGettingList;

public interface OldList<E> extends List<E>, OldCollection<E>
{
	@Override
	public XGettingList<E> parent();
}
