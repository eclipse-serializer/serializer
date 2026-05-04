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

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.afs.exceptions.AfsExceptionUnresolvablePathElement;
import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.XArrays;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.functional.XFunc;
import org.eclipse.serializer.typing.XTypes;

/**
 * A directory in an {@link AFileSystem}. A directory is an {@link AItem} that holds zero or more
 * child items: nested {@link ADirectory directories} and {@link AFile files}, each addressable by
 * a locally unique {@link AItem#identifier() identifier}.
 * <p>
 * Children are populated either by explicit {@link #ensureDirectory(String)} / {@link
 * #ensureFile(String)} calls or by {@link #inventorize()}, which reads the underlying physical
 * directory through the file system's {@link AIoHandler}. The {@link #consolidate()} family of
 * methods removes registered children that no longer have a physical counterpart.
 * <p>
 * Lookup with {@link #getDirectory(String)} / {@link #getFile(String)} returns {@code null} for
 * unknown identifiers; {@link #resolveDirectoryPath(String[], int, int)} throws
 * {@link org.eclipse.serializer.afs.exceptions.AfsExceptionUnresolvablePathElement} on missing
 * elements. The {@code ensure*} variants create missing entries.
 * <p>
 * {@link Observer}s registered on a directory receive structural-change callbacks when its child
 * files or sub-directories are created, moved or deleted.
 *
 * @see ARoot
 * @see AFile
 * @see AFileSystem
 * @see Observer
 */
public interface ADirectory extends AItem, AResolving
{
	@Override
	public default String toPathString()
	{
		return this.fileSystem().assemblePath(this);
	}

	@Override
	public default String[] toPath()
	{
		return this.fileSystem().buildPath(this);
	}
	
	/**
	 * Provides synchronized access to the live child-directory table. The table is held under the
	 * directory's mutex while {@code logic} runs, so the consumer must not retain a reference to it.
	 *
	 * @param <R>   the result type.
	 * @param logic the function to apply to the child-directory table.
	 *
	 * @return the value returned by {@code logic}.
	 */
	public <R> R accessDirectories(Function<? super XGettingTable<String, ? extends ADirectory>, R> logic);

	/**
	 * Provides synchronized access to the live child-file table. The table is held under the
	 * directory's mutex while {@code logic} runs, so the consumer must not retain a reference to it.
	 *
	 * @param <R>   the result type.
	 * @param logic the function to apply to the child-file table.
	 *
	 * @return the value returned by {@code logic}.
	 */
	public <R> R accessFiles(Function<? super XGettingTable<String, ? extends AFile>, R> logic);

	/**
	 * Variant of {@link #accessDirectories(Function)} that passes an additional subject argument
	 * to the logic, avoiding the need to capture it in a closure.
	 *
	 * @param <S>     the subject type.
	 * @param <R>     the result type.
	 * @param subject the subject argument.
	 * @param logic   the function to apply to the child-directory table and {@code subject}.
	 *
	 * @return the value returned by {@code logic}.
	 */
	public <S, R> R accessDirectories(S subject, BiFunction<? super XGettingTable<String, ? extends ADirectory>, S, R> logic);

	/**
	 * Variant of {@link #accessFiles(Function)} that passes an additional subject argument to the
	 * logic, avoiding the need to capture it in a closure.
	 *
	 * @param <S>     the subject type.
	 * @param <R>     the result type.
	 * @param subject the subject argument.
	 * @param logic   the function to apply to the child-file table and {@code subject}.
	 *
	 * @return the value returned by {@code logic}.
	 */
	public <S, R> R accessFiles(S subject, BiFunction<? super XGettingTable<String, ? extends AFile>, S, R> logic);

	/**
	 * Registers the passed {@link Observer} for structural-change notifications on this directory.
	 * Has no effect if the observer is already registered.
	 *
	 * @param observer the observer to register.
	 *
	 * @return {@code true} if the observer was newly registered.
	 */
	public boolean registerObserver(ADirectory.Observer observer);

	/**
	 * Removes a previously registered {@link Observer}.
	 *
	 * @param observer the observer to remove.
	 *
	 * @return {@code true} if the observer was registered and got removed.
	 */
	public boolean removeObserver(ADirectory.Observer observer);

