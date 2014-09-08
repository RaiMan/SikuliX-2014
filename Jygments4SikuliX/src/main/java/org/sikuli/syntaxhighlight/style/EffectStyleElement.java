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

package org.sikuli.syntaxhighlight.style;

/**
 * @author Tal Liron
 */
public class EffectStyleElement extends StyleElement
{
	//
	// Constants
	//

	public static final EffectStyleElement Bold = create( "bold" );

	public static final EffectStyleElement NoBold = create( "nobold" );

	public static final EffectStyleElement Italic = create( "italic" );

	public static final EffectStyleElement NoItalic = create( "noitalic" );

	public static final EffectStyleElement Underline = create( "underline" );

	public static final EffectStyleElement NoUnderline = create( "nounderline" );

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected EffectStyleElement( String name )
	{
		super( name );
	}

	private static EffectStyleElement create( String name )
	{
		EffectStyleElement fontStyleElement = new EffectStyleElement( name );
		add( fontStyleElement );
		return fontStyleElement;
	}
}
