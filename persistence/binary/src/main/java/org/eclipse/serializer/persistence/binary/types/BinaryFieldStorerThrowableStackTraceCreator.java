package org.eclipse.serializer.persistence.binary.types;

import java.lang.reflect.Field;

import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.exceptions.BinaryPersistenceException;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public class BinaryFieldStorerThrowableStackTraceCreator implements BinaryFieldStorerCreator<Throwable>
{
	private Field field;

	public BinaryFieldStorerThrowableStackTraceCreator()
	{
		try
		{
			this.field = Throwable.class.getDeclaredField("stackTrace");
		}
		catch (NoSuchFieldException | SecurityException e)
		{
			throw new BinaryPersistenceException("Failed to initialize filed storer!", e);
		}
	}
	
	@Override
	public Field getField()
	{
		return this.field;
	}

	@Override
	public BinaryFieldStorer<?> create(boolean isEager, boolean switchByteOrder)
	{
		if(isEager && switchByteOrder)
		{
			return new BinaryFieldStorerThrowableStackTrace_EAGER_REVERSED(this.field);
		}
		else if(isEager)
		{
			return new BinaryFieldStorerThrowableStackTrace_EAGER(this.field);
		}
		else if(switchByteOrder)
		{
			return new BinaryFieldStorerThrowableStackTrace_REVERSED(this.field);
		}
		
		return new BinaryFieldStorerThrowableStackTrace_DEFAULT(this.field);
	}

	
	
	public abstract class AbstractBinaryFieldStorerThrowableStackTrace implements BinaryFieldStorer<Throwable>
	{
		public abstract void setObject(long targetAddress, PersistenceStoreHandler<Binary> persister, Object source, long sourceOffset);
		
		private final Field field;
		
		public AbstractBinaryFieldStorerThrowableStackTrace(Field field)
		{
			this.field = field;
		}
		
		@Override
		public Field getField()
		{
			return this.field;
		}
		
		@Override
		public long storeValueFromMemory(Object source, long sourceOffset, long targetAddress,
			PersistenceStoreHandler<Binary> persister)
		{
			Throwable t = (Throwable)source;
			t.getStackTrace();
			XMemory.set_long(targetAddress, persister.apply(XMemory.getObject(source, sourceOffset)));
			return targetAddress + Binary.objectIdByteLength();
		}
	}
	

	public class BinaryFieldStorerThrowableStackTrace_DEFAULT extends AbstractBinaryFieldStorerThrowableStackTrace
	{
		public BinaryFieldStorerThrowableStackTrace_DEFAULT(Field field)
		{
			super(field);
		}

		@Override
		public void setObject(long targetAddress, PersistenceStoreHandler<Binary> persister, Object source, long sourceOffset)
		{
			XMemory.set_long(
				targetAddress,
				persister.apply(XMemory.getObject(source, sourceOffset)));
		}
	}
	
	public class BinaryFieldStorerThrowableStackTrace_REVERSED extends AbstractBinaryFieldStorerThrowableStackTrace
	{
		public BinaryFieldStorerThrowableStackTrace_REVERSED(Field field)
		{
			super(field);
		}

		@Override
		public void setObject(long targetAddress, PersistenceStoreHandler<Binary> persister, Object source, long sourceOffset)
		{
			XMemory.set_long(
				targetAddress,
				Long.reverseBytes(persister.apply(XMemory.getObject(source, sourceOffset))));
		}
	}
	
	public class BinaryFieldStorerThrowableStackTrace_EAGER extends AbstractBinaryFieldStorerThrowableStackTrace
	{
		public BinaryFieldStorerThrowableStackTrace_EAGER(Field field)
		{
			super(field);
		}

		@Override
		public void setObject(long targetAddress, PersistenceStoreHandler<Binary> persister, Object source, long sourceOffset)
		{
			XMemory.set_long(
				targetAddress,
				persister.applyEager(XMemory.getObject(source, sourceOffset)));
		}
	}
	
	public class BinaryFieldStorerThrowableStackTrace_EAGER_REVERSED extends AbstractBinaryFieldStorerThrowableStackTrace
	{
		public BinaryFieldStorerThrowableStackTrace_EAGER_REVERSED(Field field)
		{
			super(field);
		}

		@Override
		public void setObject(long targetAddress, PersistenceStoreHandler<Binary> persister, Object source, long sourceOffset)
		{
			XMemory.set_long(
				targetAddress,
				Long.reverseBytes(
						persister.applyEager(XMemory.getObject(source, sourceOffset))));
		}
	}
}
