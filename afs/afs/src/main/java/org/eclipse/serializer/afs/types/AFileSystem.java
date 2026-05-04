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

import java.util.function.Function;

import org.eclipse.serializer.afs.exceptions.AfsExceptionConsistency;
import org.eclipse.serializer.afs.exceptions.AfsExceptionMutationInUse;
import org.eclipse.serializer.afs.exceptions.AfsExceptionUnresolvableRoot;
import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.XArrays;
import org.eclipse.serializer.collections.types.XGettingTable;

/**
 * Top-level abstraction representing an entire abstract file system. An {@link AFileSystem} owns
 * the registered {@link ARoot root directories}, the {@link AccessManager} that arbitrates
 * concurrent file usage, the {@link ACreator} that instantiates new items, and the
 * {@link AIoHandler} that performs the actual I/O against the underlying storage layer.
 * <p>
 * The interface combines item-resolution ({@link AResolving}) with a writability gate
 * ({@link WriteController}), so a file system implementation also decides whether write operations
 * are allowed at all.
 * <p>
 * Typical entry points are {@link #ensureDirectoryPath(String...)} / {@link
 * #ensureFilePath(String...)} for path-based access, and {@link #ensureRoot(String)} for direct
 * root management. {@link #wrapForReading(AFile, Object)} / {@link #wrapForWriting(AFile, Object)}
 * are used by the {@link AccessManager} to materialize usage handles.
 *
 * @see ARoot
 * @see AccessManager
 * @see AIoHandler
 * @see ACreator
 */
public interface AFileSystem extends AResolving, WriteController
{
	/**
	 * The default protocol used for newly created roots when no explicit protocol is supplied.
	 *
	 * @return the default protocol (e.g. {@code "file://"}).
	 */
	public String defaultProtocol();

	/**
	 * Ensures the directory chain corresponding to the passed path elements exists, creating any
	 * missing intermediate directories.
	 *
	 * @param pathElements the path elements from root to leaf.
	 *
	 * @return the leaf directory.
	 *
	 * @see #ensureDirectoryPath(String[], int, int)
	 */
	public default ADirectory ensureDirectoryPath(final String... pathElements)
	{
		return this.ensureDirectoryPath(pathElements, 0, pathElements.length);
	}

	/**
	 * Ensures the directory chain corresponding to the slice {@code [offset, offset+length)} of
	 * the passed path-element array exists, creating any missing intermediate directories.
	 * If {@code length} is {@code 0}, the {@link #ensureDefaultRoot() default root} is returned.
	 *
	 * @param pathElements the array of path elements.
	 * @param offset       the start index of the slice.
	 * @param length       the length of the slice.
	 *
	 * @return the leaf directory.
	 */
	public ADirectory ensureDirectoryPath(String[] pathElements, int offset, int length);

	/**
	 * Ensures the file at the passed path exists in the directory hierarchy. The last element is
	 * treated as the file identifier; preceding elements form the directory path.
	 *
	 * @param pathElements the directory path followed by the file identifier.
	 *
	 * @return the file at the path.
	 */
	public default AFile ensureFilePath(final String... pathElements)
	{
		return this.ensureFilePath(pathElements, 0, pathElements.length - 1, pathElements[pathElements.length - 1]);
	}

	/**
	 * Ensures the file with the passed identifier exists in the directory described by
	 * {@code directoryPathElements}.
	 *
	 * @param directoryPathElements the directory path elements.
	 * @param fileIdentifier        the file's identifier.
	 *
	 * @return the file at the path.
	 */
	public default AFile ensureFilePath(final String[] directoryPathElements, final String fileIdentifier)
	{
		return this.ensureFilePath(directoryPathElements, 0, directoryPathElements.length, fileIdentifier);
	}

	/**
	 * Ensures the file with the passed identifier exists in the directory described by the slice
	 * {@code [offset, offset+length)} of {@code directoryPathElements}.
	 *
	 * @param directoryPathElements the array of directory path elements.
	 * @param offset                the start index of the directory-path slice.
	 * @param length                the length of the directory-path slice.
	 * @param fileIdentifier        the file's identifier.
	 *
	 * @return the file at the path.
	 */
	public AFile ensureFilePath(String[] directoryPathElements, int offset, int length, String fileIdentifier);

	/**
	 * The {@link AccessManager} that arbitrates concurrent reading and writing access on the
	 * files of this file system.
	 *
	 * @return the access manager.
	 */
	public AccessManager accessManager();

