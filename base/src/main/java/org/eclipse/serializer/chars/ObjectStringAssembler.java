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
public interface ObjectStringAssembler<T>
{
	public VarString assemble(VarString vs, T subject);
	
	public default VarString provideAssemblyBuffer()
	{
		// cannot make any assumptions about the required capacity in a generic implementation.
		return VarString.New();
	}
	
	public default String assemble(final T subject)
	{
		final VarString vs = this.provideAssemblyBuffer();
		
		this.assemble(vs, subject);
		
		return vs.toString();
	}
	
}
