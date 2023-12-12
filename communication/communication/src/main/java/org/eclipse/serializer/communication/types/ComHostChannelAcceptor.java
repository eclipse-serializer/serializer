package org.eclipse.serializer.communication.types;

/*-
 * #%L
 * Eclipse Serializer Communication Parent
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

import static org.eclipse.serializer.util.X.notNull;

import java.util.function.Consumer;

/**
 * Gateway/relay to the actual application/framework communication logic.
 * Potentially in another, maybe even dedicated thread.
 *
 * @param <C> the communication layer type
 */
@FunctionalInterface
public interface ComHostChannelAcceptor<C>
{
	public void acceptChannel(ComHostChannel<C> channel);
	
	
	
	public static <C>ComHostChannelAcceptor.Wrapper<C> Wrap(
		final Consumer<? super ComHostChannel<C>> acceptor
	)
	{
		return new ComHostChannelAcceptor.Wrapper<>(
			notNull(acceptor)
		);
	}
	
	public final class Wrapper<C> implements ComHostChannelAcceptor<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Consumer<? super ComHostChannel<C>> acceptor;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Wrapper(final Consumer<? super ComHostChannel<C>> acceptor)
		{
			super();
			this.acceptor = acceptor;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void acceptChannel(final ComHostChannel<C> channel)
		{
			this.acceptor.accept(channel);
		}
		
	}
	
}
