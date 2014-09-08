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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tal Liron
 */
public class StyleElement
{
	static
	{
		// Force these classes to load
		new ColorStyleElement( null, null, null );
		new EffectStyleElement( null );
		new FontStyleElement( null );
	}

	//
	// Static attributes
	//

	public static StyleElement getStyleElementByName( String name )
	{
		StyleElement styleElement = styleElementsByName.get( name );
		if( styleElement == null )
			styleElement = ColorStyleElement.getColorStyleElementByName( name );
		return styleElement;
	}

	//
	// Attributes
	//

	public String getName()
	{
		return name;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return name;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected static final void add( StyleElement styleElement )
	{
		if( styleElementsByName == null )
			styleElementsByName = new HashMap<String, StyleElement>();

		styleElementsByName.put( styleElement.name, styleElement );
	}

	protected StyleElement( String name )
	{
		this.name = name;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static Map<String, StyleElement> styleElementsByName;

	private final String name;
}