	/**
	 * The {@link ACreator} used to instantiate new {@link ADirectory} and {@link AFile} items in
	 * this file system.
	 *
	 * @return the creator.
	 */
	public ACreator creator();

	/**
	 * The {@link AIoHandler} that performs actual I/O against the underlying storage layer.
	 *
	 * @return the I/O handler.
	 */
	public AIoHandler ioHandler();

	/**
	 * Wraps the passed file in an {@link AReadableFile} usage handle for the given user.
	 * <p>
	 * Called by the {@link AccessManager}; clients should normally use {@link AFile#useReading()}.
	 *
	 * @param file the file to wrap.
	 * @param user the user the usage is registered under.
	 *
	 * @return a new readable handle.
	 */
	public AReadableFile wrapForReading(AFile file, Object user);

	/**
	 * Wraps the passed file in an {@link AWritableFile} usage handle for the given user.
	 * <p>
	 * Called by the {@link AccessManager}; clients should normally use {@link AFile#useWriting()}.
	 *
	 * @param file the file to wrap.
	 * @param user the user the usage is registered under.
	 *
	 * @return a new writable handle.
	 */
	public AWritableFile wrapForWriting(AFile file, Object user);

	/**
	 * Converts an existing writable handle into a readable one for the same file and user,
	 * downgrading the access claim.
	 *
	 * @param file the writable handle to convert.
	 *
	 * @return a readable handle.
	 */
	public AReadableFile convertToReading(AWritableFile file);

	/**
	 * Converts an existing readable handle into a writable one for the same file and user,
	 * upgrading the access claim if it can be granted.
	 *
	 * @param file the readable handle to convert.
	 *
	 * @return a writable handle.
	 */
	public AWritableFile convertToWriting(AReadableFile file);

	/**
	 * Looks up a registered root directory by identifier without creating it.
	 *
	 * @param identifier the root's identifier.
	 *
	 * @return the registered root, or {@code null} if no root with that identifier exists.
	 */
	public ADirectory lookupRoot(String identifier);

	/**
	 * Returns the registered root directory with the passed identifier.
	 *
	 * @param identifier the root's identifier.
	 *
	 * @return the registered root.
	 *
	 * @throws org.eclipse.serializer.afs.exceptions.AfsExceptionUnresolvableRoot if no root with that identifier is registered.
	 */
	public ADirectory getRoot(String identifier);

	/**
	 * Returns the registered root directory with the passed identifier, creating and registering
	 * it via the file system's {@link #creator() creator} if it does not yet exist.
	 *
	 * @param identifier the root's identifier.
	 *
	 * @return the existing or newly created root.
	 */
	public ADirectory ensureRoot(String identifier);

	/**
	 * Returns the registered root directory with the passed identifier, creating and registering
	 * it via the supplied {@link ARoot.Creator} if it does not yet exist.
	 *
	 * @param rootCreator the creator to use for instantiating a new root, if needed.
	 * @param identifier  the root's identifier.
	 *
	 * @return the existing or newly created root.
	 */
	public ADirectory ensureRoot(ARoot.Creator rootCreator, String identifier);
	
	/**
	 * Ensures the default root directory. May not be supported by different file system implementations.
	 * @return the root directory
	 * @throws UnsupportedOperationException if the file system doesn't have a default root
	 */
	public ADirectory ensureDefaultRoot();
	
	/**
	 * Removes the root directory with the passed identifier from the file system.
	 *
	 * @param identifier the root's identifier.
	 *
	 * @return the removed root directory.
	 *
	 * @throws org.eclipse.serializer.afs.exceptions.AfsExceptionUnresolvableRoot if no root with that identifier is registered.
	 * @throws org.eclipse.serializer.afs.exceptions.AfsExceptionMutationInUse    if the root is currently in use.
	 */
	public ADirectory removeRoot(String identifier);

	/**
	 * Registers the passed externally created root directory with this file system. Has no effect
	 * if the same root is already registered.
	 *
	 * @param rootDirectory the root to register.
	 *
	 * @return {@code true} if the root was newly registered, {@code false} if it was already present.
	 */
	public boolean addRoot(ADirectory rootDirectory);

	/**
	 * Removes the passed root directory from the file system.
	 *
	 * @param rootDirectory the root to remove.
	 *
	 * @return {@code true} if the root was registered and got removed, {@code false} otherwise.
	 *
	 * @throws org.eclipse.serializer.afs.exceptions.AfsExceptionMutationInUse if the root is currently in use.
	 */
	public boolean removeRoot(ADirectory rootDirectory);

