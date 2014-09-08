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
public class ColorStyleElement extends StyleElement
{
	//
	// Types
	//

	public enum Type
	{
		Foreground, Background, Border
	}

	//
	// Static attributes
	//

	public static ColorStyleElement getColorStyleElementByName( String name )
	{
		if( name.startsWith( "bg:" ) )
			return background( name.substring( 3 ) );
		if( name.startsWith( "border:" ) )
			return border( name.substring( 7 ) );
		return foreground( name );
	}

	public static ColorStyleElement foreground( String color )
	{
		return new ColorStyleElement( color, Type.Foreground, color );
	}

	public static ColorStyleElement background( String color )
	{
		return new ColorStyleElement( "bg:" + color, Type.Background, color );
	}

	public static ColorStyleElement border( String color )
	{
		return new ColorStyleElement( "border:" + color, Type.Border, color );
	}

	//
	// Attributes
	//

	public String getColor()
	{
		return color;
	}

	public Type getType()
	{
		return type;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected ColorStyleElement( String name, Type type, String color )
	{
		super( name );
		this.type = type;
		this.color = color;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String color;

	private final Type type;
}
