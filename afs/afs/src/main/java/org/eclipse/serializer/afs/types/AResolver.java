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

/**
 * Bridge between an external (storage-layer) item representation and the corresponding AFS items.
 * <p>
 * An {@link AResolver} translates a storage-specific directory representation {@code D} or file
 * representation {@code F} into the corresponding AFS path elements via
 * {@link #resolveDirectoryToPath(Object)} / {@link #resolveFileToPath(Object)} and from there into
 * the corresponding {@link ADirectory} / {@link AFile} through the file system's
 * {@link AResolving} entry points. The reverse direction is covered by {@link #resolve(ADirectory)}
 * and {@link #resolve(AFile)}.
 * <p>
 * The {@code ensure*} methods provide a create-if-missing variant that routes through
 * {@link AFileSystem#ensureDirectoryPath(String...)} and
 * {@link AFileSystem#ensureFilePath(String...)} respectively.
 *
 * @param <D> the storage-layer directory representation type.
 * @param <F> the storage-layer file representation type.
 *
 * @see AResolving
 * @see AFileSystem
 */
public interface AResolver<D, F>
{
	/**
	 * The {@link AFileSystem} this resolver delegates path resolution to.
	 *
	 * @return the owning file system.
	 */
	public AFileSystem fileSystem();

	/**
	 * Splits a storage-layer directory representation into AFS path elements.
	 *
	 * @param directory the storage-layer directory.
	 *
	 * @return the path elements from root to {@code directory}.
	 */
	public String[] resolveDirectoryToPath(D directory);

	/**
	 * Splits a storage-layer file representation into AFS path elements.
	 *
	 * @param file the storage-layer file.
	 *
	 * @return the path elements from root to {@code file}.
	 */
	public String[] resolveFileToPath(F file);

	/**
	 * Returns the storage-layer representation corresponding to the passed AFS directory.
	 *
	 * @param directory the AFS directory.
	 *
	 * @return the storage-layer directory representation.
	 */
	public D resolve(ADirectory directory);

	/**
	 * Returns the storage-layer representation corresponding to the passed AFS file.
	 *
	 * @param file the AFS file.
	 *
	 * @return the storage-layer file representation.
	 */
	public F resolve(AFile file);

	/**
	 * Resolves the AFS directory corresponding to the passed storage-layer directory.
	 *
	 * @param directory the storage-layer directory.
	 *
	 * @return the resolved AFS directory.
	 */
	public default ADirectory resolveDirectory(final D directory)
	{
		final String[] path = this.resolveDirectoryToPath(directory);

		return this.fileSystem().resolveDirectoryPath(path);
	}

	/**
	 * Resolves the AFS file corresponding to the passed storage-layer file.
	 *
	 * @param file the storage-layer file.
	 *
	 * @return the resolved AFS file, or {@code null} if it does not exist in its parent directory.
	 */
	public default AFile resolveFile(final F file)
	{
		final String[] path = this.resolveFileToPath(file);

		return this.fileSystem().resolveFilePath(path);
	}

	// (13.05.2020 TM)TODO: priv#49: does ensure~ really belong here?

	/**
	 * Returns the AFS directory corresponding to the passed storage-layer directory, creating any
	 * missing intermediate directories.
	 *
	 * @param directory the storage-layer directory.
	 *
	 * @return the AFS directory.
	 */
	public default ADirectory ensureDirectory(final D directory)
	{
		final String[] path = this.resolveDirectoryToPath(directory);

		return this.fileSystem().ensureDirectoryPath(path);
	}

	/**
	 * Returns the AFS file corresponding to the passed storage-layer file, creating any missing
	 * intermediate directories and the file itself if needed.
	 *
	 * @param file the storage-layer file.
	 *
	 * @return the AFS file.
	 */
	public default AFile ensureFile(final F file)
	{
		final String[] path = this.resolveFileToPath(file);

		return this.fileSystem().ensureFilePath(path);
	}

}
