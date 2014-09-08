/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2014
 */
package org.sikuli.basics;

/**
 * INTERNAL USE
 * allows to implement timed animations (e.g. mouse move)
 */
public interface Animator {

  public float step();

  public boolean running();
}

