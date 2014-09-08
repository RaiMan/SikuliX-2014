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

package org.sikuli.syntaxhighlight.grammar;

/**
 * @author Tal Liron
 */
public class RelativeState extends State
{
	//
	// Construction
	//

	public RelativeState( boolean push, int depth )
	{
		super( push ? "#push" : "#pop" + ( depth > 1 ? ":" + depth : "" ) );
		this.push = push;
		this.depth = depth;
	}

	//
	// Attributes
	//

	public boolean isPush()
	{
		return push;
	}

	public int getDepth()
	{
		return depth;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final boolean push;

	private final int depth;
}
