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

import static org.eclipse.serializer.util.X.notNull;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.collections.XArrays;

/**
 * A file in an {@link AFileSystem}. Files are leaf {@link AItem items}: they have a {@link #parent()
 * parent} directory and a content {@link #size() size}, but no children.
 * <p>
 * I/O is never performed directly on an {@link AFile}. Callers obtain a usage handle through
 * {@link #useReading()} / {@link #useWriting()} (or the {@link #tryUseReading() tryUse} variants);
 * the resulting {@link AReadableFile} / {@link AWritableFile} represents an active access claim
 * tracked by the {@link AccessManager} and must be {@link AReadableFile#release() released} when
 * the caller is done. {@link AFS} wrappers such as {@link AFS#applyWriting(AFile,
 * java.util.function.Function)} encapsulate this acquire/release lifecycle.
 * <p>
 * {@link Observer}s registered on a file are notified of writes, moves, truncations, deletions and
 * lifecycle transitions performed through writable / readable handles.
 *
 * @see AReadableFile
 * @see AWritableFile
 * @see AccessManager
 * @see Observer
 */
public interface AFile extends AItem
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
	 * A simple String representing the "name" of the file. While no two files can have the same {@link #identifier()}
	 * inside a given directory, any number of files can have the same name.<p>
	 * Depending on the file system implementation, {@link #name()} might be the same value as {@link #identifier()},
	 * but while {@link #identifier()} is guaranteed to be a unique local identifier for any file system,
	 * {@link #name()} is not.
	 * 
	 * @see #toPathString()
	 * @see #identifier()
	 * @see #type()
	 * 
	 * @return the file's name.
	 */
	public default String name()
	{
		return this.fileSystem().getFileName(this);
	}
	
	/**
	 * An optional String defining the type of the file's content.
	 * <p>
	 * If such an information makes no sense for a certain file system, this value may be <code>null</code>.
	 * 
	 * @return the file's type.
	 */
	public default String type()
	{
		return this.fileSystem().getFileType(this);
	}
	
	/**
	 * Returns the size in bytes of this file's content, without any space required for file metadata (name etc.).
	 * 
	 * @return the size in bytes of this file's content.
	 */
	public default long size()
	{
		// this.ensureExists() is too expensive. Using logic must call it precisely when needed
		// (20.09.2020 TM) priv#392
		
		return this.fileSystem().ioHandler().size(this);
	}
	
	/**
	 * Whether the file's content {@link #size() size} is zero.
	 *
	 * @return {@code true} if this file has no content.
	 */
	public default boolean isEmpty()
	{
		return this.size() == 0;
	}

	/**
	 * Registers the passed {@link Observer} to receive notifications about operations on this file.
	 * Has no effect if the observer is already registered.
	 *
	 * @param observer the observer to register.
	 *
	 * @return {@code true} if the observer was newly registered, {@code false} if it was already present.
	 */
	public boolean registerObserver(AFile.Observer observer);

	/**
	 * Removes a previously registered {@link Observer}.
	 *
	 * @param observer the observer to remove.
	 *
	 * @return {@code true} if the observer was registered and got removed, {@code false} otherwise.
	 */
	public boolean removeObserver(AFile.Observer observer);

	/**
	 * Iterates over the observers currently registered on this file, passing each to the supplied
	 * consumer.
	 *
	 * @param <C>   the consumer type.
	 * @param logic the consumer to apply.
	 *
	 * @return the passed consumer for chaining.
	 */
	public <C extends Consumer<? super AFile.Observer>> C iterateObservers(C logic);

	/**
	 * Acquires a shared (reading) usage handle on this file for the passed user.
	 * <p>
	 * The returned handle must be {@link AReadableFile#release() released} when no longer needed.
	 *
	 * @param user the user identity to register the usage under.
	 *
	 * @return a readable file handle.
	 */
	public default AReadableFile useReading(final Object user)
	{
		return this.fileSystem().accessManager().useReading(this, user);
	}

	// implementations also need to cover locking.
	/**
	 * Acquires an exclusive (writing) usage handle on this file for the passed user.
	 * <p>
	 * The returned handle must be {@link AWritableFile#release() released} when no longer needed.
	 *
	 * @param user the user identity to register the usage under.
	 *
	 * @return a writable file handle.
	 */
	public default AWritableFile useWriting(final Object user)
	{
		return this.fileSystem().accessManager().useWriting(this, user);
	}

	/**
	 * Acquires a shared (reading) usage handle on this file for the {@link #defaultUser() default user}.
	 *
	 * @return a readable file handle.
	 *
	 * @see #useReading(Object)
	 */
	public default AReadableFile useReading()
	{
		return this.fileSystem().accessManager().useReading(this);
	}

	/**
	 * Acquires an exclusive (writing) usage handle on this file for the {@link #defaultUser() default user}.
	 *
	 * @return a writable file handle.
	 *
	 * @see #useWriting(Object)
	 */
	public default AWritableFile useWriting()
	{
		return this.fileSystem().accessManager().useWriting(this);
	}

	/**
	 * Tries to acquire a shared (reading) usage handle on this file for the passed user, returning
	 * {@code null} if shared access cannot currently be granted instead of throwing.
	 *
	 * @param user the user identity to register the usage under.
	 *
	 * @return a readable file handle, or {@code null} if shared access is not currently available.
	 */
	public default AReadableFile tryUseReading(final Object user)
	{
		return this.fileSystem().accessManager().tryUseReading(this, user);
	}

	// Implementation is also responsible for locking
	/**
	 * Tries to acquire an exclusive (writing) usage handle on this file for the passed user,
	 * returning {@code null} if exclusive access cannot currently be granted instead of throwing.
	 *
	 * @param user the user identity to register the usage under.
	 *
	 * @return a writable file handle, or {@code null} if exclusive access is not currently available.
	 */
	public default AWritableFile tryUseWriting(final Object user)
	{
		return this.fileSystem().accessManager().tryUseWriting(this, user);
	}

	/**
	 * Tries to acquire a shared (reading) usage handle on this file for the {@link #defaultUser()
	 * default user}, returning {@code null} if shared access cannot currently be granted.
	 *
	 * @return a readable file handle, or {@code null} if shared access is not currently available.
	 */
	public default AReadableFile tryUseReading()
	{
		return this.fileSystem().accessManager().tryUseReading(this);
	}

	/**
	 * Tries to acquire an exclusive (writing) usage handle on this file for the {@link
	 * #defaultUser() default user}, returning {@code null} if exclusive access cannot currently
	 * be granted.
	 *
	 * @return a writable file handle, or {@code null} if exclusive access is not currently available.
	 */
	public default AWritableFile tryUseWriting()
	{
		return this.fileSystem().accessManager().tryUseWriting(this);
	}

	@Override
	public default boolean exists()
	{
		return this.fileSystem().ioHandler().exists(this);
	}

	// required to query the file size, for example
	/**
	 * Ensures that this file physically exists in the underlying storage layer, creating an empty
	 * file if necessary.
	 * <p>
	 * Acquires a writing handle internally and is therefore subject to exclusive access conflicts.
	 *
	 * @return {@code true} if the file was created by this call, {@code false} if it already existed.
	 */
	public default boolean ensureExists()
	{
		// if(this.exists()) is very expensive, so double-checking to avoid a lambda instance is a bad idea
		// (20.09.2020 TM) priv#392

		return AFS.applyWriting(this, wf -> wf.ensureExists());
	}

	/**
	 * Whether this file is currently in use by any user (reading or writing) according to the
	 * {@link AccessManager}.
	 *
	 * @return {@code true} if at least one usage claim exists for this file.
	 */
	public default boolean isUsed()
	{
		return this.fileSystem().accessManager().isUsed(this);
	}

	/**
	 * The default user the {@link AccessManager} associates with usage claims when no explicit
	 * user is supplied.
	 *
	 * @return the default user identity.
	 */
	public default Object defaultUser()
	{
		return this.fileSystem().accessManager().defaultUser();
	}
		
	
	
	/**
	 * Creates a new {@link AFile} with the passed parent directory and identifier.
	 *
	 * @param parent     the parent directory; must not be {@code null}.
	 * @param identifier the file's locally unique identifier; must not be {@code null}.
	 *
	 * @return a new {@link AFile} instance.
	 */
	public static AFile New(
		final ADirectory  parent    ,
		final String      identifier
	)
	{
		return new AFile.Default(
			notNull(parent),
			notNull(identifier)
		);
	}

	/**
	 * Default {@link AFile} implementation. Manages the file's parent reference and observer list.
	 */
	public class Default
	extends AItem.Abstract
	implements AFile
	{
		///////////////////////////////////////////////////////////////////////////
		// static fields //
		//////////////////
		
		private static final AFile.Observer[] NO_OBSERVERS = new AFile.Observer[0];
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final ADirectory parent;
		
		// memory-optimized array because there should usually be no or very few observers (<= 10).
		private AFile.Observer[] observers = NO_OBSERVERS;
		
		
		
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
		
		@Override
		public final boolean registerObserver(final AFile.Observer observer)
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
		public final boolean removeObserver(final AFile.Observer observer)
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

	// (27.05.2020 TM)TODO: priv#49: Composite Observer implementation with lambda-based factory
	/**
	 * Listener interface notified about operations performed on an {@link AFile} through one of its
	 * usage handles. All callbacks are no-ops by default; implementations only need to override the
	 * events they care about.
	 * <p>
	 * Observers are registered via {@link AFile#registerObserver(Observer)} and are invoked
	 * synchronously by the file's I/O methods, both before and after the underlying operation.
	 */
	public interface Observer
	{
		/**
		 * Invoked immediately before bytes are written to {@code targetFile}.
		 *
		 * @param targetFile the file about to be written to.
		 * @param sources    the byte buffers about to be written.
		 */
		public default void onBeforeFileWrite(
			final AWritableFile                  targetFile,
			final Iterable<? extends ByteBuffer> sources
		)
		{
			// no-op by default
		}

		/**
		 * Invoked immediately after bytes have been written to {@code targetFile}.
		 *
		 * @param targetFile the file that was written to.
		 * @param sources    the byte buffers that were written.
		 * @param writeCount the number of bytes written.
		 */
		public default void onAfterFileWrite(
			final AWritableFile                  targetFile,
			final Iterable<? extends ByteBuffer> sources   ,
			final long                           writeCount
		)
		{
			// no-op by default
		}

		/**
		 * Invoked immediately before {@code sourceFile} is moved to {@code targetFile}.
		 *
		 * @param sourceFile the file about to be moved.
		 * @param targetFile the move target.
		 */
		public default void onBeforeFileMove(
			final AWritableFile sourceFile,
			final AWritableFile targetFile
		)
		{
			// no-op by default
		}

		/**
		 * Invoked immediately after {@code sourceFile} has been moved to {@code targetFile}.
		 *
		 * @param sourceFile the file that was moved.
		 * @param targetFile the move target.
		 */
		public default void onAfterFileMove(
			final AWritableFile sourceFile,
			final AWritableFile targetFile
		)
		{
			// no-op by default
		}

		/**
		 * Invoked immediately before a readable handle is closed.
		 *
		 * @param fileToClose the handle about to be closed.
		 */
		public default void onBeforeFileClose(final AReadableFile fileToClose)
		{
			// no-op by default
		}

		/**
		 * Invoked immediately after a readable handle has been closed.
		 *
		 * @param closedFile the handle that was closed.
		 * @param result     {@code true} if the close call performed work; {@code false} if the handle was already closed.
		 */
		public default void onAfterFileClose(final AReadableFile closedFile, final boolean result)
		{
			// no-op by default
		}

		/**
		 * Invoked immediately before the underlying physical file is created.
		 *
		 * @param fileToCreate the file about to be created.
		 */
		public default void onBeforeFileCreate(final AWritableFile fileToCreate)
		{
			// no-op by default
		}

		/**
		 * Invoked immediately after the underlying physical file has been created.
		 *
		 * @param fileToCreate the file that was created.
		 */
		public default void onAfterFileCreate(final AWritableFile fileToCreate)
		{
			// no-op by default
		}

		/**
		 * Invoked immediately before the file is truncated to the passed new size.
		 *
		 * @param fileToTruncate the file about to be truncated.
		 * @param newSize        the size in bytes the file is being truncated to.
		 */
		public default void onBeforeFileTruncation(final AWritableFile fileToTruncate, final long newSize)
		{
			// no-op by default
		}

		/**
		 * Invoked immediately after the file has been truncated to the passed new size.
		 *
		 * @param truncatedFile the file that was truncated.
		 * @param newSize       the new size in bytes.
		 */
		public default void onAfterFileTruncation(final AWritableFile truncatedFile, final long newSize)
		{
			// no-op by default
		}

		/**
		 * Invoked immediately before the underlying physical file is deleted.
		 *
		 * @param fileToDelete the file about to be deleted.
		 */
		public default void onBeforeFileDelete(final AWritableFile fileToDelete)
		{
			// no-op by default
		}

		/**
		 * Invoked immediately after a delete attempt on the underlying physical file.
		 *
		 * @param deletedFile the file that was attempted to be deleted.
		 * @param result      {@code true} if the file was actually deleted by this call.
		 */
		public default void onAfterFileDelete(
			final AWritableFile deletedFile,
			final boolean       result
		)
		{
			// no-op by default
		}

	}

	/**
	 * Unwraps the passed file to its underlying actual file, walking through any {@link Wrapper}
	 * layers. Returns the passed file unchanged if it is not a wrapper.
	 *
	 * @param file the file to unwrap (may be a wrapper).
	 *
	 * @return the underlying non-wrapper {@link AFile}.
	 */
	public static AFile actual(final AFile file)
	{
		return file instanceof AFile.Wrapper
			? ((AFile.Wrapper)file).actual()
			: file
		;
	}

	/**
	 * Sub-interface of {@link AItem.Wrapper} for wrappers around an {@link AFile}, used most
	 * notably by {@link AReadableFile} and {@link AWritableFile} to associate a usage handle with
	 * the underlying file and the user that holds the claim.
	 */
	public interface Wrapper extends AFile, AItem.Wrapper
	{
		@Override
		public AFile actual();

		/**
		 * Skeletal {@link AFile.Wrapper} implementation that holds the wrapped file and user, and
		 * delegates {@link AItem} queries to the wrapped file.
		 *
		 * @param <U> the user type.
		 */
		public abstract class Abstract<U> extends AItem.Base implements AFile.Wrapper
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final AFile actual;
			private final U     user  ;
			
						
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			protected Abstract(final AFile actual, final U user)
			{
				super();
				this.actual  = notNull(actual) ;
				this.user    = notNull(user)   ;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			protected Object mutex()
			{
				// the singleton file is the mutex
				return this.actual;
			}
			
			@Override
			public U user()
			{
				return this.user;
			}
			
			@Override
			public AFile actual()
			{
				return this.actual instanceof AFile.Wrapper
					? ((AFile.Wrapper)this.actual).actual()
					: this.actual;
			}
			
			@Override
			public AFileSystem fileSystem()
			{
				return this.actual.fileSystem();
			}

			@Override
			public boolean registerObserver(final Observer observer)
			{
				return this.actual.registerObserver(observer);
			}

			@Override
			public boolean removeObserver(final Observer observer)
			{
				return this.actual.removeObserver(observer);
			}
			
			@Override
			public <C extends Consumer<? super Observer>> C iterateObservers(final C logic)
			{
				return this.actual.iterateObservers(logic);
			}

			@Override
			public ADirectory parent()
			{
				return this.actual.parent();
			}

			@Override
			public String identifier()
			{
				return this.actual.identifier();
			}

			@Override
			public String name()
			{
				return this.actual.name();
			}

			@Override
			public String type()
			{
				return this.actual.type();
			}

			@Override
			public boolean exists()
			{
				return this.actual.exists();
			}
			
		}
		
	}
	
}
