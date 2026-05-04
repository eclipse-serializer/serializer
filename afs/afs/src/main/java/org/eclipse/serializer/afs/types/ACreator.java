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
 * Factory abstraction used by an {@link AFileSystem} to instantiate {@link ADirectory}, {@link AFile}
 * and {@link ARoot} items. Implementations plug in custom item types while still routing through
 * the file system's lifecycle.
 * <p>
 * The default methods return the standard {@link ADirectory#New(ADirectory, String)} /
 * {@link AFile#New(ADirectory, String)} implementations; sub-types override them to produce
 * specialized {@link AItem} subclasses.
 *
 * @see AFileSystem#creator()
 * @see Creator
 */
public interface ACreator extends ARoot.Creator
{
	/**
	 * Creates a new sub-directory under {@code parent} with the passed identifier.
	 *
	 * @param parent     the parent directory.
	 * @param identifier the directory's locally unique identifier.
	 *
	 * @return a new directory instance.
	 */
	public default ADirectory createDirectory(final ADirectory parent, final String identifier)
	{
		return ADirectory.New(parent, identifier);
	}

	/**
	 * Creates a new file under {@code parent} with the passed identifier.
	 *
	 * @param parent     the parent directory.
	 * @param identifier the file's locally unique identifier.
	 *
	 * @return a new file instance.
	 */
	public default AFile createFile(final ADirectory parent, final String identifier)
	{
		return AFile.New(parent, identifier);
	}

	/**
	 * Creates a new file under {@code parent} with the passed identifier, name and type. The
	 * default implementation ignores {@code name} and {@code type}; sub-types may use them to
	 * create richer file representations.
	 *
	 * @param parent     the parent directory.
	 * @param identifier the file's locally unique identifier.
	 * @param name       the file's name.
	 * @param type       the file's type, or {@code null}.
	 *
	 * @return a new file instance.
	 */
	public default AFile createFile(
		final ADirectory parent    ,
		final String     identifier,
		final String     name      ,
		final String     type
	)
	{
		return this.createFile(parent, identifier);
	}


	/**
	 * Factory abstraction for instantiating an {@link ACreator} for a given {@link AFileSystem}.
	 */
	@FunctionalInterface
	public interface Creator
	{
		/**
		 * Creates an {@link ACreator} for the passed file system.
		 *
		 * @param parent the file system the creator belongs to.
		 *
		 * @return a new {@link ACreator}.
		 */
		public ACreator createCreator(AFileSystem parent);
	}

}
