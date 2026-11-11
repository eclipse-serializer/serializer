package org.eclipse.serializer.memory.sun;

/*-
 * #%L
 * Eclipse Serializer Base
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

import org.eclipse.serializer.memory.MemoryStatistics;
import org.eclipse.serializer.util.logging.Logging;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

/**
 * Memory statistics based on the platform management beans ({@code java.management}).
 * <p>
 * The heap usage after the last garbage collection is read from {@code com.sun.management.GarbageCollectorMXBean#getLastGcInfo()}
 * (module {@code jdk.management}), accessed reflectively so that there is no compile-time or module dependency on it.
 * A runtime image without {@code jdk.management} (e.g. built with jlink from the module dependencies only) must add it
 * ({@code --add-modules jdk.management}). Otherwise {@link MemoryStatistics#usedAfterLastGc()} falls back to the
 * instantaneous usage, {@link MemoryStatistics#gcCount()} is -1, and a warning is logged once.
 * <p>
 * Deliberately free of {@code sun.misc.Unsafe}, so that memory accessors not based on Unsafe can use it, too.
 * Must not be loaded on platforms without {@code java.management} (Android).
 */
public final class JdkMemoryStatistics
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	/*
	 * G1's bean for the Remark and Cleanup pauses of its concurrent cycle (JDK 20+). These pauses do not evacuate,
	 * so their "after GC" usage still contains eden, i.e. the garbage allocated since the last young collection.
	 */
	private static final String G1_CONCURRENT_COLLECTOR = "G1 Concurrent GC";

	/*
	 * Without any collector activity for this long, the post-GC value is no longer trusted: nothing has been reclaimed
	 * since, so the instantaneous usage is the better estimate. Concurrent collectors that let the instantaneous usage
	 * run up with garbage do so under allocation pressure, i.e. with cycles and pauses running all the time.
	 */
	private static final long NO_GC_ACTIVITY_LIMIT_MS = 2_000;

	private static final Logger logger = Logging.getLogger(JdkMemoryStatistics.class);



	///////////////////////////////////////////////////////////////////////////
	// static fields //
	//////////////////

	// beans and pools are fixed for the lifetime of the JVM, a racy double initialization is harmless.
	private static volatile Beans beans;

	// the post-GC usage only changes with a collection: re-read it only when the collection count changed.
	private static volatile PostGcSample lastSample;

	private static volatile boolean fallbackLogged;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static MemoryStatistics createHeapMemoryStatistics()
	{
		try
		{
			final Beans beans = beans();

			/*
			 * The post-GC sample first, the current usages after it: a collection in between then makes this check
			 * underestimate once, instead of counting the garbage it reclaimed as growth since the sample.
			 */
			final long         count  = gcCount(beans);
			final PostGcSample sample = count > 0 ? sample(count, beans) : null;

			final MemoryUsage   usage     = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
			final long          max       = usage.getMax();
			final long          committed = usage.getCommitted();
			final long          used      = usage.getUsed();
			final MemoryUsage[] oldUsages = currentUsages(beans.oldPools);
			final long          oldMax    = oldGenerationMax(oldUsages);
			final long          oldUsed   = oldMax < 0 ? -1 : oldGenerationUsedAfterLastGc(beans);

			if(sample == null)
			{
				// no collection yet (0) or no collection count at all (-1): the instantaneous usage is all there is
				return MemoryStatistics.New(max, committed, used, used, count, oldUsed, oldMax);
			}
			if(sample.usedAfterGc < 0)
			{
				logFallbackOnce("the garbage collectors provide no usage after collection", null);
				return MemoryStatistics.New(max, committed, used, used, -1, oldUsed, oldMax);
			}

			/*
			 * The post-GC value alone goes stale while the heap fills up without collections:
			 * - objects allocated directly into the old generation (e.g. G1 humongous objects) are retained
			 *   without any young collection, hence the growth of the old generation is added;
			 * - without any collector activity at all (e.g. Shenandoah while large arrays are retained),
			 *   the instantaneous usage is used.
			 * The snapshot plus the growth can exceed the current value (e.g. right after a collection freed more).
			 */
			final long usedAfterLastGc = isWithoutGcActivity(sample, beans)
				? used
				: Math.min(sample.usedAfterGc + oldGenerationGrowth(sample, oldUsages), used)
			;

			return MemoryStatistics.New(max, committed, used, usedAfterLastGc, count, oldUsed, oldMax);
		}
		catch(final Exception | LinkageError e)
		{
			// statistics must never fail: fall back to the instantaneous value, i.e. the behavior without post-GC data.
			logFallbackOnce("reading the usage after collection failed", e);
			final MemoryUsage usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
			return MemoryStatistics.New(usage.getMax(), usage.getCommitted(), usage.getUsed());
		}
	}

	private static MemoryUsage[] currentUsages(final List<MemoryPoolMXBean> pools)
	{
		final MemoryUsage[] usages = new MemoryUsage[pools.size()];
		for(int i = 0; i < usages.length; i++)
		{
			usages[i] = pools.get(i).getUsage();
		}

		return usages;
	}

	/**
	 * @return the summed maximum size of the old-generation pools, or -1 if there are none or one has no defined maximum.
	 */
	private static long oldGenerationMax(final MemoryUsage[] oldUsages)
	{
		if(oldUsages.length == 0)
		{
			return -1;
		}
		long sum = 0;
		for(final MemoryUsage usage : oldUsages)
		{
			final long max = usage.getMax();
			if(max <= 0)
			{
				return -1;
			}
			sum += max;
		}

		return sum;
	}

	/*
	 * The usage after the old generation's last collection (collection usage), not the current usage: Parallel and
	 * Serial collect the old generation only in a full collection, which runs when it is nearly full, so the current
	 * usage climbs to that level with promoted garbage regardless of the retained data. Before the first collection of
	 * the old generation the value is 0.
	 */
	private static long oldGenerationUsedAfterLastGc(final Beans beans)
	{
		long sum = 0;
		for(final MemoryPoolMXBean pool : beans.oldPools)
		{
			final MemoryUsage afterCollection = pool.getCollectionUsage();
			if(afterCollection == null)
			{
				return -1;
			}
			sum += afterCollection.getUsed();
		}

		return sum;
	}

	public static MemoryStatistics createNonHeapMemoryStatistics()
	{
		final MemoryUsage usage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();

		return MemoryStatistics.New(usage.getMax(), usage.getCommitted(), usage.getUsed());
	}

	/**
	 * @return the sum of the collection counts of all collectors, or -1 if no collector reports one.
	 */
	private static long gcCount(final Beans beans)
	{
		long    sum     = 0    ;
		boolean defined = false;
		for(final GarbageCollectorMXBean gc : beans.collectors)
		{
			final long count = gc.getCollectionCount();
			if(count >= 0)
			{
				sum    += count;
				defined = true ;
			}
		}

		return defined ? sum : -1;
	}

	// measured from the end of the latest collector activity, not from the query that noticed it (may be much later)
	private static boolean isWithoutGcActivity(final PostGcSample sample, final Beans beans)
	{
		return beans.runtime.getUptime() - sample.lastActivityEndMs >= NO_GC_ACTIVITY_LIMIT_MS;
	}

	private static PostGcSample sample(final long gcCount, final Beans beans) throws Exception
	{
		final PostGcSample sample = lastSample;
		if(sample != null && sample.gcCount == gcCount)
		{
			return sample;
		}

		final PostGcSample polled = poll(gcCount, beans);
		lastSample = polled;

		return polled;
	}

	/**
	 * The usage after the most recent collection of any polled collector.
	 * <p>
	 * Skipped: collections reporting an all-zero heap usage (the pause beans of ZGC and Shenandoah do that).
	 */
	private static PostGcSample poll(final long gcCount, final Beans beans) throws Exception
	{
		final LastGcInfoAccess access = beans.access;

		long                 latestEndTime     = Long.MIN_VALUE;
		long                 latestUsed        = -1            ;
		Map<?, ?>            latestUsage       = null          ;
		long                 lastActivityEndMs = Long.MIN_VALUE;
		for(final GarbageCollectorMXBean gc : beans.polledCollectors)
		{
			final Object gcInfo = access.getLastGcInfo.invoke(gc);
			if(gcInfo == null)
			{
				// no collection by this collector yet
				continue;
			}

			final long endTime = (Long)access.getEndTime.invoke(gcInfo);
			// activity counts also where the usage values are not used (pause beans reporting no usage)
			lastActivityEndMs = Math.max(lastActivityEndMs, endTime);

			final Map<?, ?> usageAfter = (Map<?, ?>)access.getMemoryUsageAfterGc.invoke(gcInfo);
			final long      heapUsed   = sumHeapUsage(usageAfter, beans.heapPoolNames);
			if(heapUsed > 0 && endTime > latestEndTime)
			{
				latestEndTime = endTime   ;
				latestUsed    = heapUsed  ;
				latestUsage   = usageAfter;
			}
		}
		for(final GarbageCollectorMXBean gc : beans.activityOnlyCollectors)
		{
			final Object gcInfo = access.getLastGcInfo.invoke(gc);
			if(gcInfo != null)
			{
				lastActivityEndMs = Math.max(lastActivityEndMs, (Long)access.getEndTime.invoke(gcInfo));
			}
		}

		final long[] oldPoolsAfterGc = new long[beans.oldPools.size()];
		for(int i = 0; i < oldPoolsAfterGc.length; i++)
		{
			final MemoryUsage poolUsage = latestUsage == null
				? null
				: (MemoryUsage)latestUsage.get(beans.oldPools.get(i).getName())
			;
			oldPoolsAfterGc[i] = poolUsage == null ? -1 : poolUsage.getUsed();
		}

		return new PostGcSample(gcCount, lastActivityEndMs, latestUsed, oldPoolsAfterGc);
	}

	private static long oldGenerationGrowth(final PostGcSample sample, final MemoryUsage[] oldUsages)
	{
		long growth = 0;
		for(int i = 0; i < sample.oldPoolsAfterGc.length; i++)
		{
			final long afterGc = sample.oldPoolsAfterGc[i];
			if(afterGc >= 0)
			{
				growth += Math.max(0, oldUsages[i].getUsed() - afterGc);
			}
		}

		return growth;
	}

	private static long sumHeapUsage(final Map<?, ?> usageByPool, final Set<String> heapPools)
	{
		long sum = 0;
		for(final Map.Entry<?, ?> entry : usageByPool.entrySet())
		{
			if(heapPools.contains(entry.getKey()))
			{
				sum += ((MemoryUsage)entry.getValue()).getUsed();
			}
		}

		return sum;
	}

	private static Beans beans()
	{
		Beans current = beans;
		if(current == null)
		{
			beans = current = Beans.New();
		}

		return current;
	}

	private static void logFallbackOnce(final String reason, final Throwable cause)
	{
		if(fallbackLogged)
		{
			return;
		}
		fallbackLogged = true;
		logger.warn(
			"Heap usage after garbage collection not available ({}). The Lazy reference memory check uses the"
			+ " instantaneous heap usage, which concurrent collectors (ZGC, Shenandoah) report too high, so lazy"
			+ " references may be cleared too often. If the runtime image lacks the module jdk.management, add it"
			+ " (--add-modules jdk.management).",
			reason,
			cause
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 *
	 * @throws UnsupportedOperationException when called
	 */
	private JdkMemoryStatistics()
	{
		// static only
		throw new UnsupportedOperationException();
	}



	static final class PostGcSample
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final long   gcCount          ;
		final long   lastActivityEndMs; // end of the latest collector activity, ms since the JVM start
		final long   usedAfterGc      ; // -1 if not available
		final long[] oldPoolsAfterGc  ; // per Beans#oldPools, -1 if not available



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		PostGcSample(
			final long   gcCount          ,
			final long   lastActivityEndMs,
			final long   usedAfterGc      ,
			final long[] oldPoolsAfterGc
		)
		{
			super();
			this.gcCount           = gcCount          ;
			this.lastActivityEndMs = lastActivityEndMs;
			this.usedAfterGc       = usedAfterGc      ;
			this.oldPoolsAfterGc   = oldPoolsAfterGc  ;
		}
	}

	/**
	 * The management beans, fixed for the lifetime of the JVM.
	 */
	static final class Beans
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final List<GarbageCollectorMXBean> collectors            ; // all, for the collection count
		final List<GarbageCollectorMXBean> polledCollectors      ; // providing LastGcInfo, without G1 Concurrent GC
		final List<GarbageCollectorMXBean> activityOnlyCollectors; // providing LastGcInfo, usage not used (G1 Concurrent GC)
		final Set<String>                  heapPoolNames         ;
		final List<MemoryPoolMXBean>       oldPools              ; // heap pools only filled by collections or direct allocation
		final RuntimeMXBean                runtime               ; // uptime, the time base of LastGcInfo
		final LastGcInfoAccess             access                ; // null if jdk.management is not available



		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		static Beans New()
		{
			final LastGcInfoAccess             access     = LastGcInfoAccess.New();
			final List<GarbageCollectorMXBean> collectors = List.copyOf(ManagementFactory.getGarbageCollectorMXBeans());

			final List<GarbageCollectorMXBean> polled       = new ArrayList<>();
			final List<GarbageCollectorMXBean> activityOnly = new ArrayList<>();
			if(access != null)
			{
				for(final GarbageCollectorMXBean gc : collectors)
				{
					if(!access.gcBeanType.isInstance(gc))
					{
						continue;
					}
					if(G1_CONCURRENT_COLLECTOR.equals(gc.getName()))
					{
						activityOnly.add(gc);
					}
					else
					{
						polled.add(gc);
					}
				}
			}

			final Set<String>            heapPoolNames = new HashSet<>();
			final List<MemoryPoolMXBean> oldPools      = new ArrayList<>();
			for(final MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans())
			{
				if(pool.getType() != MemoryType.HEAP)
				{
					continue;
				}
				heapPoolNames.add(pool.getName());
				if(isOldGenerationPool(pool.getName()))
				{
					oldPools.add(pool);
				}
			}

			return new Beans(
				collectors,
				List.copyOf(polled),
				List.copyOf(activityOnly),
				Set.copyOf(heapPoolNames),
				List.copyOf(oldPools),
				ManagementFactory.getRuntimeMXBean(),
				access
			);
		}

		/*
		 * The tenured pools of the generational HotSpot collectors ("G1 Old Gen", "PS Old Gen", "Tenured Gen",
		 * "ZGC Old Generation", "Shenandoah Old Gen"). Single-pool collectors (non-generational ZGC, Shenandoah)
		 * have none; they collect concurrently based on the usage anyway.
		 */
		private static boolean isOldGenerationPool(final String poolName)
		{
			return poolName.contains("Old") || poolName.contains("Tenured");
		}



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Beans(
			final List<GarbageCollectorMXBean> collectors            ,
			final List<GarbageCollectorMXBean> polledCollectors      ,
			final List<GarbageCollectorMXBean> activityOnlyCollectors,
			final Set<String>                  heapPoolNames         ,
			final List<MemoryPoolMXBean>       oldPools              ,
			final RuntimeMXBean                runtime               ,
			final LastGcInfoAccess             access
		)
		{
			super();
			this.collectors             = collectors            ;
			this.polledCollectors       = polledCollectors      ;
			this.activityOnlyCollectors = activityOnlyCollectors;
			this.heapPoolNames          = heapPoolNames         ;
			this.oldPools               = oldPools              ;
			this.runtime                = runtime               ;
			this.access                 = access                ;
		}
	}

	/**
	 * Reflective handles to {@code com.sun.management.GarbageCollectorMXBean#getLastGcInfo()} and {@code GcInfo}.
	 * Read directly instead of via the platform MBeanServer: no conversion to open types and no creation of the
	 * MBeanServer as a side effect.
	 */
	static final class LastGcInfoAccess
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final Class<?> gcBeanType           ;
		final Method   getLastGcInfo        ;
		final Method   getEndTime           ;
		final Method   getMemoryUsageAfterGc;



		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		/**
		 * @return the handles, or {@code null} if {@code jdk.management} is not available.
		 */
		static LastGcInfoAccess New()
		{
			try
			{
				final Class<?> gcBeanType = Class.forName("com.sun.management.GarbageCollectorMXBean");
				final Class<?> gcInfoType = Class.forName("com.sun.management.GcInfo");

				return new LastGcInfoAccess(
					gcBeanType,
					gcBeanType.getMethod("getLastGcInfo"),
					gcInfoType.getMethod("getEndTime"),
					gcInfoType.getMethod("getMemoryUsageAfterGc")
				);
			}
			catch(final Exception | LinkageError e)
			{
				logFallbackOnce("module jdk.management not available", e);
				return null;
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		LastGcInfoAccess(
			final Class<?> gcBeanType           ,
			final Method   getLastGcInfo        ,
			final Method   getEndTime           ,
			final Method   getMemoryUsageAfterGc
		)
		{
			super();
			this.gcBeanType            = gcBeanType           ;
			this.getLastGcInfo         = getLastGcInfo        ;
			this.getEndTime            = getEndTime           ;
			this.getMemoryUsageAfterGc = getMemoryUsageAfterGc;
		}
	}

}
