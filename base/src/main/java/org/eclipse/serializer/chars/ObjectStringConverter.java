package org.eclipse.serializer.chars;

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
 * A "{@link ObjectStringConverter}" is hereby defined as a logic instance that handles
 * both conversion to and from a String-form of instances of a certain type.
 */
public interface ObjectStringConverter<T> extends ObjectStringAssembler<T>, ObjectStringParser<T>
{
	// just a typing interface so far
}
