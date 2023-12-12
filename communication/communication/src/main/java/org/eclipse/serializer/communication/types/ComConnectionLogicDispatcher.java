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

/**
 * Meta type to allow wrapping of connection handling logic types with additional aspects like
 * authentication, encryption and the like.
 *
 * @param <C> the communication layer type
 */
public interface ComConnectionLogicDispatcher<C>
{
	public default ComConnectionAcceptorCreator<C> dispatch(final ComConnectionAcceptorCreator<C> creator)
	{
		// no-op by default
		return creator;
	}
	
	public default ComConnectionHandler<C> dispatch(final ComConnectionHandler<C> connectionHandler)
	{
		// no-op by default
		return connectionHandler;
	}
	
	
	
	public static <C> ComConnectionLogicDispatcher<C> New()
	{
		return new ComConnectionLogicDispatcher.Default<>();
	}
	
	public final class Default<C> implements ComConnectionLogicDispatcher<C>
	{
		Default()
		{
			super();
		}
		
	}
	
}
