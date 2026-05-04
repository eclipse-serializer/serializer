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

import org.eclipse.serializer.afs.exceptions.*;
import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.collections.HashTable;
import org.eclipse.serializer.collections.XArrays;
import org.eclipse.serializer.util.X;

import java.util.function.Function;

import static org.eclipse.serializer.util.X.notNull;

/**
 * Arbitrates concurrent access to the {@link AFile}s and {@link ADirectory directories} of an
 * {@link AFileSystem}. The manager tracks which user holds which kind of usage claim and rejects
 * incompatible requests with a matching {@link AfsExceptionConflict} subtype.
 * <p>
 * Two kinds of usage claims exist:
 * <ul>
 *   <li><em>Shared (reading)</em>: any number of users may simultaneously hold a shared claim on
 *       a file as long as no exclusive claim is in place.</li>
 *   <li><em>Exclusive (writing)</em>: at most one user may hold an exclusive claim on a file, and
 *       only when no other user holds a shared claim on it.</li>
 * </ul>
 * The {@code use*} methods throw on conflict; the {@code tryUse*} variants return {@code null} on
 * conflict instead. {@link #downgrade(AWritableFile)} converts an exclusive claim into a shared
 * one; {@link #unregister(AReadableFile)} releases a claim (typically called from
 * {@link AReadableFile#release()}).
 * <p>
 * Structural mutations on a directory are coordinated through {@link
 * #executeMutating(ADirectory, Function)}, which acquires a directory-level mutation lock and
 * rejects concurrent file usage claims under that subtree with {@link AfsExceptionMutation} or
 * {@link AfsExceptionMutationInUse}.
 * <p>
 * The {@link #defaultUser() default user} is the current thread, which is what the no-argument
 * convenience overloads use.
 *
 * @see AFileSystem#accessManager()
 * @see AfsExceptionConflict
 */
public interface AccessManager
{
	/**
	 * The {@link AFileSystem} this access manager belongs to.
	 *
	 * @return the owning file system.
	 */
	public AFileSystem fileSystem();

	/**
	 * Whether the passed directory or any of its descendants currently holds a usage claim.
	 *
	 * @param directory the directory to check.
	 *
	 * @return {@code true} if the directory subtree is in use.
	 */
	public boolean isUsed(ADirectory directory);

	/**
	 * Whether the passed directory is currently in a structural mutation (see
	 * {@link #executeMutating(ADirectory, Function)}).
	 *
	 * @param directory the directory to check.
	 *
	 * @return {@code true} if the directory is being mutated.
	 */
	public boolean isMutating(ADirectory directory);

	/**
	 * Whether any user currently holds a usage claim on the passed file.
	 *
	 * @param file the file to check.
	 *
	 * @return {@code true} if at least one shared or exclusive claim exists for the file.
	 */
	public boolean isUsed(AFile file);

	/**
	 * Whether any user currently holds a reading-capable claim (shared or exclusive) on the
	 * passed file.
	 *
	 * @param file the file to check.
	 *
	 * @return {@code true} if a reading-capable claim exists for the file.
	 */
	public boolean isUsedReading(AFile file);

	/**
	 * Whether any user currently holds an exclusive (writing) claim on the passed file.
	 *
	 * @param file the file to check.
	 *
	 * @return {@code true} if an exclusive claim exists for the file.
	 */
	public boolean isUsedWriting(AFile file);



	/**
	 * Whether the passed user currently holds a reading-capable claim on the file.
	 *
	 * @param file the file to check.
	 * @param user the user identity.
	 *
	 * @return {@code true} if {@code user} has a reading-capable claim on the file.
	 */
	public boolean isUsedReading(AFile file, Object user);

	/**
	 * Whether the passed user currently holds the exclusive (writing) claim on the file.
	 *
	 * @param file the file to check.
	 * @param user the user identity.
	 *
	 * @return {@code true} if {@code user} holds the exclusive claim on the file.
	 */
	public boolean isUsedWriting(AFile file, Object user);


