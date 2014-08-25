/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * added RaiMan 2013
 */
package org.sikuli.idesupport;

/**
 * all methods from/for IDE, that are Python specific
 */
public class JythonIDESupport implements IIDESupport {

	@Override
	public String[] getEndings() {
		return new String [] {"py"};
	}

	@Override
	public IIndentationLogic getIndentationLogic() {
		return new PythonIndentation();
	}
}
