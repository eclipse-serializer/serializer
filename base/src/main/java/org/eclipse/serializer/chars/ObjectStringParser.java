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


@FunctionalInterface
public interface ObjectStringParser<T>
{
	T parse(_charArrayRange input);

	default T parse(final String input)
	{
		return this.parse(_charArrayRange.New(input));
	}

}