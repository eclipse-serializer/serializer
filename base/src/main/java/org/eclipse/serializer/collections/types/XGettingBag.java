package org.eclipse.serializer.collections.types;

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

import org.eclipse.serializer.collections.interfaces.ExtendedBag;


public interface XGettingBag<E> extends XGettingCollection<E>, ExtendedBag<E>
{
	public interface Factory<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XGettingBag<E> newInstance();
	}

	
	
	@Override
	public XGettingBag<E> copy();

	@Override
	public XGettingBag<E> view();

	@Override
	public XImmutableBag<E> immure();

}
