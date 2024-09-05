package org.eclipse.serializer.exceptions;

/**
 * Indicates that no nested class with the specified name was found
 * in class c.
 */
public class NoSuchNestedClassRuntimeException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public NoSuchNestedClassRuntimeException(final Class<?> c, final String name)
	{
		super("No nested class " + name  + " found in class " + c.getName());
	}

}