	/**
	 * Acquires a shared (reading) claim on the file for the passed user, returning a usage handle.
	 *
	 * @param file the file to access.
	 * @param user the user identity to register the claim under.
	 *
	 * @return a readable handle.
	 *
	 * @throws AfsExceptionSharedAttemptExclusiveUserConflict if the file is already exclusively held by a different user.
	 */
	public AReadableFile useReading(AFile file, Object user);

	/**
	 * Acquires an exclusive (writing) claim on the file for the passed user, returning a usage handle.
	 *
	 * @param file the file to access.
	 * @param user the user identity to register the claim under.
	 *
	 * @return a writable handle.
	 *
	 * @throws AfsExceptionExclusiveAttemptConflict           if the file is already exclusively held by a different user.
	 * @throws AfsExceptionExclusiveAttemptSharedUserConflict if the file is currently held by other shared users.
	 */
	public AWritableFile useWriting(AFile file, Object user);

	/**
	 * Tries to acquire a shared (reading) claim, returning {@code null} on conflict instead of
	 * throwing.
	 *
	 * @param file the file to access.
	 * @param user the user identity to register the claim under.
	 *
	 * @return a readable handle, or {@code null} if the claim cannot currently be granted.
	 */
	public AReadableFile tryUseReading(AFile file, Object user);

	/**
	 * Tries to acquire an exclusive (writing) claim, returning {@code null} on conflict instead
	 * of throwing.
	 *
	 * @param file the file to access.
	 * @param user the user identity to register the claim under.
	 *
	 * @return a writable handle, or {@code null} if the claim cannot currently be granted.
	 */
	public AWritableFile tryUseWriting(AFile file, Object user);

	/**
	 * Downgrades an exclusive claim into a shared one, returning a readable handle for the same
	 * file and user. The exclusive handle is retired in the process.
	 *
	 * @param file the writable handle to downgrade.
	 *
	 * @return a readable handle on the same file.
	 */
	public AReadableFile downgrade(AWritableFile file);


	/**
	 * Releases the claim represented by the passed handle and retires it.
	 * <p>
	 * Normal client code uses {@link AReadableFile#release()} which routes here.
	 *
	 * @param file the readable handle to unregister.
	 *
	 * @return {@code true} if a claim was unregistered, {@code false} if the handle was already retired.
	 */
	public boolean unregister(AReadableFile file);

	/**
	 * Releases the exclusive claim represented by the passed handle and retires it.
	 *
	 * @param file the writable handle to unregister.
	 *
	 * @return {@code true} if a claim was unregistered, {@code false} if the handle was already retired.
	 */
	public boolean unregister(AWritableFile file);




	/**
	 * The default user identity used by no-argument {@code useReading}/{@code useWriting}
	 * overloads. Defaults to {@link Thread#currentThread() the current thread}.
	 *
	 * @return the default user identity.
	 */
	public default Object defaultUser()
	{
		return Thread.currentThread();
	}

	/**
	 * Acquires a shared (reading) claim for the {@link #defaultUser() default user}.
	 *
	 * @param file the file to access.
	 *
	 * @return a readable handle.
	 */
	public default AReadableFile useReading(final AFile file)
	{
		return this.useReading(file, this.defaultUser());
	}

	/**
	 * Acquires an exclusive (writing) claim for the {@link #defaultUser() default user}.
	 *
	 * @param file the file to access.
	 *
	 * @return a writable handle.
	 */
	public default AWritableFile useWriting(final AFile file)
	{
		return this.useWriting(file, this.defaultUser());
	}

	/**
	 * Tries to acquire a shared (reading) claim for the {@link #defaultUser() default user}.
	 *
	 * @param file the file to access.
	 *
	 * @return a readable handle, or {@code null} if the claim cannot currently be granted.
	 */
	public default AReadableFile tryUseReading(final AFile file)
	{
		return this.useReading(file, this.defaultUser());
	}

	/**
	 * Tries to acquire an exclusive (writing) claim for the {@link #defaultUser() default user}.
	 *
	 * @param file the file to access.
	 *
	 * @return a writable handle, or {@code null} if the claim cannot currently be granted.
	 */
	public default AWritableFile tryUseWriting(final AFile file)
	{
		return this.useWriting(file, this.defaultUser());
	}

