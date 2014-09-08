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
public class Token
{
	//
	// Construction
	//

	public Token( int pos, TokenType tokenType, String value )
	{
		this.pos = pos;
		this.tokenType = tokenType;
		this.value = value;
	}

	//
	// Attributes
	//

	public int getPos()
	{
		return pos;
	}

	public TokenType getType()
	{
		return tokenType;
	}

	public String getValue()
	{
		return value;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final int pos;

	private final TokenType tokenType;

	private final String value;
}
