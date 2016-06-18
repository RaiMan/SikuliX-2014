/*
 * Copyright 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * added RaiMan 2013
 */
package org.sikuli.basics;

/**
 * to activate Jython support use org.sikuli.script.SikulixForJython instead
 */
@Deprecated
public class SikulixForJython {
  
  static {
    org.sikuli.script.SikulixForJython.get();
  }
}
