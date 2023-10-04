/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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
module org.eclipse.serializer.persistence.binary
{
	exports org.eclipse.serializer.persistence.binary.exceptions;
	exports org.eclipse.serializer.persistence.binary.internal; /* FIXME Do not export internal stuff */
	exports org.eclipse.serializer.persistence.binary.java.io;
	exports org.eclipse.serializer.persistence.binary.java.nio.file;
	exports org.eclipse.serializer.persistence.binary.java.lang;
	exports org.eclipse.serializer.persistence.binary.java.math;
	exports org.eclipse.serializer.persistence.binary.java.net;
	exports org.eclipse.serializer.persistence.binary.java.sql;
	exports org.eclipse.serializer.persistence.binary.java.time;
	exports org.eclipse.serializer.persistence.binary.java.util.concurrent;
	exports org.eclipse.serializer.persistence.binary.java.util.regex;
	exports org.eclipse.serializer.persistence.binary.java.util;
	exports org.eclipse.serializer.persistence.binary.org.eclipse.serializer.entity;
	exports org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections;
	exports org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.lazy;
	exports org.eclipse.serializer.persistence.binary.org.eclipse.serializer.persistence.types;
	exports org.eclipse.serializer.persistence.binary.org.eclipse.serializer.reference;
	exports org.eclipse.serializer.persistence.binary.org.eclipse.serializer.util;
	exports org.eclipse.serializer.persistence.binary.types;

	requires transitive org.eclipse.serializer.persistence;
	requires transitive jdk.unsupported;

	requires transitive java.sql;  /* binary handlers */
	/* To indicate we use these modules directly, but they also come through org.eclipse.serializer.persistence */
}
