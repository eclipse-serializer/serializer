package org.eclipse.serializer;

/*-
 * #%L
 * Eclipse Serializer
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

import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceManager;
import org.eclipse.serializer.reference.Reference;
import org.eclipse.serializer.util.X;

import java.io.Closeable;

import static org.eclipse.serializer.util.X.notNull;

public interface ObjectCopier extends Closeable
{
	public <T> T copy(T source);
	
	@Override
	public void close();
	
	
	public static ObjectCopier New()
	{
		return new Default(SerializerFoundation.New());
	}
	
	public static ObjectCopier New(final SerializerFoundation<?> foundation)
	{
		return new Default(
			notNull(foundation)
		);
	}
	
	
	public static class Default implements ObjectCopier
	{
		private final SerializerFoundation<?> foundation        ;
		private PersistenceManager<Binary>    persistenceManager;
				
		Default(final SerializerFoundation<?> foundation)
		{
			super();
			this.foundation = foundation;
		}

		@SuppressWarnings("unchecked")
		@Override
		public synchronized <T> T copy(final T source)
		{
			this.lazyInit();
			
			this.persistenceManager.store(source);
			return (T)this.persistenceManager.get();
		}
		
		@Override
		public synchronized void close()
		{
			if(this.persistenceManager != null)
			{
				this.persistenceManager.objectRegistry().clearAll();
				this.persistenceManager.close();
				this.persistenceManager = null;
			}
		}
		
		private void lazyInit()
		{
			if(this.persistenceManager == null)
			{
				final Reference<Binary> buffer = X.Reference(null)               ;
				final Serializer.Source source = ()   -> X.Constant(buffer.get());
				final Serializer.Target target = data -> buffer.set(data)        ;

				this.persistenceManager = this.foundation
					.setPersistenceSource(source)
					.setPersistenceTarget(target)
					.createPersistenceManager()
				;
			}
			else
			{
				this.persistenceManager.objectRegistry().truncateAll();
			}
		}
		
	}
	
}
