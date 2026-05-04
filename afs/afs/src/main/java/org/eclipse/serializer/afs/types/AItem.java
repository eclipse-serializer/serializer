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

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.chars.XChars;

/**
 * Common supertype of every element in an {@link AFileSystem}: directories ({@link ADirectory},
 * {@link ARoot}) and files ({@link AFile}). An item is uniquely identified within its
 * {@link #parent()} directory by its {@link #identifier()} and globally within the file system
 * by its {@link #toPathString()}.
 * <p>
 * The {@link AItem.Wrapper} sub-interface marks transient view objects (such as readable / writable
 * file handles) that delegate to an underlying actual item; {@link AItem#actual(AItem)} unwraps
 * such views to the underlying item.
 *
 * @see ADirectory
 * @see AFile
 * @see ARoot
 * @see AFileSystem
 */
public interface AItem
{
	/**
	 * The {@link AFileSystem} this item belongs to. Every item is owned by exactly one file system.
	 *
	 * @return the owning file system.
	 */
	public AFileSystem fileSystem();

	/**
	 * The directory (identifying container) in which this item is located and in which
	 * no other item can have the same {@link #identifier()} as this item.
	 *
	 * @see #identifier()
	 * @see #toPathString()
	 *
	 * @return the item's parent directory.
	 */
	public ADirectory parent();
	
	/**
	 * The value that uniquely identifies the item globally in the whole file system.
	 * <p>
	 * Note that this value is usually a combination of the identifiers of {@link #parent()} directories
	 * and the local {@link #identifier()}, but such a relation is not mandatory.
	 * 
	 * @see #parent()
	 * @see #identifier()
	 * 
	 * @return the item's globally unique identifier.
	 */
	public String toPathString();

	/**
	 * The item's path expressed as the sequence of {@link #identifier() identifiers} from the root
	 * down to (and including) this item.
	 *
	 * @see #toPathString()
	 *
	 * @return the item's path elements.
	 */
	public String[] toPath();

	/**
	 * The value that uniquely identifies the item locally in its {@link #parent()} directory.
	 * 
	 * @see #parent()
	 * @see #toPathString()
	 * 
	 * @return the item's locally unique identifier.
	 */
	public String identifier();
	
	/**
	 * Queries whether the item represented by this instance actually physically exists on the underlying storage layer.
	 * 
	 * @return whether the item exists.
	 */
	public boolean exists();


	/**
	 * The default separator used to render path strings for items whose file system does not
	 * dictate a different one.
	 *
	 * @return the default path separator character ({@code '/'}).
	 */
	public static char defaultSeparator()
	{
		return '/';
	}


	/**
	 * Builds the path of the passed item by walking from the item up to the root, collecting the
	 * {@link #identifier() identifiers} in root-to-leaf order.
	 *
	 * @param item the item whose path to build.
	 *
	 * @return the path elements from root to {@code item}, inclusive.
	 */
	public static String[] buildItemPath(final AItem item)
	{
		// doing the quick loop twice is enormously faster than populating a dynamically growing collection.
		int depth = 0;
		for(AItem i = item; i != null; i = i.parent())
		{
			depth++;
		}
		
		final String[] path = new String[depth];
		for(AItem i = item; i != null; i = i.parent())
		{
			path[--depth] = i.identifier();
		}
		
		return path;
	}
	
	/**
	 * Appends a debug-friendly representation of the item (system identity hash plus quoted path)
	 * to the passed {@link VarString}.
	 *
	 * @param item the item to describe.
	 * @param vs   the {@link VarString} to append to.
	 *
	 * @return the passed {@link VarString} for chaining.
	 */
	public static VarString assembleDebugString(final AItem item, final VarString vs)
	{
		XChars.addSystemString(item, vs);
		vs.add('(').add('"');
		XChars.appendArraySeperated(vs, AItem.defaultSeparator(), (Object[])AItem.buildItemPath(item));
		vs.add('"').add(')');
		
		return vs;
	}
	
	
	/**
	 * Base class for {@link AItem} implementations that supplies a debug-friendly
	 * {@link #toString()} via {@link AItem#assembleDebugString(AItem, VarString)}.
	 */
	public abstract class Base implements AItem
	{

		@Override
		public String toString()
		{
			final VarString vs = VarString.New(50);
			AItem.assembleDebugString(this, vs);

			return vs.toString();
		}
	}

	/**
	 * Abstract {@link AItem} implementation that holds the immutable {@link #identifier()
	 * identifier} and exposes a {@link #mutex()} for synchronizing item-local operations.
	 */
	public abstract class Abstract extends AItem.Base
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String identifier;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final String identifier)
		{
			super();
			this.identifier = notNull(identifier);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public final String identifier()
		{
			return this.identifier;
		}
		
		protected final Object mutex()
		{
			// must be accessible externally, anyway, because of observer calling.
			return this;
		}
		
	}
	
		
	
	/**
	 * Unwraps the passed item to its underlying actual item, walking through any {@link Wrapper}
	 * layers. Returns the passed item unchanged if it is not a wrapper.
	 *
	 * @param item the item to unwrap (may be a wrapper).
	 *
	 * @return the underlying non-wrapper {@link AItem}.
	 */
	public static AItem actual(final AItem item)
	{
		return item instanceof AItem.Wrapper
			? ((AItem.Wrapper)item).actual()
			: item
		;
	}

	/**
	 * Marker interface for view objects that wrap an underlying {@link AItem} for the purpose of
	 * a specific {@link #user() user}'s usage (e.g. a {@link AReadableFile}/{@link AWritableFile}
	 * handle wrapping the underlying {@link AFile}).
	 *
	 * @see AItem#actual(AItem)
	 */
	public interface Wrapper extends AItem
	{
		/**
		 * The underlying non-wrapper item this wrapper delegates to.
		 *
		 * @return the actual item.
		 */
		public AItem actual();

		/**
		 * The user this wrapper was created for. Used by the {@link AccessManager} to track usage
		 * claims and resolve access conflicts.
		 *
		 * @return the user object.
		 */
		public Object user();
	}

}
