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
public class SaveRule extends Rule
{
	//
	// Construction
	//

	public SaveRule( State state )
	{
		super();
		this.state = state;
	}

	//
	// Attributes
	//

	public State getState()
	{
		return state;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final State state;
}
