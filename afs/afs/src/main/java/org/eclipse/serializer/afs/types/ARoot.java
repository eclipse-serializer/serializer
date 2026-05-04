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

import static org.eclipse.serializer.util.X.coalesce;
import static org.eclipse.serializer.util.X.notNull;

/**
 * A root directory: an {@link ADirectory} with no {@link #parent() parent}, identified within an
 * {@link AFileSystem} by its {@link #identifier() identifier} and tagged with a {@link #protocol()
 * protocol} (e.g. {@code "file://"}, {@code "https://"}). Every path in a file system starts from
 * one of its registered roots.
 *
 * @see AFileSystem#ensureRoot(String)
 * @see AFileSystem#addRoot(ADirectory)
 */
public interface ARoot extends ADirectory
{
	/**
	 * E.g.
	 * https://
	 * file://
	 *
	 * @return the protocol
	 */
	public String protocol();



	/**
	 * Factory abstraction for creating {@link ARoot} instances. Allows file system implementations
	 * (or callers of {@link AFileSystem#ensureRoot(Creator, String)}) to plug in a custom root
	 * type while still routing through the file system's lifecycle.
	 */
	@FunctionalInterface
	public interface Creator
	{
		/**
		 * Creates a new root directory with the passed protocol and identifier in the given file
		 * system.
		 *
		 * @param fileSystem the file system the root will belong to.
		 * @param protocol   the protocol tag.
		 * @param identifier the root's identifier.
		 *
		 * @return the newly created root directory.
		 */
		public ARoot createRootDirectory(AFileSystem fileSystem, String protocol, String identifier);

		/**
		 * Convenience overload that picks the protocol from {@link #protocol()} or, if that
		 * returns {@code null}, from the file system's {@link AFileSystem#defaultProtocol() default
		 * protocol}.
		 *
		 * @param fileSystem the file system the root will belong to.
		 * @param identifier the root's identifier.
		 *
		 * @return the newly created root directory.
		 */
		public default ARoot createRootDirectory(final AFileSystem fileSystem, final String identifier)
		{
			return this.createRootDirectory(
				fileSystem,
				coalesce(this.protocol(), fileSystem.defaultProtocol()),
				identifier
			);
		}

		/**
		 * The protocol this creator wants newly created roots tagged with, or {@code null} to fall
		 * back to the file system's default protocol.
		 *
		 * @return the creator-specific protocol, or {@code null}.
		 */
		public default String protocol()
		{
			return null;
		}
	}
	
	
	/**
	 * Creates a new root directory
	 * Note: {@code identifier} can be {@literal ""} since local file paths might start with a "/".
	 * @param fileSystem the root's file system
	 * @param protocol the used protocol
	 * @param identifier the identifier of the root directory
	 * @return the newly created root directory
	 * 
	 */
	public static ARoot New(
		final AFileSystem fileSystem,
		final String      protocol  ,
		final String      identifier
	)
	{
		return new ARoot.Default(
			notNull(fileSystem),
			notNull(protocol)  ,
			notNull(identifier) // may be ""
		);
	}
	
	/**
	 * Default {@link ARoot} implementation. Holds the file system and protocol tag and reports
	 * {@code null} as its parent.
	 */
	public final class Default extends ADirectory.Abstract implements ARoot
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final AFileSystem fileSystem;
		private final String      protocol  ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(
			final AFileSystem fileSystem,
			final String      protocol  ,
			final String      identifier
		)
		{
			super(identifier);
			this.protocol   = protocol  ;
			this.fileSystem = fileSystem;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final AFileSystem fileSystem()
		{
			return this.fileSystem;
		}
		
		@Override
		public final String protocol()
		{
			return this.protocol;
		}
		
		@Override
		public final ADirectory parent()
		{
			return null;
		}
		
	}
	
}
