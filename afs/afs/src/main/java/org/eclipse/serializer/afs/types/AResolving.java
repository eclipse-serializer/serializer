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

public interface AResolving
{
	// note: no single string parameter resolving here, since this type is separator-agnostic.
	
	public default AFile resolveFilePath(
		final String... pathElements
	)
	{
		return this.resolveFilePath(pathElements, 0, pathElements.length - 1, pathElements[pathElements.length - 1]);
	}
	
	public default AFile resolveFilePath(
		final String[] directoryPathElements,
		final String   fileIdentifier
	)
	{
		return this.resolveFilePath(directoryPathElements, 0, directoryPathElements.length, fileIdentifier);
	}
	
	public default AFile resolveFilePath(
		final String[] directoryPathElements,
		final int      offset               ,
		final int      length               ,
		final String   fileIdentifier
	)
	{
		final ADirectory directory = this.resolveDirectoryPath(directoryPathElements, offset, length);
		
		// if the implementation of #resolveDirectoryPath returns null, then conform to this strategy.
		return directory == null
			? null
			: directory.getFile(fileIdentifier)
		;
	}
	
	
	public default ADirectory resolveDirectoryPath(
		final String... pathElements
	)
	{
		return this.resolveDirectoryPath(pathElements, 0, pathElements.length);
	}

	public ADirectory resolveDirectoryPath(String[] pathElements, int offset, int length);
			
}
