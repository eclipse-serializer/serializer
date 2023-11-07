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
module org.eclipse.serializer.configuration
{
	exports org.eclipse.serializer.configuration.exceptions;
	exports org.eclipse.serializer.configuration.types;
	
	requires transitive java.xml;
	requires transitive org.eclipse.serializer.base;

	uses org.eclipse.serializer.configuration.types.ConfigurationBasedCreator;

}
