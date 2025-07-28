package org.eclipse.serializer.afs.exceptions;



import org.eclipse.serializer.exceptions.BaseException;

public class AfsException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AfsException()
	{
		super();
	}

	public AfsException(final String message)
	{
		super(message);
	}

	public AfsException(final Throwable cause)
	{
		super(cause);
	}

	public AfsException(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	public AfsException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
