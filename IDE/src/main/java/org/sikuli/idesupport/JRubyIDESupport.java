/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * added RaiMan 2013
 */
package org.sikuli.idesupport;

/**
 * all methods from/for IDE, that are JRuby specific
 */
public class JRubyIDESupport implements IIDESupport {

	@Override
	public String[] getEndings() {
		return new String [] {"rb"};
	}

	@Override
	public IIndentationLogic getIndentationLogic() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
