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

import java.util.regex.Pattern;

/**
 * @author Tal Liron
 */
public class UsingRule extends PatternRule
{
	//
	// Construction
	//

	public UsingRule( Pattern pattern, Lexer lexer )
	{
		super( pattern );
		this.lexer = lexer;
	}

	//
	// Attributes
	//

	public Lexer getLexer()
	{
		return lexer;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Lexer lexer;
}
