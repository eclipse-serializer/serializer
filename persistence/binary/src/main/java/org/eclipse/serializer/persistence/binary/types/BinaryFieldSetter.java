package org.eclipse.serializer.persistence.binary.types;

import java.lang.reflect.Field;

/**
 * BinaryFieldSetter are custom implementations of BinaryValueSetter
 * for a specific field. They are used by the automatically created binary type handlers
 * instead of the default BinaryValueSetter.
 * 
 * To register a custom BinaryFieldSetter see {@link org.eclipse.serializer.persistence.binary.types.BinaryFieldHandlerProvider#registerFieldSetterCreator(BinaryFieldSetter) BinaryFieldHandlerProvider}
 * 
 * @param <T> The class the handled field belongs to.
 */
public interface BinaryFieldSetter<T> extends BinaryValueSetter
{
	/**
	 * Get the Field that is handled by this BinaryFieldSetter
	 * 
	 * @return the handled field.
	 */
	Field getField();
}
