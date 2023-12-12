/*-
 * #%L
 * Eclipse Serializer Communication Binary
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
module org.eclipse.serializer.communication.binary
{
	exports org.eclipse.serializer.communication.binary.types;
	exports org.eclipse.serializer.communication.binarydynamic;
	exports org.eclipse.serializer.communication.tls;
	
	requires transitive org.eclipse.serializer.communication;
	requires transitive org.eclipse.serializer.persistence.binary;
	requires org.slf4j;
}
