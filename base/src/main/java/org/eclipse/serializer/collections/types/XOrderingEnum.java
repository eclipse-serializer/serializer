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


/**
 * 
 *
 */
public interface XOrderingEnum<E> extends XOrderingSequence<E>
{

	@Override
	public XOrderingEnum<E> shiftTo(long sourceIndex, long targetIndex);
	@Override
	public XOrderingEnum<E> shiftTo(long sourceIndex, long targetIndex, long length);
	@Override
	public XOrderingEnum<E> shiftBy(long sourceIndex, long distance);
	@Override
	public XOrderingEnum<E> shiftBy(long sourceIndex, long distance, long length);

	@Override
	public XOrderingEnum<E> swap(long indexA, long indexB);
	@Override
	public XOrderingEnum<E> swap(long indexA, long indexB, long length);

	@Override
	public XOrderingEnum<E> reverse();

}