	/**
	 * Executes a structural mutation under an exclusive directory-level mutation lock.
	 * <p>
	 * While the lock is held by the current thread, no other user may acquire a usage claim under
	 * the directory subtree (such requests fail with {@link AfsExceptionMutation}), and concurrent
	 * mutation attempts on the same directory fail with {@link AfsExceptionMutationInUse}. Calls
	 * are reentrant from the same thread.
	 *
	 * @param <R>       the result type.
	 * @param directory the directory to mutate.
	 * @param logic     the mutation function.
	 *
	 * @return the value returned by {@code logic}.
	 */
	public <R> R executeMutating(
		ADirectory                      directory,
		Function<? super ADirectory, R> logic
	);



	/**
	 * Factory abstraction for instantiating an {@link AccessManager} for a given {@link AFileSystem}.
	 */
	@FunctionalInterface
	public interface Creator
	{
		/**
		 * Creates an {@link AccessManager} instance for the passed file system.
		 *
		 * @param parent the file system the manager belongs to.
		 *
		 * @return a new access manager.
		 */
		public AccessManager createAccessManager(AFileSystem parent);
	}



	/**
	 * Creates the {@linkplain Default default} access manager implementation for the passed file system.
	 *
	 * @param fileSystem the owning file system.
	 *
	 * @return a new {@link AccessManager}.
	 */
	public static AccessManager New(final AFileSystem fileSystem)
	{
		return new AccessManager.Default<>(
			notNull(fileSystem)
		);
	}

	/**
	 * Default {@link AccessManager} implementation. Tracks usage claims through per-file entries
	 * (shared user list plus optional exclusive user) and per-directory entries (in-use child
	 * counter and mutating thread). All bookkeeping is synchronized on the file system.
	 *
	 * @param <S> the concrete {@link AFileSystem} type.
	 */
	public class Default<S extends AFileSystem> implements AccessManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final S                               fileSystem         ;
		private final HashTable<ADirectory, DirEntry> usedDirectories    ;
		private final HashTable<ADirectory, Thread>   mutatingDirectories;
		private final HashTable<AFile, FileEntry>     fileUsers          ;
			
		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(final S fileSystem)
		{
			super();
			this.fileSystem          = fileSystem     ;
			this.usedDirectories     = HashTable.New();
			this.mutatingDirectories = HashTable.New();
			this.fileUsers           = HashTable.New();
		}
		
		
		static final class DirEntry
		{
			final ADirectory directory;
			int usingChildCount;
			
			DirEntry(final ADirectory directory)
			{
				super();
				this.directory = directory;
			}
			
		}
		
		static final class FileEntry
		{
			private static final AReadableFile[] NO_SHARED_USERS = new AReadableFile[0];
		
			private AReadableFile[] sharedUsers = NO_SHARED_USERS;
			
			AWritableFile exclusive;
			
			FileEntry()
			{
				super();
			}
			
			final boolean hasSharedUsers()
			{
				return this.sharedUsers != NO_SHARED_USERS;
			}
			
			final AReadableFile getIfSoleUser(final Object user)
			{
				return this.sharedUsers.length == 1 && this.sharedUsers[0].user() == user
					? this.sharedUsers[0]
					: null
				;
			}
			
			final AReadableFile getForUser(final Object user)
			{
				for(final AReadableFile f : this.sharedUsers)
				{
					if(f.user() == user)
					{
						return f;
					}
				}
				
				return null;
			}
			
			private int indexForUser(final Object user)
			{
				return XArrays.indexOf(user, this.sharedUsers, FileEntry::isForUser);
			}
			
			static final boolean isForUser(final AReadableFile file, final Object user)
			{
				return file.user() == user;
			}
			
			final void add(final AReadableFile file)
			{
				// best performance and common case for first user
				if(this.sharedUsers == NO_SHARED_USERS)
				{
					this.sharedUsers = X.Array(file);
					return;
				}

				// general case: if not yet contained, add.
				if(this.getForUser(file.user()) == null)
				{
					this.sharedUsers = XArrays.add(this.sharedUsers, file);
					return;
				}

				// already contained
			}
			
