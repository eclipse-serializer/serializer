package test.eclipse.serializer.danglingref;

/*-
 * #%L
 * Eclipse Serializer Integration Tests
 * %%
 * Copyright (C) 2023 - 2026 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryStorer;
import org.eclipse.serializer.persistence.types.PersistenceIdSet;
import org.eclipse.serializer.persistence.types.PersistenceManager;
import org.eclipse.serializer.persistence.types.PersistenceSource;
import org.eclipse.serializer.persistence.types.PersistenceStorer;
import org.eclipse.serializer.persistence.types.PersistenceTarget;
import org.eclipse.serializer.reference.Lazy;
import org.eclipse.serializer.util.X;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Regression guard for the trusted-object-id capture (the serializer side of the store's
 * {@code reference-validation} feature): a commit's {@link Binary#trustedObjectIds()} must
 * contain exactly the object ids the storer wrote as references WITHOUT storing the entity
 * itself — registry-known skipped instances and unloaded {@code Lazy} references' cached ids —
 * and must NOT contain ids the same commit stores (referenced-and-stored is guaranteed, not
 * trusted). With capturing disabled the transport field stays {@code null}.
 * <p>
 * The capture feeds the storage-side validation ({@code reference-validation = log|fail|heal});
 * a silently shrunken or over-eager capture would turn that validation into false negatives
 * (missed dangling references) or false positives (rejected healthy stores) respectively.
 */
@Timeout(60)
public class TrustedObjectIdsCaptureTest
{
	PersistenceManager<Binary> persistenceManager;

	@AfterEach
	public void afterTest()
	{
		if(this.persistenceManager != null)
		{
			this.persistenceManager.close();
		}
	}

	/**
	 * Records the Binary handed to the target so the test can inspect the commit's
	 * trusted-id transport field. Writes always succeed.
	 */
	static final class RecordingTarget implements PersistenceTarget<Binary>
	{
		Binary lastWrite;

		@Override
		public void write(final Binary data)
		{
			this.lastWrite = data;
		}

		@Override
		public boolean isWritable()
		{
			return true;
		}
	}

	private PersistenceStorer createStorer(final RecordingTarget target, final boolean capture)
	{
		final PersistenceSource<Binary> source = new PersistenceSource<Binary>()
		{
			@Override
			public XGettingCollection<? extends Binary> read()
			{
				return X.empty();
			}

			@Override
			public XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids)
			{
				return X.empty();
			}
		};

		final SerializerFoundation<?> foundation = SerializerFoundation.New();
		this.persistenceManager = foundation
			.setPersistenceSource(source)
			.setPersistenceTarget(target)
			.createPersistenceManager()
		;

		// direct creator call: wires the foundation's shared object manager, the same
		// pass-through shape the embedded storage uses (see HealingDeferredLinkTest).
		return BinaryStorer.Creator(() -> 1, false, capture, false).createLazyStorer(
			foundation.getTypeHandlerManager(),
			foundation.getObjectManager()     ,
			this.persistenceManager           ,
			target                            ,
			foundation.getBufferSizeProvider(),
			this.persistenceManager
		);
	}

	private static boolean contains(final long[] ids, final long id)
	{
		if(ids == null)
		{
			return false;
		}
		for(final long candidate : ids)
		{
			if(candidate == id)
			{
				return true;
			}
		}
		return false;
	}

	@Test
	void captureContainsSkippedAndUnloadedLazyIdsButNotStoredIds()
	{
		final RecordingTarget   target = new RecordingTarget();
		final PersistenceStorer storer = this.createStorer(target, true);

		// a registry-known instance the lazy storer will SKIP (trusted):
		final Child skipped   = new Child("skipped");
		final long skippedOid = 1_000_000_000_920_000_001L;
		this.persistenceManager.objectRegistry().registerObject(skippedOid, skipped);

		// an unloaded Lazy: link a loaded reference to its cached oid, then clear it so only
		// the cached id remains (referent gone = the trusted, unhealable-if-missing class):
		final long lazyTargetOid = 1_000_000_000_920_000_002L;
		final Lazy<Child> unloaded = Lazy.UnregisteredReference(new Child("lazy target"));
		((Lazy.Default<Child>)unloaded).$link(lazyTargetOid, this.persistenceManager);
		unloaded.clear();

		// a child stored IN the same commit (referenced and stored -> pruned, not trusted):
		final Child storedChild = new Child("stored");

		final Parent parent = new Parent(skipped, unloaded, storedChild);
		storer.store(parent);
		storer.commit();

		assertNotNull(target.lastWrite, "the commit must have written data");
		final long[] trusted = target.lastWrite.trustedObjectIds();
		assertNotNull(trusted, "capture enabled: the commit must transport its trusted ids");

		assertTrue(contains(trusted, skippedOid),
			"a registry-known skipped instance's id must be captured as trusted, got: " + Arrays.toString(trusted));
		assertTrue(contains(trusted, lazyTargetOid),
			"an unloaded Lazy's cached id must be captured as trusted, got: " + Arrays.toString(trusted));

		final long storedChildOid = this.persistenceManager.objectRegistry().lookupObjectId(storedChild);
		assertFalse(contains(trusted, storedChildOid),
			"an id stored by the same commit is guaranteed, not trusted - it must be pruned, got: "
				+ Arrays.toString(trusted));
		final long parentOid = this.persistenceManager.objectRegistry().lookupObjectId(parent);
		assertFalse(contains(trusted, parentOid),
			"the stored root itself must never be in the trusted set, got: " + Arrays.toString(trusted));
	}

	@Test
	void captureDisabledTransportsNothing()
	{
		final RecordingTarget   target = new RecordingTarget();
		final PersistenceStorer storer = this.createStorer(target, false);

		final Child skipped = new Child("skipped");
		this.persistenceManager.objectRegistry().registerObject(1_000_000_000_920_000_003L, skipped);

		storer.store(new Parent(skipped, null, new Child("stored")));
		storer.commit();

		assertNotNull(target.lastWrite, "the commit must have written data");
		assertNull(target.lastWrite.trustedObjectIds(),
			"capture disabled: the transport field must stay null (zero overhead contract)");
	}


	///////////////////////////////////////////////////////////////////////////
	// data types //
	///////////////

	public static class Parent
	{
		public Child       skipped;
		public Lazy<Child> lazy   ;
		public Child       stored ;

		public Parent(final Child skipped, final Lazy<Child> lazy, final Child stored)
		{
			super();
			this.skipped = skipped;
			this.lazy    = lazy   ;
			this.stored  = stored ;
		}
	}

	public static class Child
	{
		public String data;

		public Child(final String data)
		{
			super();
			this.data = data;
		}
	}
}
