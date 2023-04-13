package org.eclipse.serializer.collections.types;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.util.function.Consumer;

import org.eclipse.serializer.collections.interfaces.CapacityExtendable;
import org.eclipse.serializer.collections.interfaces.ExtendedSequence;
import org.eclipse.serializer.collections.interfaces.OptimizableCollection;

public interface XPrependingSequence<E>
extends Consumer<E>, CapacityExtendable, OptimizableCollection, ExtendedSequence<E>
{
	public interface Creator<E>
	{
		public XPrependingSequence<E> newInstance();
	}



	public boolean prepend(E element);

	public boolean nullPrepend();

	@SuppressWarnings("unchecked")
	public XPrependingSequence<E> prependAll(E... elements);

	public XPrependingSequence<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	public XPrependingSequence<E> prependAll(XGettingCollection<? extends E> elements);

}
