package org.eclipse.serializer.monitoring;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 - 2024 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.eclipse.serializer.util.VMInfo;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

/**
 * Helper for managing metrics and monitoring
 * <p>
 * A note on JMX beans registration name:
 * <p>
 * If there is more then one storage started in one vm
 * an instance counter is used to generate a unique bean registration
 * names for each MonitoringManager.
 * If a custom name is configured the counter is not applied and the
 * application is responsible to ensure unique names for each storage
 * started.
 * 
 */
public interface MonitoringManager
{
	/**
	 * Register a MetricMonitor object to the MonitoringManager.
	 * 
	 * @param metrics MetricMonitor implementation.
	 */
	public void registerMonitor(final MetricMonitor metrics);

	/**
	 * Shut down this MonitoringManager instance an clean up.
	 */
	void shutdown();

	
	/**
	 * Provide a platform dependent MonitoringManager.
	 * This is either the "JMX" implementation or the "Disabled" implementation
	 * on android systems.
	 * 
	 * @param name a user defined name used to distinguish different instances.
	 * 		  if null the Manager chooses a name internaly.
	 * 
	 * @return a platform dependent MonitoringManage instance
	 */
	public static MonitoringManager PlatformDependent(final String name)
	{
		if(VMInfo.New().isAnyAndroid())
		{
			return Disabled();
		}
		
		if(name != null)
		{
			return JMX(name);
		}
		
		return JMX();
	}
		
	/**
	 * Provides a new instance of the default JMX MonitoringManager implementation.
	 * 
	 * @return a MonitoringManager.JMX instance
	 */
	public static MonitoringManager JMX()
	{
		return new JMX();
	}

	/**
	 * Provides a new instance of the default JMX MonitoringManager implementation.
	 * 
	 * @param name a user defined name used to distinguish different instances.
	 * 
	 * @return a MonitoringManager.JMX instance
	 */
	public static MonitoringManager JMX(final String name)
	{
		return new JMX(name);
	}

	/**
	 * Provides a new instance of the disabled monitor manager implementation
	 * 
	 * @return a MonitoringManager.Disabled instance
	 */
	public static MonitoringManager Disabled()
	{
		return new MonitoringManager.Disabled();
	}

	/**
	 * MonitoringManager implementation using the java management extension to provide
	 * metrics and monitoring data.
	 * 
	 * Please be aware that not all Java implementations may provide JMX support
	 * e.g. Android.
	 * 
	 */
	public class JMX implements MonitoringManager
	{
		private final static Logger logger = Logging.getLogger(MonitoringManager.class);
		
		private static AtomicInteger instanceID = new AtomicInteger();
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
			
		private final List<ObjectInstance> beans = new ArrayList<>();
		private final String storageName;

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public JMX()
		{
			super();
			this.storageName = "storage" + instanceID.getAndIncrement();
			
			logger.debug("create MonitoringManager for storage: " + this.storageName);
		}
		
		public JMX(final String storageName)
		{
			super();
			this.storageName = storageName;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		/**
		 * {@inheritDoc}
		 * The provided MetricMonitor object must be a valid JMX Bean.
		 */
		@Override
		public void registerMonitor(final MetricMonitor metric)
		{
			try
			{
				final ObjectInstance bean = ManagementFactory
					.getPlatformMBeanServer()
					.registerMBean(metric, this.createObjectName(metric));
				
				this.beans.add(bean);
				logger.debug("Registered JMX bean {}", bean.getObjectName());
				
			}
			catch (InstanceAlreadyExistsException
				| MBeanRegistrationException
				| NotCompliantMBeanException
				| MalformedObjectNameException e
			)
			{
				logger.warn("Failed to register JMX Bean", e);
			}
		}
		
		@Override
		public void shutdown()
		{
			final MBeanServer beanServer =  ManagementFactory.getPlatformMBeanServer();
			this.beans.forEach( bean -> {
				try
				{
					beanServer.unregisterMBean(bean.getObjectName());
					logger.debug("Unregistered JMX bean {}", bean.getObjectName());
				}
				catch (MBeanRegistrationException | InstanceNotFoundException e) {
					logger.warn("Failed to unregister JMX Bean", e);
				}
			});
			this.beans.clear();
		}
		
		private ObjectName createObjectName(final MetricMonitor metric) throws MalformedObjectNameException
		{
			return new ObjectName("org.eclipse.store:"
				+ (this.storageName != null ? "storage=" + this.storageName  + ",": "")
				+ metric.getName());
		}

	}

	/**
	 * Disabled Monitoring Manager implementation.
	 * This implementation disables the monitoring by not registering
	 * MetricMonitors.
	 */
	public class Disabled implements MonitoringManager
	{
		@Override
		public void registerMonitor(MetricMonitor metrics)
		{
			//NoOp
		}

		@Override
		public void shutdown()
		{
			//NoOp
		}
		
	}

}
