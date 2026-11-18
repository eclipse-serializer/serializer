package org.eclipse.serializer.reference;

import org.eclipse.serializer.util.logging.Logging;

/*-
 * #%L
 * Eclipse Serializer Base
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

import org.slf4j.Logger;

/**
 * An extension of the {@link Lazy} interface that is intended to intercept
 * calls to the clear method of the lazy reference and deny unloading the lazy data if required.
 *
 * @param <T> type of the lazy loaded object
 */
public interface ControlledLazyReference<T> extends Lazy<T>
{
	/**
	 * Sets the controller that decides whether the reference may be cleared.
	 * The controller is not persisted: after loading, the owner has to set it again. Without a controller,
	 * {@link #clear()} and {@link #clear(Lazy.ClearingEvaluator)} keep the subject loaded.
	 *
	 * @param lazyClearController the controller, or {@code null} to deny clearing
	 */
	public void setLazyClearController(LazyClearController lazyClearController);
	
	/**
	 * This implementation of the {@link ControlledLazyReference}
	 * lets a {@link LazyClearController} decide if the lazy reference can be cleared.
	 *
	 * @param <T> type parameter
	 */
	public final class Default<T> extends Lazy.Default<T> implements ControlledLazyReference<T>
	{
		private final static Logger logger = Logging.getLogger(ControlledLazyReference.class);
				
		private LazyClearController lazyClearController;
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		* Standard constructor used by normal logic to instantiate a reference.
		*
		* @param subject the subject to be referenced.
		* @param lazyClearController the lazy reference clear controller.
		*/
		public Default(final T subject, final LazyClearController lazyClearController)
		{
			this(subject, Swizzling.toUnmappedObjectId(subject), null);
			this.lazyClearController = lazyClearController;
		}
		
		/**
		* Special constructor used by logic that lazily skips the actual instance (e.g. internal loading logic)
		* but instead provides means to get the instance at a later point in time.
		*
		* @param subject the potentially already present subject to be referenced or null.
		* @param objectId the subject's object id under which it can be reconstructed by the provided loader
		* @param loader the loader used to reconstruct the actual instance originally referenced
		*/
		Default(final T subject, final long objectId, final ObjectSwizzling loader)
		{
			super(subject, objectId, loader);
		}

		@Override
		public void setLazyClearController(final LazyClearController lazyClearController)
		{
			this.lazyClearController = lazyClearController;
		}
		
		/*
		 * The controller is not persisted: a reference loaded from storage has none until its owner sets one
		 * (a LazyArrayList/LazyHashMap segment does so when the collection is loaded). Without a controller
		 * nothing is known about the owner's state, so the data stays loaded.
		 */
		private boolean allowClear()
		{
			final LazyClearController controller = this.lazyClearController;
			if(controller == null)
			{
				logger.trace("Denied unloading Lazy reference {}: no clear controller", this);
				return false;
			}
			if(controller.allowClear())
			{
				return true;
			}
			logger.trace("Denied unloading Lazy reference {}", this);
			return false;
		}
		
		@Override
		public final synchronized T clear()
		{
			return this.allowClear() ? super.clear() : this.peek();
		}
		
		@Override
		public final synchronized boolean clear(final ClearingEvaluator clearingEvaluator)
		{
			return this.allowClear() && super.clear(clearingEvaluator);
		}
	}
}
