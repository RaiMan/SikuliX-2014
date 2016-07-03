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
public abstract class PatternRule extends Rule
{
	//
	// Construction
	//

	public PatternRule( Pattern pattern )
	{
		this.pattern = pattern;
	}

	//
	// Attributes
	//

	public Pattern getPattern()
	{
		return pattern;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Pattern pattern;
}
