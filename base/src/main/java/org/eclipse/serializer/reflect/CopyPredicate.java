package org.eclipse.serializer.reflect;

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

import java.lang.reflect.Field;

@FunctionalInterface
public interface CopyPredicate
{
	public <T, S extends T> boolean test(T source, S target, Field field, Object value);
	
	
	
	public static <T, S extends T> boolean all(final T source, final S target, final Field field, final Object value)
	{
		return true;
	}
	
	public static <T, S extends T> boolean none(final T source, final S target, final Field field, final Object value)
	{
		return false;
	}
}
