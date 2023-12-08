package org.eclipse.serializer.communication.binarydynamic;

public class ComMessageClientError extends ComMessageStatus
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final String errorMessage;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComMessageClientError(final RuntimeException runtimeException)
	{
		super(false);
		this.errorMessage = runtimeException.getMessage();
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public String getErrorMessage()
	{
		return this.errorMessage;
	}

}
