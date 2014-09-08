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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.sikuli.syntaxhighlight.format.Formatter;
import org.sikuli.syntaxhighlight.grammar.Lexer;
import org.sikuli.syntaxhighlight.grammar.Token;

/**
 * @author Tal Liron
 */
public abstract class Jygments
{
	//
	// Static operations
	//

	public static Iterable<Token> lex( String code, Lexer lexer )
	{
		return lexer.getTokens( code );
	}

	public static void format( Iterable<Token> tokens, Formatter formatter, Writer writer ) throws IOException
	{
		formatter.format( tokens, writer );
	}

	public static void highlight( String code, Lexer lexer, Formatter formatter, Writer writer ) throws IOException
	{
		format( lex( code, lexer ), formatter, writer );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private Jygments()
	{
	}
}
