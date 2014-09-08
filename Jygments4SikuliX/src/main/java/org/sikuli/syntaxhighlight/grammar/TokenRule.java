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

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Tal Liron
 */
public class TokenRule extends PatternRule
{
	//
	// Construction
	//

	public TokenRule( Pattern pattern, List<TokenType> tokenTypes )
	{
		this( pattern, tokenTypes, (List<State>) null );
	}

	public TokenRule( Pattern pattern, List<TokenType> tokenTypes, List<State> nextStates )
	{
		super( pattern );
		this.nextStates = nextStates;
		this.tokenTypes = tokenTypes;
	}

	//
	// Attributes
	//

	public List<TokenType> getTokenTypes()
	{
		return tokenTypes;
	}

	public List<State> getNextStates()
	{
		return nextStates;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final List<TokenType> tokenTypes;

	private final List<State> nextStates;
}
