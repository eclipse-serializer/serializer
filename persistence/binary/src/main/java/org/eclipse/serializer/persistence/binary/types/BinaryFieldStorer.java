package org.eclipse.serializer.persistence.binary.types;

import java.lang.reflect.Field;

/**
 * BinaryFieldStorer are custom implementations of BinaryValueStorer
 * for a specific field. They are used by the automatically created binary type handlers
 * instead of the default BinaryFieldStorer.
 * 
 * To register a custom BinaryFieldStorer see {@link org.eclipse.serializer.persistence.binary.types.BinaryFieldHandlerProvider#registerFieldStorerCreator(BinaryFieldStorer) BinaryFieldHandlerProvider}
 * 
 * @param <T> The class the handled field belongs to.
 */
public interface BinaryFieldStorer<T> extends BinaryValueStorer
{
	/**
	 * Get the Field that is handled by this BinaryFieldStorer
	 * 
	 * @return the handled field.
	 */
	Field getField();
}
