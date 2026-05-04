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
 * Abstraction for path-based item lookup. Unlike the {@code ensure*} family on {@link AFileSystem}
 * and {@link ADirectory}, the {@code resolve*} methods do not create missing entries: they return
 * the existing item or signal absence according to the implementation contract (typically by
 * returning {@code null} for files when the parent directory is unknown, and throwing
 * {@link org.eclipse.serializer.afs.exceptions.AfsExceptionUnresolvablePathElement} /
 * {@link org.eclipse.serializer.afs.exceptions.AfsExceptionUnresolvableRoot} for missing
 * directories).
 * <p>
 * The interface is separator-agnostic: paths are passed as arrays of identifiers, never as a
 * single string. Use {@link AFileSystem#resolvePath(String)} to split a path string first.
 *
 * @see AFileSystem
 * @see ADirectory
 */
public interface AResolving
{
	// note: no single string parameter resolving here, since this type is separator-agnostic.

	/**
	 * Resolves the file at the passed path. The last element is treated as the file identifier;
	 * preceding elements form the directory path.
	 *
	 * @param pathElements the directory path followed by the file identifier.
	 *
	 * @return the resolved file, or {@code null} if the file does not exist in its parent directory.
	 */
	public default AFile resolveFilePath(
		final String... pathElements
	)
	{
		return this.resolveFilePath(pathElements, 0, pathElements.length - 1, pathElements[pathElements.length - 1]);
	}

	/**
	 * Resolves the file with the passed identifier in the directory described by
	 * {@code directoryPathElements}.
	 *
	 * @param directoryPathElements the directory path elements.
	 * @param fileIdentifier        the file's identifier.
	 *
	 * @return the resolved file, or {@code null} if the file does not exist in its parent directory.
	 */
	public default AFile resolveFilePath(
		final String[] directoryPathElements,
		final String   fileIdentifier
	)
	{
		return this.resolveFilePath(directoryPathElements, 0, directoryPathElements.length, fileIdentifier);
	}

	/**
	 * Resolves the file with the passed identifier in the directory described by the slice
	 * {@code [offset, offset+length)} of {@code directoryPathElements}.
	 *
	 * @param directoryPathElements the array of directory path elements.
	 * @param offset                the start index of the directory-path slice.
	 * @param length                the length of the directory-path slice.
	 * @param fileIdentifier        the file's identifier.
	 *
	 * @return the resolved file, or {@code null} if the parent directory could not be resolved or the file is unknown.
	 */
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


	/**
	 * Resolves the directory at the passed path.
	 *
	 * @param pathElements the directory path elements from root to leaf.
	 *
	 * @return the resolved directory.
	 */
	public default ADirectory resolveDirectoryPath(
		final String... pathElements
	)
	{
		return this.resolveDirectoryPath(pathElements, 0, pathElements.length);
	}

	/**
	 * Resolves the directory described by the slice {@code [offset, offset+length)} of the passed
	 * path-element array.
	 *
	 * @param pathElements the array of path elements.
	 * @param offset       the start index of the slice.
	 * @param length       the length of the slice.
	 *
	 * @return the resolved directory.
	 */
	public ADirectory resolveDirectoryPath(String[] pathElements, int offset, int length);

}
