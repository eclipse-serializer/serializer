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

public interface StringStamper
{
	String stampString(char[] chars, int offset, int length);



	final class Default implements StringStamper
	{
		@Override
		public String stampString(final char[] chars, final int offset, final int length)
		{
			return new String(chars, offset, length);
		}

	}

}
