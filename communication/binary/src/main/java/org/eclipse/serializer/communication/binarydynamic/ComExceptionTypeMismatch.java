package org.eclipse.serializer.communication.binarydynamic;

import org.eclipse.serializer.com.ComException;

public class ComExceptionTypeMismatch extends ComException
{
	private final long   typeId;
	private final String typeName;
	
	public ComExceptionTypeMismatch(final long typeId, final String typeName)
	{
		super(String.format("local type %s does not match to remote type with type id %d!",
			typeName,
			typeId
		));
		
		this.typeId = typeId;
		this.typeName = typeName;
	}

	protected long getTypeId()
	{
		return this.typeId;
	}

	protected String getType()
	{
		return this.typeName;
	}

}
