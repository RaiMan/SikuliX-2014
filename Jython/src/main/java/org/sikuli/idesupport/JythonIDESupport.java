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
 * all methods from/for IDE, that are Python specific
 */
public class JythonIDESupport implements IDESupport {

	public static IndentationLogic getIndentationLogic() {
		return new PythonIndentation();
	}
}
