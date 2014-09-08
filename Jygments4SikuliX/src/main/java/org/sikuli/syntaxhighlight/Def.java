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
public abstract class Def<C>
{
	//
	// Attributes
	//

	public boolean isResolved()
	{
		return resolved;
	}

	public Def<C> getCause( C container )
	{
		return null;
	}

	//
	// Operations
	//

	public boolean resolve( C container ) throws ResolutionException
	{
		return false;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected boolean resolved = false;
}
