package org.eclipse.serializer.communication.binarydynamic;

public class ComMessageClientTypeMismatch extends ComMessageStatus
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final long typeId;
	private final String typeName;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComMessageClientTypeMismatch(final long typeId, String typeName)
	{
		super(false);
		this.typeId = typeId;
		this.typeName = typeName;
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected long getTypeId()
	{
		return this.typeId;
	}


	protected String getType()
	{
		return this.typeName;
	}
}
