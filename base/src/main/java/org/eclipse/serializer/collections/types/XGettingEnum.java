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

import java.util.function.Consumer;

public interface XGettingEnum<E> extends XGettingSet<E>, XGettingSequence<E>
{
	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableEnum<E> immure();

	@Override
	public XGettingEnum<E> copy();

	@Override
	public XGettingEnum<E> toReversed();

	@Override
	public XGettingEnum<E> view();

	@Override
	public XGettingEnum<E> view(long lowIndex, long highIndex);

	@Override
	public XGettingEnum<E> range(long lowIndex, long highIndex);

	@Override
	public <P extends Consumer<? super E>> P iterate(P procedure);

}
