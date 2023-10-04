/*-
 * #%L
 * Eclipse Serializer Persistence JDK8
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
module org.eclipse.serializer.persistence.binary.jdk8
{
	exports org.eclipse.serializer.persistence.binary.jdk8.java.util;
	exports org.eclipse.serializer.persistence.binary.jdk8.types;

	requires transitive org.eclipse.serializer.persistence.binary;
	requires jdk.unsupported;
	/* To indicate we use these modules directly, but they also come through org.eclipse.serializer.persistence.binary */
	requires org.eclipse.serializer.base;
	requires org.eclipse.serializer.persistence;
}
