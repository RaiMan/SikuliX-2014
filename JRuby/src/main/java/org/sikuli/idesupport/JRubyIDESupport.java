/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * added RaiMan 2013
 */
package org.sikuli.idesupport;

import org.sikuli.basics.IDESupport;
import org.sikuli.basics.IndentationLogic;

/**
 * all methods from/for IDE, that are JRuby specific
 */
public class JRubyIDESupport implements IDESupport {

	@Override
	public String[] getEndings() {
		return new String [] {"rb"};
	}

	@Override
	public IndentationLogic getIndentationLogic() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
