package org.eclipse.serializer.typing;

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

public interface _longKeyValue
{
	public long key();
	public long value();



	public class Default implements _longKeyValue
	{
		private final long key;
		private final long value;

		public Default(final long key, final long value)
		{
			super();
			this.key = key;
			this.value = value;
		}

		@Override
		public long key()
		{
			return this.key;
		}

		@Override
		public long value()
		{
			return this.value;
		}

		@Override
		public String toString()
		{
			return "(" + this.key + " -> " + this.value + ")";
		}

	}

}