	/**
	 * Iterates over the observers currently registered on this directory.
	 *
	 * @param <C>   the consumer type.
	 * @param logic the consumer to apply.
	 *
	 * @return the passed consumer for chaining.
	 */
	public <C extends Consumer<? super ADirectory.Observer>> C iterateObservers(C logic);

	/**
	 * Ensures that this directory physically exists in the underlying storage layer, creating it
	 * (and any missing parents, depending on the implementation) if necessary.
	 *
	 * @return {@code true} if the directory was created by this call, {@code false} if it already existed.
	 */
	public default boolean ensureExists()
	{
		return this.fileSystem().ioHandler().ensureExists(this);
	}

	/**
	 * Returns the child sub-directory with the passed identifier, creating and registering it if
	 * it is not yet known.
	 *
	 * @param identifier the locally unique identifier of the sub-directory.
	 *
	 * @return the existing or newly created sub-directory.
	 */
	public ADirectory ensureDirectory(String identifier);

	/**
	 * Returns the child file with the passed identifier, creating and registering it if it is not
	 * yet known. Both name and type are derived from the identifier by the file system.
	 *
	 * @param identifier the locally unique identifier of the file.
	 *
	 * @return the existing or newly created file.
	 *
	 * @see #ensureFile(String, String, String)
	 */
	public default AFile ensureFile(final String identifier)
	{
		return this.ensureFile(identifier, null, null);
	}

	/**
	 * Returns the child file with the identifier derived from the passed name and type, creating
	 * and registering it if it is not yet known.
	 *
	 * @param name the file's {@link AFile#name() name}.
	 * @param type the file's {@link AFile#type() type}, or {@code null}.
	 *
	 * @return the existing or newly created file.
	 *
	 * @see #ensureFile(String, String, String)
	 */
	public default AFile ensureFile(final String name, final String type)
	{
		return this.ensureFile(null, name, type);
	}

	/**
	 * Returns the child file matching the passed coordinates, creating and registering it if it is
	 * not yet known. Either {@code identifier} or {@code name} must be non-{@code null}; missing
	 * coordinates are derived from the supplied ones via the file system.
	 *
	 * @param identifier the locally unique identifier, or {@code null} to derive it from {@code name}/{@code type}.
	 * @param name       the file's {@link AFile#name() name}, or {@code null} to derive it from {@code identifier}.
	 * @param type       the file's {@link AFile#type() type}, or {@code null}.
	 *
	 * @return the existing or newly created file.
	 */
	public AFile ensureFile(String identifier, String name, String type);

	/**
	 * Returns the child item ({@link AFile} or {@link ADirectory}) with the passed identifier, or
	 * {@code null} if no child with that identifier is registered.
	 *
	 * @param identifier the locally unique identifier.
	 *
	 * @return the matching child item, or {@code null}.
	 */
	public AItem getItem(String identifier);

	/**
	 * Returns the child sub-directory with the passed identifier, or {@code null} if none is
	 * registered.
	 *
	 * @param identifier the locally unique identifier.
	 *
	 * @return the matching sub-directory, or {@code null}.
	 */
	public ADirectory getDirectory(String identifier);

	/**
	 * Returns the child file with the passed identifier, or {@code null} if none is registered.
	 *
	 * @param identifier the locally unique identifier.
	 *
	 * @return the matching file, or {@code null}.
	 */
	public AFile getFile(String identifier);

	/**
	 * Iterates over all registered child items (directories first, then files).
	 *
	 * @param <C>      the iterator type.
	 * @param iterator the consumer to apply.
	 *
	 * @return the passed iterator for chaining.
	 */
	public <C extends Consumer<? super AItem>> C iterateItems(C iterator);

	/**
	 * Iterates over all registered child sub-directories.
	 *
	 * @param <C>      the iterator type.
	 * @param iterator the consumer to apply.
	 *
	 * @return the passed iterator for chaining.
	 */
	public <C extends Consumer<? super ADirectory>> C iterateDirectories(C iterator);

	/**
	 * Iterates over all registered child files.
	 *
	 * @param <C>      the iterator type.
	 * @param iterator the consumer to apply.
	 *
	 * @return the passed iterator for chaining.
	 */
	public <C extends Consumer<? super AFile>> C iterateFiles(C iterator);

