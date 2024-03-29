package org.eclipse.serializer.configuration.yaml.types;

/*-
 * #%L
 * Eclipse Serializer Configuration YAML
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

import static org.eclipse.serializer.util.X.notNull;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.configuration.types.Configuration;
import org.eclipse.serializer.configuration.types.ConfigurationAssembler;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

public interface ConfigurationAssemblerYaml extends ConfigurationAssembler
{
	public static ConfigurationAssemblerYaml New()
	{
		final DumperOptions options = new DumperOptions();
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		
		return new ConfigurationAssemblerYaml.Default(options);
	}
	
	public static ConfigurationAssemblerYaml New(
		final DumperOptions options
	)
	{
		return new ConfigurationAssemblerYaml.Default(
			notNull(options)
		);
	}
	
	
	public static class Default implements ConfigurationAssemblerYaml
	{
		private final DumperOptions options;
		
		Default(
			final DumperOptions options
		)
		{
			super();
			this.options = options;
		}
		
		@Override
		public VarString assemble(
			final VarString     vs           ,
			final Configuration configuration
		)
		{
			return vs.add(
				new Yaml(this.options).dump(this.toMap(configuration))
			);
		}
		
		private Map<String, ?> toMap(
			final Configuration configuration
		)
		{
			final Map<String, Object> map = new HashMap<>();
			
			configuration.keys().forEach(key ->
				map.put(key, configuration.get(key))
			);
			
			configuration.children().forEach(child ->
				map.put(child.key(), this.toMap(child))
			);
			
			return map;
		}
		
	}
	
}
