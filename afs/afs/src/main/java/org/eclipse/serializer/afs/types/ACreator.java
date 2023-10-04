package org.eclipse.serializer.afs.types;

/*-
 * #%L
 * Eclipse Serializer Abstract File System
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

public interface ACreator extends ARoot.Creator
{
	public default ADirectory createDirectory(final ADirectory parent, final String identifier)
	{
		return ADirectory.New(parent, identifier);
	}
	
	public default AFile createFile(final ADirectory parent, final String identifier)
	{
		return AFile.New(parent, identifier);
	}
	
	public default AFile createFile(
		final ADirectory parent    ,
		final String     identifier,
		final String     name      ,
		final String     type
	)
	{
		return this.createFile(parent, identifier);
	}
	
	
	@FunctionalInterface
	public interface Creator
	{

		public ACreator createCreator(AFileSystem parent);
	}
	
}