			final boolean removeShared(final AReadableFile file)
			{
				if(this.sharedUsers.length == 1 && this.sharedUsers[0] == file)
				{
					this.sharedUsers = NO_SHARED_USERS;
					
					return true;
				}
				
				final int index = this.indexForUser(file.user());
				if(index < 0)
				{
					Default.throwUnregisteredException(file, this);
				}
				
				// should never happen since creation/registration checks for that
				if(this.sharedUsers[index] != file)
				{
					throw new AfsExceptionConsistency(
						"Inconsistency detected: to be removed file \""
						+ file
						+ "\" is not the same as the one contained for the same user: \""
						+ this.sharedUsers[index]
						+ "\"."
					);
				}
				
				this.removeIndex(index);
				
				return false;
			}
			
			final void removeForUser(final Object user)
			{
				final int index = this.indexForUser(user);
				if(index < 0)
				{
					return;
				}

				this.removeIndex(index);
			}
			
			private void removeIndex(final int index)
			{
				// must enforce use of empty constant in any case.
				if(index == 0 && this.sharedUsers.length == 1)
				{
					this.sharedUsers = NO_SHARED_USERS;
				}
				else
				{
					this.sharedUsers = XArrays.remove(this.sharedUsers, index);
				}
			}
			
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final Object mutex()
		{
			return this.fileSystem;
		}
		
		@Override
		public final S fileSystem()
		{
			return this.fileSystem;
		}

		@Override
		public boolean isUsed(
			final ADirectory directory
		)
		{
			synchronized(this.mutex())
			{
				return this.usedDirectories.get(ADirectory.actual(directory)) != null;
			}
		}
		
		@Override
		public boolean isMutating(
			final ADirectory directory
		)
		{
			synchronized(this.mutex())
			{
				return this.mutatingDirectories.keys().contains(ADirectory.actual(directory));
			}
		}
		
