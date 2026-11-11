package org.eclipse.serializer.reference;

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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.serializer.memory.MemoryStatistics;
import org.eclipse.serializer.monitoring.LazyReferenceManagerMonitor;
import org.eclipse.serializer.monitoring.MonitoringManager;
import org.eclipse.serializer.time.XTime;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public interface LazyReferenceManager
{
	public void register(Lazy<?> lazyReference);
	
	public LazyReferenceManager registerAll(LazyReferenceManager other);

	public void cleanUp(long nanoTimeBudget);

	public default void cleanUp()
	{
		this.cleanUp(Long.MAX_VALUE);
	}
	
	public void cleanUp(long nanoTimeBudget, Lazy.Checker checker);

	public default void cleanUp(final Lazy.Checker checker)
	{
		this.cleanUp(Long.MAX_VALUE, checker);
	}

	/**
	 * Clears all registered lazy references that were stored and are not marked as used.
	 * Unstored and used-marked ones keep their subject, as clearing them would lose data.
	 */
	public void clear();

	public LazyReferenceManager start();

	public LazyReferenceManager stop();
	
	public LazyReferenceManager addController(LazyReferenceManager.Controller controller);
	
	public boolean removeController(LazyReferenceManager.Controller controller);
	
	public boolean isRunning();

	public <P extends Consumer<? super Lazy<?>>> P iterate(P iterator);

	public <P extends Consumer<? super LazyReferenceManager.Controller>> P iterateControllers(P iterator);

	

	/**
	 * Replaces the global manager. The new manager takes over all registered references and controllers, and later
	 * registrations at the previous manager are forwarded to it.
	 * If the previous manager is running, the new one is started and the previous one is stopped.
	 *
	 * @param referenceManager the new global manager
	 * @return the previous global manager
	 */
	public static LazyReferenceManager set(final LazyReferenceManager referenceManager)
	{
		return Static.set(referenceManager);
	}

	public static LazyReferenceManager get()
	{
		return Static.get();
	}


	public final class Static
	{
		/*
		 * Read without a lock: every Lazy creation reads it, while set() may wait for controllers, e.g. a storage
		 * that holds its state lock during its whole start.
		 */
		static volatile LazyReferenceManager globalReferenceManager = LazyReferenceManager.New();

		// serializes replacements, so that the handover of one is complete before the next one starts
		static synchronized LazyReferenceManager set(final LazyReferenceManager referenceManager)
		{
			final LazyReferenceManager old = globalReferenceManager;
			if(referenceManager == old)
			{
				// checked before the forwarding below, which would otherwise point the manager to itself
				throw new IllegalArgumentException(
					"The " + LazyReferenceManager.class.getSimpleName() + " is already the global one."
				);
			}

			// a manager that becomes global again must register itself instead of forwarding (else a forwarding cycle)
			if(referenceManager instanceof Default)
			{
				((Default)referenceManager).forwardRegistrationsTo(null);
			}
			/*
			 * Lazy instances may have got the old manager just before the swap. Forwarding is set up before the takeover:
			 * every registration at the old manager is either taken over or forwarded, none stays with it alone.
			 */
			if(old instanceof Default)
			{
				((Default)old).forwardRegistrationsTo(referenceManager);
			}
			try
			{
				referenceManager.registerAll(old);
			}
			catch(final Throwable t)
			{
				// the old manager stays global: it must not forward to the manager that failed to take over
				if(old instanceof Default)
				{
					((Default)old).forwardRegistrationsTo(null);
				}
				throw t;
			}
			globalReferenceManager = referenceManager;

			// storages start the manager only on their own start: without the handover, clearing would stop silently.
			final boolean oldWasRunning = old.isRunning();

			/*
			 * The old manager is retired in any case: while its controllers disallow running, its thread may still sleep
			 * and hold the JMX bean. Stop first: the bean name is shared and only free after it has been unregistered.
			 */
			old.stop();
			if(oldWasRunning)
			{
				referenceManager.start();
			}
			if(referenceManager instanceof Default)
			{
				// a replacement started before set() could not register its JMX bean while the old one held the name
				((Default)referenceManager).ensureMonitorRegistered();
			}

			return old;
		}

		static LazyReferenceManager get()
		{
			return globalReferenceManager;
		}

		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		 * Dummy constructor to prevent instantiation of this static-only utility class.
		 * 
		 * @throws UnsupportedOperationException when called
		 */
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}

	}

	public final class Clearer implements Lazy.Checker
	{
		@Override
		public boolean check(final Lazy<?> lazyReference)
		{
			/*
			 * Only stored references that are not marked as used: an unstored one cannot be reloaded, and a used mark
			 * pins changes that are not stored yet (e.g. GigaMap segments). Clearing either would lose data.
			 */
			lazyReference.clear(ref -> true);
			return true;
		}
	}

	
	
	public static LazyReferenceManager New()
	{
		return New(Lazy.Checker());
	}

	public static LazyReferenceManager New(final long millisecondTimeout)
	{
		return New(Lazy.Checker(millisecondTimeout));
	}
	
	public static LazyReferenceManager New(final Lazy.Check customCheck)
	{
		return New(Lazy.Checker(customCheck));
	}

	public static LazyReferenceManager New(final Lazy.Checker checker)
	{
		return New(checker, Default.DEFAULT_CHECK_INTERVAL_MS, Default.DEFAULT_TIME_BUDGET_NS);
	}

	public static LazyReferenceManager New(
		final Lazy.Checker checker               ,
		final long         milliTimeCheckInterval,
		final long         nanoTimeBudget
	)
	{
		return New(
			checker,
			_longReference.New(milliTimeCheckInterval),
			_longReference.New(nanoTimeBudget)        ,
			MonitoringManager.PlatformDependent()
		);
	}
	
	public static LazyReferenceManager New(
		final Lazy.Checker      checker                       ,
		final _longReference    milliTimeCheckIntervalProvider,
		final _longReference    nanoTimeBudgetProvider        ,
		final MonitoringManager monitoringManager
	)
	{
		return new Default(
			checker,
			milliTimeCheckIntervalProvider,
			nanoTimeBudgetProvider,
			monitoringManager
		);
	}

	public final class Default implements LazyReferenceManager
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		final static Logger logger = Logging.getLogger(Default.class);
		
		private static final Clearer CLEARER = new Clearer();

		// defaults mean to check every second with a budget of 1 MS (0.1% thread activity)
		        static final long    DEFAULT_CHECK_INTERVAL_MS = 1_000        ;
		        static final long    DEFAULT_TIME_BUDGET_NS    =     1_000_000;



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Lazy.Checker checker                            ;
		private final    _longReference millitimeCheckIntervalProvider;
		private final    _longReference nanoTimeBudgetProvider        ;
		private final    Entry          head   = new Entry(null)      ;
		private          Entry          tail   = this.head            ;
		private          Entry          cursor = this.head            ; // current "last" entry for checking
		
		private boolean              running        ;
		private Thread               cleanupThread  ;
		private LazyReferenceManager successor      ; // set when replaced as the global manager, see register()
		private ControllerEntry      headController ;
		private long                 controllerCount;
		
		private final MonitoringManager           monitorManager;
		private final LazyReferenceManagerMonitor monitor       ;

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Lazy.Checker checker                 ,
			final _longReference checkIntervalProvider ,
			final _longReference nanoTimeBudgetProvider,
			final MonitoringManager monitorManager
		)
		{
			super();
			this.checker                        = checker               ;
			this.millitimeCheckIntervalProvider = checkIntervalProvider ;
			this.nanoTimeBudgetProvider         = nanoTimeBudgetProvider;
			this.monitorManager                 = monitorManager        ;
			
			this.monitor = new LazyReferenceManagerMonitor(this);
		}
		
		private synchronized boolean mayRun()
		{
			if(this.headController == null)
			{
				// if no external controller is or was present, the LRM controls itself on its own.
				return this.controllerCount == 0;
			}
			
			// check for orphaned head controller and consolidate to next non-orphaned one (or null!)
			final LazyReferenceManager.Controller ac;
			if((ac = this.headController.get()) == null)
			{
				this.headController = this.headController.consolidateSelf();
				
				// mus call recursively in case null is returned or GC cleared a weak reference in the mean time.
				return this.mayRun();
			}
			
			return ac.mayRun()
				? true
				: this.headController.checkChain()
			;
		}
		
		// NOT threadsafe! Must be secured by accessing outer LRM methods
		static final class ControllerEntry extends WeakReference<LazyReferenceManager.Controller>
		{
			ControllerEntry next;
			
			ControllerEntry(final LazyReferenceManager.Controller controller)
			{
				super(controller);
			}
			
			final boolean checkChain()
			{
				return this.next != null && this.next.isEnabled(this);
			}

			final boolean isEnabled(final ControllerEntry last)
			{
				final LazyReferenceManager.Controller controller;
				if((controller = this.get()) == null)
				{
					// if this chain entry is an orphan AND the last one, the default value is returned
					if((last.next = this.next) == null)
					{
						return false;
					}
					
					// if it is not the last one, the next entry is asked/checked
					return this.next.isEnabled(last);
				}
				
				// if at least one existing controllers returns true, then true it is.
				if(controller.mayRun())
				{
					return true;
				}

				// check next controller if present. Otherwise, this controler's false counts
				return this.next != null && this.next.isEnabled(this);
			}
			
			static final ControllerEntry consolidate(final ControllerEntry root)
			{
				ControllerEntry current = root;
				while(current != null && current.get() == null)
				{
					current = current.next;
				}
				if(current != null)
				{
					current.consolidateTail();
				}
				
				return current;
			}
			
			final ControllerEntry consolidateSelf()
			{
				return consolidate(this);
			}
			
			final ControllerEntry consolidateTail()
			{
				this.next = consolidate(this.next);
				
				return this;
			}
			
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		

		final void internalCleanUp(final Entry start, final long nanoTimeBudget, final Lazy.Checker checker)
		{
			/* (22.06.2016 TM)NOTE: full clear does not clear fully
			 * productive use of the full clear call clears only like 6 of 300 references on a regular basis.
			 * Sometimes all, but most of the time not.
			 * Even though the application is single threaded as far as lazy reference creation is concerned
			 * (simple "main test" class execution)
			 * 
			 * (06.02.2020 TM)NOTE: since then, the LRM has been massively overhauled.
			 * However, the basic logic of entry iteration and the "Clearer" checker remained unchanged.
			 * So the age old note might still be relevant.
			 */

			final Entry currentTail;

			/*
			 * This local synchronized block is crucial to prevent deadlocks!
			 * The reference manager thread may never keep a lock on the manager instance and then require
			 * a lock on a lazy reference in order to complete its cleanup cycle.
			 * Consider the following scenario:
			 * - application thread locks lazy instance #1 to load its content.
			 * - manager thread locks the manager instance for the whole check cycle, starts checking.
			 * - loading of the LI#1 content causes LI#2 to be created and registered at the manager
			 * - manager thread wants to check LI#1 for timeout.
			 * - So app.thread holds LI#1 lock, requires mgr.lock, mgr.thread holds mgr.lock, requires LI#1 lock
			 * => deadlock (happened in productive use, although very rare)
			 * The solution to prevent this is:
			 * The lock on the manager instance is only held for a very short time without requiring any additional lock
			 * internally to leave the lock again, only to consistently query the current tail entry.
			 * The rest of the algorithm does not interfere with any other thread, so it can be done without lock.
			 *
			 * Of course, it is important that no other method calling this method keeps the mgr.lock for the whole
			 * check cycle, otherwise the deadlock can still occur (i.e. no synchronized method!).
			 * Happened in productive use after fixing this method :(.
			 */
			synchronized(this)
			{
				currentTail = this.tail;
			}

			Entry last, e = (last = start).nextLazyManagerEntry;

			// special case check initially, there is only the head with no next entry
			if(e == null)
			{
				return;
			}
		
			checker.beginCheckCycle();

			// the budget is for checking references: the checker's cycle preparation (memory statistics) must not consume it
			final long timeBudgetBound = XTime.calculateNanoTimeBudgetBound(nanoTimeBudget);

			cleanUp:
			do // do at least one check, no matter what
			{
				// keep strong reference to avoid intermediate garbage collection
				final Lazy<?> ref = e.get();

				// check for orphan entry
				if(ref != null)
				{
					// leave checking logic completely to checker (also for lock atomicity reasons)
					checker.check(ref);
				}
				else if(e != currentTail)
				{
					// remove orphan entry (never remove current tail entry for list consistency reasons)
					e = last.nextLazyManagerEntry = e.nextLazyManagerEntry;
					continue;
				}

				if(e == currentTail)
				{
					/*
					 * if the iteration reached the current tail entry, the cursor gets reset (outside the loop)
					 * and the iteration gets aborted. Rationale behind that:
					 * It might be that there have been added new entry to the chain while the iteration was
					 * executed. However, these entries are newly created, hence will hardly timeout right away.
					 * The oldest entries near the head are much more likely for that, so it is efficient to restart.
					 */
					last = this.head;
					break cleanUp;
				}

				e = (last = e).nextLazyManagerEntry;
			}
			while(System.nanoTime() < timeBudgetBound);

			// remember last checked entry for next cleanup run. Cursor field is strictly only used by one thread.
			this.cursor = last;

			checker.endCheckCycle();
			
			this.updateStatistics();
		}
		
		private void updateStatistics()
		{
			/*
			 * This method is intentionally not synchronized.
			 * locking the LazyReferenceManager instance here may
			 * cause deadlocks. See comments in internalCleanUp above.
			 */
			
			int lazyReferences       = 0;
			int loadedLazyReferences = 0;
			
			for(Entry e = this.head; (e = e.nextLazyManagerEntry) != null;)
			{
				lazyReferences++;
				final Lazy<?> ref = e.get();
				if(ref != null && ref.isLoaded())
				{
					loadedLazyReferences++;
				}
			}
			
			this.monitor.update(lazyReferences, loadedLazyReferences);
		}

		final void cleanUpBudgeted()
		{
			// this method may NOT (and needs not to) be synchronized (see comment in internalCleanUp).

			// perform actual cleanup for the dynamically specified nano time budget
			this.cleanUp(this.nanoTimeBudgetProvider.get());
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void register(final Lazy<?> lazyReference)
		{
			final LazyReferenceManager successor;
			synchronized(this)
			{
				if((successor = this.successor) == null)
				{
					// uniqueness of references is guaranteed by calling this method only exactely once per reference instance
					this.tail = this.tail.nextLazyManagerEntry = new Entry(lazyReference);
					return;
				}
			}

			// outside this lock: no manager holds its own lock while taking another manager's lock (no lock order to keep)
			successor.register(lazyReference);
		}

		/**
		 * Forwards all later registrations to the passed manager, the global manager that replaced this one, or ends
		 * forwarding for {@code null}.
		 */
		final synchronized void forwardRegistrationsTo(final LazyReferenceManager successor)
		{
			this.successor = successor;
		}

		final synchronized void ensureMonitorRegistered()
		{
			if(this.running)
			{
				// a failed registration is not recorded, so register anew
				this.monitorManager.shutdown();
				this.monitorManager.registerMonitor(this.monitor);
			}
		}
		

		@Override
		public LazyReferenceManager registerAll(final LazyReferenceManager other)
		{
			if(other == this)
			{
				throw new IllegalArgumentException(
					"Other " + LazyReferenceManager.class.getSimpleName() + " may not be this."
				);
			}

			/*
			 * A snapshot first: if this manager forwards to the other one, registering while iterating it would never end.
			 * Not synchronized: this manager's lock is never held while the other one's is taken, in neither order.
			 */
			final List<Lazy<?>> references = new ArrayList<>();
			other.iterate(references::add);
			for(final Lazy<?> reference : references)
			{
				this.register(reference);
			}
			
			other.iterateControllers(ac ->
				this.addController(ac)
			);
			
			return this;
		}

		@Override
		public void clear()
		{
			// from the head: the cursor of a budgeted cycle may stand anywhere in the list
			this.internalCleanUp(this.head, Long.MAX_VALUE, CLEARER);
		}

		@Override
		public void cleanUp(final long nanoTimeBudget)
		{
			this.internalCleanUp(this.cursor, nanoTimeBudget, this.checker);
		}
		
		@Override
		public void cleanUp(final long nanoTimeBudget, final Lazy.Checker checker)
		{
			this.internalCleanUp(this.cursor, nanoTimeBudget, checker);
		}
		
		@Override
		public final synchronized boolean isRunning()
		{
			return this.running && this.mayRun();
		}

		@Override
		public synchronized LazyReferenceManager start()
		{
			// check for already running condition to avoid starting more than more thread
			if(!this.running && this.mayRun())
			{
				/*
				 * Running only once the thread is started: a failing start (e.g. "unable to create native thread") must
				 * not leave the manager marked as running without a thread, which no start() would ever repair.
				 * The thread waits for this monitor in its loop condition, so the order is safe.
				 */
				final Thread thread = new LazyReferenceCleanupThread(new WeakReference<>(this), this.millitimeCheckIntervalProvider);
				thread.start();
				this.running       = true  ;
				this.cleanupThread = thread;

				// registered only while running: the bean name is shared by all managers
				this.monitorManager.registerMonitor(this.monitor);
			}
			
			return this;
		}

		@Override
		public synchronized LazyReferenceManager stop()
		{
			// detach and wake up the thread, so that a start() right after this creates the only running thread
			final Thread thread = this.detachCleanupThread();
			if(thread != null)
			{
				thread.interrupt();
			}

			return this;
		}

		private Thread detachCleanupThread()
		{
			final Thread thread = this.cleanupThread;
			this.running       = false;
			this.cleanupThread = null ;
			this.monitorManager.shutdown();

			return thread;
		}

		/**
		 * The loop condition of the cleanup thread: a thread detached by {@link #stop()} must end, even if the
		 * manager has been started again in the meantime. A thread ending because no controller allows running
		 * resets the running state in the same lock, so that a following {@link #start()} creates a new thread.
		 */
		final synchronized boolean mayContinue(final Thread thread)
		{
			if(this.cleanupThread != thread)
			{
				return false;
			}
			if(this.isRunning())
			{
				return true;
			}
			this.cleanupThreadTerminated(thread);
			return false;
		}

		final synchronized void cleanupThreadTerminated(final Thread thread)
		{
			// only for the current thread: a detached older thread must not reset the state of a newer one
			if(this.cleanupThread == thread)
			{
				this.detachCleanupThread();
			}
		}
		
		@Override
		public final synchronized LazyReferenceManager addController(
			final LazyReferenceManager.Controller controller
		)
		{
			if(controller == null)
			{
				return this;
			}
			
			// either set as head instance or scroll to the end and add as tail instance.
			if(this.headController == null)
			{
				this.headController = new ControllerEntry(controller);
				this.controllerCount++;
				
				return this;
			}
			
			ControllerEntry current = this.headController;
			while(current.next != null)
			{
				// no need for orphan removal logic here as the checking logic already does that on every check
				if(current.get() == controller)
				{
					return this;
				}
				current = current.next;
			}
			current.next = new ControllerEntry(controller);
			this.controllerCount++;
			
			return this;
		}

		@Override
		public final synchronized boolean removeController(
			final LazyReferenceManager.Controller controller
		)
		{
			// head entry special case
			if(this.headController != null && this.headController.get() == controller)
			{
				// adding logic ensures there can be at the most one entry, so one match suffices to end the loop.
				this.headController = this.headController.next;
				this.controllerCount--;
				this.stopIfNoControllers();
				return true;
			}
			
			if(this.headController == null)
			{
				// no (more) controllers present, hence passed controller not found.
				return false;
			}
			
			// normal case loop starting with a non-null, non-matching head entry
			ControllerEntry last = this.headController;
			for(ControllerEntry e; (e = last.next) != null; last = e)
			{
				// no need for orphan removal logic here as the checking logic already does that on every check
				if(e.get() == controller)
				{
					// remove chain element by replacing the reference to it by that to its next.
					last.next = e.next;
					this.controllerCount--;
					
					// adding logic ensures there can be at the most one entry, so one match suffices to end the loop.
					this.stopIfNoControllers();
					return true;
				}
			}
			
			// passed controller not found
			return false;
		}

		/**
		 * When there are no controllers attached to this object, stop the LazyReferenceManager so that no
		 * daemon threads running anymore and program can exit normally.
		 */
		private void stopIfNoControllers()
		{
			if (this.controllerCount == 0)
			{
				this.stop();
			}
		}

		@Override
		public <P extends Consumer<? super LazyReferenceManager.Controller>> P iterateControllers(
			final P iterator
		)
		{
			for(ControllerEntry acc = this.headController; acc != null; acc = acc.next)
			{
				final LazyReferenceManager.Controller ac;
				if((ac = acc.get()) != null)
				{
					iterator.accept(ac);
				}
			}
			return iterator;
		}

		@Override
		public synchronized <P extends Consumer<? super Lazy<?>>> P iterate(final P iterator)
		{
			for(Entry e = this.head; (e = e.nextLazyManagerEntry) != null;)
			{
				final Lazy<?> ref = e.get();
				if(ref != null)
				{
					iterator.accept(ref);
				}
			}
			return iterator;
		}


		static final class LazyReferenceCleanupThread extends Thread
		{
			// a shorter (or non-positive) interval would let the thread spin
			private static final long MINIMUM_CHECK_INTERVAL_MS = 1;

			// lazy reference for automatic thread termination
			private final WeakReference<LazyReferenceManager.Default> parent               ;
			private final _longReference                              checkIntervalProvider;

			// failure reporting, only accessed by this thread
			private final FailureReport cycleFailures = new FailureReport(
				"LazyReferenceManager cleanup cycle failed, no lazy references are cleared while this persists"
			);
			private final FailureReport intervalFailures = new FailureReport(
				"LazyReferenceManager check interval could not be determined, using the default interval"
			);

			LazyReferenceCleanupThread(
				final WeakReference<LazyReferenceManager.Default> parent,
				final _longReference checkIntervalProvider
			)
			{
				super(LazyReferenceManager.class.getSimpleName() + '@' + System.identityHashCode(parent));
				this.parent                = parent               ;
				this.checkIntervalProvider = checkIntervalProvider;
			}

			private static boolean isUnrecoverable(final Throwable t)
			{
				/*
				 * An OutOfMemoryError is exactly the situation clearing lazy references is meant to relieve, so the
				 * thread must keep going. Only errors indicating a broken VM end it.
				 */
				return t instanceof InternalError || t instanceof UnknownError;
			}

			private void sleepUntilNextCycle()
			{
				long intervalMs;
				try
				{
					intervalMs = this.checkIntervalProvider.get();
				}
				catch(final Throwable t)
				{
					// like a failing cycle: only errors indicating a broken VM end the thread
					if(isUnrecoverable(t))
					{
						throw t;
					}
					this.intervalFailures.report(t);
					intervalMs = DEFAULT_CHECK_INTERVAL_MS;
				}

				try
				{
					// a negative interval would make Thread.sleep throw and kill this thread, zero would make it spin
					Thread.sleep(Math.max(intervalMs, MINIMUM_CHECK_INTERVAL_MS));
				}
				catch(final InterruptedException e)
				{
					// sleep interrupted (e.g. by stop()), proceed with the loop condition immediately
				}
			}


			@Override
			public void run()
			{
				logger.debug("LazyReferenceManager started");

				boolean terminatedRegularly = false;
				try
				{
					LazyReferenceManager.Default parent;
					while((parent = this.parent.get()) != null)
					{
						try
						{
							// check for running state. Must be the first action in case of swallowed exception.
							// A thread detached by stop() ends, even if the manager has been started again.
							if(!parent.mayContinue(this))
							{
								break;
							}

							// perform check
							parent.cleanUpBudgeted();
							this.cycleFailures.recovered();
						}
						catch(final Throwable t)
						{
							if(isUnrecoverable(t))
							{
								// let the thread die, see finally
								throw t;
							}
							/*
							 * Thread may not die on any other exception or error, just continue looping
							 * as long as parent exists and running is true.
							 * The sleep below must happen in this case, too, otherwise a persistent failure
							 * makes this thread spin without any pause.
							 */
							this.cycleFailures.report(t);
						}

						// very nasty: must clear the reference from the stack in order for the WeakReference to work
						parent = null;

						// extra nasty: must sleep with nulled reference for WeakReference to work, not before.
						this.sleepUntilNextCycle();
					}
					terminatedRegularly = true;
				}
				finally
				{
					// let a later start() create a new thread instead of believing this one is still running
					final LazyReferenceManager.Default parent = this.parent.get();
					if(parent != null)
					{
						parent.cleanupThreadTerminated(this);
					}
					if(!terminatedRegularly)
					{
						logger.error("LazyReferenceManager thread terminated abnormally, no lazy references are cleared until start() is called again.");
					}
				}

				// either parent has been garbage collected or stopped, so terminate.
				logger.debug("LazyReferenceManager stopped");
			}
		}

		/**
		 * Rate-limited reporting of a recurring failure: at most one warning (with stack trace) and one recovery
		 * message per {@link #LOG_INTERVAL_MS}, also when failures and successes alternate.
		 * Must not fail itself: it is used while handling errors such as an OutOfMemoryError.
		 */
		static final class FailureReport
		{
			private static final long LOG_INTERVAL_MS = 60_000;

			private final String message;

			private long failures            ; // since the last recovery
			private long failuresSinceLastLog;
			// 0 (epoch) means "never": the first report is logged right away
			private long lastFailureLogMs    ;
			private long lastRecoveryLogMs   ;

			FailureReport(final String message)
			{
				super();
				this.message = message;
			}

			final void report(final Throwable t)
			{
				try
				{
					this.failures++;
					this.failuresSinceLastLog++;

					final long now = System.currentTimeMillis();
					if(now - this.lastFailureLogMs < LOG_INTERVAL_MS)
					{
						return;
					}
					logger.warn("{} ({} failure(s) since the last report).", this.message, this.failuresSinceLastLog, t);
					this.lastFailureLogMs     = now;
					this.failuresSinceLastLog = 0  ;
				}
				catch(final Throwable reportingFailure)
				{
					// e.g. no memory left to log: the failure is counted, logging is retried next time
				}
			}

			final void recovered()
			{
				if(this.failures == 0)
				{
					return;
				}
				try
				{
					final long now = System.currentTimeMillis();
					if(now - this.lastRecoveryLogMs >= LOG_INTERVAL_MS)
					{
						logger.info("{}: recovered after {} failure(s).", this.message, this.failures);
						this.lastRecoveryLogMs = now;
					}
					this.failures = 0;
				}
				catch(final Throwable reportingFailure)
				{
					// logging is retried at the next recovery
				}
			}
		}


		static final class Entry extends WeakReference<Lazy<?>>
		{
			Entry nextLazyManagerEntry; // explicit naming to avoid ambiguity with WeakReference's field

			public Entry(final Lazy<?> referent)
			{
				super(referent);
			}

		}

	}

	
	@FunctionalInterface
	public interface Controller
	{
		public boolean mayRun();
	}
	
	
	@FunctionalInterface
	public interface CycleEvaluator
	{
		public void evaluateCycle(MemoryStatistics memoryStatistics, long cycleClearCount, double memoryQuota);
				
	}

}