	/**
	 * Provides synchronized access to the live root-directory table. The table is held under the
	 * file system's lock while {@code logic} runs; the consumer must not retain a reference to it.
	 *
	 * @param <R>   the result type.
	 * @param logic the function to apply to the root-directory table.
	 *
	 * @return the value returned by {@code logic}.
	 */
	public <R> R accessRoots(Function<? super XGettingTable<String, ADirectory>, R> logic);

	/**
	 * Validates that the passed item belongs to this file system and returns it for chaining.
	 *
	 * @param <I>  the item type.
	 * @param item the item to validate.
	 *
	 * @return the validated item.
	 *
	 * @throws org.eclipse.serializer.afs.exceptions.AfsExceptionConsistency if the item belongs to a different file system.
	 */
	public <I extends AItem> I validateMember(I item);

	/**
	 * Assembles the path string of the passed file using this file system's path conventions.
	 *
	 * @param file the file whose path to assemble.
	 *
	 * @return the assembled path string.
	 */
	public default String assemblePath(final AFile file)
	{
		return this.assemblePath(file, VarString.New()).toString();
	}

	/**
	 * Assembles the path string of the passed directory using this file system's path conventions.
	 *
	 * @param directory the directory whose path to assemble.
	 *
	 * @return the assembled path string.
	 */
	public default String assemblePath(final ADirectory directory)
	{
		return this.assemblePath(directory, VarString.New()).toString();
	}

	/**
	 * Derives a file identifier from the passed name and (optional) type according to the file
	 * system's naming conventions.
	 *
	 * @param fileName the file's name.
	 * @param fileType the file's type, or {@code null}.
	 *
	 * @return the derived identifier.
	 */
	public String deriveFileIdentifier(String fileName, String fileType);

	/**
	 * Derives the file name from the passed identifier according to the file system's naming
	 * conventions.
	 *
	 * @param fileIdentifier the file's identifier.
	 *
	 * @return the derived name.
	 */
	public String deriveFileName(String fileIdentifier);

	/**
	 * Derives the file type from the passed identifier according to the file system's naming
	 * conventions, or {@code null} if no type can be derived.
	 *
	 * @param fileIdentifier the file's identifier.
	 *
	 * @return the derived type, or {@code null}.
	 */
	public String deriveFileType(String fileIdentifier);


	/**
	 * Appends the path of the passed file to the supplied {@link VarString}.
	 *
	 * @param file the file whose path to append.
	 * @param vs   the {@link VarString} to append to.
	 *
	 * @return the passed {@link VarString} for chaining.
	 */
	public VarString assemblePath(AFile file, VarString vs);

	/**
	 * Appends the path of the passed directory to the supplied {@link VarString}.
	 *
	 * @param directory the directory whose path to append.
	 * @param vs        the {@link VarString} to append to.
	 *
	 * @return the passed {@link VarString} for chaining.
	 */
	public VarString assemblePath(ADirectory directory, VarString vs);


	/**
	 * Builds the path of the passed item as an array of identifiers from root to leaf.
	 *
	 * @param item the item whose path to build.
	 *
	 * @return the path as an array of identifiers.
	 */
	public String[] buildPath(AItem item);

	/*
	 * Default implementation assumes items can be handled in a unified way.
	 * If not, the interface allows for switching it around.
	 */
	/**
	 * Builds the path of the passed file. Default implementation delegates to
	 * {@link #buildPath(AItem)}.
	 *
	 * @param file the file whose path to build.
	 *
	 * @return the path as an array of identifiers.
	 */
	public default String[] buildPath(final AFile file)
	{
		return this.buildPath((AItem)file);
	}

	/**
	 * Builds the path of the passed directory. Default implementation delegates to
	 * {@link #buildPath(AItem)}.
	 *
	 * @param directory the directory whose path to build.
	 *
	 * @return the path as an array of identifiers.
	 */
	public default String[] buildPath(final ADirectory directory)
	{
		return this.buildPath((AItem)directory);
	}

	/**
	 * Splits the passed full path string into individual path elements. The default implementation
	 * splits on {@code '/'} and {@code '\\'}.
	 *
	 * @param fullPath the full path string.
	 *
	 * @return the path elements.
	 */
	public default String[] resolvePath(final String fullPath)
	{
		return fullPath.split("[/\\\\]");
	}


	/**
	 * Returns the file's {@link AFile#name() name} according to this file system's naming
	 * conventions.
	 *
	 * @param file the file whose name to return.
	 *
	 * @return the file's name.
	 */
	public String getFileName(AFile file);