		@Override
		public boolean isUsed(final AFile file)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(AFile.actual(file));
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive != null || e.hasSharedUsers();
			}
		}

		@Override
		public boolean isUsedReading(final AFile file)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(AFile.actual(file));
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive != null || e.hasSharedUsers();
			}
		}
		
		@Override
		public boolean isUsedWriting(final AFile file)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(AFile.actual(file));
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive != null;
			}
		}
		
		@Override
		public boolean isUsedReading(
			final AFile  file,
			final Object user
		)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(AFile.actual(file));
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive == user || e.getForUser(user) != null;
			}
		}
		
		@Override
		public boolean isUsedWriting(
			final AFile  file,
			final Object user
		)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(AFile.actual(file));
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive != null && e.exclusive.user() == user;
			}
		}
				
		@Override
		public final <R> R executeMutating(
			final ADirectory                      directory,
			final Function<? super ADirectory, R> logic
		)
		{
			synchronized(this.mutex())
			{
				final ADirectory actual = ADirectory.actual(directory);
				
				// Step 1: check for already existing mutating entry
				final Thread mutatingThread = this.mutatingDirectories.get(actual);
				if(mutatingThread == Thread.currentThread())
				{
					// execute logic WITHOUT removing logic since the call is obviously nested.
					return logic.apply(actual);
				}
				if(mutatingThread != null)
				{
					throw new AfsExceptionMutationInUse(
						"Directory \"" + directory.toPathString() + "\" already used for mutation by \"" + mutatingThread + "\"."
					);
				}
				
				// Step 2: check for already existing using entry
				final Object user = this.usedDirectories.get(actual);
				if(user != null && user != Thread.currentThread())
				{
					throw new AfsExceptionMutationInUse(
						"Directory \"" + directory.toPathString() + "\" already used by \"" + user + "\"."
					);
				}
				
				// Step 3: create mutating entry, execute logic, remove entry in any case.
				this.mutatingDirectories.add(actual, Thread.currentThread());
				try
				{
					return logic.apply(directory);
				}
				finally
				{
					this.mutatingDirectories.removeFor(actual);
				}
			}
		}
				
		@Override
		public AReadableFile useReading(final AFile file, final Object user)
		{
			synchronized(this.mutex())
			{
				return this.internalUseReading(file, user, CONFLICT_HANDLER_EXCEPTION);
			}
		}
		
		@Override
		public AReadableFile tryUseReading(final AFile file, final Object user)
		{
			synchronized(this.mutex())
			{
				return this.internalUseReading(file, user, CONFLICT_HANDLER_NO_OP);
			}
		}
		
		@Override
		public AWritableFile useWriting(final AFile file, final Object user)
		{
			synchronized(this.mutex())
			{
				return this.internalUseWriting(file, user, CONFLICT_HANDLER_EXCEPTION);
			}
		}
		
		@Override
		public AWritableFile tryUseWriting(final AFile file, final Object user)
		{
			synchronized(this.mutex())
			{
				return this.internalUseWriting(file, user, CONFLICT_HANDLER_NO_OP);
			}
		}
		
		private FileEntry createFileEntry(final AFile actual)
		{
			final FileEntry e = new FileEntry();
			this.fileUsers.add(actual, e);
			
			return e;
		}
		
		private AReadableFile registerReading(
			final FileEntry entry ,
			final AFile     actual,
			final Object    user
		)
		{
			final AReadableFile wrapper = this.registerReading(actual, user);
			entry.add(wrapper);
			
			return wrapper;
		}
		
		private AReadableFile registerReading(
			final AFile  actual,
			final Object user
		)
		{
			this.checkForMutatingParents(actual, user);
			this.incrementDirectoryUsageCount(actual.parent());

			return this.fileSystem().wrapForReading(actual, user);
		}
		
		private AReadableFile convertToReading(
			final AWritableFile file
		)
		{
			this.checkForMutatingParents(file.actual(), file.user());
			this.incrementDirectoryUsageCount(file.actual().parent());

			return this.fileSystem().convertToReading(file);
		}
				
		protected final void incrementDirectoryUsageCount(final ADirectory directory)
		{
			final ADirectory actual = ADirectory.actual(directory);
			
			DirEntry entry = this.usedDirectories.get(actual);
			if(entry == null)
			{
				entry = this.addUsedDirectoryEntry(actual);
				
				// new entry means increment usage count for parent incrementally
				if(actual.parent() != null)
				{
					this.incrementDirectoryUsageCount(actual.parent());
				}
			}
			
			entry.usingChildCount++;
			
			// note: child count incrementation on one level does not concern the parent directory count.
		}
		
		private DirEntry addUsedDirectoryEntry(final ADirectory actual)
		{
			final DirEntry entry;
			this.usedDirectories.add(actual, entry = new DirEntry(actual));
			
			return entry;
		}
		
		@Override
		public AReadableFile downgrade(final AWritableFile file)
		{
			synchronized(this.mutex())
			{
				file.validateIsNotRetired();
				
				final AFile actual = AFile.actual(file);
				final FileEntry e = this.fileUsers.get(actual);
				if(e == null)
				{
					throw new IllegalStateException("File not registered as used: " + file.toPathString());
				}
				
				final AReadableFile wrapper = this.convertToReading(file);
				e.add(wrapper);
				
				// may not retire file before conversion since that might need some of file's state.
				e.exclusive = null;
				
				return wrapper;
			}
		}
		
		protected final AReadableFile internalUseReading(
			final AFile           file           ,
			final Object          user           ,
			final ConflictHandler conflictHandler
		)
		{
			final AFile actual = AFile.actual(file);
			final FileEntry e = this.fileUsers.get(actual);
			if(e == null)
			{
				return this.registerReading(this.createFileEntry(actual), actual, user);
			}
			
			if(e.exclusive != null)
			{
				if(e.exclusive.user() == user)
				{
					return e.exclusive;
				}
				
				conflictHandler.handleSharedAttemptExclusiveUserConflict(actual, user, e);
			}
			
			AReadableFile wrapper = e.getForUser(user);
			if(wrapper == null)
			{
				wrapper = this.registerReading(e, actual, user);
			}
			
			return wrapper;
		}
		
		protected final AWritableFile internalUseWriting(
			final AFile           file           ,
			final Object          user           ,
			final ConflictHandler conflictHandler
		)
		{
			final AFile actual = AFile.actual(file);
			final FileEntry e = this.fileUsers.get(actual);
			if(e == null)
			{
				return this.createFileEntry(actual).exclusive = this.registerWriting(actual, user);
			}
			
			if(e.exclusive != null)
			{
				if(e.exclusive.user() == user)
				{
					return e.exclusive;
				}
				
				conflictHandler.handleExclusiveAttemptConflict(actual, user, e);
				
				return null;
			}
			
			if(e.hasSharedUsers())
			{
				final AReadableFile soleUserFile = e.getIfSoleUser(user);
				if(soleUserFile != null)
				{
					e.exclusive = this.convertToWriting(soleUserFile);
					e.removeForUser(user);
				}
				else
				{
					conflictHandler.handleExclusiveAttemptSharedUsersConflict(actual, user, e);
				}
			}
			else
			{
				e.exclusive = this.registerWriting(actual, user);
			}
			
			return e.exclusive;
		}
		
		private static final ConflictHandler CONFLICT_HANDLER_NO_OP = new ConflictHandler()
		{
			@Override
			public void handleSharedAttemptExclusiveUserConflict(
				final AFile     actual,
				final Object    user  ,
				final FileEntry entry
			)
			{
				// no-op
			}
			
			@Override
			public void handleExclusiveAttemptConflict(
				final AFile     actual,
				final Object    user  ,
				final FileEntry entry
			)
			{
				// no-op
			}
			
			@Override
			public void handleExclusiveAttemptSharedUsersConflict(
				final AFile     actual,
				final Object    user  ,
				final FileEntry entry
			)
			{
				// no-op
			}
		};
		
		static String toStringWithIdentity(final Object o)
		{
			return o == null
				? null
				: "(" + XChars.systemString(o) + ") " + o.toString()
			;
		}
		
		private static final ConflictHandler CONFLICT_HANDLER_EXCEPTION = new ConflictHandler()
		{
			
			@Override
			public void handleSharedAttemptExclusiveUserConflict(
				final AFile     actual,
				final Object    user  ,
				final FileEntry entry
			)
			{
				throw new AfsExceptionSharedAttemptExclusiveUserConflict(
					"File is already exclusively used by a different user: " + actual
					+ ". Exclusive user: " + toStringWithIdentity(entry.exclusive.user())
					+ ". Attempting user: " + toStringWithIdentity(user) + "."
				);
			}
			
			@Override
			public void handleExclusiveAttemptConflict(
				final AFile     actual,
				final Object    user  ,
				final FileEntry entry
			)
			{
				throw new AfsExceptionExclusiveAttemptConflict(
					"File is already used by a different exclusive user: " + actual
					+ ". Exclusive user: " + toStringWithIdentity(entry.exclusive.user())
					+ ". Attempting user: " + toStringWithIdentity(user) + "."
				);
			}
			
			@Override
			public void handleExclusiveAttemptSharedUsersConflict(
				final AFile     actual,
				final Object    user  ,
				final FileEntry entry
			)
			{
				throw new AfsExceptionExclusiveAttemptSharedUserConflict(
					"File \"" + actual.toPathString()
					+ "\" cannot be accessed exclusively since there are shared users present."
				);
			}
		};
		
		
		interface ConflictHandler
		{
			public void handleSharedAttemptExclusiveUserConflict(AFile actual, Object user, FileEntry entry);

			public void handleExclusiveAttemptConflict(AFile actual, Object user, FileEntry entry);

			public void handleExclusiveAttemptSharedUsersConflict(AFile actual, Object user, FileEntry entry);
		}

		
		private AWritableFile registerWriting(
			final AFile  actual,
			final Object user
		)
		{
			this.checkForMutatingParents(actual, user);
			this.incrementDirectoryUsageCount(actual.parent());
						
			return this.fileSystem().wrapForWriting(actual, user);
		}
		
		private AWritableFile convertToWriting(
			final AReadableFile file
		)
		{
			this.checkForMutatingParents(file.actual(), file.user());
			this.incrementDirectoryUsageCount(file.actual().parent());

			return this.fileSystem().convertToWriting(file);
		}
		
		private void checkForMutatingParents(final AFile actual, final Object user)
		{
			for(ADirectory p = actual.parent(); p != null; p = p.parent())
			{
				final Thread mutatingThread = this.mutatingDirectories.get(p);
				if(mutatingThread != null && mutatingThread != user)
				{
					throw new AfsExceptionMutation(
						"File \"" + actual.toPathString()
						+ "\" cannot be accessed by user \"" + user + "\" since directory \""
						+ p.toPathString() + "\" is in the process of being changed by user thread \"" + mutatingThread + "\"."
					);
				}
			}
		}
				
		@Override
		public boolean unregister(final AReadableFile file)
		{
			synchronized(this.mutex())
			{
				// logic has to cover writing case, anyway.
				return this.internalUnregister(file);
			}
		}
		
		@Override
		public boolean unregister(final AWritableFile file)
		{
			synchronized(this.mutex())
			{
				return this.internalUnregister(file);
			}
		}
		
		protected boolean internalUnregister(final AReadableFile file)
		{
			final AFile actual = file.actual();
			final FileEntry e = this.fileUsers.get(actual);
			if(e == null)
			{
				return false;
			}
			
			if(!this.internalUnregister(file, e))
			{
				return false;
			}
			
			this.decrementDirectoryUsageCount(actual.parent());
			optimizeMemoryUsage(this.fileUsers);
			
			return true;
		}
		
		private static void optimizeMemoryUsage(final HashTable<?, ?> collection)
		{
			if((collection.size() & 127) != 0)
			{
				return;
			}

			collection.optimize();
		}
		
		protected void decrementDirectoryUsageCount(final ADirectory directory)
		{
			final ADirectory actual = ADirectory.actual(directory);

			final DirEntry entry = this.getNonNullDirEntry(actual);
			if(--entry.usingChildCount == 0)
			{
				if(actual.parent() != null)
				{
					this.decrementDirectoryUsageCount(actual.parent());
				}
				this.usedDirectories.removeFor(actual);
				optimizeMemoryUsage(this.usedDirectories);
			}
		}
		
		protected final DirEntry getNonNullDirEntry(final ADirectory directory)
		{
			final ADirectory actual = ADirectory.actual(directory);

			final DirEntry entry = this.usedDirectories.get(actual);
			if(entry == null)
			{
				throw new IllegalStateException("Directory not registered as used: " + directory.toPathString());
			}
			
			return entry;
		}
		
		protected boolean internalUnregister(final AReadableFile file, final FileEntry entry)
		{
			// idempotence
			if(file.isRetired())
			{
				return false;
			}
			
			// AWritableFile "is a" AReadableFile, so it could be passed here and must be covered in any case.
			if(file instanceof AWritableFile)
			{
				// exclusive entries never have a shared entry (since they are not shared).
				this.unregisterExclusive((AWritableFile)file, entry);
			}
			else
			{
				this.unregisterShared(file, entry);
			}
			
			return true;
		}
		
		protected void unregisterShared(final AReadableFile file, final FileEntry entry)
		{
			file.retire();
			
			if(entry.removeShared(file) && entry.exclusive == null)
			{
				// if there is no more need for the entry itself, remove it.
				this.fileUsers.removeFor(file.actual());
			}
		}
		
		protected void unregisterExclusive(final AWritableFile file, final FileEntry entry)
		{
			this.validateExclusive(file, entry);
			this.removeExclusive(file, entry);
		}
		
		protected void validateExclusive(final AWritableFile file, final FileEntry entry)
		{
			if(entry.exclusive == file)
			{
				return;
			}
			
			throwUnregisteredException(file, entry);
		}
		
		protected static void throwUnregisteredException(final AReadableFile file, final FileEntry entry)
		{
			throw new AfsExceptionConsistency(
				"Inconsistency detected: attempting to unregister non-retired but not registered file \""
				+ file + "\"."
			);
		}
		
		protected void removeExclusive(final AWritableFile file, final FileEntry entry)
		{
			entry.exclusive = null;
			this.fileUsers.removeFor(file.actual());
			file.retire();
		}
				
	}
	
}