	/**
	 * Reads the underlying physical directory through the file system's {@link AIoHandler} and
	 * registers any children that are not yet known to this directory instance.
	 *
	 * @return this directory for chaining.
	 */
	public ADirectory inventorize();
	
	/**
	 * Returns a snapshot collection of all registered child items.
	 *
	 * @return a snapshot of the child items.
	 */
	public default XGettingEnum<AItem> listItems()
	{
		return AFS.listItems(this, XFunc.all());
	}

	/**
	 * Returns a snapshot collection of all registered child sub-directories.
	 *
	 * @return a snapshot of the child sub-directories.
	 */
	public default XGettingEnum<ADirectory> listDirectories()
	{
		return AFS.listDirectories(this, XFunc.all());
	}

	/**
	 * Returns a snapshot collection of all registered child files.
	 *
	 * @return a snapshot of the child files.
	 */
	public default XGettingEnum<AFile> listFiles()
	{
		return AFS.listFiles(this, XFunc.all());
	}

	/**
	 * Whether the passed item is a direct child of this directory.
	 *
	 * @param item the item to test.
	 *
	 * @return {@code true} if {@code item.parent() == this}.
	 */
	public boolean contains(AItem item);

	/**
	 * Whether the passed directory is a direct child of this directory.
	 *
	 * @param directory the directory to test.
	 *
	 * @return {@code true} if {@code directory.parent() == this}.
	 */
	public default boolean contains(final ADirectory directory)
	{
		return this.contains((AItem)directory);
	}

	/**
	 * Whether the passed file is a direct child of this directory.
	 *
	 * @param file the file to test.
	 *
	 * @return {@code true} if {@code file.parent() == this}.
	 */
	public default boolean contains(final AFile file)
	{
		return this.contains((AItem)file);
	}

	/**
	 * Whether the passed item is a transitive descendant of this directory (i.e. located somewhere
	 * inside it, at any nesting depth).
	 *
	 * @param item the item to test.
	 *
	 * @return {@code true} if this directory is an ancestor of {@code item}.
	 */
	public boolean containsDeep(AItem item);

	/**
	 * Whether the passed directory is a transitive descendant of this directory.
	 *
	 * @param directory the directory to test.
	 *
	 * @return {@code true} if this directory is an ancestor of {@code directory}.
	 */
	public default boolean containsDeep(final ADirectory directory)
	{
		return this.containsDeep((AItem)directory);
	}

	/**
	 * Whether the passed file is a transitive descendant of this directory.
	 *
	 * @param file the file to test.
	 *
	 * @return {@code true} if this directory is an ancestor of {@code file}.
	 */
	public default boolean containsDeep(final AFile file)
	{
		return this.containsDeep((AItem)file);
	}


	/**
	 * Whether this directory directly contains a registered child (file or directory) with the
	 * passed identifier.
	 *
	 * @param itemName the identifier to test.
	 *
	 * @return {@code true} if such a child is registered.
	 */
	public boolean containsItem(String itemName);

	/**
	 * Whether this directory directly contains a registered sub-directory with the passed
	 * identifier.
	 *
	 * @param directoryName the identifier to test.
	 *
	 * @return {@code true} if such a sub-directory is registered.
	 */
	public boolean containsDirectory(String directoryName);

	/**
	 * Whether this directory directly contains a registered file with the passed identifier.
	 *
	 * @param fileName the identifier to test.
	 *
	 * @return {@code true} if such a file is registered.
	 */
	public boolean containsFile(String fileName);
		
	
	@Override
	public ADirectory resolveDirectoryPath(String[] pathElements, int offset, int length);
	
	@Override
	public default boolean exists()
	{
		return this.fileSystem().ioHandler().exists(this);
	}
	
	/**
	 * Removes all child items ({@link ADirectory} or {@link AFile}) that have no physical equivalent.
	 * @return the amount of removed items
	 */
	public int consolidate();

	/**
	 * Removes all registered child sub-directories that have no physical equivalent in the
	 * underlying storage layer.
	 *
	 * @return the number of removed sub-directories.
	 */
	public int consolidateDirectories();

	/**
	 * Removes all registered child files that have no physical equivalent in the underlying
	 * storage layer.
	 *
	 * @return the number of removed files.
	 */
	public int consolidateFiles();
	