	/**
	 * Returns the file's {@link AFile#type() type} according to this file system's naming
	 * conventions, or {@code null} if no type applies.
	 *
	 * @param file the file whose type to return.
	 *
	 * @return the file's type, or {@code null}.
	 */
	public String getFileType(AFile file);

	
	
	/**
	 * Skeletal {@link AFileSystem} implementation that maintains the registered root directories,
	 * the {@link ACreator}, the {@link AccessManager} and the typed {@link AIoHandler}, and that
	 * also acts as its own {@link AResolver} and {@link ACreator}.
	 *
	 * @param <H> the concrete {@link AIoHandler} type.
	 * @param <D> the directory type used for resolution.
	 * @param <F> the file type used for resolution.
	 */
	public abstract class Abstract<H extends AIoHandler, D, F> implements AFileSystem, AResolver<D, F>, ACreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String                          defaultProtocol;
		private final EqHashTable<String, ADirectory> rootDirectories; // ARoot or relative top-level directory
		private final ACreator                        creator        ;
		private final AccessManager                   accessManager  ;
		private final H                               ioHandler      ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(
			final String defaultProtocol,
			final H      ioHandler
		)
		{
			this(defaultProtocol, null, ioHandler);
		}
		
		protected Abstract(
			final String           defaultProtocol,
			final ACreator.Creator creatorCreator ,
			final H                ioHandler
		)
		{
			this(defaultProtocol, creatorCreator, AccessManager::New, ioHandler);
		}
		
