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

public final class CharView implements CharSequence
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final char[] data;
	final int offset;
	final int length;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public CharView(final char[] data, final int offset, final int length)
	{
		super();
		this.data   = data  ;
		this.offset = offset;
		this.length = length;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public void validateIndex(final int index)
	{
		if(index < 0 || index >= this.length)
		{
			throw new StringIndexOutOfBoundsException(index);
		}
	}

	public void validateRange(final int offset, final int length)
	{
		this.validateIndex(offset);
		this.validateIndex(offset + length - 1);
	}

	// (30.07.2013 TM)TODO: reading methods like in VarString



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public int length()
	{
		return this.length;
	}

	@Override
	public char charAt(final int index)
	{
		this.validateIndex(index);
		return this.data[this.offset + index];
	}

	@Override
	public CharSequence subSequence(final int start, final int end)
	{
		this.validateIndex(start);
		this.validateIndex(end);
		if(start > end)
		{
			throw new IllegalArgumentException();
		}
		return new CharView(this.data, this.offset + start, end - start);
	}

	@Override
	public String toString()
	{
		return new String(this.data, this.offset, this.length);
	}

}