	/* (03.06.2020 TM)TODO: priv#49: directory mutation:
	 * - move directory to target directory
	 * - delete directory
	 * - rename directory
	 * 
	 * Each is only allowed if there are no uses for that directory
	 * 
	 * (19.07.2020 TM):
	 * Downgraded to T0D0 since we do not require directory mutations (for now...).
	 */
	
	/**
	 * Returns true if the directory does not contain any other file or directories
	 * 
	 * @return true if this directory is empty
	 */
	public boolean isEmpty();
	
	/**
	 * Skeletal {@link ADirectory} implementation that maintains the in-memory child tables and
	 * observer list and synchronizes mutating access on the directory's mutex.
	 */
	public abstract class Abstract
	extends AItem.Abstract
	implements ADirectory
	{
		///////////////////////////////////////////////////////////////////////////
		// static fields //
		//////////////////
		
		private static final ADirectory.Observer[] NO_OBSERVERS = new ADirectory.Observer[0];
		private static final EqHashTable<String, Object> EMPTY = EqHashTable.NewCustom(0);
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		@SuppressWarnings("unchecked") // safe due to not containing any elements
		static <T> EqHashTable<String, T> emptyTable()
		{
			return (EqHashTable<String, T>)EMPTY;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private EqHashTable<String, ADirectory> directories = emptyTable();
		private EqHashTable<String, AFile>      files       = emptyTable();

		// memory-optimized array because there should usually be no or very few observers (<= 10).
		private ADirectory.Observer[] observers = NO_OBSERVERS;
		
		// note the 8 bytes cost for this flag due to memory padding. Or there are 7 more bytes "free" for future fields.
		private boolean inventorized;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final String identifier)
		{
			super(identifier);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
			
		@Override
		public final boolean isEmpty() 
		{
			synchronized(this.mutex())
			{
				return this.fileSystem().ioHandler().isEmpty(this);
			}
		}
		
		@Override
		public final AItem getItem(final String identifier)
		{
			synchronized(this.mutex())
			{
				final AFile file = this.getFile(identifier);
				if(file != null)
				{
					return file;
				}
				
				return this.getDirectory(identifier);
			}
		}
		
		private ADirectory internalGetDirectory(final String identifier)
		{
			return this.directories.get(identifier);
		}
		
		@Override
		public final ADirectory getDirectory(final String identifier)
		{
			synchronized(this.mutex())
			{
				return this.internalGetDirectory(identifier);
			}
		}
		
		@Override
		public final AFile getFile(final String identifier)
		{
			synchronized(this.mutex())
			{
				return this.files.get(identifier);
			}
		}
		
		@Override
		public final ADirectory inventorize()
		{
			synchronized(this.mutex())
			{
				this.fileSystem().ioHandler().inventorize(this);
			}
			
			return this;
		}
		
		private void ensureInventorized()
		{
			if(this.inventorized)
			{
				return;
			}
			
			this.inventorize();
			this.inventorized = true;
		}
		
		@Override
		public final <C extends Consumer<? super AItem>> C iterateItems(final C iterator)
		{
			synchronized(this.mutex())
			{
				this.iterateDirectories(iterator);
				this.iterateFiles(iterator);
			}
					
			return iterator;
		}
		
		@Override
		public <C extends Consumer<? super ADirectory>> C iterateDirectories(final C iterator)
		{
			synchronized(this.mutex())
			{
				this.ensureInventorized();
				this.directories.values().iterate(iterator);
			}
					
			return iterator;
		}
		
		@Override
		public <C extends Consumer<? super AFile>> C iterateFiles(final C iterator)
		{
			synchronized(this.mutex())
			{
				this.ensureInventorized();
				this.files.values().iterate(iterator);
			}
					
			return iterator;
		}
		
		@Override
		public int consolidate()
		{
			long count = 0;
			synchronized(this.mutex())
			{
				final XGettingEnum<String> physicalItems = this.fileSystem().ioHandler().listItems(this);
				
				count += this.directories.keys().removeBy(dirName ->
					!physicalItems.contains(dirName)
				);
				
				count += this.files.keys().removeBy(fileName ->
					!physicalItems.contains(fileName)
				);
			}
			
			return XTypes.to_int(count);
		}
		
		@Override
		public int consolidateDirectories()
		{
			long count = 0;
			synchronized(this.mutex())
			{
				final XGettingEnum<String> physicalDirectories = this.fileSystem().ioHandler().listDirectories(this);
				
				count += this.directories.keys().removeBy(dirName ->
					!physicalDirectories.contains(dirName)
				);
			}
			
			return XTypes.to_int(count);
		}
		
		@Override
		public int consolidateFiles()
		{
			long count = 0;
			synchronized(this.mutex())
			{
				final XGettingEnum<String> physicalFiles = this.fileSystem().ioHandler().listFiles(this);
				count += this.files.keys().removeBy(fileName ->
					!physicalFiles.contains(fileName)
				);
			}
			
			return XTypes.to_int(count);
		}
		
		@Override
		public final boolean contains(final AItem item)
		{
			// cannot lock both since hierarchy order is not clear. But one is sufficient, anyway.
			synchronized(this.mutex())
			{
				return item.parent() == this;
			}
		}
		
		@Override
		public boolean containsDeep(final AItem item)
		{
			// cannot lock both since hierarchy order is not clear. But one is sufficient, anyway.
			synchronized(this.mutex())
			{
				for(AItem i = item; (i = i.parent()) != null;)
				{
					if(i == this)
					{
						return true;
					}
				}
				
				return false;
			}
		}
		
		@Override
		public final boolean containsItem(final String itemName)
		{
			synchronized(this.mutex())
			{
				return this.containsFile(itemName)
					|| this.containsDirectory(itemName)
				;
			}
		}
		
		@Override
		public final boolean containsDirectory(final String directoryName)
		{
			synchronized(this.mutex())
			{
				return this.directories.get(directoryName) != null;
			}
		}
		
		@Override
		public final boolean containsFile(final String fileName)
		{
			synchronized(this.mutex())
			{
				return this.files.get(fileName) != null;
			}
		}
		
		private void register(final String identifier, final ADirectory directory)
		{
			if(this.directories == ADirectory.Abstract.<ADirectory>emptyTable())
			{
				this.directories = EqHashTable.New();
			}
			this.directories.add(identifier, directory);
		}
		
		private void register(final String identifier, final AFile file)
		{
			if(this.files == ADirectory.Abstract.<AFile>emptyTable())
			{
				this.files = EqHashTable.New();
			}
			this.files.add(identifier, file);
		}
		
		@Override
		public final ADirectory ensureDirectory(final String identifier)
		{
			synchronized(this.mutex())
			{
				ADirectory directory = this.directories.get(identifier);
				if(directory == null)
				{
					directory = this.fileSystem().creator().createDirectory(this, identifier);
					this.register(identifier, directory);
					// note: inventorize is only called on-demand.
				}
				
				return directory;
			}
		}
		
		@Override
		public final AFile ensureFile(final String identifier, final String name, final String type)
		{
			// either identifier or name must be non-null. Type may be null.
			final String effIdnt, effName, effType;
			
			if(identifier == null)
			{
				effName = notNull(name);
				effType = mayNull(type);
				effIdnt = this.fileSystem().deriveFileIdentifier(name, type);
			}
			else
			{
				effIdnt = identifier;
				effName = name != null
					? name
					: this.fileSystem().deriveFileName(identifier)
				;
				effType = type != null
					? type
					: this.fileSystem().deriveFileType(identifier) // might return null yet again.
				;
			}
			
			synchronized(this.mutex())
			{
				AFile file = this.files.get(effIdnt);
				if(file == null)
				{
					file = this.fileSystem().creator().createFile(this, effIdnt, effName, effType);
					this.register(effIdnt, file);
				}
				
				return file;
			}
		}
		
		@Override
		public final ADirectory resolveDirectoryPath(
			final String[] pathElements,
			final int      offset      ,
			final int      length
		)
		{
			// length means distance in this case. If no more distance remains (length 0), "this" is the result.
			if(length == 0)
			{
				// note: identifier validation makes no sense at this point. Length 0 always means "this".
				return this;
			}
			
			// array bounds validation after trivial / always-correct length 0 case
			XArrays.validateArrayRange(pathElements, offset, length);
			
			// requires the central lock but calls an internal method, so this lock must be acquired here
			synchronized(this.fileSystem())
			{
				ADirectory currentDirectory = this;
				for(int o = offset, l = length; l > 0; o++, l--)
				{
					final ADirectory resolvedChildDirectory = currentDirectory.getDirectory(pathElements[o]);
					if(resolvedChildDirectory == null)
					{
						throw new AfsExceptionUnresolvablePathElement(
							VarString.New()
							.add("Unresolvable path element \"")
							.add(pathElements[offset])
							.add("\" in path \"")
							.addAll(pathElements, VarString::commaSpace)
							.deleteLast(2)
							.add('"', '.')
							.toString()
						);
					}
					
					// recursion implemented as iteration instead of recursive calls (potential stack overflow)
					currentDirectory = resolvedChildDirectory;
				}
				
				return currentDirectory;
			}
		}
		
		@Override
		public final <R> R accessDirectories(
			final Function<? super XGettingTable<String, ? extends ADirectory>, R> logic
		)
		{
			synchronized(this.mutex())
			{
				return logic.apply(this.directories);
			}
		}
		
		@Override
		public final <R> R accessFiles(
			final Function<? super XGettingTable<String, ? extends AFile>, R> logic
		)
		{
			synchronized(this.mutex())
			{
				return logic.apply(this.files);
			}
		}

		@Override
		public final<S, R> R accessDirectories(
			final S                                                                     subject,
			final BiFunction<? super XGettingTable<String, ? extends ADirectory>, S, R> logic
		)
		{
			synchronized(this.mutex())
			{
				return logic.apply(this.directories, subject);
			}
		}
		
		@Override
		public final<S, R> R accessFiles(
			final S                                                                subject,
			final BiFunction<? super XGettingTable<String, ? extends AFile>, S, R> logic
		)
		{
			synchronized(this.mutex())
			{
				return logic.apply(this.files, subject);
			}
		}
		
		@Override
		public final boolean registerObserver(final ADirectory.Observer observer)
		{
			synchronized(this.mutex())
			{
				// best performance and common case for first observer
				if(this.observers == NO_OBSERVERS)
				{
					this.observers = X.Array(observer);
					return true;
				}

				// general case: if not yet contained, add.
				if(!XArrays.contains(this.observers, observer))
				{
					this.observers = XArrays.add(this.observers, observer);
					return true;
				}

				// already contained
				return false;
			}
		}
		
		@Override
		public final boolean removeObserver(final ADirectory.Observer observer)
		{
			synchronized(this.mutex())
			{
				// best performance and special (also weirdly common) case for last/sole observer.
				if(this.observers.length == 1 && this.observers[0] == observer)
				{
					this.observers = NO_OBSERVERS;
					return true;
				}

				// cannot be contained in empty array. Should happen a lot, worth checking.
				if(this.observers == NO_OBSERVERS)
				{
					return false;
				}

				// general case: remove if contained.
				final int index = XArrays.indexOf(observer, this.observers);
				if(index >= 0)
				{
					XArrays.remove(this.observers, index);
					return true;
				}
				
				// not contained.
				return false;
			}
		}
		
		@Override
		public final <C extends Consumer<? super Observer>> C iterateObservers(final C logic)
		{
			synchronized(this.mutex())
			{
				return XArrays.iterate(this.observers, logic);
			}
		}
		
	}
	
	
	/**
	 * Creates a new {@link ADirectory} with the passed parent and identifier.
	 *
	 * @param parent     the parent directory; must not be {@code null}.
	 * @param identifier the directory's locally unique identifier; must not be {@code null}.
	 *
	 * @return a new directory instance.
	 */
	public static ADirectory New(final ADirectory parent, final String identifier)
	{
		return new ADirectory.Default(
			notNull(parent),
			notNull(identifier)
		);
	}

	/**
	 * Default {@link ADirectory} implementation with a fixed parent reference; the owning
	 * {@link AFileSystem} is reached through {@link #parent()}.
	 */
	public class Default extends Abstract
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ADirectory parent;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final ADirectory parent, final String identifier)
		{
			super(identifier);
			this.parent = parent;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final ADirectory parent()
		{
			return this.parent;
		}
		
		@Override
		public final AFileSystem fileSystem()
		{
			return this.parent.fileSystem();
		}
		
	}
		
