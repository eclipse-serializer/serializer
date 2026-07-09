package test.eclipse.serializer.loading;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.serializer.PassThroughSerializerFoundation;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryField;
import org.eclipse.serializer.persistence.binary.types.ChunksWrapper;
import org.eclipse.serializer.persistence.binary.types.CustomBinaryHandler;
import org.eclipse.serializer.persistence.types.PersistenceIdSet;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceManager;
import org.eclipse.serializer.persistence.types.PersistenceObjectRegistry;
import org.eclipse.serializer.persistence.types.PersistenceSource;
import org.eclipse.serializer.persistence.types.PersistenceTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Pins the object-registry publication point of {@code BinaryLoader} (internal issue #72):
 * a locally created instance may be registered in the global object registry only AFTER the
 * build's {@code complete} phase, because until then it is not in a consistent state and
 * lock-free readers ({@code PersistenceManager#lookupObject},
 * {@code DefaultObjectRegistry#lookupObject}) would observe a blank instance.
 * <p>
 * Setup: a {@link PersistenceManager} with a recording {@link PersistenceTarget} and a
 * replaying {@link PersistenceSource} (the recorded chunks are copied, mirroring a real
 * write/read round trip), wired with the pass-through context dispatcher — the same wiring
 * the embedded storage uses, where loaders operate directly on the manager's global registry.
 * Storing, clearing the registry, and loading again forces the loader down the
 * "create new instance" path for known object ids.
 */
@Timeout(60)
public class DeferredRegistryPublicationTest
{
	PersistenceManager<Binary> persistenceManager;

	final BulkList<Binary> recordedChunks = BulkList.New();

	@AfterEach
	public void afterTest()
	{
		if(this.persistenceManager != null)
		{
			this.persistenceManager.close();
		}
	}

	private PersistenceManager<Binary> createPersistenceManager(final ProbeHandler probeHandler)
	{
		final PersistenceTarget<Binary> target = new PersistenceTarget<Binary>()
		{
			@Override
			public void write(final Binary data)
			{
				/*
				 * Copy the written buffers instead of aliasing them: mirrors a real storage
				 * write/read round trip and guards against later buffer reuse by the storer.
				 */
				final ByteBuffer[] buffers = data.buffers();
				final ByteBuffer[] copies  = new ByteBuffer[buffers.length];
				for(int i = 0; i < buffers.length; i++)
				{
					final ByteBuffer source = buffers[i].duplicate();
					final ByteBuffer copy   = XMemory.allocateDirectNative(source.remaining());
					copy.put(source);
					copy.flip();
					copies[i] = copy;
				}
				DeferredRegistryPublicationTest.this.recordedChunks.add(ChunksWrapper.New(copies));
			}

			@Override
			public boolean isWritable()
			{
				return true;
			}
		};

		final PersistenceSource<Binary> source = new PersistenceSource<Binary>()
		{
			@Override
			public XGettingCollection<? extends Binary> read()
			{
				return DeferredRegistryPublicationTest.this.recordedChunks;
			}

			@Override
			public XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids)
			{
				// replay everything; the loader picks what it needs via its build items.
				return DeferredRegistryPublicationTest.this.recordedChunks;
			}
		};

		final SerializerFoundation<?> foundation = PassThroughSerializerFoundation.New()
			.setPersistenceSource(source)
			.setPersistenceTarget(target)
		;
		if(probeHandler != null)
		{
			foundation.registerCustomTypeHandler(probeHandler);
		}

		return this.persistenceManager = foundation.createPersistenceManager();
	}

	/**
	 * Deterministic publication-point pin, no threads involved: a custom type handler
	 * checks from within the loader's own {@code initializeState} and {@code complete}
	 * phases that the instance under construction is NOT yet visible in the global
	 * object registry, and the test checks that it IS visible right after the load.
	 */
	@Test
	void instanceIsPublishedToTheRegistryOnlyAfterComplete()
	{
		final ProbeHandler               probeHandler = new ProbeHandler();
		final PersistenceManager<Binary> manager      = this.createPersistenceManager(probeHandler);
		final PersistenceObjectRegistry  registry     = manager.objectRegistry();

		final Probe probe    = new Probe("payload");
		final long  probeOid = manager.store(probe);
		assertSame(probe, registry.lookupObject(probeOid), "stored instance must be registered");

		// simulate a fresh session: the data exists only in the recorded chunks.
		registry.clear();
		assertNull(registry.lookupObject(probeOid), "probe must not be registered after registry clear");

		probeHandler.armObservation(registry, probeOid);

		final Object reloaded = manager.getObject(probeOid);

		assertNotNull(reloaded);
		assertNotSame(probe, reloaded, "a new instance must have been created");
		assertEquals("payload", ((Probe)reloaded).getValue(), "reloaded state must be complete");

		assertEquals(Boolean.FALSE, probeHandler.registeredDuringInitialize,
			"the instance under construction must not be registry-visible during initializeState");
		assertEquals(Boolean.FALSE, probeHandler.registeredDuringComplete,
			"the instance under construction must not be registry-visible entering complete");
		assertSame(reloaded, registry.lookupObject(probeOid),
			"the fully built instance must be registered after the load");
		assertSame(reloaded, manager.getObject(probeOid),
			"a subsequent load must resolve to the registered instance");
	}

	/**
	 * Threaded race regression (the issue's scenario at serializer level): a concurrent
	 * reader spinning on the lock-free {@code PersistenceManager#lookupObject} must never
	 * observe a partially initialized instance. JDK hash collections are populated in the
	 * loader's {@code complete} phase, so before the fix the reader reliably saw the map
	 * blank (size 0) the moment it was registered.
	 */
	@Test
	void concurrentReaderMustNeverObserveABlankInstance() throws Exception
	{
		final int MAP_SIZE = 100_000;

		final PersistenceManager<Binary> manager  = this.createPersistenceManager(null);
		final PersistenceObjectRegistry  registry = manager.objectRegistry();

		final HashMap<String, String> map = new HashMap<>();
		for(int i = 0; i < MAP_SIZE; i++)
		{
			map.put("key-" + i, "value-" + i);
		}
		manager.store(map);
		final long mapOid = registry.lookupObjectId(map);

		// simulate a fresh session: the data exists only in the recorded chunks.
		registry.clear();
		assertNull(registry.lookupObject(mapOid), "map must not be registered after registry clear");

		final AtomicReference<Integer>   firstObservedSize = new AtomicReference<>();
		final AtomicReference<Throwable> readerFailure     = new AtomicReference<>();
		final Thread reader = new Thread(() ->
		{
			try
			{
				Object o;
				while((o = manager.lookupObject(mapOid)) == null)
				{
					Thread.onSpinWait();
				}
				firstObservedSize.set(((Map<?, ?>)o).size());
			}
			catch(final Throwable t)
			{
				readerFailure.set(t);
			}
		}, "concurrent-reader");
		reader.start();

		final Object reloaded = manager.getObject(mapOid);
		assertEquals(MAP_SIZE, ((Map<?, ?>)reloaded).size(), "loader thread itself must see the complete map");

		reader.join(TimeUnit.SECONDS.toMillis(30));
		assertFalse(reader.isAlive(), "reader thread must have observed the registered instance");
		if(readerFailure.get() != null)
		{
			fail("reader thread failed", readerFailure.get());
		}

		assertEquals(MAP_SIZE, firstObservedSize.get().intValue(),
			"concurrent reader must never observe a partially initialized instance");
	}


	///////////////////////////////////////////////////////////////////////////
	// test doubles //
	/////////////////

	/**
	 * Observes from within the loader's build phases whether the instance under
	 * construction is (wrongly) already visible in the global object registry.
	 * Same-thread registry lookups are safe here: the registry guards its tables
	 * with an internal mutex, not the build monitor.
	 */
	static final class ProbeHandler extends CustomBinaryHandler<Probe>
	{
		private final BinaryField<Probe> value = Field(String.class, Probe::getValue, Probe::setValue);

		private PersistenceObjectRegistry registry     ;
		private long                      probeObjectId;

		Boolean registeredDuringInitialize;
		Boolean registeredDuringComplete  ;

		ProbeHandler()
		{
			super(Probe.class);
		}

		void armObservation(final PersistenceObjectRegistry registry, final long probeObjectId)
		{
			this.registry      = registry     ;
			this.probeObjectId = probeObjectId;
		}

		private boolean isProbeRegistered()
		{
			return this.registry.lookupObject(this.probeObjectId) != null;
		}

		@Override
		protected Probe instantiate(final Binary data)
		{
			return new Probe(null);
		}

		@Override
		public void initializeState(final Binary data, final Probe instance, final PersistenceLoadHandler handler)
		{
			if(this.registry != null)
			{
				this.registeredDuringInitialize = this.isProbeRegistered();
			}
			super.initializeState(data, instance, handler);
		}

		@Override
		public void complete(final Binary data, final Probe instance, final PersistenceLoadHandler handler)
		{
			if(this.registry != null)
			{
				this.registeredDuringComplete = this.isProbeRegistered();
			}
			super.complete(data, instance, handler);
		}
	}


	///////////////////////////////////////////////////////////////////////////
	// data types //
	///////////////

	public static class Probe
	{
		String value;

		public Probe(final String value)
		{
			super();
			this.value = value;
		}

		public String getValue()
		{
			return this.value;
		}

		public void setValue(final String value)
		{
			this.value = value;
		}
	}
}
