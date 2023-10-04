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

public interface AResolver<D, F>
{
	public AFileSystem fileSystem();
	
	public String[] resolveDirectoryToPath(D directory);
	
	public String[] resolveFileToPath(F file);
	
	public D resolve(ADirectory directory);
	
	public F resolve(AFile file);

	public default ADirectory resolveDirectory(final D directory)
	{
		final String[] path = this.resolveDirectoryToPath(directory);
		
		return this.fileSystem().resolveDirectoryPath(path);
	}

	public default AFile resolveFile(final F file)
	{
		final String[] path = this.resolveFileToPath(file);
		
		return this.fileSystem().resolveFilePath(path);
	}
	
	// (13.05.2020 TM)TODO: priv#49: does ensure~ really belong here?

	public default ADirectory ensureDirectory(final D directory)
	{
		final String[] path = this.resolveDirectoryToPath(directory);
		
		return this.fileSystem().ensureDirectoryPath(path);
	}

	public default AFile ensureFile(final F file)
	{
		final String[] path = this.resolveFileToPath(file);
		
		return this.fileSystem().ensureFilePath(path);
	}
		
}