	// (19.05.2020 TM)TODO: priv#49: call directory Observer directory methods
	/**
	 * Listener interface notified about structural changes to a directory: creation, move and
	 * deletion of child files and sub-directories. Each event has matching {@code onBefore*}
	 * and {@code onAfter*} callbacks.
	 */
	public interface Observer
	{
		/**
		 * Invoked immediately before a child file is created.
		 *
		 * @param fileToCreate the file about to be created.
		 */
		public void onBeforeFileCreate(AWritableFile fileToCreate);

		/**
		 * Invoked immediately after a child file has been created.
		 *
		 * @param createdFile the newly created file.
		 */
		public void onAfterFileCreate(AWritableFile createdFile);


		/**
		 * Invoked immediately before a child file is moved.
		 *
		 * @param fileToMove the file about to be moved.
		 * @param targetFile the move target.
		 */
		public void onBeforeFileMove(AWritableFile fileToMove, AWritableFile targetFile);

		/**
		 * Invoked immediately after a child file has been moved.
		 *
		 * @param movedFile  the file that was moved.
		 * @param targetFile the move target.
		 */
		public void onAfterFileMove(AWritableFile movedFile, AWritableFile targetFile);


		/**
		 * Invoked immediately before a child file is deleted.
		 *
		 * @param fileToDelete the file about to be deleted.
		 */
		public void onBeforeFileDelete(AWritableFile fileToDelete);

