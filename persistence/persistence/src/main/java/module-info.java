/*-
 * #%L
 * Eclipse Serializer Persistence
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
module org.eclipse.serializer.persistence
{
	exports org.eclipse.serializer.persistence.exceptions;
	exports org.eclipse.serializer.persistence.types;
	exports org.eclipse.serializer.persistence.util;

	requires transitive org.eclipse.serializer.afs;
	/* To indicate we use these modules directly, but they also come through org.eclipse.serializer.afs */
	requires org.eclipse.serializer.base;
	requires org.slf4j;
}
