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

package org.sikuli.syntaxhighlight.contrib;

import java.io.IOException;
import java.io.Writer;

import org.sikuli.syntaxhighlight.format.Formatter;
import org.sikuli.syntaxhighlight.grammar.Token;
import org.sikuli.syntaxhighlight.style.Style;

/**
 * @author Tal Liron
 */
public class DebugFormatter extends Formatter
{
	//
	// Construction
	//

	public DebugFormatter()
	{
		this( null, false, null, null );
	}

	public DebugFormatter( Style style, boolean full, String title, String encoding )
	{
		super( style, full, title, encoding );
	}

	//
	// Formatter
	//

	@Override
	public void format( Iterable<Token> tokenSource, Writer writer ) throws IOException
	{
		for( Token token : tokenSource )
			writer.write( token.getPos() + " " + token.getType() + ": " + token.getValue() + "\n" );
		writer.flush();
	}
}
