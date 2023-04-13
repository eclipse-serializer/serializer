/*-
 * #%L
 * Eclipse Serializer Persistence Binary
 * %%
 * Copyright (C) 2023 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */
module org.eclipse.serializer.persistence.binary
{
	exports org.eclipse.serializer.persistence.binary.java.util.regex;
	exports org.eclipse.serializer.persistence.binary.java.util;
	exports org.eclipse.serializer.persistence.binary.java.time;
	exports org.eclipse.serializer.persistence.binary.types;
	exports org.eclipse.serializer.persistence.binary.java.math;
	exports org.eclipse.serializer.persistence.binary.internal;
	exports org.eclipse.serializer.persistence.binary.java.sql;
	exports org.eclipse.serializer.persistence.binary.org.eclipse.serializer.util;
	exports org.eclipse.serializer.persistence.binary.exceptions;
	exports org.eclipse.serializer.persistence.binary.java.lang;
	exports org.eclipse.serializer.persistence.binary.org.eclipse.serializer.reference;
	exports org.eclipse.serializer.persistence.binary.java.io;
	exports org.eclipse.serializer.persistence.binary.java.nio.file;
	exports org.eclipse.serializer.persistence.binary.java.net;
	exports org.eclipse.serializer.persistence.binary.java.util.concurrent;
	exports org.eclipse.serializer.persistence.binary.org.eclipse.serializer.entity;
	exports org.eclipse.serializer.persistence.binary.org.eclipse.serializer.persistence.types;
	exports org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections;
	
	requires java.sql;
	requires jdk.unsupported;
	requires org.eclipse.serializer.afs;
	requires org.eclipse.serializer.base;
	requires org.eclipse.serializer.persistence;
}
