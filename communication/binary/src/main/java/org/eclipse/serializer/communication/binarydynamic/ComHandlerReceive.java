package org.eclipse.serializer.communication.binarydynamic;

public interface ComHandlerReceive<T extends ComMessage>
{
	public Object processMessage(T message);

	public Object processMessage(Object received);

	default boolean continueReceiving()
	{
		return false;
	}
		 
}