		/**
		 * Invoked immediately after a child file deletion attempt.
		 *
		 * @param deletedFile the file that was attempted to be deleted.
		 * @param result      {@code true} if the file was actually deleted by this call.
		 */
		public void onAfterFileDelete(AWritableFile deletedFile, boolean result);


		/**
		 * Invoked immediately before a child sub-directory is created.
		 *
		 * @param directoryToCreate the directory about to be created.
		 */
		public void onBeforeDirectoryCreate(ADirectory directoryToCreate);

		/**
		 * Invoked immediately after a child sub-directory has been created.
		 *
		 * @param createdDirectory the newly created sub-directory.
		 */
		public void onAfterDirectoryCreate(ADirectory createdDirectory);


		/**
		 * Invoked immediately before a child sub-directory is moved.
		 *
		 * @param directoryToMove the sub-directory about to be moved.
		 * @param targetDirectory the move target.
		 */
		public void onBeforeDirectoryMove(ADirectory directoryToMove, ADirectory targetDirectory);

		/**
		 * Invoked immediately after a child sub-directory has been moved.
		 *
		 * @param movedDirectory  the sub-directory that was moved.
		 * @param sourceDirectory the source directory the move originated from.
		 */
		public void onAfterDirectoryMove(ADirectory movedDirectory, ADirectory sourceDirectory);


