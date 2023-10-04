package org.eclipse.serializer.reference;

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
public interface _longReference
{
	public long get();



	public static _longReference New(final long value)
	{
		return new Default(value);
	}

	public final class Default implements _longReference
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final long value;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final long value)
		{
			super();
			this.value = value;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long get()
		{
			return this.value;
		}

	}

}
