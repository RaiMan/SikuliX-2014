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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tal Liron
 */
public class NestedDef<C> extends Def<C>
{
	//
	// Operations
	//

	public void addDef( Def<C> def )
	{
		defs.add( def );
	}

	//
	// Def
	//

	@Override
	public boolean resolve( C container ) throws ResolutionException
	{
		// Keep resolving until done
		boolean didSomething = false, keepGoing = true;
		while( keepGoing )
		{
			keepGoing = false;
			for( Def<C> def : new ArrayList<Def<C>>( defs ) )
			{
				if( !def.isResolved() )
				{
					if( def.resolve( container ) )
					{
						keepGoing = true;
						didSomething = true;
					}
				}
			}
		}

		// Are we resolved?
		resolved = true;
		for( Def<C> def : defs )
		{
			if( !def.isResolved() )
			{
				resolved = false;
				break;
			}
		}

		return didSomething;
	}

	@Override
	public Def<C> getCause( C container )
	{
		for( Def<C> def : defs )
			if( !def.isResolved() )
				return def;
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final List<Def<C>> defs = new ArrayList<Def<C>>();
}
