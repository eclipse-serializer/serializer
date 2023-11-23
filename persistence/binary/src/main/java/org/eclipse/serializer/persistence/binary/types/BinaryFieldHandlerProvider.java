package org.eclipse.serializer.persistence.binary.types;

import java.lang.reflect.Field;

import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public interface BinaryFieldHandlerProvider
{
	/**
	 * Register a BinaryFieldStorerCreator.
	 * @param fieldStorer BinaryFieldStorerCreator to be registered.
	 */
	public void registerFieldStorerCreator(BinaryFieldStorerCreator<?> fieldStorerCreator);
	
	/**
	 * Unregister the BinaryFieldStorerCreator for the specified field if any.
	 * 
	 * @param field the Field.
	 * @return the unregistered BinaryFieldStorerCreator.
	 */
	public BinaryFieldStorerCreator<?> unregisterFieldStorerCreator(Field field);
	
	/**
	 * Lookup the BinaryValueStorer registered for the specified field.
	 * @param field the field.
	 * @param switchByteOrder true if inversed byte order.
	 * @param isEager true if eager stroing.
	 * @return the registered BinaryValueStorer or null.
	 */
	public BinaryValueStorer lookupFieldStorer(Field field, boolean isEager, boolean switchByteOrder);

	/**
	 * Register a removeFieldSetterCreator.
	 * @param fieldSetter removeFieldSetterCreator to be registered.
	 */
	public void registerFieldSetterCreator(BinaryFieldSetterCreator<?> fieldSetterCreator);
	
	/**
	 * Unregister the removeFieldSetterCreator for the specified field if any.
	 * 
	 * @param field the Field.
	 * @return the unregistered removeFieldSetterCreator.
	 */
	public BinaryFieldSetterCreator<?> removeFieldSetterCreator(Field field);
	
	/**
	 * Lookup the BinaryFieldSetter registered for the specified field.
	 * @param field the field.
	 * @return the registered BinaryFieldSetter or null.
	 */
	public BinaryFieldSetter<?> lookupFieldSetter(Field field, boolean switchByteOrder);
	
	
	/**
	 * Create the default BinaryFieldHandlerProvider instance.
	 * 
	 * @return a new BinaryFieldHandlerProvider.
	 */
	public static BinaryFieldHandlerProvider New()
	{
		return new BinaryFieldHandlerProvider.Default();
	}
	
	/**
	 * Default implementation of BinaryFieldHandlerProvider interface.
	 */
	public class Default implements BinaryFieldHandlerProvider
	{
		private final static Logger logger = Logging.getLogger(BinaryFieldHandlerProvider.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final EqHashTable<Field, BinaryFieldStorerCreator<?>> fieldStorerCreators;
		private final EqHashTable<Field, BinaryFieldSetterCreator<?>> fieldSetterCreators;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default()
		{
			this.fieldStorerCreators = EqHashTable.New();
			this.fieldSetterCreators = EqHashTable.New();
			this.registerFieldStorerCreator(new BinaryFieldStorerThrowableStackTraceCreator());
		}
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void registerFieldStorerCreator(BinaryFieldStorerCreator<?> fieldStorerCreator)
		{
			this.fieldStorerCreators.add(fieldStorerCreator.getField(), fieldStorerCreator);
		}
					
		@Override
		public BinaryFieldStorerCreator<?> unregisterFieldStorerCreator(Field field)
		{
			return this.fieldStorerCreators.removeFor(field);
		}
				
		@Override
		public BinaryFieldStorer<?> lookupFieldStorer(Field field, boolean isEager, boolean switchByteOrder)
		{
			BinaryFieldStorerCreator<?> storerCreator = this.fieldStorerCreators.get(field);
					
			if(storerCreator != null)
			{
				BinaryFieldStorer<?> storer = storerCreator.create(isEager, switchByteOrder);
				logger.debug("lookup custom field storer: " + field.getName() + " => "
					+ (storer != null ? storer.getClass().getName() : "none"));
				
				return storer;
			}
			
			return null;
		}
		
		@Override
		public void registerFieldSetterCreator(BinaryFieldSetterCreator<?> fieldSetterCreator)
		{
			this.fieldSetterCreators.add(fieldSetterCreator.getField(), fieldSetterCreator);
		}
		
		@Override
		public BinaryFieldSetterCreator<?> removeFieldSetterCreator(Field field)
		{
			return this.fieldSetterCreators.removeFor(field);
		}
		
		@Override
		public BinaryFieldSetter<?> lookupFieldSetter(Field field, boolean switchByteOrder)
		{
			BinaryFieldSetterCreator<?> setterCreator = this.fieldSetterCreators.get(field);
				
			if(setterCreator != null)
			{
				BinaryFieldSetter<?> setter = setterCreator.create(switchByteOrder);
				logger.debug("lookup custom field setter: " + field.getName() + " => "
					+ (setter != null ? setter.getClass().getName() : "none"));
				return setter;
			}
			
			return null;
		}
	
	}
	
}
