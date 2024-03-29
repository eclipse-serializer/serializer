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
 * Assembler for configurations to export to INI (properties) format.
 * 
 * @see Configuration#store(ConfigurationStorer, ConfigurationAssembler)
 */
public interface ConfigurationAssemblerIni extends ConfigurationAssembler
{
	/**
	 * Pseudo-constructor to create a new INI assembler.
	 * 
	 * @return a new INI assembler
	 */
	public static ConfigurationAssemblerIni New()
	{
		return new ConfigurationAssemblerIni.Default();
	}
	
	
	public static class Default implements ConfigurationAssemblerIni
	{
		Default()
		{
			super();
		}
		
		@Override
		public VarString assemble(
			final VarString     vs           ,
			final Configuration configuration
		)
		{
			configuration.coalescedTable().iterate(kv ->
				vs.add(kv.key()).add(" = ").add(kv.value()).lf()
			);
			
			return vs;
		}
		
	}
	
}
