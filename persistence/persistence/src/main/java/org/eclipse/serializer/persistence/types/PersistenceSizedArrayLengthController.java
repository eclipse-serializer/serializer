package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
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

import org.eclipse.serializer.math.XMath;

/**
 * Decides what capacity to actually allocate when reconstructing a sized array (e.g. the backing array of
 * an {@link java.util.ArrayList}) during loading. The persistent representation carries both the originally
 * specified capacity and the actual element count; this controller picks the value used for the allocation.
 * <p>
 * The choice is a trade-off between fidelity and safety. When loading data from <em>trusted</em> storage
 * the original capacity should be preserved so program behavior does not change. When loading data from
 * <em>untrusted</em> sources (e.g. network input) an attacker could otherwise force huge allocations by
 * fabricating an oversized capacity field &mdash; an "array bomb". Three bundled strategies cover the
 * spectrum:
 * <ul>
 * <li>{@link Unrestricted} &mdash; honor the specified capacity unchanged. Recommended for trusted storage.</li>
 * <li>{@link Fitting} &mdash; ignore the specified capacity and shrink to the actual element count.
 * Recommended for untrusted communication.</li>
 * <li>{@link Limited} &mdash; honor the specified capacity but cap it at a configured maximum (or at the
 * actual element count when that is larger). A pragmatic middle ground.</li>
 * </ul>
 */
public interface PersistenceSizedArrayLengthController
{
	/**
	 * Decides the array capacity to allocate for the passed sized-array record.
	 *
	 * @param specifiedCapacity  the capacity value read from the persistent record.
	 * @param actualElementCount the actual element count read from the persistent record.
	 *
	 * @return the capacity that should be allocated.
	 */
	public int controlArrayLength(int specifiedCapacity, int actualElementCount);



	/**
	 * Recommended for storing data (does not change program behavior).
	 * @return an unrestricted array length controller
	 *
	 */
	public static PersistenceSizedArrayLengthController.Unrestricted Unrestricted()
	{
		return new PersistenceSizedArrayLengthController.Unrestricted();
	}

	/**
	 * Recommended for communication (prevents array bombs).
	 * @return a fitting array length controller
	 */
	public static PersistenceSizedArrayLengthController.Fitting Fitting()
	{
		return new PersistenceSizedArrayLengthController.Fitting();
	}

	/**
	 * Returns a {@link Limited} controller that caps the specified capacity at {@code maximumCapacity} (or
	 * at the actual element count when that is larger). The maximum must be positive.
	 *
	 * @param maximumCapacity the maximum allocation capacity.
	 *
	 * @return the limited controller.
	 */
	public static PersistenceSizedArrayLengthController.Limited Limited(final int maximumCapacity)
	{
		return new PersistenceSizedArrayLengthController.Limited(
			XMath.positive(maximumCapacity)
		);
	}

	/**
	 * Honors the specified capacity unchanged, preserving program behavior across persistence roundtrips.
	 * Recommended for trusted storage.
	 */
	public final class Unrestricted implements PersistenceSizedArrayLengthController
	{
		Unrestricted()
		{
			super();
		}

		@Override
		public final int controlArrayLength(final int specifiedCapacity, final int actualElementCount)
		{
			return specifiedCapacity;
		}

	}

	/**
	 * Ignores the specified capacity and shrinks the allocation to the actual element count, preventing
	 * array-bomb attacks at the cost of dropping any spare capacity. Recommended for untrusted
	 * communication.
	 */
	public final class Fitting implements PersistenceSizedArrayLengthController
	{
		Fitting()
		{
			super();
		}

		@Override
		public final int controlArrayLength(final int specifiedCapacity, final int actualElementCount)
		{
			return actualElementCount;
		}

	}

	/**
	 * Honors the specified capacity but caps it at a configured maximum &mdash; or at the actual element
	 * count when that already exceeds the maximum. Pragmatic middle ground between {@link Unrestricted}
	 * and {@link Fitting}.
	 */
	public final class Limited implements PersistenceSizedArrayLengthController
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int limit;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Limited(final int limit)
		{
			super();
			this.limit = limit;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		/**
		 * The configured maximum capacity (above the actual element count).
		 *
		 * @return the limit.
		 */
		public final int limit()
		{
			return this.limit;
		}

		@Override
		public final int controlArrayLength(final int specifiedCapacity, final int actualElementCount)
		{
			return Math.min(specifiedCapacity, Math.max(this.limit, actualElementCount));
		}
		
	}
	
}
