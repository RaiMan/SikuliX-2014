/**
 * Copyright 2010-2014 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of a BSD license. See
 * attached license.txt.
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package org.sikuli.syntaxhighlight;

/**
 * @author Tal Liron
 */
public class ResolutionException extends Exception
{
	//
	// Construction
	//

	public ResolutionException( String message )
	{
		super( message );
	}

	public ResolutionException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public ResolutionException( Throwable cause )
	{
		super( cause );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}
