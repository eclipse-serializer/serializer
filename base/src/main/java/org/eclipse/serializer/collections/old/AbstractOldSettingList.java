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

import org.eclipse.serializer.collections.types.XSettingList;

public abstract class AbstractOldSettingList<E> extends AbstractOldGettingList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractOldSettingList(final XSettingList<E> list)
	{
		super(list);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XSettingList<E> parent()
	{
		return (XSettingList<E>)this.subject;
	}

	@Override
	public E set(final int index, final E element)
	{
		return ((XSettingList<E>)this.subject).setGet(index, element);
	}

	@Override
	public AbstractOldSettingList<E> subList(final int fromIndex, final int toIndex)
	{
		/* XSettingList implementations always create a SubList instance whose implementation creates an
		 * OldSettingList bridge instance, so this cast is safe (and inevitable).
		 */
		return (AbstractOldSettingList<E>)(((XSettingList<E>)this.subject).range(fromIndex, toIndex).old());
	}

}
