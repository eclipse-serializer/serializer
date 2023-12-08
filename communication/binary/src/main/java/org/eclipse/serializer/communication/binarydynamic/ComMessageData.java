package org.eclipse.serializer.communication.binarydynamic;

public class ComMessageData implements ComMessage
{
	final Object data;

	public ComMessageData(final Object data)
	{
		super();
		this.data = data;
	}

	public Object getData()
	{
		return this.data;
	}
}
