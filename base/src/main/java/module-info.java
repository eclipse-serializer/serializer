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
module org.eclipse.serializer.base
{
	/* All packages are exported */
	exports org.eclipse.serializer.branching;
	exports org.eclipse.serializer.bytes;
	exports org.eclipse.serializer.chars;
	exports org.eclipse.serializer.collections;
	exports org.eclipse.serializer.collections.interfaces;
	exports org.eclipse.serializer.collections.old;
	exports org.eclipse.serializer.collections.lazy;
	exports org.eclipse.serializer.collections.sorting;
	exports org.eclipse.serializer.collections.types;
	exports org.eclipse.serializer.com;
	exports org.eclipse.serializer.concurrency;
	exports org.eclipse.serializer.entity;
	exports org.eclipse.serializer.equality;
	exports org.eclipse.serializer.exceptions;
	exports org.eclipse.serializer.functional;
	exports org.eclipse.serializer.hashing;
	exports org.eclipse.serializer.io;
	exports org.eclipse.serializer.math;
	exports org.eclipse.serializer.memory;
	exports org.eclipse.serializer.memory.android;
	exports org.eclipse.serializer.memory.sun;
	exports org.eclipse.serializer.meta;
	exports org.eclipse.serializer.monitoring;
	exports org.eclipse.serializer.reference;
	exports org.eclipse.serializer.reflect;
	exports org.eclipse.serializer.time;
	exports org.eclipse.serializer.typing;
	exports org.eclipse.serializer.util;
	exports org.eclipse.serializer.util.cql;
	exports org.eclipse.serializer.util.iterables;
	exports org.eclipse.serializer.util.logging;
	exports org.eclipse.serializer.util.similarity;
	exports org.eclipse.serializer.util.traversing;
	exports org.eclipse.serializer.util.xcsv;
	exports org.eclipse.serializer.wrapping;


	requires transitive java.management;
	requires transitive jdk.unsupported;
	requires transitive org.slf4j;

}

