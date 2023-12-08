package org.eclipse.serializer.communication.binarydynamic;

import org.eclipse.serializer.com.ComException;

/**
 * Thrown when a typeDefinition received from the remote host
 * contains a type that can't be resolved to an exiting class
 * on the local system.
 *
 */
public class ComExceptionRemoteClassNotFound extends ComException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Constructs a <code>ComExceptionRemoteClassNotFound</code> with no detail message.
	 * 
	 * @param typeName the type name of the missing class
	 */
	public ComExceptionRemoteClassNotFound(final String typeName)
	{
		super("Class not found: " + typeName);
	}

}