		protected Abstract(
			final String                defaultProtocol     ,
			final ACreator.Creator      creatorCreator      ,
			final AccessManager.Creator accessManagerCreator,
			final H                     ioHandler
		)
		{
			super();
			this.rootDirectories = EqHashTable.New();
			this.defaultProtocol = defaultProtocol  ;
			this.creator         = this.ensureCreator(creatorCreator);
			this.ioHandler       = ioHandler        ;
			
			// called at the very last just in case the creator needs some of the other state
			this.accessManager = accessManagerCreator.createAccessManager(this);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final void validateIsWritable()
		{
			this.ioHandler.validateIsWritable();
		}
		
		@Override
		public final boolean isWritable()
		{
			return this.ioHandler.isWritable();
		}
		
		protected ACreator ensureCreator(final ACreator.Creator creatorCreator)
		{
			return creatorCreator == null
				? this
				: creatorCreator.createCreator(this)
			;
		}
		
		@Override
		public AFileSystem fileSystem()
		{
			return this;
		}
		
		@Override
		public final String defaultProtocol()
		{
			return this.defaultProtocol;
		}
		
		@Override
		public ACreator creator()
		{
			return this.creator;
		}
		
		@Override
		public AccessManager accessManager()
		{
			return this.accessManager;
		}
		
		@Override
		public H ioHandler()
		{
			return this.ioHandler;
		}
		

		@Override
		public final synchronized ADirectory lookupRoot(final String identifier)
		{
			return this.rootDirectories.get(identifier);
		}

		@Override
		public final synchronized ADirectory getRoot(final String identifier)
		{
			final ADirectory existing = this.lookupRoot(identifier);
			if(existing != null)
			{
				return existing;
			}

			throw new AfsExceptionUnresolvableRoot("No root directory found with identifier \"" + identifier + "\".");
		}

		@Override
		public final synchronized ADirectory ensureRoot(final String identifier)
		{
			return this.ensureRoot(this.creator, identifier);
		}
				
		@Override
		public final synchronized <I extends AItem> I validateMember(final I item)
		{
			if(item.fileSystem() == this)
			{
				return item;
			}

			throw new AfsExceptionConsistency(
				"Incompatible parent FileSystem of " + XChars.systemString(item) + ":"
				+ XChars.systemString(item.fileSystem()) + " != this (" + XChars.systemString(this) + ")."
			);
		}
		
		private boolean validateRegisteredRootDirectory(final ADirectory rootDirectory)
		{
			final String rootIdentifier = rootDirectory.identifier();
			final ADirectory registered = this.rootDirectories.get(rootIdentifier);
			if(registered == null)
			{
				return false;
			}
			
			if(registered == rootDirectory)
			{
				return true;
			}
			
			throw new AfsExceptionConsistency(
				"Inconsistent root directories for identifier \"" + rootIdentifier + "\": "
				+ XChars.systemString(registered) + " != " + XChars.systemString(rootDirectory)
			);
		}
		
		private void validateIsUnusedRootDirectory(final ADirectory rootDirectory)
		{
			if(!this.accessManager.isUsed(rootDirectory))
			{
				return;
			}
			
			throw new AfsExceptionMutationInUse(
				"Root directory \"" + rootDirectory.identifier() + " is used an cannot be removed."
			);
		}

		@Override
		public final synchronized ADirectory ensureRoot(
			final ARoot.Creator rootCreator,
			final String        identifier
		)
		{
			ADirectory root = this.rootDirectories.get(identifier);
			if(root != null)
			{
				return root;
			}
			
			root = rootCreator.createRootDirectory(this, identifier);
			this.rootDirectories.add(identifier, root);
			
			return root;
		}
		
		@Override
		public ADirectory ensureDefaultRoot()
		{
			throw new UnsupportedOperationException(
				"This file system implementation (" + this.getClass().getName() +
				") doesn't support a default root. " +
				"Please ensure to create files only in named parent directories."
			);
		}

		@Override
		public final synchronized boolean addRoot(final ADirectory rootDirectory)
		{
			this.validateMember(rootDirectory);
			
			// validate and check for already registered (abort condition)
			if(this.validateRegisteredRootDirectory(rootDirectory))
			{
				return false;
			}
			
			return this.rootDirectories.add(rootDirectory.identifier(), rootDirectory);
		}
		
		@Override
		public final synchronized ADirectory removeRoot(final String name)
		{
			final ADirectory rootDirectory = this.getRoot(name);

			this.removeRoot(rootDirectory);
			
			return rootDirectory;
		}
		
		@Override
		public final synchronized boolean removeRoot(final ADirectory rootDirectory)
		{
			if(!this.validateRegisteredRootDirectory(rootDirectory))
			{
				return false;
			}
			
			this.validateIsUnusedRootDirectory(rootDirectory);
			
			// remove only if no inconsistency was detected.
			this.rootDirectories.removeFor(rootDirectory.identifier());
			
			return true;
		}
		
		@Override
		public final synchronized <R> R accessRoots(
			final Function<? super XGettingTable<String, ADirectory>, R> logic
		)
		{
			return logic.apply(this.rootDirectories);
		}
		
				
		@Override
		public final synchronized ADirectory resolveDirectoryPath(
			final String[] pathElements,
			final int      offset      ,
			final int      length
		)
		{
			// getRoot guarantees non-null or exception.
			final ADirectory root = this.getRoot(pathElements[offset]);
			
			return root.resolveDirectoryPath(pathElements, offset + 1, length - 1);
		}
		
		@Override
		public final synchronized ADirectory ensureDirectoryPath(
			final String[] pathElements,
			final int      offset      ,
			final int      length
		)
		{
			if(length <= 0)
			{
				return this.ensureDefaultRoot();
			}
			
			XArrays.validateArrayRange(pathElements, offset, length);
						
			final ADirectory root = this.ensureRoot(pathElements[offset]);
			
			ADirectory directory = root;
			for(int o = offset + 1, l = length - 1; l > 0; o++, l--)
			{
				final String pathElement = pathElements[o];
				ADirectory elementDir = directory.getDirectory(pathElement);
				if(elementDir == null)
				{
					elementDir = directory.ensureDirectory(pathElement);
				}
				
				directory = elementDir;
			}
			
			return directory;
		}
		
		@Override
		public final synchronized AFile ensureFilePath(
			final String[] directoryPathElements,
			final int      offset               ,
			final int      length               ,
			final String   fileIdentifier
		)
		{
			final ADirectory directory = this.ensureDirectoryPath(directoryPathElements, offset, length);
			
			AFile file = directory.getFile(fileIdentifier);
			if(file == null)
			{
				file = directory.ensureFile(fileIdentifier);
			}
			
			return file;
		}
		
		protected abstract VarString assembleItemPath(AItem item, VarString vs);
		
		@Override
		public VarString assemblePath(final ADirectory directory, final VarString vs)
		{
			return this.assembleItemPath(directory, vs);
		}
		
		@Override
		public VarString assemblePath(final AFile file, final VarString vs)
		{
			return this.assembleItemPath(file, vs);
		}
		
		@Override
		public String[] buildPath(final AItem item)
		{
			return AItem.buildItemPath(item);
		}

		@Override
		public ARoot createRootDirectory(
			final AFileSystem fileSystem,
			final String      protocol  ,
			final String      identifier
		)
		{
			return ARoot.New(fileSystem, protocol, identifier);
		}
		
	}
	
}