		/**
		 * Invoked immediately before a child sub-directory is deleted.
		 *
		 * @param directoryToDelete the sub-directory about to be deleted.
		 */
		public void onBeforeDirectoryDelete(ADirectory directoryToDelete);

		/**
		 * Invoked immediately after a child sub-directory deletion attempt.
		 *
		 * @param deletedDirectory the sub-directory that was attempted to be deleted.
		 * @param result           {@code true} if the sub-directory was actually deleted by this call.
		 */
		public void onAfterDirectoryDelete(ADirectory deletedDirectory, boolean result);

	}
	
	
	/**
	 * Unwraps the passed directory to its underlying actual directory, walking through any
	 * {@link Wrapper} layers. Returns the passed directory unchanged if it is not a wrapper.
	 *
	 * @param directory the directory to unwrap (may be a wrapper).
	 *
	 * @return the underlying non-wrapper {@link ADirectory}.
	 */
	public static ADirectory actual(final ADirectory directory)
	{
		return directory instanceof ADirectory.Wrapper
			? ((ADirectory.Wrapper)directory).actual()
			: directory
		;
	}

	/**
	 * Sub-interface of {@link AItem.Wrapper} for wrappers around an {@link ADirectory}.
	 */
	public interface Wrapper extends ADirectory, AItem.Wrapper
	{
		@Override
		public ADirectory actual();

	}

}
