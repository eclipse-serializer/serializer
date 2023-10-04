package org.eclipse.serializer.configuration.types;

/*-
 * #%L
 * Eclipse Serializer Configuration
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

import org.eclipse.serializer.chars.VarString;

/**
 * Assembler to export a configuration into an external format.
 *
 */
@FunctionalInterface
public interface ConfigurationAssembler
{
	/**
	 * Assembles all entries and child-configurations to an external format.
	 * 
	 * @param configuration the source
	 * @return a String representation of the external format
	 */
	public default VarString assemble(final Configuration configuration)
	{
		return this.assemble(VarString.New(), configuration);
	}
	
	/**
	 * Assembles all entries and child-configurations to an external format.
	 * 
	 * @param vs existing target VarString
	 * @param configuration the source
	 * @return a String representation of the external format
	 */
	public VarString assemble(VarString vs, Configuration configuration);
}
