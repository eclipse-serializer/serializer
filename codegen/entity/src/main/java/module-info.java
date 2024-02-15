/*-
 * #%L
 * Eclipse Serializer Codegen for Entities
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

module org.eclipse.serializer.codegen.entity
{
	exports org.eclipse.serializer.codegen.entity;

	provides javax.annotation.processing.Processor
	    with org.eclipse.serializer.codegen.entity.EntityProcessor
	;

	requires java.compiler;
	requires transitive java.management;
	requires transitive jdk.unsupported;
	requires transitive org.eclipse.serializer.base;
}
